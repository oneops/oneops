class Design::ComponentsController < Base::ComponentsController
  def new
    find_template
    @component = Cms::DjCi.build({:nsPath      => design_platform_ns_path(@assembly, @platform),
                                  :ciClassName => component_class_name(),
                                  :ciName      => @template_name},
                                 {:owner => {}})

    if @template
      ci_attributes = @component.ciAttributes.attributes
      ci_attributes.keys.each do |attribute|
        default = @template.ciAttributes.attributes[attribute]
        ci_attributes[attribute] = default if default
        ci_attributes[attribute] = '' if default == '--ENCRYPTED--'
      end
    end

    respond_to do |format|
      format.html do
        build_linkable_component_sibling_map(@component)
        render :action => :edit
      end

      format.json { render_json_ci_response(true, @component) }
    end
  end

  def create
    find_template

    ns_path    = design_platform_ns_path(@assembly, @platform)
    attrs      = params[:cms_dj_ci].merge(:nsPath => ns_path, :ciClassName => component_class_name)
    attr_props = attrs.delete(:ciAttrProps)
    @component = Cms::DjCi.build(attrs, attr_props)
    relation   = Cms::DjRelation.build(:relationName => 'base.Requires',
                                       :nsPath       => ns_path,
                                       :fromCiId     => @platform.ciId,
                                       :toCi         => @component)
    relation.relationAttributes = @template.requires.relationAttributes.attributes.merge(:template => @template_name).slice(*relation.meta.attributes[:mdAttributes].map(&:attributeName)).reject {|k, v| v.blank?}

    build_linkable_component_sibling_map(@component)
    ok = @component_siblings.size < @cardinality.max
    unless ok
      message = "Not allowed to add more '#{@template_name}' components."
      @component.errors.add(:base, message)
      flash[:error] = message
    end

    ok = execute_nested(@component, relation, :save) if ok
    @component = relation.toCi if ok

    if ok
    # Make sure all "DependsOn" relations are present (according to platform template) since we added a new sibling.
      platform_template = Cms::Ci.first(:params => {:nsPath      => platform_pack_design_ns_path(@platform),
                                                    :ciClassName => "mgmt.#{@platform.ciClassName}"})
      component_template_id_name_map = Cms::Relation.all(:params => {:ciId              => platform_template.ciId,
                                                                     :relationShortName => 'Requires',
                                                                     :direction         => 'from'}).inject({}) { |m, t| m[t.toCiId] = t.toCi.ciName; m }
      components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                   :relationShortName => 'Requires',
                                                   :direction         => 'from'})

      Cms::Relation.all(:params => {:nsPath            => platform_template.nsPath,
                                    :relationShortName => 'DependsOn'}).each do |depends_relation_template|
        components.select {|d| d.relationAttributes.template == component_template_id_name_map[depends_relation_template.fromCiId]}.each do |from_component|
          components.select {|d| d.relationAttributes.template == component_template_id_name_map[depends_relation_template.toCiId]}.each do |to_component|
            if from_component.toCiId == @component.ciId || to_component.toCiId == @component.ciId
              depends_relation = Cms::DjRelation.build(:relationName => "#{scope}.DependsOn",
                                                       :nsPath       => ns_path,
                                                       :fromCiId     => from_component.toCiId,
                                                       :toCiId       => to_component.toCiId)
              # Copy attributes.
              ci_attributes    = depends_relation.relationAttributes.attributes
              ci_attributes.keys.each do |attribute|
                default = depends_relation_template.relationAttributes.attributes[attribute]
                ci_attributes[attribute] = default if default
              end
              depends_relation.relationAttributes.source = 'template'
              ok = execute_nested(@component, depends_relation, :save)
              break unless ok
            end
          end
        end
      end
    end

    ok = save_sibling_depends_on_relations if ok

    respond_to do |format|
      format.html do
        if ok
          redirect_to_show_platform
        else
          render :action => :edit
        end
      end

      format.json { render_json_ci_response(ok, @component) }
    end
  end

  def destroy
    ok = false
    find_template
    if find_component_siblings.size > requires_relation.relationAttributes.constraint.split('..').first.to_i
      ok = execute(@component, :destroy)
      flash[:error] = "Failed to delete #{@component.ciName}." unless ok
    else
      @component.errors.add(:base, 'Cannot delete required component.')
      flash[:error] = "Cannot delete required component #{@component.ciName}."
    end

    respond_to do |format|
      format.html { redirect_to_show_platform }
      format.json { render_json_ci_response(ok, @component) }
    end
  end


  protected

  def find_platform
    @assembly = locate_assembly(params[:assembly_id])
    @platform = Cms::DjCi.locate(params[:platform_id], assembly_ns_path(@assembly), 'catalog.Platform')
  end

  def find_component
    @component = locate_ci_in_platform_ns(params[:id], @platform, params[:class_name], :attrProps => 'owner')
  end

  def redirect_to_show_platform
    redirect_to assembly_design_platform_url(@assembly, @platform)
  end
end
