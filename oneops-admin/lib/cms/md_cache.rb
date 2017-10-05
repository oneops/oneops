class Cms::MdCache < ActiveResource::Base
  self.prefix = '/adapter/rest/md'
  self.element_name = ''
  self.include_root_in_json = false
  self.include_format_in_path = false

  def self.reset
    return get('cache/trigger_reset'), nil
  rescue Exception => e
    return false, e
  end
end
