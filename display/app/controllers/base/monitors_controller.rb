class Base::MonitorsController < ApplicationController
  include ::RfcHistory

  helper_method :is_custom_monitor?

  def index
    find_monitors

    respond_to do |format|
      format.html { render '_monitor_list' }
      format.js { render 'base/monitors/index' }
      format.json { render :json => @monitors }
    end
  end

  def show
    respond_to do |format|
      format.html do
        if @monitor.ciClassName == 'catalog.Monitor'
          redirect_to edit_assembly_design_platform_component_path(@assembly, @platform, @component, :anchor => "monitors/list_item/#{@monitor.ciId}")
        else
          redirect_to edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, @component, :anchor => "monitors/list_item/#{@monitor.ciId}")
        end
      end

      format.js   { render 'base/monitors/edit' }
      format.json { render_json_ci_response(true, @monitor) }
    end
  end

  def update
    dj_ci         = params[:cms_dj_ci]
    ci_attrs      = dj_ci[:ciAttributes]
    ci_attr_props = dj_ci[:ciAttrProps]
    unless is_custom_monitor?(@monitor)
      mutable_attrs = %w(cmd_options sample_interval thresholds heartbeat duration)
      ci_attrs.slice!(*mutable_attrs)
    end
    ci_attr_props[:owner].slice!(*ci_attrs.keys) if ci_attr_props && ci_attr_props[:owner]

    ok = execute(@monitor, :update_attributes, dj_ci)
    flash[:error] = 'Failed to update monitor.' unless ok

    respond_to do |format|
      format.js { render 'base/monitors/edit' }
      format.json { render_json_ci_response(ok, @monitor) }
    end
  end

  def toggle
    enabled = @monitor.ciAttributes.enable == 'true'
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

  def watched_by
    render_json_ci_response(@watched_by_rel.present?, @watched_by_rel)
  end

  def update_watched_by
    dj_relation    = params[:cms_dj_relation] || params[:cms_relation]
    rel_attrs      = dj_relation[:relationAttributes]
    rel_attr_props = dj_relation[:relationAttrProps]
    mutable_attrs = %w(docUrl notifyOnlyOnStateChange)
    rel_attrs.slice!(*mutable_attrs)
    rel_attr_props[:owner].slice!(*rel_attrs.keys) if rel_attr_props && rel_attr_props[:owner]

    @watched_by_rel.fromCi = nil # So we do not validate "fromCi" object on save, otherwise errors in ''fromCi'  may fail the whole save.
    ok = execute(@watched_by_rel, :update_attributes, dj_relation)
    flash[:error] = 'Failed to update.' unless ok

    respond_to do |format|
      format.js { render 'base/monitors/edit' }
      format.json { render_json_ci_response(ok, @watched_by_rel) }
    end
  end

  def is_custom_monitor?(monitor = @monitor)
    monitor.ciAttributes.custom == 'true'
  end

  protected

  def ci_resource
    @monitor
  end

  def find_monitors
    pack_ns_path = platform_pack_ns_path(@platform)
    @monitors = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                :relationShortName => 'WatchedBy',
                                                :direction         => 'from',
                                                :includeToCi       => true}).map do |r|
      monitor = r.toCi
      monitor.add_policy_locations(pack_ns_path)
      monitor
    end
  end
end
