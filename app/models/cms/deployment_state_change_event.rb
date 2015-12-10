class Cms::DeploymentStateChangeEvent < Cms::Base
  self.prefix          = "#{Settings.cms_path_prefix}/dj/simple/deployments/:deploymentId/"
  self.element_name    = 'history'
  self.collection_name = 'history'
  self.primary_key     = :eventId

  def to_param
    eventId.to_s
  end
end
