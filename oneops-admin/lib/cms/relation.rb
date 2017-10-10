class Cms::Relation < ActiveResource::Base
  self.prefix       = '/adapter/rest/cm/simple/'
  self.element_name = 'relation'
  self.primary_key  = :ciRelationId

  def self.build(attributes = {})
    attrs = self.from_relation_md(attributes[:relationName]).merge(attributes)
    self.new(attrs)
  end

  def self.bulk(relations)
    return [], nil if relations.blank?
    begin
      return JSON.parse(post('bulk', {}, relations.to_json).body).map {|r| new(r, true)}, nil
    rescue Exception => e
      return nil, e
    end
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

  def merge_attributes(attrs)
    existng_attrs = relationAttributes.attributes
    existng_attrs.keys.each {|name| existng_attrs[name] = attrs[name] if attrs[name]}
  end


  private

  def self.get_relation_md(relation_name)
    Cms::RelationMd.look_up(relation_name)
  end

  def self.from_relation_md(relation_name)
    attrs = ActiveSupport::HashWithIndifferentAccess.new
    props = ActiveSupport::HashWithIndifferentAccess.new(:relationName       => relation_name,
                                                         :nsPath             => '',
                                                         :comments           => '',
                                                         :fromCiId           => 0,
                                                         :toCiId             => 0,
                                                         :relationAttributes => attrs)
    if relation_name
      self.get_relation_md(relation_name).attributes[:mdAttributes].each do |a|
        attrs[a.attributeName] = a.defaultValue
      end
    end
    return props
  end
end
