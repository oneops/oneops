class Search::Log < Search::Base
  self.site = Settings.log_site

  def self.find_by_request_ids(request_ids)
    result = {}
    begin
      result = search('/logstash-*',
                      {:requestId => request_ids,
                       :_source   => %w(requestId message level),
                       :sort      => %w(@timestamp),
                       :size      => 1000000}).inject({}) do |m, hit|
        request_id    = hit['requestId']
        m[request_id] ||= {'id' => request_id, 'logData' => []}
        m[request_id]['logData'] << hit
        m
      end
      result = result.values
    rescue Exception => e
      handle_exception e, "Failed to get request logs from ES for requests: #{request_ids}"
    end
    return result
  end

  def self.find_by_ci_ids(ci_ids)
    result = {}
    begin
      result = search('/logstash-*',
                      {:ciId    => ci_ids,
                       :_source => %w(ciId @timestamp message level type),
                       :sort    => %w(@timestamp),
                       :size    => 1000000}).inject({}) do |m, hit|
        ci_id    = hit['ciId']
        m[ci_id] ||= {'id' => ci_id, 'logData' => []}
        m[ci_id]['logData'] << hit
        m
      end
      result = result.values
    rescue Exception => e
      handle_exception e, "Failed to get instance logs from ES for instances: #{ci_ids.inspect}"
    end
    return result
  end
end
