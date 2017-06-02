class Search::Notification < Search::Base
  self.prefix       = '/cms-2*/notification/_search'
  self.element_name = ''

  def self.find_by_ci_id(ci_id, options)
    silent = options.delete(:_silent)
    size   = options.delete(:size) || 100
    from   = options.delete(:from) || 0
    filter = [{:term => {:cmsId => ci_id}},
              {:range => {'timestamp' => {'gte' => options[:since].to_i * 1000}}}]
    source = options[:source]
    filter << {:term => {:source => source}} if source.present?
    search_params = {
      :query => {
        :bool => {
          :must => filter
        }
      },
      :sort  => [{'timestamp' => {:order => 'desc'}}],
      :size  => size,
      :from  => from
    }
    return run_search('', search_params, silent)
  end

  def self.find_by_ns(ns_path, options = {})
    silent = options.delete(:_silent)
    size   = options.delete(:size) || 100
    from   = options.delete(:from) || 0
    sort   = options.delete(:sort).presence || {'timestamp' => {:order => 'desc'}}
    sort   = [sort] unless sort.is_a?(Array)

    query = [{:wildcard => {'nsPath.keyword' => "#{ns_path}*"}}]

    start_time = options.delete(:start)
    query << {:range => {'timestamp' => {:gte => start_time}}} if start_time.present?

    end_time = options.delete(:end)
    query << {:range => {'timestamp' => {:lte => end_time}}} if end_time.present?

    query_string = options.delete(:query)
    query << {:query_string => {:query => query_string}} if query_string.present?

    options.each_pair do |field, value|
      query << {:term => {field => value}} if value.present?
    end

    search_params = {
      :query => {
        :bool => {
          :must => query
        }
      },
      :sort  => sort,
      :size  => size,
      :from  => from
    }
    return run_search('', search_params, silent)
  end

  def self.histogram(ns_path, ranges, options = {})
    silent = options.delete(:_silent)

    query = [{:wildcard => {'nsPath.keyword' => "#{ns_path}*"}},
             {:range => {'timestamp' => {:gte => ranges.first.first,
                                         :lte => ranges.last.last}}}]

    query_string = options.delete(:query)
    query << {:query_string => {:query => query_string}} if query_string.present?

    options.each_pair do |field, value|
      query << {:term => {field => value}} if value.present?
    end

    search_params = {
      :query => {
        :bool => {
          :must => query
        }
      },
      :aggs => {
        :time_histogram => {
          :range => {:field => 'timestamp', :ranges => ranges.map{|r| {:from => r.first, :to => r.last}}},
          :aggs => {
            :by_source => {
              :terms => {:field => 'source', :size => 99, :order => {'_term' => 'desc'}}
            },
            :by_severity => {
              :terms => {:field => 'severity', :size => 99, :order => {'_term' => 'asc'}}
            }
          }
        }
      },
      :sort => [{'timestamp' => {:order => 'desc'}}],
      :size  => 0
    }

    result = nil
    begin
      data = JSON.parse(post('', {}, search_params.to_json).body)
      result = data['aggregations']['time_histogram']['buckets']
    rescue Exception => e
      handle_exception e, "Failed to perform notification histogram search with params: #{search_params.to_json}"
      if silent
        result = silent.is_a?(TrueClass) ? nil : silent
      else
        raise e
      end
    end
    return result
  end
end
