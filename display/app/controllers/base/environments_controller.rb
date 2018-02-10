class Base::EnvironmentsController < ApplicationController
  helper_method :needs_deployment?

  protected

  def load_platform_instances_info
    @platform_instance_counts = Cms::Ci.count_and_group_by_ns(environment_bom_ns_path(@environment))
  end

  def needs_deployment?
    return true if @environment.ciState == 'replace' || @bom_release # Last condition (open bom release without deployment) should not really happen under new flow but...
    if @deployment && @deployment.deploymentState == 'canceled'
      # Cancelled unfinished deployment but there might have been env commit since then to reverse changes (i.e. env ciState will not be 'replace')
      deployment_bom = Cms::Release.find(@deployment.releaseId)
      last_closed_manifest = Cms::Release.latest(:nsPath => environment_manifest_ns_path(@environment), :releaseState => 'closed')
      return deployment_bom && last_closed_manifest && deployment_bom.releaseId >= last_closed_manifest.releaseId
    end
    return false
  end
end
