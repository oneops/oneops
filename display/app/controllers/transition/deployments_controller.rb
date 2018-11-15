class Transition::DeploymentsController < ApplicationController
  before_filter :find_assembly_and_environment, :except => [:log_data, :time_stats, :progress]
  before_filter :find_deployment, :only => [:status, :show, :edit, :update, :time_stats, :progress]

  swagger_controller :deployments, 'Environment Deployment Functions'

  def index
    if @environment || @assembly
      @source = params[:source].presence || (request.format.html? || request.format.xhr? ? 'es' : 'cms')

      ns_path = @environment ? "#{@environment.nsPath}/#{@environment.ciName}/bom" : assembly_ns_path(@assembly)

      if @source == 'cms' || @source == 'simple'
        search_params = {:nsPath => ns_path, :recursive => @environment.blank?}
        %w(start end).each do |k|
          date = params[k]
          search_params[k] = Time.parse(date).to_i * 1000 if date.present?
        end

        @deployments = Cms::Deployment.all(:params => search_params)
      else
        size          = (params[:size].presence || 1000).to_i
        offset        = (params[:offset].presence || 0).to_i
        sort          = params[:sort].presence || {'created' => 'desc'}
        filter        = params[:filter]
        search_params = {:nsPath => ns_path, :size => size, :from => offset, :sort => sort, :_silent => []}
        search_params[:query] = filter if filter.present?

        @deployments = Cms::Deployment.search(search_params)
      end

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
      format.csv do
        fields = [:deploymentId, :nsPath, :state, :created_at, :created_by, :comments]
        rows = @deployments.map do |d|
          [d.deploymentId, d.nsPath[0..-5], d.deploymentState, d.created_timestamp, d.createdBy, d.comments].join(',')
        end
        render :text => fields.join(',') + "\n" + rows.join("\n")   #, :content_type => 'text/data_string'
      end

      format.yaml {render :text => @deployments.as_json(:only => [:deploymentId, :releaseId, :nsPath, :deploymentState, :created, :createdBy, :updated, :comments, :description]).to_yaml, :content_type => 'text/data_string'}
    end
  end

  def latest
    @deployment = Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")
    render_json_ci_response(@deployment.present?, @deployment)
  end

  def status
    step = nil
    if @deployment.deploymentState != 'complete' && @deployment.deploymentState != 'failed'
      step = params[:exec_order].to_i
      step = nil unless step > 0
    end
    load_deployment_states(step)

    respond_to do |format|
      format.js do
        @deployment.log_data = {}

        current_state  = params[:current_state]
        if current_state == 'pending' || current_state != @deployment.deploymentState
          load_state_history
          load_approvals
        end

        record_ids = params[:deployment_record_ids]
        if record_ids.present?
          @deployment.log_data = get_log_data(record_ids.map(&:to_i)).inject({}) do |m, log|
            m[log['id'].to_i] = log['logData']
            m
          end
        end

        load_time_stats if step.nil? || @deployment_rfc_cis_info.values.all? {|i| i[:state] == 'complete' || i[:state] == 'failed' || i[:state] == 'canceled'}

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
    release_rfc_cis = @release.rfc_cis.inject({}) {|h, c| h.update(c.rfcId => c)}
    @rfc_cis = @deployment.rfc_cis.collect { |rfc| release_rfc_cis[rfc.rfcId].deployment = rfc; release_rfc_cis[rfc.rfcId] }

    respond_to do |format|
      format.js do
        @manifest = Cms::Release.find(@release.parentReleaseId)

        load_time_stats
        load_state_history
        load_approvals
        load_clouds_and_platforms

        render :action => :show
      end

      format.json do
        release_rfc_relations = @release.rfc_relations.inject({}) {|h, c| h.update(c.rfcId => c) }
        @rfc_relations = @deployment.rfc_relations.collect { |rfc| release_rfc_relations[rfc.rfcId].deployment = rfc; release_rfc_relations[rfc.rfcId] }
        render :json => {:rfc_cis => @rfc_cis, :rfc_relations => @rfc_relations}
      end
    end
  end

  swagger_api :bom do
    summary 'Commit open release changes (optionally) and preview plan for pending deployment changes.'
    param_path_parent_ids :assembly, :environment
    param :query, :commit, :string, :optional, "Set to 'true' to commit open environment release."
    param :query, :exclude_platforms, :string, :optional, 'Commas separated list of platform CI IDs to be excluded from deployment plan.'
    param :query, :desc, :string, :optional, 'Optional release commit comments.'
    param :query, :cost, :string, :optional, "Set to 'true' to return cost change info for deployment plan."
    param :query, :capacity, :string, :optional, "Set to 'true' to return capacity increase/decrease info for deployment plan."
    notes <<-NOTE
This request allows to preview deployment plan. Optionally you can commit open release changes for environment before
generating the plan.  Since deployment plan is generated in-memory only with no persistence it should not take too long
and therefore this is a blocking request.
JSON body payload example:
<pre>
{
  "commit": "true",
  "desc": "Some env commit comments"
  "capacity": "true"
}
</pre>
NOTE
  end
  def bom
    data = nil
    if @environment.ciState == 'locked'
      message = 'Cannot commit while deployment preparation is in progress.'
    elsif @environment.ciState == 'manifest_locked'
      message = 'Cannot commit while design pull is in progress.'
    else
      data, message = Transistor.preview_bom(@environment.ciId,
                                             :commit      => params[:commit] == 'true',
                                             :description => params[:desc],
                                             :exclude     => params[:exclude_platforms],
                                             :includeRFCs => 'cis',
                                             :cost        => params[:cost] == 'true',
                                             :capacity    => params[:capacity] == 'true')
    end

    respond_to do |format|
      format.js do
        if data
          @rfc_cis = data['rfcs']['cis']
          if @rfc_cis.present?
            @manifest = Cms::Release.find(data['release'].parentReleaseId)
            @deployment = Cms::Deployment.build(:nsPath => environment_bom_ns_path(@environment))
            @last_deployment = Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")
            load_clouds_and_platforms
            load_ops_state_data
            check_for_override
            @cost, _ = data['cost']
            @capacity = data['capacity']
          end
        else
          flash[:error] = message.sub(/^ERROR:BOM:/, '').gsub("\n", '<br>')
          render :js => 'hide_modal();'
        end
      end

      format.json {render_json_ci_response(data.present?, data, [message])}
    end
  end

  def new
    compile_status
    render :action => :edit
  end

  def compile_status
    # TODO - deprecated (old way)
    if @environment.ciState != 'locked' && (@environment.comments.blank? || !@environment.comments.start_with?('ERROR:'))
      find_open_bom_release
      if @release
        @deployment = Cms::Deployment.latest(:releaseId => @release.releaseId)
        load_state_history
        load_approvals
        load_bom_release_data

        @manifest = Cms::Release.find(@release.parentReleaseId)
      end
    end
  end

  swagger_api :create do
    summary 'Create and start deployment.'
    param_path_parent_ids :assembly, :environment
    param :form, 'cms_deployment[exclude_platforms]', :string, :optional, 'Commas separated list of platform CI IDs to be excluded from deployment.'
    param :form, 'cms_deployment[comments]', :string, :optional, 'Optional deployment comments.'
    notes <<-NOTE
JSON body payload example:
<pre>
{
  "cms_deployment": {
    "exclude_platforms": "",
    "comments": "Some deployment comments",
    "nsPath": "/oneops/dynatrace/e1/bom"
  }
}
</pre>
NOTE
  end
  def create
    deployment_hash = params[:cms_deployment]
    deployment_hash[:nsPath] = environment_bom_ns_path(@environment)

    override_password = deployment_hash.delete(:override_password)
    @deployment       = Cms::Deployment.build(deployment_hash)

    release_id = deployment_hash[:releaseId]
    if release_id.blank?
      ok = verify_override(override_password)
      if ok
        deployment = Cms::Deployment.latest(:nsPath => environment_bom_ns_path(@environment))
        if @environment.ciState == 'locked' ||
            (deployment && %w(active paused failed).include?(@deployment.deploymentState))
          ok = false
          @environment.errors.add('Cannot deploy: there is already an active deployment in progress for this environment.')
        else
          ok, message = Transistor.deploy(@environment.ciId, @deployment, params[:exclude_platforms] || '')
          if ok
            @environment.ciState = 'locked'
            @deployment = nil
          else
            @environment.errors.add(:base, message)
          end
        end
      end

      respond_to do |format|
        format.js {flash[:error] = @environment.errors.full_messages.join(';') unless ok}
        format.json { render_json_ci_response(ok, @environment, message) }
      end
    else
      # TODO - deprecated (old way)
      ok = verify_override(override_password)
      ok = execute(@deployment, :save) if ok
      respond_to do |format|
        format.js do
          if ok
            @release = Cms::ReleaseBom.find(@deployment.releaseId)
            load_state_history
            load_approvals
            load_bom_release_data
            render :action => :edit
          else
            flash[:error] = "Failed to create deployment: #{@deployment.errors.full_messages.join(';')}."
            render :js => ''
          end
        end

        format.json {render_json_ci_response(ok, @deployment) }
      end
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
    cms_deployment = params[:cms_deployment]
    deployment_state = cms_deployment[:deploymentState]

    # TODO 7/27/2015  This is just a temp code for backward compatibility to mimic old-style deployment approval
    # by auto settlement of all pending approval records based on deployment state.  This should be removed
    # eventually as deployment approval should be done via new deployment approval record settling API.
    # 01/29/2018  While old-style approval still must be supported, adding "approval_token" check to ensure
    # this will work for "unsecured" support/compliance governing CIs.
    approving = deployment_state == 'active'
    if @deployment.deploymentState == 'pending' && (approving || deployment_state == 'canceled')
      load_approvals
      if @approvals.present?
        govern_ci_map = Cms::Ci.all(:params => {:ids => @approvals.map(&:governCiId).join(',')}).
          select {|ci| ci.ciAttributes.attributes['approval_auth_type'] == 'none'}.
          to_map(&:ciId)
        approvals_to_settle = @approvals.select {|a| a.state == 'pending' && govern_ci_map[a.governCiId]}

        if approvals_to_settle.present?
          comments = cms_deployment[:comments]
          approvals_to_settle = approvals_to_settle.map do |a|
            {:approvalId   => a.approvalId,
             :deploymentId => @deployment.deploymentId,
             :state        => approving ? 'approved' : 'rejected',
             :expiresIn    => -1,
             :comments     => "#{'!! ' if approving}#{comments}"}
          end
          ok, message = Cms::DeploymentApproval.settle(approvals_to_settle)
          @deployment.errors.add(:base, message) unless ok
        elsif !approving
          ok = execute(@deployment, :update_attributes, cms_deployment)
        else
          ok = true
        end
      else
        ok = execute(@deployment, :update_attributes, cms_deployment)
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

  def wo_rfcs
    rfc_id = params[:rfcId].to_i
    @rfc_ci = Cms::RfcCi.find(rfc_id)
    if @rfc_ci
      @rfc_relations = Cms::RfcRelation.all(:params => {:releaseId => @rfc_ci.releaseId, :fromCiId => @rfc_ci.ciId}) +
                       Cms::RfcRelation.all(:params => {:releaseId => @rfc_ci.releaseId, :toCiId => @rfc_ci.ciId})
    end
    respond_to do |format|
      format.js
      format.json { render :json => {:ci => @rfc_ci, :relations => @rfc_relations}}
    end
  end

  def time_stats
    load_time_stats
    render :json => @time_stats
  end

  def progress
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@deployment.present?, @deployment) }
    end
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

  def load_deployment_states(step = nil)
    @deployment_rfc_cis_info = @deployment.new_record? ? {} : @deployment.rfc_cis(step).inject({}) do |states, rfc|
      states[rfc.rfcId] = {:recordId => rfc.dpmtRecordId, :state => rfc.dpmtRecordState, :comments => rfc.comments}
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

  def find_open_bom_release
    @release = Cms::ReleaseBom.first(:params => {:nsPath => "#{environment_ns_path(@environment)}/bom", :releaseState => 'open'})
  end

  def load_bom_release_data
    load_deployment_states
    load_clouds_and_platforms

    @rfc_cis = @release.rfc_cis

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

  def load_clouds_and_platforms
    @clouds = Cms::Relation.all(:params => {:ciId              => @environment.ciId,
                                            :direction         => 'from',
                                            :relationShortName => 'Consumes',
                                            :targetClassName   => 'account.Cloud'}).to_map_with_value {|r| [r.toCiId, r.toCi]}

    @platforms = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'ComposedOf'}).to_map_with_value do |r|
      platform = r.toCi
      ["#{platform.ciName}/#{platform.ciAttributes.major_version}", platform]
    end

    platform_consumes = Cms::DjRelation.all(:params => {:nsPath            => environment_manifest_ns_path(@environment),
                                                        :fromClassName     => 'manifest.Platform',
                                                        :relationShortName => 'Consumes',
                                                        :recursive         => true})
    @primary_clouds = platform_consumes.
      select {|r| r.relationAttributes.adminstatus != 'offline' && r.relationAttributes.priority == '1'}.
      group_by {|r| r.nsPath.split('/', 6).last}
    @priority = platform_consumes.to_map_with_value {|r| ["#{r.nsPath.split('/', 6).last}/#{r.toCiId}", r.relationAttributes.priority]}
  end

  def check_for_override
    doc = MiscDoc.deployment_to_all_primary_check.document
    ns_path = environment_manifest_ns_path(@environment)
    return unless doc['*'] || doc.any? {|k, v| (ns_path.start_with?(k) || /#{k}/.match(ns_path)) && v}

    platforms = []
    find_open_bom_release unless @release
    load_bom_release_data unless @rfc_cis
    @rfc_cis.group_by {|rfc| rfc.nsPath.split('/bom/').last}.each do |p, rfcs|
      primary_clouds = @primary_clouds[p]
      if primary_clouds.size > 1
        cloud_map = primary_clouds.to_map(&:toCiId)
        deployment_order = rfcs.inject({}) do |h, rfc|
          cloud = cloud_map[rfc.ciName.split('-')[-2].to_i]
          h[cloud.relationAttributes.dpmt_order] = true if cloud
          h
        end
        platforms << p if deployment_order.size == 1
      end
    end
    @override = {:SIMULTANEOUS_DEPLOYMENT_TO_ALL_PRIMARY => {:platforms => platforms}} if platforms.present?
  end

  def verify_override(override_password)
    return true unless check_for_override
    ok = current_user.authenticate(override_password)
    @deployment.errors.add(:base, 'invalid password, you must provide valid password to proceed') unless ok
    ok
  end
end
