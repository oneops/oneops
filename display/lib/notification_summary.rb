module NotificationSummary
  HISTOGRAM_GROUPING = [{:name => :by_severity, :label => 'By Severity', :colors => {:info => '#aaa', :warning => '#FF7F0E', :critical => '#D62728'}},
                        {:name => :by_source, :label => 'By Source', :colors => {:deployment => '#2CA02C', :ops => '#FF7F0E', :procedure => '#1F77B4'}}]

  def notifications
    @ns_path = self.class.method_defined?(:notification_ns_path) ? notification_ns_path : search_ns_path
    @ns_path = "#{@ns_path}/" if @ns_path && @ns_path[-1] != '/'
    @notifications = Search::Notification.find_by_ns(@ns_path, :size => (params[:size] || 50).to_i, :_silent => true)

    start_time = (Time.now.beginning_of_hour + 1.hour - 1.day)
    @histogram = nil
    if @notifications.present? && @notifications.first['timestamp'] / 1000 > start_time.to_i
      @histogram = {:groupings => HISTOGRAM_GROUPING,
                    :labels    => {:x => 'Time (hours)', :y => 'Count'},
                    :title     => 'Hourly Counts'}
      ranges = []
      (0..23).to_a.each do |i|
        ranges << [(start_time + i.hours).to_i * 1000, (start_time + (i + 1).hours).to_i * 1000]
      end
      hist_data = Search::Notification.histogram(@ns_path, ranges, :_silent => true)
      if hist_data.present?
        time_offset = browser_timezone_offset * 3600
        @histogram[:x] = ranges.map {|r| "#{Time.at(r.first / 1000 + time_offset).utc.strftime('%H:%M')} - #{Time.at(r.last / 1000 + time_offset).utc.strftime('%H:%M')}"}
        @histogram[:y] = hist_data.inject([]) do |a, r|
          # A little hack below in sorting severity buckets (" sort_by {|b| -b['key'].size} ") to ensure
          # proper order of 'critical', 'warning', 'info'.
          a << {:by_source   => r['by_source']['buckets'].map { |b| {:label => b['key'], :value => b['doc_count']} },
                :by_severity => r['by_severity']['buckets'].sort_by {|b| -b['key'].size}.map { |b| {:label => b['key'], :value => b['doc_count']} }}
        end
      end
    end

    respond_to do |format|
      format.html {render 'base/notifications/_notifications_summary'}
      format.js {render 'base/notifications/notifications'}
      format.json {render :json => @notifications}
    end
  end
end
