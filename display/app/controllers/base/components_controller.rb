class Base::ComponentsController < ApplicationController
  include ::RfcHistory

  before_filter :find_platform
  before_filter :find_component, :only => [:show, :edit, :update, :destroy, :history, :update_services]

  def index
    pack_ns_path = platform_pack_ns_path(@platform)
    @components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'Requires',
                                                  :attrProps         => 'owner'}).map do |r|
      component = r.toCi
      component.add_policy_locations(pack_ns_path)
      component
    end

    respond_to do |format|
      format.js do
        @policy_compliance = Cms::Ci.violates_policies(@components, false, true) if Settings.check_policy_compliance

        platform_ns_path = @environment ? transition_platform_ns_path(@environment, @platform) : design_platform_ns_path(@assembly, @platform)
        monitors_map = Cms::DjRelation.all(:params => {:nsPath => platform_ns_path,
                                                       :relationShortName => 'WatchedBy'}).inject({}) do |h, r|
          h[r.fromCiId] ||= 0
          h[r.fromCiId] += 1
          h
        end

        attachments_map = Cms::DjRelation.all(:params => {:nsPath            => platform_ns_path,
                                                          :relationShortName => 'EscortedBy'}).inject({}) do |h, r|
          h[r.fromCiId] ||= 0
          h[r.fromCiId] += 1
          h
        end
        @components.each do |c|
          c.monitors = monitors_map[c.ciId].to_i
          c.attachments = attachments_map[c.ciId].to_i
        end
      end

      format.json { render :json => @components }
    end
  end

  def show
    edit
  end

  def edit
    load_depends_on_relations
    load_dependents
    respond_to do |format|
      format.html do
        find_template
        build_linkable_component_sibling_map(@component)

        if in_transition?
          @managed_via = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                         :direction         => 'from',
                                                         :relationShortName => 'ManagedVia'})

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
        load_depends_on_relations
        depends_on.each_pair do |to_id, dor_hash|
          relation = @depends_on_relations.detect {|r| r.toCiId == to_id.to_i || r.toCi.ciName == to_id}
          if relation
            ok = execute_nested(@component, relation, :update_attributes, dor_hash) if relation.relationAttributes.flex == 'true'
            break unless ok
          end
        end
      end
    end

    ok = save_sibling_depends_on_relations if in_design? && ok

    respond_to do |format|
      # format.html { ok ? redirect_to_show_platform : edit }
      format.html { edit }
      format.json { render_json_ci_response(ok, @component) }
    end
  end

  def update_services
    requires_relation
    ok = true
    if request.put?
      @requires.relationAttributes.services = params[:services].select(&:present?).join(',')
      @requires.relationAttrProps.owner.services = params[:owner]
      ok = execute(@requires, :save)
    end

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

  def ci_resource
    @component
  end

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

    platform_template = Cms::Ci.first(:params => {:nsPath      => platform_pack_ns_path(@platform),
                                                  :ciClassName => "#{scope}.Platform"})

    find_params = {:ciId              => platform_template.ciId,
                   :direction         => 'from',
                   :relationShortName => 'Requires'}
    requires = Cms::Relation.all(:params => find_params).find {|r| r.toCi.ciName == @template_name}
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
    component_map = components.to_map(&:toCiId)
    Cms::DjRelation.all(:params => {:nsPath            => @component.nsPath,
                                    :relationShortName => 'DependsOn',
                                    :targetClassName   => @component.ciClassName,
                                    :attr              => 'source:eq:user'}).
      select {|r| r.rfcAction != 'delete' && component_map[r.fromCiId]}
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
    component_id_map = find_component_siblings.to_map(&:toCiId)
    component_id_map.delete(@component.ciId)
    new_depends_on_ids = (params[:sibling_depends_on].presence || []).inject([]) do |a, ci_id|
      if ci_id.present?
        ci_id = ci_id.to_i
        a << ci_id if component_id_map.include?(ci_id)
      end
      a
    end
    new_depends_on_id_map = new_depends_on_ids.to_map

    old_depends_on_rels = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                          :direction         => 'from',
                                                          :relationShortName => 'DependsOn'}).
      select {|r| r.rfcAction != 'delete' && component_id_map[r.toCiId]}

    ok = true

    # Destroy relations to siblings that became unlinked.
    old_depends_on_rels.each do |r|
      unless new_depends_on_id_map[r.toCiId]
        ok = execute_nested(@component, r, :destroy)
        break unless ok
      end
    end

    # Create relations to siblings that became linked.
    if ok
      (new_depends_on_ids - old_depends_on_rels.map(&:toCiId)).each do |component_id|
        relation = Cms::DjRelation.build({:nsPath       => @component.nsPath,
                                          :relationName => 'catalog.DependsOn',
                                          :fromCiId     => @component.ciId,
                                          :toCiId       => component_id,
                                          :relationAttributes => {:source => 'user'}})
        ok = execute_nested(@component, relation, :save)
        break unless ok
      end
    end

    return ok
  end

  def load_depends_on_relations
    @depends_on_relations ||= Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                              :direction         => 'from',
                                                              :relationShortName => 'DependsOn',
                                                              :attrProps         => 'owner'})

  end

  def load_dependents
    @dependents ||= Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                    :direction         => 'to',
                                                    :relationShortName => 'DependsOn',
                                                    :includeFromCi     => true}).map(&:fromCi)

  end
end
