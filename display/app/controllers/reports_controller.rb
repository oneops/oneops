class ReportsController < ApplicationController
  before_filter :weak_ci_relation_data_consistency, :only => [:compute, :cost, :health, :notification]

  AVAILABLE_LABEL = '~Free~'
  CONSUMED_LABEL  = '~Used~'

  def compute
    report_data = params[:data]
    report_data = JSON.parse(report_data) if report_data.present?
    report_data = compute_report_data if report_data.blank?

    respond_to do |format|
      format.html { render 'organization/_reports' }
      format.json { render :json => report_data }

      format.js do
        recipients = params[:recipients]
        if recipients.present?
          ReportMailer.compute(recipients.split(','),
                               :data  => report_data,
                               :user  => user_signed_in? && current_user,
                               :title => "Compute report for '#{report_data['data']['name']}'.",
                               :note  => params[:note]).deliver

          flash[:notice] = 'Report has been emailed to specified recipients.'
          render :js => ''
        else
          @div  = params[:update]
          @data = report_data
          render :action => :compute
        end
      end
    end
  end

  def health
    respond_to do |format|
      format.html
      format.json do
        component_rels = Cms::Relation.all(:params => {:nsPath       => organization_ns_path,
                                                       :recursive    => true,
                                                       :relationName => 'manifest.Requires'})
        state_info     = Operations::Sensor.component_states(component_rels.map(&:toCiId))

        data = component_rels.inject({}) do |m, rel|
          health_info = state_info[rel.toCiId.to_s]
          #unless health_info
          #  tot = rand(8).to_i + 2
          #  prob = rand(50)
          #  health_info = {'total' => tot, 'good' => tot - (prob < 35 ? 0 : (prob < 45 ? 1 : 2))}
          #end
          if health_info
            info                                    = JSON.parse(rel.comments)
            foo, org, assembly, env, area, platform = rel.nsPath.split('/')
            info['toCiClass']

            m[assembly]                                 ||= {:url => assembly_url(assembly)}
            m[assembly][env]                            ||= {:url => assembly_operations_environment_url(assembly, env)}
            m[assembly][env][platform]                  ||= LeafNode.new(:metrics => {:size => 0},
                                                                         :heat    => {:all => 1, :unhealthy => 1, :overutilized => 1, :underutilized => 1, :notify => 1},
                                                                         :url     => assembly_operations_environment_platform_url(assembly, env, platform))
            total                                       = health_info['total']
            m[assembly][env][platform][:metrics][:size] += total
            total                                       = total.to_f
            heat                                        = m[assembly][env][platform][:heat]
            heat[:all]                                  = min(heat[:all], -(total - (health_info['good'] || 0)) / total)
            heat[:unhealthy]                            = min(heat[:unhealthy], -(health_info['unhealthy'] || 0) / total)
            heat[:notify]                               = min(heat[:notify], -(health_info['notify'] || 0) / total)
            heat[:overutilized]                         = min(heat[:overutilized], -(health_info['overutilized'] || 0) / total)
            heat[:underutilized]                        = min(heat[:underutilized], -(health_info['underutilized'] || 0) / total)
          end
          m
        end

        report_data = {:scope   => %w(Assembly Environment Platform),
                       :metrics => [{:name => :size, :label => 'Size'}],
                       :heat    => [{:name => :unhealthy, :label => 'Unhealthy', :reverse => true},
                                    {:name => :notify, :label => 'Notify', :reverse => true},
                                    {:name => :overutilized, :label => 'Overutilized', :reverse => true},
                                    {:name => :underutilized, :label => 'Underutilized', :reverse => true},
                                    {:name => :all, :label => 'All', :reverse => true}],
                       :data    => graph_node(current_user.organization.name, data)}

        render :json => report_data
      end
    end
  end

  def notification
    @size = (params[:size].presence || 200).to_i
    respond_to do |format|
      format.html do
        @notifications = []
      end

      format.js do
        histogram = !params.include?(:offset)
        fetch_notifications(histogram)
        unless histogram || @notifications
          flash[:error] = 'Failed to load notifications.'
          render :js => ''
        end
      end

      format.json do
        fetch_notifications
        if @notifications
          set_pagination_response_headers(@notifications)
          render :json => @notifications
        else
          render :json => {:code => 500, :exception => 'Failed to fetch notifications.'}, :status => :internal_server_error
        end
      end
    end
  end

  def cost
    min_ns_path = search_ns_path
    @ns_path    = params[:ns_path] || min_ns_path
    @ns_path    = min_ns_path unless @ns_path.start_with?(min_ns_path)

    begin
      @start_date = Date.parse(params[:start_date])
    rescue Exception => e
      @start_date = Date.today.prev_month(2).beginning_of_month
    end

    begin
      @end_date = Date.parse(params[:end_date])
    rescue Exception => e
      @end_date = Date.today.end_of_month
    end
    @end_date = @start_date.end_of_month if @start_date > @end_date

    @interval = params[:interval]
    inteval_length = (@end_date - @start_date).days
    unless @interval.present? && %w(month week day).include?(@interval) && inteval_length >= 1.send(@interval)
      if inteval_length > 31.days
        @interval = 'month'
      elsif inteval_length > 7.days
        @interval = 'week'
      else
        @interval = 'day'
      end
    end

    @tags          = params[:tags]
    groupings     = [{:name => :by_service, :label => 'Service Type'}]
    ns_path_split = @ns_path.split('/')
    ns_path_depth = ns_path_split.size
    groupings << {:name => :by_cloud, :label => 'Cloud', :url => lambda {|x| edit_cloud_path(x)}} if ns_path_depth > 1

    if ns_path_depth == 0
      # Cross-org.
      groupings << {:name  => :by_organization,
                    :label => 'Organization',
                    :path  => :by_ns,
                    :sum   => lambda {|x| x.split('/')[1]},
                    :url   => lambda {|x| organization_public_profile_path(:org_name => x)}}
      groupings << {:name  => :by_assembly,
                    :label => 'Assembly',
                    :path  => :by_ns,
                    :sum   => lambda {|x| x.split('/', 4)[1..2].join('/')},
                    :url   => lambda {|x| assembly_path(x)}}

      add_tag_groupings(groupings, @tags)
    elsif ns_path_depth == 2
      # Org level.
      groupings << {:name  => :by_assembly,
                    :label => 'Assembly',
                    :path  => :by_ns,
                    :sum   => lambda {|x| x.split('/')[2]},
                    :url   => lambda {|x| assembly_path(x)}}
      groupings << {:name  => :by_environment,
                    :label => 'Environment',
                    :path  => :by_ns,
                    :sum   => lambda {|x| x.split('/')[2..3].join('/')},
                    :url   => lambda {|x| split = x.split('/'); assembly_transition_environment_path(split[0], split[1])}}

      add_tag_groupings(groupings, @tags)
    elsif ns_path_depth == 3
      # Assembly level.
      groupings << {:name  => :by_environment,
                    :label => 'Environment',
                    :path  => :by_ns,
                    :sum   => lambda {|x| x.split('/')[3]},
                    :url   => lambda {|x| split = x.split('/'); assembly_transition_environment_path(ns_path_split[2], x)}}
      groupings << {:name  => :by_platform,
                    :label => 'Platform',
                    :path  => :by_ns,
                    :sum   => lambda {|x| ns_split = x.split('/'); "#{ns_split[-4]}/#{ns_split[-2]} ver.#{ns_split[-1]}"},
                    :url   => lambda {|x| split = x.split('/'); assembly_transition_environment_platform_path(ns_path_split[2], split[0], split[1].sub(' ver.', '!'))}}
    elsif ns_path_depth == 4
      # Env level.
      groupings << {:name  => :by_platform,
                    :label => 'Platform',
                    :path  => :by_ns,
                    :sum   => lambda {|x| ns_split = x.split('/'); "#{ns_split[-2]} ver.#{ns_split[-1]}"},
                    :url   => lambda {|x| assembly_transition_environment_platform_path(ns_path_split[2], ns_path_split[3], x.sub(' ver.', '!'))}}
    end

    data = Search::Cost.cost_time_histogram(@ns_path, @start_date, @end_date, @interval, @tags)
    if data
      x, y = data[:buckets].inject([[],[]]) do |xy, time_bucket|
        case @interval
          when 'month'
            xy.first << Date.parse(time_bucket['from_as_string']).strftime('%b %Y')
          when 'day'
            xy.first << Date.parse(time_bucket['from_as_string']).strftime('%b %d')
          else
            xy.first << "#{Date.parse(time_bucket['from_as_string']).strftime('%b %d')} - #{(Date.parse(time_bucket['to_as_string']) - 1.day).strftime('%b %d')}"
        end
        xy.last << groupings.inject({}) do |grouping_data, grouping|
          grouping_name = grouping[:name]
          sum = grouping[:sum]
          url = grouping[:url]
          grouping_data[grouping_name] = time_bucket[(grouping[:path].presence || grouping_name).to_s]['buckets'].inject([]) do |aa, grouping_bucket|
            aa << {:label => grouping_bucket['key'],
                   :value => grouping_bucket['cost']['value'],
                   :url   => url && !sum && url.call(grouping_bucket['key'])}
          end
          if sum
            sum_aggs = grouping_data[grouping_name].inject({}) do |r, e|
              key = sum.call(e[:label])
              r[key] ||= 0
              r[key] += e[:value]
              r
            end
            grouping_data[grouping_name] = sum_aggs.map {|k, v| {:label => k,
                                                                 :value => v,
                                                                 :url   => url && url.call(k)}}
          end
          grouping_data[:total] = time_bucket['total']['value']
          grouping_data
        end
        xy
      end
      time_unit = data[:interval].capitalize
      @cost = {:title     => "#{time_unit == 'Day' ? 'Dai' : time_unit}ly Cost",
               :units     => {:x => @interval, :y => data[:unit]},
               :labels    => {:x => nil, :y => nil},
               :groupings => groupings,
               :total     => data[:total],
               :x         => x,
               :y         => y}
    end

    # @cost = {:title     => 'Monthly Cost',
    #          :labels    => {:x => 'Month', :y => 'Cost (USD)'},
    #          :groupings => [{:name => :service_type, :label => 'By Service Type'},
    #                         {:name => :cloud, :label => 'By Cloud'}],
    #          :x         => %w(Aug Sep Oct Nov),
    #          :y         => [{:service_type => [{:label => 'compute', :value => 7},
    #                                            {:label => 'dns', :value => 1},
    #                                            {:label => 'storage', :value => 4}],
    #                          :cloud        => [{:label => 'dal1', :value => 16},
    #                                            {:label => 'dfw1', :value => 13},
    #                                            {:label => 'dfw2', :value => 3}]},
    #                         {:service_type => [{:label => 'compute', :value => 2.7},
    #                                            {:label => 'dns', :value => 1.4},
    #                                            {:label => 'storage', :value => 1.2}],
    #                          :cloud        => [{:label => 'dal1', :value => 3.7},
    #                                            {:label => 'dfw1', :value => 2.4},
    #                                            {:label => 'dfw3', :value => 1.5}]},
    #                         {:service_type => [{:label => 'compute', :value => 23},
    #                                            {:label => 'dns', :value => 6},
    #                                            {:label => 'storage', :value => 4}],
    #                          :cloud        => [{:label => 'dal1', :value => 13},
    #                                            {:label => 'dfw1', :value => 7},
    #                                            {:label => 'dfw2', :value => 2},
    #                                            {:label => 'dfw3', :value => 5}]},
    #                         {:service_type => [{:label => 'compute', :value => 15},
    #                                            {:label => 'whatever', :value => 5.5},
    #                                            {:label => 'dns', :value => 2.5},
    #                                            {:label => 'storage', :value => 5.5}],
    #                          :cloud        => [{:label => 'dal1', :value => 8},
    #                                            {:label => 'dfw1', :value => 7},
    #                                            {:label => 'dfw2', :value => 6}]}]}
    # @cost = {:title  => 'Monthly Cost',
    #          :labels => {:x => 'Month', :y => 'Cost (USD)'},
    #          :x      => %w(Aug Sep Oct Nov),
    #          :y      => [12, 5, 19, 25]}
    respond_to do |format|
      format.html
      format.js
      format.json do
        if @cost
          @cost[:tags] = @tag_info if @tag_info
          render :json => @cost
        else
          render :json => {:errors => ['Failed to fetch cost data']}, :status => :internal_server_error
        end
      end
    end
  end

  def add_tag_groupings(groupings, tags)
    tags.each {|tag| groupings << {:name => "by_#{tag}".to_sym, :label => tag}} if tags.present?
  end


  protected

  def fetch_notifications(histogram = false)
    path          = params[:ns_path]
    suffix        = path.present? ? path.gsub(/^#{organization_ns_path}/, '') : ''
    ns_path       = "#{organization_ns_path}/#{suffix.gsub(/^\//, '')}"
    search_params = {}
    period        = params[:period] || 'today'
    now           = Time.now
    case period
      when 'yesterday'
        start_time = now.yesterday.beginning_of_day.to_i
        end_time   = now.beginning_of_day.to_i
      when 'this week'
        start_time = now.beginning_of_week.to_i
        end_time   = now.end_of_week.to_i
      when 'last week'
        start_time = now.beginning_of_week.yesterday.beginning_of_week.to_i
        end_time   = now.beginning_of_week.yesterday.end_of_week.to_i
      else
        start_time = now.beginning_of_day.to_i
        end_time   = now.tomorrow.beginning_of_day.to_i
    end

    # Start and end times are in seconds but notification timestamp is in ms.
    start_time *= 1000
    end_time *= 1000

    type                     = params[:source]
    search_params[:type]     = type if type.present? && type != 'all'
    severity                 = params[:severity]
    search_params[:severity] = severity if severity.present? && severity != 'all'

    queries =[]
    query   = params[:query]
    queries << "*#{query}*" if query.present?
    filter = params[:filter]
    queries << filter[:query] if filter.present?
    search_params[:query] = queries.join(' AND ') if queries.present?

    if histogram
      @histogram = {:x         => [],
                    :y         => [],
                    :groupings => ::NotificationSummary::HISTOGRAM_GROUPING,
                    :labels    => {:y => 'Count'}}
      if period == 'today' || period == 'yesterday'
        @histogram[:title]      = 'Hourly Counts'
        @histogram[:labels][:x] = 'Time (hours)'

        ranges, unit = notification_histogram_ranges(start_time, end_time, 24)
        time_offset = browser_timezone_offset * 3600
        @histogram[:x] = ranges.map {|r| "#{Time.at(r.first / 1000 + time_offset).utc.strftime('%H:%M')} - #{Time.at(r.last / 1000 + time_offset).utc.strftime('%H:%M')}"}
      else
        @histogram[:title]      = 'Daily Counts'
        @histogram[:labels][:x] = 'Day of the week'

        ranges, unit = notification_histogram_ranges(start_time, end_time, 7)
        @histogram[:x] = %w(Mon Tue Wed Thu Fri Sat Sun)
      end
      hist_data = Search::Notification.histogram(ns_path, ranges, search_params.merge(:_silent => true))
      unless hist_data
        @histogram = nil
        return
      end

      y_values = [0] * @histogram[:x].length
      hist_data.each do |r|
        # A little hack below in sorting severity buckets (" sort_by {|b| -b['key'].size} ") to ensure
        # proper order of 'critical', 'warnning', 'info'.
        y_values[(r['from'] - start_time) / unit] = {:by_source => r['by_source']['buckets'].map{|b| {:label => b['key'], :value => b['doc_count']}},
                                                     :by_severity => r['by_severity']['buckets'].sort_by {|b| -b['key'].size}.map{|b| {:label => b['key'], :value => b['doc_count']}}}
      end
      @histogram[:y] = y_values
    else
      search_params[:size]  = @size
      search_params[:from]  = (params[:offset].presence || 0).to_i
      search_params[:sort]  = params[:sort]
      search_params[:start] = start_time
      search_params[:end]   = end_time

      @notifications = Search::Notification.find_by_ns(ns_path, search_params)
    end
  end

  def notification_histogram_ranges(start_time, end_time, range_count)
    unit   = (end_time - start_time) / range_count
    ranges = range_count.times.inject([]) { |a, i| a << [start_time + i * unit, start_time + (i + 1) * unit] }
    return ranges, unit
  end

  def compute_report_data
    graph_data, scope = compute_report_graph_data
    return {:scope   => scope,
            :metrics => [{:name => :cores, :label => 'Cores'},
                         {:name => :ram, :label => 'Memory'},
                         {:name => :count, :label => 'Instances'}],
            :data    => graph_data}
  end

  def compute_report_graph_data
    scope      = []
    assemblies = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                         :ciClassName => 'account.Assembly'}).inject({}) do |h, a|
      h[a.ciName] = a
      h
    end

    cloud_id = params[:cloud].to_i
    if params[:grouping] == 'cloud'
      if cloud_id > 0
        cloud = locate_cloud(cloud_id)
        data  = Cms::Relation.all(:params => {:ciId              => cloud_id,
                                              :direction         => 'to',
                                              :relationShortName => 'DeployedTo',
                                              :targetClassName   => 'Compute',
                                              :includeFromCi     => true}).inject({}) do |m, r|
          foo, org, assembly, env, area, platform = r.fromCi.nsPath.split('/')
          m[assembly]                             ||= {:url => assembly.present? ? assembly_url(assembly) : nil, :info => {:owner => assemblies[assembly].ciAttributes.owner}}
          m[assembly][env]                        ||= {:url => assembly.present? && env.present? ? assembly_operations_environment_url(assembly, env) : nil}
          m[assembly][env][platform]              ||= LeafNode.new(:metrics => empty_compute_metrics,
                                                                   :url     => assembly.present? && env.present? && platform.present? ? assembly_operations_environment_platform_url(assembly, env, platform) : nil)
          aggregate_compute_metrics(m[assembly][env][platform][:metrics], r.fromCi)
          m
        end

        graph_data = graph_node(cloud.ciName, data)
        scope      = [{'Assembly' => ['name', 'owner']}, 'Environment', 'Platform']
      else
        clouds                              = get_cloud_map(clouds_ns_path)
        quota_map, cloud_compute_tenant_map = get_quota_map

        data = Cms::Relation.all(:params => {:nsPath            => organization_ns_path,
                                             :relationShortName => 'DeployedTo',
                                             :fromClassName     => 'Compute',
                                             :recursive         => true,
                                             :includeFromCi     => true}).inject({}) do |m, r|
          bom_compute                                  = r.fromCi
          foo, org, assembly, env, area, platform      = bom_compute.nsPath.split('/')
          cloud                                        = clouds[r.toCiId]
          cloud_name                                   = cloud ? cloud.ciName : '???'
          compute_service_info                         = cloud_compute_tenant_map[r.toCiId]
          key                                          = compute_service_info ? compute_service_info[:quota_key] : cloud_name
          m[key]                                       ||= {}
          m[key][CONSUMED_LABEL]                       ||= {}
          m[key][CONSUMED_LABEL][cloud_name]           ||= {:url => edit_cloud_url(cloud)}
          m[key][CONSUMED_LABEL][cloud_name][assembly] ||= LeafNode.new(:metrics => empty_compute_metrics,
                                                                        :url     => assembly.present? && env.present? ? assembly_operations_environment_url(assembly, env) : nil,
                                                                        :info    => {:owner => assemblies[assembly].ciAttributes.owner})
          aggregate_compute_metrics(m[key][CONSUMED_LABEL][cloud_name][assembly][:metrics], bom_compute)
          m
        end

        graph_data = graph_node(current_user.organization.name, data)

        compute_org_tenant_nodes = graph_data[:children]
        quota_map.each_pair do |compute_org_tenant, quota|
          loc_tenant_node = compute_org_tenant_nodes.find { |node| node[:name] == compute_org_tenant }
          unless loc_tenant_node
            loc_tenant_node = {:name => compute_org_tenant, :children => [], :level => 3, :metrics => empty_compute_metrics, :id => random_node_id}
            compute_org_tenant_nodes << loc_tenant_node
          end
          insert_quota_data(loc_tenant_node, quota)
        end
        aggregate_graph_node(graph_data)

        scope = ['Service/Org/Tenant', 'Allocation', 'Cloud', {'Assembly' => ['name', 'owner']}]
      end
    else
      assembly_id = params[:assembly]
      if assembly_id.present?
        assembly_ci = locate_assembly(assembly_id)
        ns_path     = assembly_ns_path(assembly_ci)
        scope       = %w(Environment Platform)
      else
        ns_path = organization_ns_path
        scope   = [{'Assembly' => ['name', 'owner']}, 'Environment', 'Platform']
      end
      data = Cms::Relation.all(:params => {:nsPath            => ns_path,
                                           :relationShortName => 'DeployedTo',
                                           :fromClassName     => 'Compute',
                                           :recursive         => true,
                                           :includeFromCi     => true}).inject({}) do |m, r|
        ci                                      = r.fromCi
        foo, org, assembly, env, area, platform = ci.nsPath.split('/')
        m[assembly]                             ||= {:url => assembly.present? ? assembly_url(assembly) : nil, :info => {:owner => assemblies[assembly].ciAttributes.owner}}
        m[assembly][env]                        ||= {:url => assembly.present? && env.present? ? assembly_operations_environment_url(assembly, env) : nil}
        m[assembly][env][platform]              ||= LeafNode.new(:metrics => empty_compute_metrics,
                                                                 :url     => assembly.present? && env.present? && platform.present? ? assembly_operations_environment_platform_url(assembly, env, platform) : nil)
        aggregate_compute_metrics(m[assembly][env][platform][:metrics], ci)
        m
      end

      if assembly_id
        graph_data = graph_node(assembly_ci.ciName, data[assembly_ci.ciName])
      else
        graph_data = graph_node(current_user.organization.name, data)
        quota      = get_quota_map[0].values.inject(empty_compute_metrics) do |m, q|
          m[:count] += q[:count]
          m[:cores] += q[:cores]
          m[:ram]   += q[:ram]
          m
        end
        insert_quota_data(graph_data, quota)
      end
    end

    return graph_data, scope
  end

  def get_quota_map(ns_path = organization_ns_path)
    cloud_compute_tenant_map = {}
    quota                    = Cms::DjRelation.all(:params => {:nsPath            => ns_path,
                                                               :relationShortName => 'Provides',
                                                               :fromClassName     => 'account.Cloud',
                                                               :recursive         => true,
                                                               :attr              => 'service:eq:compute',
                                                               :includeToCi       => true}).inject({}) do |m, r|
      compute      = r.toCi
      attributes   = compute.ciAttributes
      compute_name = compute.ciName
      org          = r.toCi.nsPath.split('/')[1]
      tenant       = compute.ciAttributes.attributes.has_key?(:tenant) ? attributes.tenant : ''
      key          = "#{compute_name}/#{org}/#{tenant}"

      cloud_compute_tenant_map[r.fromCiId] = {:compute => compute_name, :tenant => tenant, :quota_key => key}

      count = attributes.respond_to?(:max_instances) ? attributes.max_instances.to_i : 0
      cores = attributes.respond_to?(:max_cores) ? attributes.max_cores.to_i : 0
      ram   = attributes.respond_to?(:max_ram) ? attributes.max_ram.to_i : 0
      if (!m[key] && (count > 0 || cores > 0 || ram > 0)) || (m[key] && count > 0 && cores > 0 && ram > 0)
        m[key] = {:count => count, :cores => cores, :ram => ram}
      end
      m
    end

    return quota, cloud_compute_tenant_map
  end

  def get_cloud_map(ns_path = clouds_ns_path)
    Cms::Ci.all(:params => {:nsPath      => ns_path,
                            :ciClassName => 'account.Cloud',
                            :recursive   => true}).inject({}) do |m, c|
      m[c.ciId] = c
      m
    end
  end

  def aggregate_compute_metrics(metrics, ci)
    metrics[:count] += 1
    metrics[:cores] += (ci.ciAttributes.cores || 1).to_i if ci.ciAttributes.attributes.include?('cores')
    metrics[:ram]   += (ci.ciAttributes.ram || 2000).to_i if ci.ciAttributes.attributes.include?('ram')
  end

  def random_node_id
    SecureRandom.random_number(36**6).to_s(36)
  end

  def graph_node(name, data)
    result = {:name => name, :id => random_node_id}
    return result.merge(:metrics => empty_compute_metrics) unless data

    result[:url] = data.delete(:url)
    info         = data.delete(:info) || {}
    info.each_pair do |k, v|
      result[k] = v
    end
    if data.is_a?(LeafNode)
      result[:metrics] = data[:metrics]
      result[:level]   = 0
      heat             = data[:heat]
      result[:heat]    = heat if heat
    else
      result[:children] = data.keys.sort.map { |e| graph_node(e, data[e]) }
      aggregate_graph_node(result)
      result[:level] = (result[:children].map { |c| c[:level] }.max || -1) + 1
    end
    return result
  end

  def aggregate_graph_node(node)
    node[:metrics] = node[:children].inject({}) do |m, c|
      c[:metrics].each_pair { |k, v| m[k] = (m[k] || 0) + v }
      m
    end
    node[:heat]    = node[:children].inject({}) do |m, c|
      c[:heat].each_pair { |k, v| m[k] = min((m[k] || 1), v) } if c[:heat]
      m
    end
    node.delete(:heat) if node[:heat].blank?
  end

  def insert_quota_data(node, quota)
    total = node[:metrics]
    if total && quota
      quota.keys.each { |metric| quota[metric] = [total[metric] || 0, quota[metric] || 0].max }
      node[:children] << {:name => AVAILABLE_LABEL, :level => node[:level], :metrics => {:count => quota[:count] - total[:count], :cores => quota[:cores] - total[:cores], :ram => quota[:ram] - total[:ram]}, :id => random_node_id}
      node[:metrics] = quota
    end
  end

  def empty_compute_metrics
    {:count => 0, :cores => 0, :ram => 0}
  end

  def min(a, b)
    a > b ? b : a
  end

  class LeafNode < Hash
    def initialize(data)
      data.each_pair { |k, v| self[k] = v }
    end
  end
end
