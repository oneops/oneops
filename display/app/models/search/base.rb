class Search::Base < ActiveResource::Base
  self.site         = Settings.search_site
  self.prefix       = ''
  self.element_name = ''
  self.timeout      = Rails.env.shared? ? 3 : Settings.search_http_timeout

  def self.search_raw(index, payload)
    JSON.parse(post(index, {}, payload.to_json).body)
  end

  def self.search(index, options = {})
    silent       = options.delete(:_silent)
    timeout      = options.delete(:_timeout)
    source       = options.delete(:_source)
    size         = options.delete(:size) || 9999
    from         = options.delete(:from) || 0
    sort         = options.delete(:sort) || []
    as_is_query  = options.delete(:_query)
    if as_is_query.blank?
      query_string = options.delete(:query)
      query        = build_common_query(options, options.delete(:nsPath))
      query << {:query_string => query_string.is_a?(Hash) ? query_string : {:query => query_string}} if query_string.present?
      as_is_query = {:bool => {:must => query}}
    end

    search_params = {
      :query => as_is_query,
      :sort  => sort,
      :size  => size,
      :from  => from
    }
    search_params[:_source] = source if source.present?

    return run_search("#{index}/_search", search_params, silent, timeout)
  end

  def self.search_latest_by_ns(index, ns_path, options = {})
    size  = options.delete(:size) || 9999
    query = build_common_query(options, ns_path)

    search_params = {
      :query => {:bool => {:must => query}},
      :aggs  => {
        :group_by_ns => {
          :terms => {:field => 'nsPath.keyword', :size => size},
          :aggs => {
            :latest => {
              :top_hits => {
                :sort => [{'created' => {:order => 'desc'}}],
                :size => 1
              }
            }
          }
        }
      },
      :size => 0
    }

    result = nil
    begin
      path = "#{index}/_search"
      data   = JSON.parse(Search::Base.post(path, {}, search_params.to_json).body)
      result = data['aggregations']['group_by_ns']['buckets'].map do |bucket|
        bucket['latest']['hits']['hits'][0]['_source']
      end
    rescue Exception => e
      Rails.logger.warn("Failed to perform search_latest_by_ns #{path} with params: #{search_params.to_json}\n#{e}")
      raise e
    end
    return result
  end

  def self.percolate(index, target, options = {})
    filter = build_common_query(options)

    params = {
      :query => {:bool => {:must => filter}},
      :size  => 999
    }

    result = nil
    begin
      if target.is_a?(Fixnum) || target.is_a?(String)
        path = "#{index}/#{target}/_percolate"
        data = JSON.parse(Search::Base.post(path, {}, params.to_json).body)
      else
        path = "#{index}/_percolate"
        params[:doc] = target
        data = JSON.parse(Search::Base.post(path, {}, params.to_json).body)
      end
      result = data['matches'].map {|e| e['_id']}
    rescue Exception => e
      Rails.logger.warn("Failed to perform percolate #{path} with params: #{params.to_json}\n#{e}")
      raise e
    end
    return result
  end

  def self.custom_method_collection_url(method_name, options = {})
    prefix_options, query_options = split_options(options)
    "#{prefix(prefix_options)}#{method_name}#{query_string(query_options)}"
  end

  def self.mpercolate(index, targets, options)
    filter = build_common_query(options)

    params = {
      :query => {:bool => {:must => filter}},
      :size  => 999
    }

    payload = []
    targets.each do |target|
      if target.is_a?(Fixnum) || target.is_a?(String)
        payload << {:percolate => {:id => target.to_s}}.to_json
        params.delete(:doc)
      else
        payload << {:percolate => {}}.to_json
        params[:doc] = target
      end
      payload << params.to_json
    end
    payload << "\n"

    begin
      data = JSON.parse(post("#{index}/_mpercolate", {}, payload.join("\n")).body)
      result = data['responses'].map {|r| r['matches'].map {|e| e['_id']}}
    rescue Exception => e
      Rails.logger.warn("Failed to perform mpercolate for #{targets} with params: #{params.to_json}\n#{e}")
      raise e
    end

    return result
  end


  protected

  def self.handle_exception(exception, message)
    Rails.logger.warn "#{message}: #{exception}"
  end

  def self.build_common_query(options, ns_path = nil)
    query = []
    if ns_path.present?
      ns_path_key = 'nsPath.keyword'
      if ns_path.is_a?(Array)
        query << {:terms => {ns_path_key => ns_path}}
      elsif /[\*\?]/ =~ ns_path
        query << {:wildcard => {ns_path_key => ns_path}}
      else
        query << {:term => {ns_path_key => ns_path}}
      end
    end

    options.each_pair do |field, value|
      if value.is_a?(Range)
        query << {:range => {field => {'gte' => value.first, 'lte' => value.last}}}
      elsif value.is_a?(Array)
        query << {:terms => {field => value}}
      elsif value.is_a?(String) && /[\*\?]/ =~ value
        query << {:wildcard => {field => value}}
      elsif value.present?
        query << {:term => {field => value}}
      end
    end

    return query
  end

  def self.run_search(path, search_params, silent = nil, timeout = nil)
    result = nil
    old_timeout = self.timeout
    self.timeout = timeout if timeout
    begin
      data          = JSON.parse(post(path, {}, search_params.to_json).body)
      result        = data['hits']['hits'].map { |r| r['_source'] }
      info          = result.info
      info[:total]  = data['hits']['total']
      info[:offset] = search_params[:from]
      info[:size]   = search_params[:size]
    rescue Exception => e
      handle_exception e, "Failed to perform search #{path} with params: #{search_params.to_json}"
      if silent
        result = silent.is_a?(TrueClass) ? nil : silent
      else
        raise e
      end
    ensure
      self.timeout = old_timeout if timeout
    end
    result
  end
end
