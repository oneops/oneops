class Cms::Ci < ActiveResource::Base
  self.prefix = "/adapter/rest/cm/simple/"
  self.format = :json
  self.include_root_in_json = false
  self.primary_key = :ciId
  
  def self.build(attributes = {})
    attrs = self.from_ci_md(attributes[:ciClassName]).merge(attributes)
    self.new(attrs)
  end
  
  def relations(options = {})
  	Cms::Relation.all( :params => { :ciId => self.id }.merge(options) )
  end
  
  def find_or_create_resource_for(name)
   	case name
   	when :ciAttributes
   		self.class.const_get(:Cms).const_get(:AttrMap)
    when :created
      self.class.const_get(:Time)
   	else
   		super
   	end
  end
  
  def created_timestamp
    Time.at(self.created / 1000)
  end
  
  def updated_timestamp
    Time.at(self.created / 1000)
  end
  
  def meta
  	self.class.get_ci_md(self.ciClassName)
  end

  def to_param
    ciId.to_s
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

  private

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end

  def self.get_ci_md(ciClassName)
    Cms::CiMd.find(ciClassName)
  end

  def self.from_ci_md(ciClassName)
    ciParams = ActiveSupport::HashWithIndifferentAccess.new
    ciParams[:ciName] = ""
    ciParams[:ciClassName] = ciClassName
    ciParams[:nsPath] = "/"
    ciParams[:comments] = ""
    ciAttrs = ActiveSupport::HashWithIndifferentAccess.new
    if ciClassName
    	
	    self.get_ci_md(ciClassName).attributes[:mdAttributes].each do |a|
	      ciAttrs[a.attributeName] = a.defaultValue
	    end
	end
	ciParams[:ciAttributes] = ciAttrs
    return ciParams
  end

end
