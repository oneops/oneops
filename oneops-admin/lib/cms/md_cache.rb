require "net/http"
require "uri"

class Cms::MdCache

  def self.cache_refresh
    time = Time.now.to_i
    cmsapi = ENV['CMSAPI'] || 'http://cmsapi:8080'
    uri = URI.parse("#{cmsapi}/adapter/rest/cm/simple/vars?name=MD_UPDATE_TIMESTAMP&value=#{time}");
    begin
      response = Net::HTTP.get_response(uri)
    rescue ActiveResource::BadRequest, ActiveResource::ResourceNotFound, ActiveResource::ResourceInvalid
      Log.debug("bad request /resource not found!")
    rescue Exception => e
      STDERR.puts(e.inspect)
    end

  end

end
