class Cms::RfcRelation < Cms::Relation
  self.prefix = "/adapter/rest/dj/simple/rfc/"
  self.format = :json
  self.element_name = "relation"
  self.primary_key = :rfcId

  def self.build(attributes = {})
    rfcParams = ActiveSupport::HashWithIndifferentAccess.new
    rfcParams[:releaseId] = 0
    rfcParams[:rfcAction] = ""
    rfcParams[:execOrder] = 0
    attrs = rfcParams.merge(attributes)
    super(attrs)
  end

  def to_param
    rfcId.to_s
  end
  
  def find_or_create_resource_for(name)
    case name
    when :relationBaseAttributes
      self.class.const_get(:Cms).const_get(:AttrMap)
    else
      super
    end
  end

end
