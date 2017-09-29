class Cms::RelationMd < ActiveResource::Base
  self.prefix = '/adapter/rest/md/'
  self.format = :json
  self.include_root_in_json = false
  self.include_format_in_path = false
  self.element_name = 'relation'
  self.primary_key = :relationId

  def find_or_create_resource_for_collection(name)
    case name
    when :mdAttributes
      self.class.const_get(:Cms).const_get(:AttrMd)
    when :targets
      self.class.const_get(:Cms).const_get(:TargetMd)
    else
      super
    end
  end

  def to_param
    relationName.to_s
  end

  def self.bulk(relations)
    return post('bulk', {}, relations.to_json).body
  rescue Exception => e
    return false, e
  end
end
