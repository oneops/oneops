class Transition::ComponentsController < Base::ComponentsController
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


  protected

  def find_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
  end

  def find_component(id = params[:id])
    @component = locate_ci_in_platform_ns(id, @platform, nil, :attrProps => 'owner')
  end

  def redirect_to_show_platform
    redirect_to assembly_transition_environment_platform_url(@assembly, @environment, @platform)
  end
end
