class Cms::Base < ActiveResource::Base
  self.site                   = Settings.cms_site
  self.include_format_in_path = false
  self.timeout                = Settings.cms_http_timeout

  def self.all(*args)
    result = super(*args)
    result ? result.to_a : result
  end

  def created
    time = attributes[:created]
    time.is_a?(String) ? (Time.parse(ensure_utc(time)).to_f * 1000).to_i : time
  end

  def updated
    time = attributes[:updated]
    time.is_a?(String) ? (Time.parse(ensure_utc(time)).to_f * 1000).to_i : time
  end

  def created_timestamp
    Time.at(self.created / 1000)
  end

  def updated_timestamp
    Time.at(self.updated / 1000)
  end

  def to_pretty
    JSON.pretty_unparse(JSON.parse(to_json))
  end


  protected

  def self.handle_exception(exception, message)
    body = nil
    error_message = exception.message
    if exception.respond_to?(:response) && exception.response.body
      begin
        body = JSON.parse(exception.response.body)
        error_message = body['message']
        Rails.logger.warn "#{message}: #{"[#{body['code']} - #{error_message}]"}"
      rescue Exception => e
        Rails.logger.warn "#{message}: #{exception.message}"
      end
    else
      Rails.logger.warn "#{message}: #{exception.message}"
    end
    return error_message
  end

  private

  def ensure_utc(time)
    "#{time}#{'Z' unless time.end_with?('Z')}"
  end
end

#  This override is needed to skip active resource stripping the first level hash if it has only one value.
# This was causing problems for some custom GET REST calls (i.e Relation.count) which return a json with only one entry.
module ActiveResource
  module Formats
    def self.remove_root(data)
      data
    end
  end
end
