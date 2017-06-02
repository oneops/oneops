class Design::MonitorsController < Base::MonitorsController
  before_filter :find_monitor_and_parents

  def new
    @monitor = Cms::DjCi.build({:ciClassName => 'catalog.Monitor',
                                :nsPath      => @component.nsPath,
                                :ciAttributes => {:custom => 'true'}},
                               {:owner => {}})
    # @monitor.ciAttributes.custom = 'true'

    respond_to do |format|
      format.js   { render('base/monitors/edit') }
      format.json { render_json_ci_response(true, @monitor) }
    end
  end

  def create
    dj_ci = params[:cms_dj_ci]
    dj_ci[:ciAttributes][:custom] = true
    @monitor = Cms::DjCi.build(dj_ci.merge(:nsPath      => @component.nsPath,
                                           :ciClassName => 'catalog.Monitor'))
    @watched_by_rel = Cms::DjRelation.build(:nsPath             => @component.nsPath,
                                            :relationName       => 'catalog.WatchedBy',
                                            :fromCiId           => @component.ciId,
                                            :toCi               => @monitor,
                                            :relationAttributes => {:source => 'design'})

    ok = execute_nested(@monitor, @watched_by_rel, :save)
    @monitor = @watched_by_rel.toCi if ok

    respond_to do |format|
      format.js   { ok ? index : render('base/monitors/edit') }
      format.json { render_json_ci_response(ok, @monitor) }
    end
  end

  def destroy
    ok = is_custom_monitor?
    unless ok
      message = 'Only custom monitors may be deleted.'
      flash[:error] = message
      @monitor.errors.add(:base, message)
    end

    ok = execute(@monitor, :destroy) if ok

    respond_to do |format|
      format.js { ok ? index : render('base/monitors/edit') }
      format.json { render_json_ci_response(ok, @attachment) }
    end
  end


  private

  def find_monitor_and_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @platform    = locate_design_platform(params[:platform_id], @assembly)
    component_id = params[:component_id]
    @component   = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?

    monitor_id = params[:id]
    if monitor_id.present?
      @monitor = locate_ci_in_platform_ns(monitor_id, @platform, 'catalog.Monitor', :attrProps => 'owner')
      @watched_by_rel = Cms::DjRelation.first(:params => {:ciId              => @monitor.ciId,
                                                          :relationShortName => 'WatchedBy',
                                                          :direction         => 'to',
                                                          :attrProps         => 'owner'})
      @component ||= @watched_by_rel.fromCi
    end
  end
end
