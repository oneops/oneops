class Cms::Relation < Cms::Base
  self.prefix      = "#{Settings.cms_path_prefix}/cm/simple/"
  self.primary_key = :ciRelationId

  validate do |r|
    [:fromCi, :toCi].each do |key|
      if r.respond_to?(key)
        ci = r.send(key)
        if ci.present? && !ci.valid?
          ci.errors.full_messages.each {|e| r.errors.add(:base, e)}
          # ci.errors.clear
        end
      end
    end
  end

  def self.build(attributes = {})
    attrs = self.from_relation_md(attributes[:relationName]).deep_merge(attributes)
    self.new(attrs)
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
    Cms::RelationMd.look_up(self.relationName)
  end

  def to_param
    ciRelationId.to_s
  end

  def new_record?
    !persisted?
  end

  def persisted?
    id.to_i > 0
  end

  def self.count(options)
    self.get(:count, options)
  end

  def attrOwner
    attributes[:relationAttrProps] && relationAttrProps.attributes['owner']
  end


  private

  def self.from_relation_md(relationName)
    rel_params = ActiveSupport::HashWithIndifferentAccess.new
    rel_params[:relationName] = relationName
    rel_params[:nsPath] = ''
    rel_params[:comments] = ''
    rel_params[:fromCiId] = ''
    rel_params[:toCiId] = 0
    rel_attrs = ActiveSupport::HashWithIndifferentAccess.new
    if relationName
	    Cms::RelationMd.look_up(relationName).attributes[:mdAttributes].each do |a|
	      rel_attrs[a.attributeName] = a.defaultValue || ''
	    end
  	end
    rel_params[:relationAttributes] = rel_attrs
    return rel_params
  end
end
