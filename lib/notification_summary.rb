module NotificationSummary
  def notifications
    @ns_path = "#{search_ns_path}/"
    @notifications = Search::Notification.find_by_ns(@ns_path, :size => 50, :_silent => true)

    start_time = (Time.now.beginning_of_hour + 1.hour - 1.day)
    @histogram = nil
    if @notifications.present? && @notifications.first['timestamp'] / 1000 > start_time.to_i
      @histogram = {:groupings => [{:name => :by_source, :label => 'By Source', :colors => {:deployment => 'green', :ops => 'orange', :procedure => 'blue'}},
                                   {:name => :by_severity, :label => 'By Severity', :colors => {:info => 'blue', :warning => 'orange', :critical => 'red'}}],
                    :labels    => {:x => 'Time (hours)', :y => 'Count'},
                    :title     => 'Hourly Counts'}
      ranges = []
      (0..23).to_a.each do |i|
        ranges << [(start_time + i.hours).to_i * 1000, (start_time + (i + 1).hours).to_i * 1000]
      end
      hist_data = Search::Notification.histogram(@ns_path, ranges, :_silent => true)
      if hist_data.present?
        @histogram[:x] = ranges.map {|r| "#{Time.at(r.first / 1000).strftime('%H:%M')} - #{Time.at(r.last / 1000).strftime('%H:%M')}"}
        @histogram[:y] = hist_data.inject([]) do |a, r|
          a << {:by_source   => r['by_source']['buckets'].map { |b| {:label => b['key'], :value => b['doc_count']} },
                :by_severity => r['by_severity']['buckets'].map { |b| {:label => b['key'], :value => b['doc_count']} }}
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
