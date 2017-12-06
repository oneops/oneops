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
require 'cms/md_cache'

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

ActiveResource::Base.site                   = ENV['CMSAPI'] || 'http://cmsapi:8080'
ActiveResource::Base.format                 = :json
ActiveResource::Base.include_root_in_json   = false
ActiveResource::Base.include_format_in_path = false

class String
  def red(background = false)
    colorize("\e[31m", background)
  end

  def green(background = false)
    colorize("\e[32m", background)
  end

  def yellow(background = false)
    colorize("\e[33m", background)
  end

  def blue(background = false)
    colorize("\e[34m", background)
  end

  def magenta(background = false)
    colorize("\e[35m", background)
  end

  def cyan(background = false)
    colorize("\e[36m", background)
  end

  def colorize(color_code, background = false)
    "\e[0m#{"\e[7m" if background}#{color_code}#{self}\e[0m"
  end
end

class Hash
  def diff(hash, only = nil)
    result = (only.presence ||keys).inject(nil) do |r, key|
      if hash.include?(key)
        val = self[key]
        other_val = hash[key]
        unless val == other_val
          r = add_to_diff(r, :diff, {key => [val, other_val]})
        end
      else
        r = add_to_diff(r, :extra, key)
      end
      r
    end
    result = (only.presence || hash.keys).inject(result) do |r, key|
      r = add_to_diff(r, :missing, key) unless include?(key)
      r
    end
    return result
  end


  private

  def add_to_diff(diff, type, key)
    diff ||= {}
    (diff[type] ||= []) << key
    diff
  end
end
