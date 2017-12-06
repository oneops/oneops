class Cms::RelationMd < ActiveResource::Base
  self.prefix       = '/adapter/rest/md/'
  self.element_name = 'relation'
  self.primary_key  = :relationId

  cattr_accessor :md_cache
  self.md_cache = {}

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


  def self.look_up(relation_name)
    key = "Cms::RelationMd:relation_name=#{relation_name}"
    md = md_cache[key]
    return md if md

    md = find(relation_name)
    md_cache[key] = md
    return md
  end
end
