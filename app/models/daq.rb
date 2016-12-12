class Daq < ActiveResource::Base
  self.site         = Settings.metrics_site
  self.prefix       = '/daq-1.0.0'
  self.element_name = ''
  self.timeout      = 60

  def self.charts(req_set)
    result = nil
    if req_set.present?
      begin
        content_type = headers['Content-Type']
        headers['Content-Type'] = 'application/x-www-form-urlencoded'
        result = JSON.parse(post('getPerfData', {}, "reqSet=#{req_set.to_json}").body)
      rescue Exception => e
        handle_exception e, "Failed to get charts data for: #{req_set.inspect}"
      ensure
        headers['Content-Type'] = content_type
      end
    end
    return result
  end

  def self.logs(req_set)
    result = []
    if req_set.present?
      if Settings.log_data_source == 'es'
        result = Search::Log.find_by_request_ids(req_set.map {|e| e[:id]})
      elsif Settings.log_data_source == 'fake'
        result = req_set.map do |e|
          {'id' => e[:id],
           'logData' => [{'message' => "#{Time.now} - This is pretend log for id=#{e[:id]}"}] * (Time.now.sec + 1)}
        end
      else
        begin
          content_type = headers['Content-Type']
          headers['Content-Type'] = 'application/x-www-form-urlencoded'
          result = JSON.parse(post('getActionOrWorkorderLogData', {}, "reqSet=#{req_set.to_json}").body)
        rescue Exception => e
          handle_exception e, "Failed to get logs for: #{req_set.inspect}"
        ensure
          headers['Content-Type'] = content_type
        end
      end
    end
    return result
  end

  def self.instance_logs(req_set)
    result = {}
    if req_set.present?
      if Settings.log_data_source == 'es'
        result = Search::Log.find_by_ci_ids(req_set.map {|e| e[:ci_id]})
      else
        begin
          content_type = headers['Content-Type']
          headers['Content-Type'] = 'application/x-www-form-urlencoded'
          result = JSON.parse(post('getLogData', {}, "reqSet=#{req_set.to_json}").body)
        rescue Exception => e
          handle_exception e, "Failed to get instance logs for: #{req_set.inspect}"
        ensure
          headers['Content-Type'] = content_type
        end
      end
    end
    return result
  end

  def self.custom_method_collection_url(method_name, options = {})
    super.gsub(/.#{self.format.extension}/, '')
  end


  private

  def self.handle_exception(exception, message)
    Rails.logger.warn "#{message}: #{exception}"
  end
end
