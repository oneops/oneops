class Cms::DeploymentRelation < Cms::Ci
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/deployments/:deploymentId/"
  self.element_name = 'relation'
  self.primary_key  = :rfcId

  def self.build(attributes = {})
    rfc_params = ActiveSupport::HashWithIndifferentAccess.new
    rfc_params[:dpmtRecordState] = ''
    attrs = rfc_params.merge(attributes)
    super(attrs)
  end

  def to_param
    rfcId.to_s
  end
end
