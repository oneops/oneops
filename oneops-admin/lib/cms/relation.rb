class Cms::Relation < ActiveResource::Base
  self.prefix = "/adapter/rest/cm/simple/"
  self.format = :json
  self.include_root_in_json = false
  self.primary_key = :ciRelationId

  def self.build(attributes = {})
    attrs = self.from_relation_md(attributes[:relationName]).merge(attributes)
    self.new(attrs)
  end

  def created_timestamp
    Time.at(self.created / 1000)
  end

  def updated_timestamp
    Time.at(self.created / 1000)
  end

  def from
  	Cms::Ci.find(fromCiId)
  end

  def to
  	Cms::Ci.find(toCiId)
  end

  def find_or_create_resource_for(name)
  	case name
  	when :relationAttributes
  		self.class.const_get(:Cms).const_get(:AttrMap)
  	when :fromCi
  		self.class.const_get(:Cms).const_get(:Ci)
  	when :toCi
  		self.class.const_get(:Cms).const_get(:Ci)
  	else
  		super
  	end
  end

  def meta
    self.class.get_relation_md(self.relationName)
  end

  def to_param
    ciRelationId.to_s
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

  def panel_html_id(name = relationName)
    "#{name}_info"
  end

  def self.panel_html_id(name)
    "#{name}_info"
  end


  private

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end

  def self.get_relation_md(relationName)
    Cms::RelationMd.find(relationName)
  end

  def self.from_relation_md(relationName)
    relationParams = ActiveSupport::HashWithIndifferentAccess.new
    relationParams[:relationName] = relationName
    relationParams[:nsPath] = ""
    relationParams[:comments] = ""
    relationParams[:fromCiId] = ""
    relationParams[:toCiId] = ""
    relationAttrs = ActiveSupport::HashWithIndifferentAccess.new
    if relationName
	    self.get_relation_md(relationName).attributes[:mdAttributes].each do |a|
	      relationAttrs[a.attributeName] = a.defaultValue
	    end
  	end
    relationParams[:relationAttributes] = relationAttrs
    return relationParams
  end
end
