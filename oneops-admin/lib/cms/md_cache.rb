class Cms::MdCache < ActiveResource::Base
  self.prefix = '/adapter/rest/md'
  self.element_name = ''

  def self.reset
    return get('cache/trigger_reset'), nil
  rescue Exception => e
    return false, e
  end
end
