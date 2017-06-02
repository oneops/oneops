class Transition::ComponentsController < Base::ComponentsController
  DEPENDS_ON_EDITABLE_ATTRS = %w(pct_dpmt current min max step_up step_down propagate_to)

  def touch
    ok = true
    component_ci_ids = params[:componentCiIds]
    if component_ci_ids
      component_ci_ids.each do |id|
        find_component(id)
        ok = execute(@component, :touch)
        break unless ok
      end
    else
      find_component
      ok = execute(@component, :touch)
    end

    respond_to do |format|
      format.js { flash[:error] = %('Failed to "touch" component '#{@component.ciName}'!') unless ok }

      format.json { render_json_ci_response(ok, @component) }
    end
  end

  def deploy
    if @environment.ciAttributes.codpmt != 'true'
      render :json => {:errors => ['Continuous deployment is not turned on for this environm']}, :status => :unprocessable_entity
      return
    end

    # Assumption here is that the url is always qualified by ciNames not ciIds.
    components = Cms::DjCi.all(:params => {:nsPath => @platform.nsPath, :ciName => params[:id], :recursive => true})
    if components.size == 1
      @component = components.first
      ok = execute(@component, :touch, {:releaseType => 'oneops::autodeploy'})
      render_json_ci_response(ok, @component)
    else
      render :json => {:errors => ['Ambiguous: more than one component.']}, :status => :unprocessable_entity
    end
  end

  def depends_on
    find_component
    load_depends_on_relations
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(true, @depends_on_relations) }
    end
  end

  def update_depends_on
    find_component
    ok = true
    load_depends_on_relations
    (params[:depends_on] || {}).each_pair do |to_id, dor_hash|
      relation = @depends_on_relations.detect {|r| r.toCiId == to_id.to_i || r.toCi.ciName == to_id}
      if relation
        attrs = dor_hash[:relationAttributes].slice(*DEPENDS_ON_EDITABLE_ATTRS)
        relation.relationAttributes.attributes.update(attrs)
        owner = relation.relationAttrProps.owner.attributes
        attrs.keys.each {|a| owner[a] = 'manifest'}
        ok = execute_nested(@component, relation, :save)
        break unless ok
      end
    end

    respond_to do |format|
      format.js {render :action => :depends_on}
      format.json { render_json_ci_response(ok, @depends_on_relations, @component.errors.full_messages) }
    end
  end


  protected

  def find_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
  end

  def find_component(id = params[:id], class_name = params[:class_name])
    @component = locate_component_in_manifest_ns(id, @platform, class_name, :attrProps => 'owner')
  end

  def redirect_to_show_platform
    redirect_to assembly_transition_environment_platform_url(@assembly, @environment, @platform)
  end
end
