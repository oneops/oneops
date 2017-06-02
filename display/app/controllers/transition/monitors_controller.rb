class Transition::MonitorsController < Base::MonitorsController
  before_filter :find_monitor_and_parents

  def update_watched_by
    # Special trick: we will try to update "cm" resource (and not "dj" one) in order to prevent creation of manifest
    # release and need for deployment.  However, if this relation is new (rfcAction == 'add') then there is no underlying
    # "cm" for it yet and therefore we will work with "dj" version.
    unless @watched_by_rel.rfcAction == 'add'
      @watched_by_rel = Cms::Relation.first(:params => {:ciId              => @monitor.ciId,
                                                        :relationShortName => 'WatchedBy',
                                                        :direction         => 'to',
                                                        :includeFromCi     => false,
                                                        :includeToCi       => false})
    end

    super
  end


  private

  def find_monitor_and_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
    component_id = params[:component_id]
    @component   = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?

    monitor_id = params[:id]
    if monitor_id.present?
      @monitor = locate_ci_in_platform_ns(monitor_id, @platform, 'manifest.Monitor', :attrProps => 'owner')
      @watched_by_rel = Cms::DjRelation.first(:params => {:ciId              => @monitor.ciId,
                                                          :relationShortName => 'WatchedBy',
                                                          :direction         => 'to',
                                                          :attrProps         => 'owner'})
      @component ||= @watched_by_rel.fromCi
    end
  end
end
