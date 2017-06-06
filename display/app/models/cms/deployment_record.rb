class Cms::DeploymentRecord < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/deployments/:deploymentId/"
  self.element_name = 'record'
  self.primary_key  = :dpmtRecordId

  def self.build(attributes = {})
    attrs = ActiveSupport::HashWithIndifferentAccess.new
    attrs[:dpmtRecordState] = ''
    attrs = attrs.merge(attributes)
    super(attrs)
  end

  def to_param
    dpmtRecordId.to_s
  end
end
