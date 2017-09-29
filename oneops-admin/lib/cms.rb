$:.unshift(File.dirname(__FILE__)) unless $:.include?(File.dirname(__FILE__)) || $:.include?(File.expand_path(File.dirname(__FILE__)))

module Cms
  VERSION = '0.0.4'
end

require 'active_resource'

require 'cms/attr_map'
require 'cms/ci_md'
require 'cms/attr_md'
require 'cms/target_md'
require 'cms/relation_md'
require 'cms/ci'
require 'cms/relation'
require 'cms/namespace'

class ActiveResource::Connection
  # Creates new Net::HTTP instance for communication with
  # remote service and resources.
  def http
    http = Net::HTTP.new(@site.host, @site.port)
    # http.use_ssl = @site.is_a?(URI::HTTPS)
    http.verify_mode  = OpenSSL::SSL::VERIFY_NONE if http.use_ssl?
    http.read_timeout = @timeout if @timeout
    # Here's the addition that allows you to see the output
    http.set_debug_output $stderr if ENV['CMS_TRACE'] != nil && ENV['CMS_TRACE'].match(/(true|t|yes|y|1)$/i) != nil
    return http
  end
end

ActiveResource::Base.site                 = ENV['CMSAPI'] || 'http://cmsapi:8080'
ActiveResource::Base.format               = :json
ActiveResource::Base.include_root_in_json = false
