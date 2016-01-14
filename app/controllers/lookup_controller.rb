class LookupController < ApplicationController
  before_filter :clear_active_resource_headers

  def ci
    begin
      ci = Cms::DjCi.find(params[:id])
    rescue
    end

    authorize_and_respond(ci)
  end

  def release
    begin
      release = Cms::Release.find(params[:id])
    rescue
    end

    authorize_and_respond(release)
  end

  def deployment
    begin
      deployment = Cms::Deployment.find(params[:id])
    rescue
    end

    authorize_and_respond(deployment)
  end

  def procedure
    begin
      procedure = Cms::Procedure.find(params[:id])
      ci = procedure && Cms::DjCi.find(procedure.ciId)
    rescue
    end

    authorize_and_respond(procedure, ci && ci.nsPath)
  end


  def counterparts
    # Saving an additional lookup - construct a barebone Ci on-the-fly.
    ci = params[:ci]
    ci[:ciId] = ci[:ciId].to_i
    @ci = Cms::Ci.new(ci, true)
    unless @ci
      render :text => 'Ci not found', :status => :not_found
      return
    end

    root, org, assembly, rest = @ci.nsPath.split('/')
    unless current_user.has_any_dto?(assembly)
      unauthorized
      return
    end

    ci_class_name = @ci.ciClassName
    find_params = {:nsPath      => "/#{org}/#{assembly}",
                   :recursive   => true,
                   :ciClassName => ci_class_name}
    find_params[:ciName] = @ci.ciName unless ['Assembly', 'Environment', 'Platform'].include?(ci_class_name.split('.').last)
    @counterparts = Cms::Ci.all(:params => find_params)
    split = ci_class_name.split('.')
    if split[0] == 'catalog'
      split[0] = 'manifest'
      find_params[:ciClassName] = split.join('.')
      @counterparts += Cms::Ci.all(:params => find_params)
    end

    respond_to do |format|
      format.js
      format.json {render :json => @counterparts}
    end
  end

  def policy_violations
    clazz = nil
    attrs = params[:cms_ci]
    if attrs.present?
      clazz = Cms::Ci
    else
      attrs = params[:cms_dj_ci]
      clazz = Cms::DjCi if attrs.present?
    end

    unless clazz
      render :json => {:errors => ["Invalid payload: 'cms_ci' || 'cms_dj_ci' structure is expected."]}, :status => :unprocessable_entity
      return
    end

    ci = clazz.new(attrs, true)
    unless authorize(ci.nsPath)
      respond_to do |format|
        format.js
        format.json {render :json => {}, :status => :unauthorized}
      end
      return
    end

    @violations = ci.violates_policies
    respond_to do |format|
      format.js
      format.json {render :json => @violations}
    end
  end


  private

  def authorize_and_respond(target, ns_path = nil)
    unless target
      render :json => {}, :status => :not_found
      return
    end

    if authorize(ns_path || target.nsPath)
      render_json_ci_response(true, target)
    else
      render :json => {}, :status => :unauthorized
    end
  end

  def authorize(ns_path)
    root, org_name = ns_path.split('/')
    org = current_user.organizations.where('organizations.name' => org_name).first
    return false unless org

    unless is_admin?(org) || has_org_scope?(org)
      paths = org.ci_proxies.where(:ns_path => "/#{org_name}").joins(:teams).where('teams.id IN (?)', current_user.all_team_ids(org.id)).pluck(:ns_path)
      return false unless paths.find { |p| ns_path.start_with?(p) }
    end

    return true
  end
end

