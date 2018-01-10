module CostSummary
  UNIT = 'USD/hour'
  def cost_rate
    @cost_rate = Search::Cost.cost_rate(search_ns_path)
    respond_to do |format|
      format.json do
        if @cost_rate
          render :json => @cost_rate
        else
          render :json => {:errors => ['Failed to fetch cost rate data']}, :status => :internal_server_error
        end
      end
    end
  end

  def cost
    end_date = Date.today
    end_date -= 1.day if end_date.day == 1
    @ns_path = search_ns_path

    @cost = Search::Cost.cost(@ns_path, end_date.beginning_of_month, end_date)

    @cost_rate = Search::Cost.cost_rate(@ns_path)

    cost_hist = Search::Cost.cost_time_histogram(@ns_path, end_date.prev_month(2).beginning_of_month, end_date, :month)
    if cost_hist
      buckets = cost_hist[:buckets]
      last_bucket_index = buckets.size - 1
      x = []
      y = []
      buckets.each_with_index do |time_bucket, i|
        bucket_total = time_bucket['total']['value'].round(2)
        if x.size > 0 || bucket_total > 0 || i == last_bucket_index  # skip months in the beginning if they are 'costless' but always add last month.
          x << Date.parse(time_bucket['from_as_string']).strftime('%b %Y')
          y << {:cost => [{:label => 'realized', :value => bucket_total}]}
        end
      end

      cost_rate = @cost_rate && @cost_rate[:total]
      if cost_rate && y.size > 0
        y.last[:cost] << {:label => 'projected',
                          :value => (cost_rate * (end_date.end_of_month.to_time.to_i - end_date.to_time.to_i) / 3600.0).round(2)}
      end

      @cost_projection = {:title     => 'Cost Projection',
                          :units     => {:x => 'month', :y => cost_hist[:unit]},
                          :labels    => {:x => nil, :y => nil},
                          :groupings => [{:name => :cost, :label => 'Month Projection', :colors => {'realized' => '#398ADB', 'projected' => '#B5D3F1'}}],
                          :total     => cost_hist[:total],
                          :x         => x,
                          :y         => y}
    end

    respond_to do |format|
      format.html {render 'base/cost/_cost'}
      format.js {render 'base/cost/cost'}
      format.json do
        if @cost
          if @cost_projection
            projection = {:unit => cost_hist[:unit]}
            y_values = @cost_projection[:y]
            @cost_projection[:x].each_with_index do |x, i|
              projection[x] = y_values[i][:cost].to_map_with_value {|v| [v[:label], v[:value]]}
            end
            @cost_projection = projection
          end
          render :json => {:cost => @cost, :cost_rate => @cost_rate, :projection => @cost_projection}
        else
          render :json => {:errors => ['Failed to fetch cost rate data']}, :status => :internal_server_error
        end
      end
    end
  end
end
