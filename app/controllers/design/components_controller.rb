class Design::ComponentsController < Base::ComponentsController
  def new
    find_template
    @component = Cms::DjCi.build({:nsPath      => design_platform_ns_path(@assembly, @platform),
                                  :ciClassName => component_class_name,
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

    if @template.blank?
      @component.errors.add(:base, "Unknown component type '#{@template_name}'.")
    elsif @template.ciState == 'pending_deletion'
      @component.errors.add(:base, "Component type '#{@template_name}' is obsolete.")
    else
      build_linkable_component_sibling_map(@component)
      if @component_siblings.size >= @cardinality.max
        @component.errors.add(:base, "Not allowed to add more '#{@template_name}' components.")
      end
    end

    ok = @component.errors.blank?
    if ok
      @requires = Cms::DjRelation.build(:relationName => 'base.Requires',
                                       :nsPath       => ns_path,
                                       :fromCiId     => @platform.ciId,
                                       :toCi         => @component)
      @requires.relationAttributes = @template.requires.relationAttributes.attributes.
        merge(:template => @template_name).
        slice(*@requires.meta.attributes[:mdAttributes].map(&:attributeName)).
        reject {|k, v| v.blank?}

      # ok = execute_nested(@component, @requires, :save)
      @requires = Transistor.create_component(@platform.ciId, @requires)
      @component = @requires.toCi
      ok = @requires.errors.blank?
      if ok
        unless save_sibling_depends_on_relations
          @component.errors.add(:base, 'Created component but failed to save some peer dependencies.')
        end
      end
    end


    respond_to do |format|
      format.html do
        if ok && @component.errors.blank?
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
    @platform = locate_design_platform(params[:platform_id], @assembly)
  end

  def find_component
    @component = locate_ci_in_platform_ns(params[:id], @platform, params[:class_name], :attrProps => 'owner')
  end

  def redirect_to_show_platform
    redirect_to assembly_design_platform_url(@assembly, @platform)
  end
end
