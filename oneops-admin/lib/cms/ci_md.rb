class Cms::CiMd < ActiveResource::Base
  self.prefix       = '/adapter/rest/md/'
  self.element_name = 'class'
  self.primary_key  = :classId

  cattr_accessor :md_cache
  self.md_cache = {}

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

  def self.look_up(ci_class_name)
    key = "Cms::CiMd:ci_class_name=#{ci_class_name}"
    md = md_cache[key]
    return md if md

    md = find(ci_class_name)
    md_cache[key] = md
    return md
  end
end
