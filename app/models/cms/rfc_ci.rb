class Cms::RfcCi < Cms::Ci
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/rfc/"
  self.element_name = 'ci'
  self.primary_key = :rfcId

  def self.build(attributes = {})
    rfc_params = ActiveSupport::HashWithIndifferentAccess.new
    rfc_params[:releaseId] = 0
    rfc_params[:rfcAction] = ''
    rfc_params[:execOrder] = 0
    attrs = rfc_params.merge(attributes)
    super(attrs)
  end

  def to_param
    rfcId.to_s
  end

  def rfc_created_timestamp
    Time.at(self.rfcCreated / 1000)
  end

  def rfc_updated_timestamp
    Time.at(self.rfcUpdated / 1000)
  end

  def find_or_create_resource_for(name)
    case name
    when :ciBaseAttributes
      self.class.const_get(:Cms).const_get(:AttrMap)
    else
      super
    end
  end

  def attrOwner
    attributes[:ciAttrProps] && ciAttrProps.attributes['owner']
  end
end
