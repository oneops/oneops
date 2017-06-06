class Cms::Base < ActiveResource::Base
  self.site = Settings.cms_site

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

  # modify standard paths to not include format extension
  # the format is already defined in the header
  def self.new_element_path(prefix_options = {})
    drop_extension(super)
  end

  def self.element_path(id, prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  def self.collection_path(prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  def self.custom_method_collection_url(method_name, options = {})
    drop_extension(super)
  end

  def custom_method_element_url(method_name, options = {})
    self.class.drop_extension(super)
  end

  def to_pretty
    JSON.pretty_unparse(JSON.parse(to_json))
  end


  protected

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end

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
