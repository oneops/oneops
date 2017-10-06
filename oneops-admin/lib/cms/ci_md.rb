class Cms::CiMd < ActiveResource::Base
  self.prefix = '/adapter/rest/md/'
  self.format = :json
  self.include_root_in_json = false
  self.include_format_in_path = false
  self.element_name = 'class'
  self.primary_key = :classId

  def find_or_create_resource_for_collection(name)
    case name
    when :mdAttributes
      self.class.const_get(:Cms).const_get(:AttrMd)
    else
      super
    end
  end

  def to_param
    className.to_s
  end

  def self.bulk(classes)
    return post('bulk', {}, classes.to_json).body
  rescue Exception => e
    return false, e
  end
end
