  class Transition::DeploymentsController < ApplicationController
  before_filter :find_assembly_and_environment, :except => [:log_data, :time_stats]
  before_filter :find_deployment, :only => [:status, :show, :edit, :update, :time_stats]

  def index
    if @environment || @assembly
      ns_path = @environment ? "#{@environment.nsPath}/#{@environment.ciName}/bom" : assembly_ns_path(@assembly)
      size    = (params[:size].presence || 1000).to_i
      offset  = (params[:offset].presence || 0).to_i
      sort    = params[:sort].presence || {'created' => 'desc'}
      filter  = params[:filter]
      search_params = {:nsPath => ns_path, :size => size, :from => offset, :sort => sort, :_silent => []}
      search_params[:query] = filter if filter.present?
      # @deployments = Cms::Deployment.all(:params => {:nsPath => ns_path})
      @deployments = Cms::Deployment.search(search_params)

      set_pagination_response_headers(@deployments)
    else
      # Org scope.
      unless is_admin? || has_org_scope?
        unauthorized
        return
      end

      opts = {}
      profiles = params[:profiles]
      if profiles.present?
        profiles = (profiles.is_a?(Array) ? profiles : profiles.split(',')).to_map
        envs     = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                           :recursive   => true,
                                           :ciClassName => 'manifest.Environment'})
        opts[:nsPath] = envs.inject([]) do |a, e|
          a << environment_bom_ns_path(e) if profiles[e.ciAttributes.profile]
          a
        end
      else
        opts[:nsPath] = "#{organization_ns_path}/*"
      end

      created = params[:created]
      if created.present?
        created = created.split(',')
        opts[:created] = (Time.at(created[0].to_i) .. (Time.at(created[1] ? Time.at(created[1].to_i) : Time.now)))
        opts[:sort] = 'created'
      end
      @deployments = Cms::Deployment.search(opts)
    end

    respond_to do |format|
      format.html {render 'transition/environments/_deployments'}
      format.js {render :action => :index}
      format.json {render :json => @deployments}
    end
  end

  def latest
    @deployment = Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")
    render_json_ci_response(@deployment.present?, @deployment)
  end

  def status
    load_deployment_states

    respond_to do |format|
      format.js do
        rfcs = params[:rfcs] || []
        if rfcs.blank?
          @deployment.log_data = {}
        else
          deployment_cis = @deployment.rfc_cis.inject({}) {|m, deployment_ci| m[deployment_ci.rfcId] = deployment_ci; m}
          ids = rfcs.inject([]) do |a, rfc_id|
            deployment_ci = deployment_cis[rfc_id.to_i]
            a << deployment_ci.dpmtRecordId if deployment_ci && deployment_ci.dpmtRecordState != 'pending'
            a
          end

          log_data = get_log_data(ids).inject({}) do |m, log|
            m[log['id'].to_i] = log['logData']
            m
          end

          @deployment.log_data = rfcs.inject({}) do |m, rfc_id|
            deployment_ci = deployment_cis[rfc_id.to_i]
            m[rfc_id] = deployment_ci && deployment_ci.dpmtRecordState != 'pending' ? (log_data[deployment_ci.dpmtRecordId] || []) : []
            m
          end
        end

        current_state  = params[:current_state]
        if current_state == 'pending' || current_state != @deployment.deploymentState
          load_state_history
          load_approvals
        end
        load_time_stats

        render :action => :status
      end

      format.json do
        @deployment.rfc_info = @deployment_rfc_cis_info if @deployment
        render_json_ci_response(@deployment.present?, @deployment)
      end
    end
  end

  def show
    @release = Cms::ReleaseBom.find(@deployment.releaseId)
    release_rfc_cis = @release.rfc_cis.inject({}) {|h, c| h.update(c.rfcId => c) }
    @rfc_cis = @deployment.rfc_cis.collect { |rfc| release_rfc_cis[rfc.rfcId].deployment = rfc; release_rfc_cis[rfc.rfcId] }
    release_rfc_relations = @release.rfc_relations.inject({}) {|h, c| h.update(c.rfcId => c) }
    @rfc_relations = @deployment.rfc_relations.collect { |rfc| release_rfc_relations[rfc.rfcId].deployment = rfc; release_rfc_relations[rfc.rfcId] }

    respond_to do |format|
      format.js do
        @manifest = Cms::Release.find(@release.parentReleaseId)

        load_time_stats
        load_state_history
        load_approvals

        render :action => :show
      end

      format.json do
        render :json => {:rfc_cis => @rfc_cis, :rfc_relations => @rfc_relations}
      end
    end
  end

  def new
    compile_status
    render :action => :edit
  end

  def compile_status
    if @environment.ciState != 'locked' && (@environment.comments.blank? || !@environment.comments.start_with?('ERROR:'))
      @release = Cms::ReleaseBom.first(:params => {:nsPath => "#{environment_ns_path(@environment)}/bom", :releaseState => 'open'})
      if @release
        # Deployment might have been already started in a separate browser session.
        @deployment  = Cms::Deployment.latest(:releaseId => @release.releaseId)
        @deployment = Cms::Deployment.build(:releaseId => @release.releaseId) unless @deployment && @deployment.deploymentState == 'active'
        load_bom_release_data
        @manifest = Cms::Release.find(@release.parentReleaseId)
      end
    end
  end

  def create
    @deployment = Cms::Deployment.build(params[:cms_deployment])
    ok = execute(@deployment, :save)

    respond_to do |format|
      format.js do
        if ok
          @release = Cms::ReleaseBom.find(@deployment.releaseId)
          load_state_history
          load_approvals
          load_bom_release_data
          render :action => :edit
        else
          flash[:error] = "Failed to deploy: #{@deployment.errors.full_messages}"
          render :js => ''
        end
      end

      format.json { render_json_ci_response(ok, @deployment) }
    end
  end

  def edit
    load_state_history
    load_approvals

    respond_to do |format|
      format.js do
        @release = Cms::ReleaseBom.find(@deployment.releaseId)
        load_bom_release_data

        render :action => :edit
      end

      format.json do
        @deployment.state_history = @state_history
        @deployment.approvals     = @approvals
        render_json_ci_response(true, @deployment)
      end
    end
  end

  def update
    # TODO 7/27/2015  This is just a temp code for backward compatibility to mimic old-style deploymet approvals/rejection by
    # auto approving/rejecting all pending approval records.  This should be removed eventually.  All deployment
    # approvals/rejections should be done via new deployment approval record settling.
    cms_deployment = params[:cms_deployment]
    deployment_state = cms_deployment[:deploymentState]

    approving = deployment_state == 'active'
    if @deployment.deploymentState == 'pending' && (approving || deployment_state == 'canceled')
      comments = cms_deployment[:comments]
      load_approvals
      approvals_to_settle = @approvals.select {|a| a.state == 'pending'}.map do |a|
        {:approvalId   => a.approvalId,
         :deploymentId => @deployment.deploymentId,
         :state        => approving ? 'approved' : 'rejected',
         :expiresIn    => 1,
         :comments     => "#{'!! ' if approving}#{comments}"}
      end

      if approvals_to_settle.present?
        ok, message = Cms::DeploymentApproval.settle(approvals_to_settle)
        @deployment.errors.add(:base, message) unless ok
      else
        ok = true
      end
    else
      ok = execute(@deployment, :update_attributes, cms_deployment)
    end

    @deployment.check_pausing_state if ok

    respond_to do |format|
      format.js do
        if ok
          edit
        else
          flash[:error] = "Failed to update: #{@deployment.errors.full_messages}"
          render :js => ''
        end
      end

      format.json { render_json_ci_response(ok, @deployment) }
    end
  end

  def log_data
    rfc_id = params[:rfcId].to_i
    @deployment_ci = Cms::DeploymentCi.all(:params => {:deploymentId => params[:id]}).find {|r| r.rfcId == rfc_id}
    unless @deployment_ci
      render :text => 'Deployment record not found', :status => :not_found
      return
    end

    ids = [@deployment_ci.dpmtRecordId]
    raw_data  = get_log_data(ids)
    @log_data = raw_data.blank? ? [] : raw_data[0]['logData']
    respond_to do |format|
      format.html do
        @rfc = Cms::RfcCi.find(rfc_id)
        render :layout => 'log'
      end
      format.js
      format.json { render :json => raw_data}
      format.text { render :text => @log_data.map {|m| m['message']}.join("\n")}
    end
  end

  def time_stats
    load_time_stats
    render :json => @time_stats
  end


  protected

  def read_only_request?
    action_name == 'status' || action_name == 'log_data' || super
  end

  private

  def find_deployment
    @deployment = Cms::Deployment.find(params[:id])
    render :text => 'not_found', :status => :not_found if @deployment.blank?
  end

  def get_log_data(ids)
    Daq.logs(ids.map {|id| {:id => id}})
  end

  def find_assembly_and_environment
    assembly_id = params[:assembly_id]
    return if assembly_id.blank?
    @assembly    = locate_assembly(assembly_id)

    env_id = params[:environment_id]
    return if env_id.blank?
    @environment = locate_environment(env_id, @assembly)
  end

  def load_deployment_states
    @deployment_rfc_cis_info = @deployment.new_record? ? {} : @deployment.rfc_cis.inject({}) do |states, rfc|
      states[rfc.rfcId] = {:state => rfc.dpmtRecordState, :comments => rfc.comments}
      states
    end
  end

  def load_ops_state_data
    @managed_via = Cms::Relation.all(:params => {:nsPath       => environment_bom_ns_path(@environment),
                                                 :recursive    => true,
                                                 :relationName => 'bom.ManagedVia'}).inject({}) do |h, r|
      h[r.fromCiId] = r
      h
    end

    @ops_states = Operations::Sensor.states((@rfc_cis.map(&:ciId) + @managed_via.values.map(&:toCiId)).uniq)
  end

  def load_bom_release_data
    load_deployment_states

    @clouds  = @release.clouds(@environment).inject({}) { |h, r| h[r.toCiId] = r.toCi; h }
    @rfc_cis = @release.rfc_cis

    @platforms = @release.platforms(@environment).inject({}) do |h, c|
      platform = c.toCi
      h["#{platform.ciName}/#{platform.ciAttributes.major_version}"] = platform
      h
    end
    load_ops_state_data
  end

  def load_time_stats
    @time_stats = Search::WorkOrder.time_stats(@deployment)
  end

  def load_state_history
    @state_history = Cms::DeploymentStateChangeEvent.all(:params => {:deploymentId => @deployment.deploymentId})
  end

  def load_approvals
    @approvals = Cms::DeploymentApproval.all(:params => {:deploymentId => @deployment.deploymentId})
  end
end
