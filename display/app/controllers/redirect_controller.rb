class RedirectController < ApplicationController
  skip_before_filter :authenticate_user!, :check_eula, :check_organization, :check_reset_password, :check_username, :set_active_resource_headers
  before_filter :clear_active_resource_headers
  before_filter :find_ci, :only => [:ci, :instance, :monitor_doc]
  before_filter :find_rfc, :only => [:rfc]

  def ns
    ns = params[:path]
    ns = "/#{ns}" unless ns.first == '/'
    redirect_to path_to_ns(ns)
  end

  def release
    begin
      release = Cms::Release.find(params[:id])
    rescue
    end

    if release
      redirect_to path_to_release(release)
    else
      render :text => 'Release not found'
     end
  end

  def deployment
    begin
      deployment = Cms::Deployment.find(params[:id])
    rescue
    end

    if deployment
      rfc_id = params[:rfc_id]
      redirect_to "#{path_to_deployment(deployment)}#{"/rfc_id/#{rfc_id}" if rfc_id.present?}"
    else
      render :text => 'Deployment not found'
     end
  end

  def procedure
    procedure = Cms::Procedure.find(params[:id])
    ci = procedure && Cms::DjCi.find(procedure.ciId)
    if ci
      redirect_to "#{path_to_ci(ci, 'operations')}#procedures/list_item/#{procedure.procedureId}"
    else
      render :text => 'Procedure not found'
    end
  end

  def ci
    redirect_to path_to_ci(@ci, params[:dto])
  end

  def rfc
    redirect_to "#{path_to_ci(@ci, params[:dto])}/history#/release_rfc_list/#{@rfc.rfcId}"
  end

  def instance
    foo, org, assembly, env, bom, platform, version = @ci.nsPath.split('/')
    redirect_to assembly_operations_environment_platform_instance_path(:org_name       => org,
                                                                       :assembly_id    => assembly,
                                                                       :environment_id => env,
                                                                       :platform_id    => platform,
                                                                       :id             => @ci.ciId)
  end

  def monitor_doc
    @component_id = Cms::DjRelation.first(:params => {:ciId              => @ci.ciId,
                                                      :direction         => 'to',
                                                      :relationShortName => 'RealizedAs'}).fromCiId

    monitor_name = params[:monitor]
    monitor = Cms::DjRelation.all(:params => {:ciId              => @component_id,
                                              :direction         => 'from',
                                              :relationShortName => 'WatchedBy',
                                              :includeToCi       => true}).find { |m| m.toCi.ciName == monitor_name }
    unless monitor
      render :text => 'Monitor not found'
      return
    end
    url = monitor.relationAttributes.docUrl.strip
    if url.present?
      redirect_to url
    else
      render :text => 'No doc URL found'
    end
  end


  private

  def find_ci
    begin
      ci_id = params[:id]
      @ci = Cms::DjCi.find(ci_id)
    rescue
    end

    unless @ci
      flash[:error] = "CI #{ci_id} not found."
      redirect_to :root
    end
  end

  def find_rfc
    begin
      rfc_id = params[:id]
      @rfc = Cms::RfcCi.find(rfc_id)
    rescue
    end

    unless @rfc
      flash[:error] = "Rfc #{rfc_id} not found."
      redirect_to :root
      return
    end

    begin
      @ci = Cms::DjCi.find(@rfc.ciId)
    rescue
    end

    unless @ci
      flash[:error] = "CI #{@rfc.ciId} for rfc #{rfc_id} not found."
      redirect_to :root
    end
  end
end
