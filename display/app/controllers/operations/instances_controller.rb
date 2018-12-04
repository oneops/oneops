class Operations::InstancesController < ApplicationController
  include ::RfcHistory

  before_filter :find_parents, :except => [:state, :update]
  before_filter :find_instance, :except => [:index, :by_cloud, :state, :update, :destroy]
  before_filter :weak_ci_relation_data_consistency, :only => [:index, :by_cloud, :show, :notifications]

  CustomAction = Struct.new(:actionId, :actionName, :description)

  helper_method :allow_replace?

  def self.load_custom_actions(component)
    Cms::Relation.all(:params => {:ciId              => component.ciId,
                                  :direction         => 'from',
                                  :relationShortName => 'EscortedBy'}).map(&:toCi).inject([]) do |m, a|
      m << CustomAction.new(a.ciId, 'user-custom-attachment', a.ciName) if a.ciAttributes.run_on.include?('on-demand')
      m
    end
  end

  def index
    component_scope = params[:component_id].present?
    @instances      = []
    @ops_states     = {}
    @clouds         = {}
    @state          = params[:instances_state] || 'all'
    deployed_to     = []
    instance_ids    = []

    if @state.present?
      if component_scope
        deployed_to = Cms::Relation.all(:params => {:nsPath            => scope_ns_path,
                                                    :relationShortName => 'DeployedTo',
                                                    :fromClassName     => @component.ciClassName.sub(/^manifest./, 'bom.'),
                                                    :attr              => "fromCiName:like:#{@component.ciName}-%"})
        # Just in case there are components with similar ciName, e.g. "artifact" and "artifact-2"
        deployed_to = deployed_to.select {|d| d.comments.include?(%("fromCiName":"#{@component.ciName}-#{d.toCiId}-))}
      else
        deployed_to = Cms::Relation.all(:params => {:nsPath            => scope_ns_path,
                                                    :relationShortName => 'DeployedTo',
                                                    :recursive         => !@platform})
      end

      instance_ids = deployed_to.map(&:fromCiId)
      @clouds = Cms::Ci.all(:params => {:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'}).to_map(&:ciId)
    end

    if instance_ids.present?
      deployed_to_map = deployed_to.inject({}) do |h, rel|
        rel.toCi = @clouds[rel.toCiId]
        h[rel.fromCiId] = rel
        h
      end

      @ops_states = Operations::Sensor.states(instance_ids)
      unless @state == 'all'
        instance_ids = instance_ids.select do |i|
          state = @ops_states[i]
          state && @state.include?(state)
        end
      end
      @instances = Cms::Ci.list(instance_ids).map {|i| Cms::Ci.new(i, true)}

      if @platform
        pack_ns_path = platform_pack_ns_path(@platform)
        @instances.each {|i| i.add_policy_locations(pack_ns_path)}
      end

      @instances.each do |i|
        ops_state = @ops_states[i.ciId]
        i.opsState   = ops_state
        i.cloud      = deployed_to_map[i.ciId]   #  Only for backward-compatibility.
        i.deployedTo = deployed_to_map[i.ciId].try(:toCi)
      end
    end

    respond_to do |format|
      format.html do
        render_index(component_scope)
        render 'operations/instances/_instance_list'
      end
      format.js {render_index(component_scope)}

      format.json { render :json => @instances }
    end
  end

  def by_cloud
    @instances  = []
    @ops_states = {}
    @cloud      = locate_cloud(params[:cloud])
    deployed_to = Cms::Relation.all(:params => {:nsPath            => scope_ns_path,
                                                :relationShortName => 'DeployedTo',
                                                :fromClassName     => @component.ciClassName.sub(/^manifest./, 'bom.'),
                                                :attr              => "toCiId:eq:#{@cloud.ciId} AND fromCiName:like:#{@component.ciName}-#{@cloud.ciId}-%",
                                                :includeFromCi     => true})
    instance_ids = deployed_to.map(&:fromCiId)
    if instance_ids.present?
      @ops_states = Operations::Sensor.states(instance_ids)
      pack_ns_path = platform_pack_ns_path(@platform)
      @instances = deployed_to.inject([]) do |a, r|
        r.toCi = @cloud
        instance = r.fromCi
        instance.opsState   = @ops_states[instance.ciId]
        instance.cloud = r
        instance.add_policy_locations(pack_ns_path)
        a << instance
      end
    end

    respond_to do |format|
      format.js {render_index(true)}
      format.json { render :json => @instances }
    end
  end


  def show
    @ops_state  = Operations::Sensor.states([@instance])[@instance.ciId]
    @ops_events = Operations::Sensor.events(@instance.ciId)

    @from_relations = Cms::DjRelation.all(:params => {:ciId        => @instance.ciId,
                                                      :direction   => 'from',
                                                      :includeToCi => true})

    @dependents = Cms::DjRelation.all(:params => {:ciId            => @instance.ciId,
                                                  :direction       => 'to',
                                                  :relationName    => 'bom.DependsOn',
                                                  :includeFromCi   => true}).map(&:fromCi)

    respond_to do |format|
      format.html do
        if @instance.rfcId > 0
          release = @instance.releaseId
        else
          @instance_rfc = Cms::RfcCi.find(@instance.lastAppliedRfcId) if @instance.lastAppliedRfcId.present?
          release = @instance_rfc.releaseId
        end

        unless @component.lastAppliedRfcId == @realized_as.relationAttributes.last_manifest_rfc.to_i
          @component_rfc = Cms::RfcCi.find(@component.lastAppliedRfcId) if @component.lastAppliedRfcId.present?
          @component_release = Cms::Release.find(@component_rfc.releaseId) if @component_rfc
        end

        @bom_release = Cms::Release.find(release)
        @release     = Cms::Release.find(@bom_release.parentReleaseId)

        @history_procedures = Cms::Procedure.all(:params => {:actionCiId => @instance.ciId,
                                                             :limit      => 100})
        @procedures         = @history_procedures.select {|p| p.ciId == @instance.ciId}

        @range = params[:range] || 'hour'

        load_custom_actions

        @inprogress_records = @instance.records('inprogress')
        @monitors = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                    :relationShortName => 'WatchedBy',
                                                    :direction         => 'from',
                                                    :includeToCi       => true}).map(&:toCi)

        @deployed_to = @from_relations.select {|r| r.relationName == 'base.DeployedTo'}.first
        if @platform && @deployed_to
          @consumes = Cms::DjRelation.all(:params => {:nsPath    => transition_platform_ns_path(@environment, @platform),
                                                      :direction => 'to'}).find {|r| r.toCiId == @deployed_to.toCiId}
        end
      end

      format.json do
        deployed_to = @from_relations.find {|r| r.relationName == 'base.DeployedTo'}

        @instance.opsState   = @ops_state
        @instance.opsEvents  = @ops_events
        @instance.cloud      = deployed_to
        @instance.deployedTo = deployed_to.try(:toCi)
        @instance.dependsOn  = @from_relations.select {|r| r.relationName == 'bom.DependsOn'}.map(&:toCi)
        @instance.dependents = @dependents

        render_json_ci_response(true, @instance)
      end
    end
  end

  def update
    @assembly = locate_assembly(params[:assembly_id])
    unauthorized unless has_operations?(@assembly.ciId)   # Has to have operations privilleges org scope.
    @instance = Cms::Ci.find(params[:id])
    ci_attrs = params[:ciAttributes] || {}
    update_attrs = ci_attrs.slice(*%w(hypervisor))
    ok = false
    if update_attrs.blank?
      @instance.errors.add(:base, 'Specify ci attributes to update.')
    elsif @instance.ciClassName.end_with?('.Compute') && update_attrs.size == ci_attrs.size
      ok = execute(@instance, :update_attributes, {:ciAttributes => update_attrs})
    else
      @instance.errors.add(:base, "Not allowed to update #{'some of ' if update_attrs.size > 0}specified ci attributes.")
    end
    render_json_ci_response(ok, @instance)
  end

  def destroy
    # TODO traverse and delete ManagedVia
    @instance = Cms::Ci.locate(params[:id], @platform.nsPath.gsub('/manifest/', '/bom/'))
    ok = @instance && @instance.destroy

    respond_to do |format|
      format.js { render :js => "window.location='#{assembly_operations_environment_platform_component_path(@assembly, @environment, @platform, @component)}'" }
      format.json { render_json_ci_response(ok, @instance) }
    end
  end

  def state
    raise UnauthorizedException.new unless is_admin? || has_operations?(params[:assembly_id])
    id = params[:id]
    if id.present?
      @instance = Cms::Ci.find(id)
      # Temp hack to allow only global admins do lb replacement.
      if allow_replace?([@instance])
        @instance.state(params[:state], :relationName => 'bom.ManagedVia', :direction => 'to')
        if @instance.errors.present?
          @error = "Failed to process replace (#{@instance.errors.full_messages}). There are likely some pending deployment changes for this instance and/or other instances managed via this one. " +
                   'Please deploy or discard pending deployment changes before proceeding with replace.';
        end
      else
        @error = 'Only global admins are allowed to perform LB replacement.'
      end
      respond_to do |format|
        format.js
        format.json { render_json_ci_response(@error.present?, @instance) }
      end
    else
      ids = params[:ids]
      # Temp hack to allow only global admins do lb replacement.
      if allow_replace?(Cms::Ci.all(:params => {:ids => ids.join(',')}))
        result, @error = Cms::Ci.state(ids, params[:state], :relationName => 'bom.ManagedVia', :direction => 'to')
        if @error.present?
          @error = "Failed to process bulk replace (#{@error}). There are likely some pending deployment changes for some of the selected instances and/or other instances managed via these ones. " +
                   ' Please deploy or discard pending deployment changes for these instances before proceeding with bulk replace.';
        end
      else
        @error = 'Only global admins are allowed to perform LB replacement.'
      end

      respond_to do |format|
        format.js do
          @instance = Cms::Ci.find(ids.first) if @error.present? && ids.size == 1
        end

        format.json { render_json_ci_response(result, result, [@error]) }
      end
    end
  end

  def cancel_deployment
    records = @instance.records('inprogress')
    records.each {|r| r.update_attribute(:dpmtRecordState, 'canceled')}

    respond_to do |format|
      format.js { @inprogress_records = @instance.records('inprogress') }
      format.json {render :json => records}
    end
  end


  def availability
    created        = @instance.created / 1000
    start_time     = 7.days.ago.to_i
    @notifications = Search::Notification.find_by_ci_id(@instance.ciId, :since => start_time, :_silent => true)
    if @notifications
      @ops_state  = Operations::Sensor.states([@instance])[@instance.ciId]
      @availability     = [calculate_availability(@notifications.select {|n| n['source'] == 'ops'},
                                                  @ops_state,
                                                  start_time > created ? start_time : created,
                                                  Time.now.to_i).merge(:label => '7 days')]
    end

    respond_to do |format|
      format.html {render '_availability'}
      format.js
      format.json {render :json => @availability}
    end
  end

  def notifications
    now        = Time.now
    start_time = now.prev_month.beginning_of_month.to_i
    @notifications = Search::Notification.find_by_ci_id(@instance.ciId, :since => start_time)
    if @notifications
      now_time   = now.to_i
      today      = now.beginning_of_day.to_i
      yesterday  = now.yesterday.beginning_of_day.to_i
      this_week  = now.beginning_of_week.to_i
      prev_week  = now.prev_week.beginning_of_week.to_i
      this_month = now.beginning_of_month.to_i
      ops_state = Operations::Sensor.states([@instance])[@instance.ciId]
      if ops_state
        ops_notifications = @notifications.select {|n| n['source'] == 'ops'}
        @availability = [['yesterday',  yesterday,  today],
                         ['this week',  this_week,  now_time],
                         ['last week',  prev_week,  this_week],
                         ['this month', this_month, now_time],
                         ['last month', start_time, this_month]].inject([]) do |a, period|
          created = @instance.created / 1000
          a << calculate_availability(ops_notifications,
                                      ops_state,
                                      period[1] > created ? period[1] : created,
                                      period[2]).merge(:label => period[0]) if created < period[2]
          a
        end
      end

      @histogram = {:x => [], :y => [], :title => 'Daily Counts', :labels => {:x => 'Day', :y => 'Count'}}
      day = 24 * 3600
      start_time = today - 30 * day
      ((now_time - start_time) / day + 1).times {|i| @histogram[:x] << Time.at(start_time + i * day).strftime('%m/%d')}
      @histogram[:y] = @notifications.inject([0] * @histogram[:x].length) do |a, n|
        notification_time = n['timestamp'] / 1000
        a[(notification_time - start_time) / day] += 1 if notification_time >= start_time
        a
      end
    end

    respond_to do |format|
      format.html {render '_notifications'}
      format.js
      format.json {render :json => @notifications}
    end
  end

  def logs
    data = Daq.instance_logs([{:ci_id => @instance.ciId, :start => 0, :end => 1.minute.from_now.to_i * 1000}])
    @log_data = data && data[0] && data[0]['logData']
  end


  protected

  def ci_resource
    @instance
  end


  private

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    env_id       = params[:environment_id]
    @environment = locate_environment(env_id, @assembly) if env_id.present?
    platform_id  = params[:platform_id]
    @platform    = locate_manifest_platform(platform_id, @environment) if platform_id.present?
    component_id = params[:component_id]
    @component   = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?
  end

  def find_instance
    ci_id = params[:id]
    begin
      @instance = Cms::DjCi.locate(ci_id, @platform.nsPath.gsub('/manifest/', '/bom/'))
    rescue
    end

    unless @instance
      if request.format == :json
        render_json_ci_response(false, nil)
      else
        flash[:error] = "Instance #{ci_id} not found."
        redirect_to assembly_operations_environment_path(@assembly, @environment)
      end
      return
    end

    @instance.add_policy_locations(platform_pack_ns_path(@platform))

    @realized_as = Cms::DjRelation.first(:params => {:ciId              => @instance.ciId,
                                                     :direction         => 'to',
                                                     :relationShortName => 'RealizedAs'})

    @component = @realized_as.fromCi if @realized_as && !@component
  end

  def render_index(component_scope)
    if @instances.present?
      if component_scope && @component.ciClassName.end_with?('.Compute')
        managed_via_rels = []
        @ips_map = @instances.inject({}) do |h, compute|
          attrs = compute.ciAttributes.attributes
          h[compute.ciId] = attrs['private_ip'].presence || attrs['public_ip']
          h
        end
      else
        managed_via_rels = Cms::Relation.all(:params => {:nsPath       => scope_ns_path,
                                                         :recursive    => !@platform,
                                                         :relationName => 'bom.ManagedVia'})
        computes = Cms::Ci.list(managed_via_rels.map(&:toCiId).uniq).to_map {|c| c['ciId']}
        @ips_map = managed_via_rels.inject({}) do |h, r|
          compute = computes[r.toCiId]
          if compute
            attrs = compute['ciAttributes']
            h[r.fromCiId] = attrs['private_ip'].presence || attrs['public_ip'] if attrs
          end
          h
        end
      end

      unless component_scope
        @managed_via_health = managed_via_rels.inject({}) do |h, r|
          h[r.fromCiId] = @ops_states[r.toCiId]
          h
        end
        @deployment = Cms::Deployment.latest(:nsPath => scope_ns_path)
        @deployment_info = Search::WorkOrder.state_info(@deployment) if @deployment
      end

      @instance_procedures = Cms::Procedure.all(:params => {:nsPath    => @environment ? environment_bom_ns_path(@environment) : assembly_ns_path(@assembly),
                                                            :recursive => true,
                                                            :actions   => true,
                                                            :state     => 'active,pending',
                                                            :limit     => 1000}).inject({}) do |m, p|
        p.actions.each {|a| m[a.ciId] = p}
        m
      end

      bad_state_instance_ids = @instances.inject([]) do |a, i|
        ops_state = @ops_states[i.ciId]
        a << i.ciId if ops_state.present? && ops_state != 'good'
        a
      end
      events = Operations::Sensor.events_for_instances(bad_state_instance_ids)
      @bad_state_events = events.to_map_with_value {|e| [e.keys.first.to_i, e.values.first.values.flatten]} if events.present?
    else
      @instance_procedures = {}
    end

    @actions = (@component && @instances.present?) ? @instances.first.meta.actions : [CustomAction.new('repair', 'repair', 'repair')]
    load_custom_actions
  end

  def load_custom_actions
    @custom_actions = @component ? Operations::InstancesController.load_custom_actions(@component) : []
  end

  def scope_ns_path
    if @platform || @component
      bom_platform_ns_path(@environment, @platform)
    elsif @environment
      environment_bom_ns_path(@environment)
    elsif @assembly
      assembly_ns_path(@assembly)
    end
  end

  def calculate_availability(ops_notifications, end_state, start_time, end_time)
    state = end_state
    time = end_time
    # This logic relies on notifications sorted chronologically in descending order (most recent to oldest).
    availability = ops_notifications.inject({:total => (end_time - start_time), :states => {}}) do |a, n|
      notification_time = n['timestamp'] / 1000
      break a if notification_time < start_time

      payload = n['payload']
      if notification_time < end_time
        state_bucket = (payload && payload['newState']).presence || state
        a[:states][state_bucket] = (a[:states][state_bucket] || 0) + time - notification_time
        time = notification_time
      end
      state = (payload && payload['oldState']).presence || 'unknown'
      a
    end

    availability[:states][state] = (availability[:states][state] || 0) + (time - start_time)
    availability_base_time = (availability[:total] - (availability[:states]['unknown'] || 0))
    availability[:availability] = availability_base_time > 0 ? (availability[:states]['good'] || 0).to_f / availability_base_time : nil
    return availability
  end

  def allow_replace?(instances)
    class_names = Settings.replace_by_global_admin_only
    class_names.blank? || !global_admin_mode? || is_global_admin? || instances.none? {|i| class_names.include?(i.ciClassName.split('.').last.downcase)}
  end
end
