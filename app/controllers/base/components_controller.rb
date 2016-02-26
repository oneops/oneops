class Base::ComponentsController < ApplicationController
  before_filter :find_platform
  before_filter :find_component, :only => [:show, :edit, :update, :destroy, :history, :update_services]

  def index
    @components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'Requires',
                                                  :attrProps         => 'owner'}).map(&:toCi)

    respond_to do |format|
      format.js do
        @policy_compliance = Cms::Ci.violates_policies(@components, false, true) if Settings.check_policy_compliance
      end

      format.json { render :json => @components }
    end
  end

  def show
    load_depends_on_and_dependents
    @component.dependents = @dependents
    @component.dependsOn  = @depends_on_relations.map(&:toCi)
    render_json_ci_response(@component.present?, @component)
  end

  def edit
    load_depends_on_and_dependents
    respond_to do |format|
      format.html do
        find_template
        @managed_via = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                       :direction         => 'from',
                                                       :relationShortName => 'ManagedVia'})

        build_linkable_component_sibling_map(@component)

        if in_transition?
          @attachments = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                         :direction         => 'from',
                                                         :relationShortName => 'EscortedBy'}).map(&:toCi).sort_by { |r| r.ciName }
        end
        render :action => :edit
      end

      format.json do
        @component.dependents = @dependents
        @component.dependsOn  = @depends_on_relations.map(&:toCi)
        render_json_ci_response(true, @component)
      end
    end
  end

  def update
    find_template
    ok = execute(@component, :update_attributes, params[:cms_dj_ci])

    # Save "DependsOn" relation changes.
    if ok
      depends_on = params[:depends_on]
      if depends_on.present?
        find_params = {:ciId              => @component.ciId,
                       :direction         => 'from',
                       :relationShortName => 'DependsOn',
                       :includeToCi       => true,
                       :attrProps         => 'owner'}
        @depends_on_relations = Cms::DjRelation.all(:params => find_params)
        depends_on.each_pair do |to_id, dor_hash|
          relation = @depends_on_relations.detect {|r| r.toCiId == to_id.to_i || r.toCi.ciName == to_id}
          if relation
            ok = execute_nested(@component, relation, :update_attributes, dor_hash) if relation.relationAttributes.flex == 'true'
            break unless ok
          end
        end
      end
    end

    ok = save_sibling_depends_on_relations if ok

    respond_to do |format|
      format.html { ok ? redirect_to_show_platform : edit }
      format.json { render_json_ci_response(ok, @component) }
    end
  end

  def history
    @history = @component.history
    respond_to do |format|
      format.js { render 'base/components/history' }
      format.json { render :json => @history }
    end
  end

  def update_services
    requires_relation
    @requires.relationAttributes.services = params[:services].select(&:present?).join(',')
    @requires.relationAttrProps.owner.services = params[:owner]
    ok = execute(@requires, :save)

    respond_to do |format|
      format.js do
        flash[:error] = 'Failed to update services.' unless ok
        find_template
        render 'base/components/update_services'
      end

      format.json { render_json_ci_response(ok, @requires) }
    end
  end

  protected

  def find_platform
    # Overridden by subclasses.
  end

  def find_component
    # Overridden by subclasses to load component.
  end

  def redirect_to_show_platform
    # Overridden by subclasses.
  end

  def requires_relation
    @requires = Cms::DjRelation.first(:params => {:ciId              => @component.ciId,
                                                  :direction         => 'to',
                                                  :relationShortName => 'Requires',
                                                  :attrProps         => 'owner'})
  end

  def find_template
    return if @template_name && @template

    @template_name = params[:template_name].presence || requires_relation.relationAttributes.template
    scope = in_transition? ? 'mgmt.manifest' : 'mgmt.catalog'

    platform_template = Cms::Ci.first(:params => {:nsPath      => in_transition? ? platform_pack_transition_ns_path(@platform) : platform_pack_design_ns_path(@platform),
                                                  :ciClassName => "#{scope}.Platform"})

    find_params = {:ciId              => platform_template.ciId,
                   :direction         => 'from',
                   :relationShortName => 'Requires'}
    requires = Cms::Relation.all(:params => find_params).detect { |r| r.toCi.ciName == @template_name }
    unless requires
      @template = nil
      return
    end

    @template = requires.toCi
    @template.requires = requires
    @cardinality = requires.relationAttributes.constraint.gsub('*', '999').split('..').map(&:to_i)
  end

  def component_class_name
    return nil unless @template
    # just drop mgmt. from the front
    component_class_name = @template.ciClassName.split('.')
    component_class_name.shift
    component_class_name.join('.')
  end

  def find_component_siblings
    @component_siblings ||= Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                            :relationShortName => 'Requires',
                                                            :direction         => 'from',
                                                            :targetClassName   => @component.ciClassName,
                                                            :attr              => "template:eq:#{@template_name}",
                                                            :attrProps         => 'owner'})
  end

  def find_sibling_depends_relations(components)
    component_map = components.inject({}) { |h, c| h[c.toCiId] = c; h }
    Cms::DjRelation.all(:params => {:nsPath            => in_transition? ?
                                                            transition_platform_ns_path(@environment, @platform) :
                                                            design_platform_ns_path(@assembly, @platform),
                                    :relationShortName => 'DependsOn',
                                    :targetClassName   => @component.ciClassName,
                                    :attr              => 'source:eq:user'}).select() { |r| component_map[r.fromCiId] }
  end

  def build_linkable_component_sibling_map(root_component)
    components = @components || find_component_siblings
    if root_component.new_record?
      @linkable_sibling_map = components.inject({}) { |m, p| m[p] = false; m }
      return
    end

    depends_relations    = find_sibling_depends_relations(components)
    linked_component_ids = find_linked_components_ids(depends_relations, [root_component.ciId])
    linked_component_ids << root_component.ciId
    linked_component_ids.uniq!
    @linkable_sibling_map = components.reject {|d| linked_component_ids.include?(d.toCiId) }.inject({}) do |map, sibling|
      map[sibling] = depends_relations.detect {|r| r.fromCiId == root_component.ciId && r.toCiId == sibling.toCiId}
      map
    end
  end

  def find_linked_components_ids(relations, root_ids)
    root_ids_map = root_ids.inject({}) {|h, id| h[id] = true; h}
    result = relations.inject({}) {|h, r| h[r.fromCiId] = true if root_ids_map[r.toCiId]; h}.keys
    result += find_linked_components_ids(relations, result) if result.present?
    return result
  end

  def save_sibling_depends_on_relations
    new_depends_on_ids = (params[:sibling_depends_on].presence || []).map do |id|
      begin
        Cms::DjCi.locate(id, @component.nsPath).ciId.to_i
      rescue
        nil
      end
    end
    new_depends_on_ids.compact!

    old_depends_on_relations = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                               :direction         => 'from',
                                                               :relationShortName => 'DependsOn'})
    component_id_map         = find_component_siblings.inject({}) { |m, d| m[d.toCiId] = d; m }
    old_depends_on_relations = old_depends_on_relations.select { |r| component_id_map.has_key?(r.toCiId) }
    old_depends_on_ids       = old_depends_on_relations.map(&:toCiId)

    ok = true

    # Destroy relations to siblings that became unlinked.
    (old_depends_on_ids - new_depends_on_ids).each do |component_id|
      relation = old_depends_on_relations.detect { |r| r.toCiId == component_id }
      ok = execute_nested(@component, relation, :destroy)
      break unless ok
    end

    # Create relations to siblings that became linked.
    if ok
      (new_depends_on_ids - old_depends_on_ids).each do |component_id|
        relation = Cms::DjRelation.build({:nsPath       => @component.nsPath,
                                          :relationName => "#{scope}.DependsOn",
                                          :fromCiId     => @component.ciId,
                                          :toCiId       => component_id,})
        relation.relationAttributes.source = 'user'
        ok = execute_nested(@component, relation, :save)
        break unless ok
      end
    end

    return ok
  end

  def load_depends_on_and_dependents
    @dependents = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                  :direction         => 'to',
                                                  :relationShortName => 'DependsOn',
                                                  :includeFromCi     => true}).map(&:fromCi)
    @depends_on_relations = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                            :direction         => 'from',
                                                            # removed temporary until talking to VZ about search condition with non-existing attr
                                                            #:attr              => "source:neq:user",
                                                            :relationShortName => 'DependsOn',
                                                            :attrProps         => 'owner'}) unless @depends_on_relations.present?

  end
end
