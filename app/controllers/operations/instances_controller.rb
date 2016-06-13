class Operations::InstancesController < ApplicationController
  include ::RfcHistory

  NOTIFICATION_RANGES       = %w(day week month)
  NOTIFICATION_RANGE_LENGTH = HashWithIndifferentAccess.new(:day   => 60 * 60 * 24,
                                                            :week  => 60 * 60 * 24 * 7,
                                                            :month => 60 * 60 * 24 * 31)

  before_filter :find_parents, :except => [:state]
  before_filter :find_instance, :except => [:index, :state, :destroy]

  CustomAction = Struct.new(:actionId, :actionName, :description)

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
    @clouds         = {}
    @state          = params[:instances_state]
    deployed_to     = []
    if @state.present?
      if component_scope
        @instances = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                     :direction         => 'from',
                                                     :relationShortName => 'RealizedAs'}).map(&:toCi)
        deployed_to = Cms::DjRelation.all(:params => {:nsPath            => @instances.first.nsPath,
                                                      :relationShortName => 'DeployedTo'}) if @instances.present?
      else
        deployed_to = Cms::DjRelation.all(:params => {:nsPath            => scope_ns_path,
                                                      :relationShortName => 'DeployedTo',
                                                      :recursive         => true,
                                                      :includeFromCi     => true})
        @instances = deployed_to.map(&:fromCi)
      end

      @clouds = Cms::Ci.all(:params => {:nsPath      => clouds_ns_path,
                                        :ciClassName => 'account.Cloud'}).inject({}) do |h, c|
        h[c.ciId] = c
        h
      end
    end

    if @instances.present?
      if @platform
        pack_ns_path = platform_pack_ns_path(@platform)
        @instances.each { |i| i.add_policy_locations(pack_ns_path) }
      end

      deployed_to_map = deployed_to.inject({}) do |h, rel|
        rel.attributes.delete(:fromCi)   # This is very important not only to reduce the payload size but more importantly to prevent potential cyclic references during json generation in json responder.
        rel.toCi = @clouds[rel.toCiId]
        h[rel.fromCiId] = rel
        h
      end

      @ops_states = @instances.blank? ? {} : Operations::Sensor.states(@instances)
      @instances = @instances.select do |i|
        i.opsState   = @ops_states[i.ciId]
        i.cloud      = deployed_to_map[i.ciId]
        i.deployedTo = deployed_to_map[i.ciId].try(:toCi)

        @state == 'all' || (i.opsState && @state.include?(i.opsState))
      end
    end

    respond_to do |format|
      format.js do
        if @instances.present?
          unless component_scope
            @managed_via_health = Cms::Relation.all(:params => {:nsPath       => scope_ns_path,
                                                                :recursive    => true,
                                                                :relationName => 'bom.ManagedVia'}).inject({}) do |h, r|
              h[r.fromCiId] = @ops_states[r.toCiId]
              h
            end
            @deployment = Cms::Deployment.latest(:nsPath => scope_ns_path)
            @deployment_info = Search::WorkOrder.state_info(@deployment) if @deployment
          end

          @instance_procedures = Cms::Procedure.all(:params => {:nsPath    => organization_ns_path,
                                                                :recursive => true,
                                                                :actions   => true,
                                                                :state     => 'active',
                                                                :limit     => 10000}).inject({}) do |m, p|
            p.actions.each {|a| m[a.ciId] = p}
            m
          end
        else
          @instance_procedures = {}
        end

        @actions = (@component && @instances.present?) ? @instances.first.meta.actions : [CustomAction.new('repair', 'repair', 'repair')]
        load_custom_actions
      end

      format.json { render :json => @instances }
    end
  end

  def show
    @ops_state  = Operations::Sensor.states([@instance])[@instance.ciId]
    @ops_events = Operations::Events.for_instance(@instance.ciId)

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

        @history_procedures = Cms::Procedure.all(:params => {:actionCiId => @instance.ciId})
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
      @instance.state(params[:state], :relationName => 'bom.ManagedVia', :direction => 'to')
      @error = @instance.errors.full_messages if @instance.errors.present?
      respond_to do |format|
        format.js
        format.json { render_json_ci_response(@error.present?, @instance) }
      end
    else
      ids = params[:ids]
      result, @error = Cms::Ci.state(ids, params[:state], :relationName => 'bom.ManagedVia', :direction => 'to')
      respond_to do |format|
        format.js do
          @instance = Cms::Ci.find(ids.first) if @error.present? && ids.size == 1
        end

        format.json { render_json_ci_response(result, result, [@error]) }
      end
    end
  end

  def cancel_deployment
    @instance.records('inprogress').each {|r| r.update_attribute(:dpmtRecordState, 'canceled')}
    @inprogress_records = @instance.records('inprogress')
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

    @component = @realized_as.fromCi unless @component
  end

  def load_custom_actions
    @custom_actions = @component ? Operations::InstancesController.load_custom_actions(@component) : []
  end

  def scope_ns_path
    if @platform
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
end
