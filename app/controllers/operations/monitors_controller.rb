class Operations::MonitorsController < Base::MonitorsController
  CHART_TIME_RANGES       = %w(hour day week month year)
  CHART_TIME_RANGE_LENGTH = HashWithIndifferentAccess.new(:hour  => 60 * 60,
                                                          :day   => 60 * 60 * 24,
                                                          :week  => 60 * 60 * 24 * 7,
                                                          :month => 60 * 60 * 24 * 31,
                                                          :year  => 60 * 60 * 24 * 365)
  CHART_TIME_RANGE_STEP   = HashWithIndifferentAccess.new(:hour  => 60,
                                                          :day   => 60 * 5,
                                                          :week  => 60 * 15,
                                                          :month => 60 * 60,
                                                          :year  => 60 * 60 * 24)

  before_filter :find_parents, :only => [:index, :show, :charts]

  def self.threshold_monitor_map(monitors)
    monitors.inject({}) do |map, monitor|
      map[monitor.ciName] ||= {}
      JSON.parse(monitor.ciAttributes.thresholds).keys.inject(map[monitor.ciName]) {|m, th| m[th] = monitor; m}
      map[monitor.ciName]['Heartbeat'] = monitor if monitor.ciAttributes.heartbeat == 'true'
      map
    end
  end

  def index
    pack_ns_path = platform_pack_ns_path(@platform)
    @monitors = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                :relationShortName => 'WatchedBy',
                                                :direction         => 'from',
                                                :includeToCi       => true}).map do |r|
      monitor = r.toCi
      monitor.add_policy_locations(pack_ns_path)
      monitor
    end

    @events = {}
    events_by_monitor = Operations::Sensor.events(@instance.ciId)
    if events_by_monitor.present?
      monitor_threshold_map = self.class.threshold_monitor_map(@monitors)
      events_by_monitor.each_pair do |monitor_name, events|
        events.each do |event|
          threshold_map = monitor_threshold_map[monitor_name]
          monitor = threshold_map && threshold_map[event['name']]
          if monitor
            state = event['state']
            @events[monitor.ciId] ||= {}
            @events[monitor.ciId][state] ||= []
            @events[monitor.ciId][state] << event
          end
        end
      end
    end

    respond_to do |format|
      format.html {render 'operations/monitors/_monitors'}
      format.js
      format.json { render :json => @monitors }
    end
  end

  def show
    @monitor = locate_ci_in_platform_ns(params[:id], @platform, 'manifest.Monitor')
    @range = params[:range] || 'hour'
    @monitor.charts = get_charts(@instance, [@monitor], params)

    respond_to do |format|
      format.js
      format.json { render :json => @monitor }
    end
  end

  def charts
    @monitors = Cms::Ci.all(:params => {:ids => params[:ids].join(',')}).inject([]) do |h, monitor|
      h << monitor if monitor.nsPath == @platform.nsPath   # Security safe check just in case.
      h
    end
    @charts = get_charts(@instance, @monitors, params)
    respond_to do |format|
      format.js
      format.json { render :json => @charts}
    end
  end


  private

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
    component_id = params[:component_id]
    @component   = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?
    @instance    = Cms::DjCi.locate(params[:instance_id], @platform.nsPath.gsub('/manifest/', '/bom/'))
    unless @component
      @component = Cms::DjRelation.first(:params => {:ciId              => @instance.ciId,
                                                     :direction         => 'to',
                                                     :relationShortName => 'RealizedAs'}).fromCi
    end
  end

  def get_charts(instance, monitors, options)
    start_time = options[:start_time].to_i
    end_time   = options[:end_time].to_i
    step       = options[:step].to_i
    unless start_time > 0 && step > 0 && (end_time > start_time)
      @range       = options[:range].presence || session[:monitor_chart_range].presence || 'hour'
      step         = CHART_TIME_RANGE_STEP[@range]
      range_length = CHART_TIME_RANGE_LENGTH[@range]
      current_time = Time.now.to_i
      end_time     = current_time - (current_time % step)
      start_time   = end_time - range_length

      session[:monitor_chart_range] = @range
    end

    groups = monitors.inject({}) do |s, monitor|
      monitor_name = monitor.ciName
      metrics      = ActiveSupport::JSON.decode(monitor.ciAttributes.metrics)
      metrics.each do |metric_name, metric|
        group = (metric.has_key?('display_group') && !metric['display_group'].empty?) ? metric['display_group'] : monitor_name
        s[group] ||= {:ci_id      => instance.ciId,
                      :name       => group,
                      :series     => [],
                      :start_time => start_time,
                      :end_time   => end_time,
                      :step       => step,
                      :data       => []}
        s[group][:series] << "#{monitor_name}:#{metric_name}"
      end
      s
    end

    chart_data = Daq.charts([{:ci_id   => instance.ciId,
                            :start   => start_time,
                            :end     => end_time,
                            :step    => step,
                            :metrics => groups.values.inject([]) { |a, g| a += g[:series]; a}}])
    if chart_data
      chart_data_map = chart_data.inject({}) do |m, metric_data|
        m[metric_data['header']['metric']] = metric_data
        m
      end
      groups.values.each do |group|
        group[:series].each {|metric| group[:data] << chart_data_map[metric]}
      end
    end

    return groups.values
  end
end
