class Cms::RfcRelation < Cms::Relation
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/rfc/"
  self.element_name = 'relation'
  self.primary_key  = :rfcId

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

  def find_or_create_resource_for(name)
    case name
    when :relationBaseAttributes
      self.class.const_get(:Cms).const_get(:AttrMap)
    else
      super
    end
  end

  def rfc_created_timestamp
    self.rfcCreated && Time.at(self.rfcCreated / 1000)
  end

  def rfc_updated_timestamp
    self.rfcUpdated && Time.at(self.rfcUpdated / 1000)
  end

  def ci_hash
    unless @ci_hash
      begin
        @ci_hash = comments.present? ? JSON.parse(comments) : {}
      rescue Exception => e
        @ci_hash = {}
      end
    end
    return @ci_hash
  end
end
