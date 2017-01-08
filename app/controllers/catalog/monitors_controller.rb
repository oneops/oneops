class Catalog::MonitorsController < Base::MonitorsController
  before_filter :find_parents
  before_filter :find_monitor, :only => [:show]

  private

  def find_parents
    design_id    = params[:design_id]
    component_id = params[:component_id]
    if design_id
      # Catalog design scope.
      @design    = locate_catalog_design(design_id)
      @platform  = locate_design_platform(params[:platform_id], @design)
      @component = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?
    else
      # Packs scope.
      @platform = locate_pack_platform(params[:platform_id], params[:source], params[:pack], params[:version], params[:availability])
      @component = Cms::Ci.locate(component_id, @platform.nsPath, params[:class_name]) if component_id.present?
    end
  end

  def find_monitor
    @monitor = Cms::Ci.locate(params[:id], @design ? catalog_design_platform_ns_path(@design, @platform) : @platform.nsPath, 'Monitor')
    watched_by_params = {:ciId              => @monitor.ciId,
                         :relationShortName => 'WatchedBy',
                         :direction         => 'to'}
    watched_by_params[:attrProps] = 'owner' if @design
    @watched_by_rel = Cms::Relation.first(:params => watched_by_params)
    @component ||= @watched_by_rel.fromCi
  end
end
