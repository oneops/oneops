module CostSummary
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
    @ns_path = search_ns_path

    @cost = Search::Cost.cost(@ns_path, end_date.beginning_of_month, end_date)

    @cost_rate = Search::Cost.cost_rate(@ns_path)

    cost_hist = Search::Cost.cost_time_histogram(@ns_path, end_date.prev_month(2).beginning_of_month, end_date, :month)
    if cost_hist
      x, y = cost_hist[:buckets].inject([[], []]) do |xy, time_bucket|
        xy.first << Date.parse(time_bucket['from_as_string']).strftime('%b %Y')
        xy.last << {:cost => [{:label => 'realized', :value => time_bucket['total']['value'].round(2)}]}
        xy
      end

      cost_rate = @cost_rate && @cost_rate[:total]
      if cost_rate
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
