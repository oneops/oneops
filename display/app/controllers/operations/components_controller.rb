class Operations::ComponentsController < Base::ComponentsController
  def show
    respond_to do |format|
      format.html do
        @instance_counts = Cms::Relation.count(:nsPath            => bom_platform_ns_path(@environment, @platform),
                                               :relationShortName => 'DeployedTo',
                                               :fromClassName     => @component.ciClassName.sub(/^manifest./, 'bom.'),
                                               :attr              => "fromCiName:like:#{@component.ciName}-%",
                                               :groupBy           => 'toCiId')
Rails.logger.info "==== #{@instance_counts}"
        @clouds = Cms::Relation.all(:params => {:relationName    => 'base.Consumes',
                                                :ciId            => @platform.ciId,
                                                :targetClassName => 'account.Cloud',
                                                :direction       => 'from',
                                                :includeToCi     => true})
        @actions = Cms::CiMd.look_up(@component.ciClassName.sub(/^manifest./, 'bom.')).actions
        @custom_actions = Operations::InstancesController.load_custom_actions(@component)
      end

      format.json {render_json_ci_response(true, @component)}
    end
  end

  def actions
    actions = @component.meta.actions + Operations::InstancesController.load_custom_actions(@component)
    render :json => actions
  end

  def charts
    all_instance_ids = Cms::Relation.all(:params => {:ciId              => @component.ciId,
                                                     :direction         => 'from',
                                                     :relationShortName => 'RealizedAs',
                                                     :includeToCi       => false}).map(&:toCiId)
    instance_ids = params[:instance_ids]
    if instance_ids.blank?
      instance_ids = all_instance_ids
    else
      instance_ids = all_instance_ids & instance_ids.map(&:to_i)
    end

    metrics = params[:metrics]
    if metrics.blank?
      metrics = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'WatchedBy',
                                                 :includeToCi       => true}).map(&:toCi).inject([]) do |a, monitor|
        monitor_name = monitor.ciName
        a + ActiveSupport::JSON.decode(monitor.ciAttributes.metrics).keys.map {|m| "#{monitor_name}:#{m}"}
      end
    end

    start_time = params[:start_time].to_i
    end_time   = params[:end_time].to_i
    step       = params[:step].to_i
    unless start_time > 0 && step > 0 && end_time > start_time
      range        = params[:range] || 'hour'
      step         = Operations::MonitorsController::CHART_TIME_RANGE_STEP[range]
      range_length = Operations::MonitorsController::CHART_TIME_RANGE_LENGTH[range]
      current_time = Time.now.to_i
      end_time     = current_time - (current_time % step)
      start_time   = end_time - range_length
    end

    data = Daq.charts(instance_ids.map { |id| {:ci_id   => id,
                                               :start   => start_time,
                                               :end     => end_time,
                                               :step    => step,
                                               :metrics => metrics}})

    respond_to do |format|
      format.html
      format.js
      format.json {render :json => data}
    end

  end


  private

  def find_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
    component_id = params[:id]
    @component = locate_component_in_manifest_ns(component_id, @platform, params[:class_name]) if component_id.present?
  end
end
