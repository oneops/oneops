class Transition::MonitorsController < Base::MonitorsController
  before_filter :find_monitor_and_parents

  def index
    pack_ns_path = platform_pack_ns_path(@platform)
    @monitors = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                :relationShortName => 'WatchedBy',
                                                :direction         => 'from',
                                                :includeToCi       => true}).map do |r|
      monitor = r.toCi
      monitor.add_policy_locations(pack_ns_path)
      monitor
    end

    respond_to do |format|
      format.html { render '_monitor_list' }
      format.js   { render :action => :index }
      format.json { render :json => @monitors }
    end
  end

  def show
    respond_to do |format|
      format.html { redirect_to edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, @component, :anchor => "monitors/list_item/#{@monitor.ciId}") }
      format.json { render_json_ci_response(true, @monitor) }
    end
  end

  def new
    @monitor = Cms::DjCi.build(:ciClassName => 'manifest.Monitor',
                               :nsPath      => @component.nsPath)
    @monitor.ciAttributes.custom = 'true'

    render :action => :edit

  end

  def create
    dj_ci = params[:cms_dj_ci]
    dj_ci[:ciAttributes][:custom] = true
    @monitor = Cms::DjCi.build(dj_ci.merge(:nsPath      => @component.nsPath,
                                           :ciClassName => 'manifest.Monitor'))
    @watched_by_rel = Cms::DjRelation.build(:nsPath       => @component.nsPath,
                                            :relationName => 'manifest.WatchedBy',
                                            :fromCiId     => @component.ciId,
                                            :toCi         => @monitor)

    ok = verify_custom_monitors_allowed
    if ok
      ok = execute_nested(@monitor, @watched_by_rel, :save)
      @monitor = @watched_by_rel.toCi if ok
    end

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @monitor) }
    end

  end

  def update
    dj_ci = params[:cms_dj_ci]
    unless is_custom_monitor?
      attrs = dj_ci[:ciAttributes]
      dj_ci[:ciAttributes] = %w(cmd_options sample_interval thresholds heartbeat duration).inject({}) do |m, a|
        m[a] = attrs[a]
        m
      end
    end

    ok = execute(@monitor, :update_attributes, dj_ci)

    if ok
      flash[:notice] = 'Monitor was successfully updated.'
    else
      flash[:error] = 'Failed to update monitor.'
    end

    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(ok, @monitor)}
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
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @attachment) }
    end
  end

  def watched_by
    render_json_ci_response(@watched_by_rel.present?, @watched_by_rel)
  end

  def update_watched_by
    # Special trick: we will try to update "cm" resource (and not "dj" one) in order to prevent creation of manifest
    # release and need for deployment.  However, if this relation is new (rfcAction == 'add') then there is no underlying
    # "cm" for it yet and therefore we will work with "dj" version.
    relation_attr = (params[:cms_dj_relation] || params[:cms_relation])[:relationAttributes]
    unless @watched_by_rel.rfcAction == 'add'
      @watched_by_rel = Cms::Relation.first(:params => {:ciId              => @monitor.ciId,
                                                        :relationShortName => 'WatchedBy',
                                                        :direction         => 'to',
                                                        :includeFromCi     => false,
                                                        :includeToCi       => false})
    end

    @watched_by_rel.relationAttributes.attributes = relation_attr
    @watched_by_rel.fromCi = nil   # So we do not validate "fromCi" object on save, otherwise errors in ''fromCi'  may fail the whole save.
    ok = execute(@watched_by_rel, :save)

    flash[:notice] = 'Successfully updated.' if ok

    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(ok, @watched_by_rel)}
    end
  end

  def toggle
    enabled  = @monitor.ciAttributes.enable == 'true'
    @monitor.ciAttributes.enable = enabled ? 'false' : 'true'
    ok = execute(@monitor, :save)

    respond_to do |format|
      format.js do
        flash[:error] = "Failed to #{enabled ? 'disable' : 'enable'} monitor #{@monitor.ciName}: #{@monitor.errors.full_messages.join(' ')}." unless ok
        index
      end
      format.json { render_json_ci_response(ok, @monitor) }
    end
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
      @monitor = locate_ci_in_platform_ns(monitor_id, @platform, 'manifest.Monitor')
      @watched_by_rel = Cms::DjRelation.first(:params => {:ciId              => @monitor.ciId,
                                                          :relationShortName => 'WatchedBy',
                                                          :direction         => 'to'})
      @component ||= @watched_by_rel.fromCi
    end
  end

  def verify_custom_monitors_allowed
    return true if custom_monitors_allowed?

    message = 'This environment does not support custom monitors.'
    flash[:error] = message
    @monitor.errors.add(:base, message)
    return false
  end
end
