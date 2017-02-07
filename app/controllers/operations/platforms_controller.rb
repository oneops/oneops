class Operations::PlatformsController < Base::PlatformsController
  include ::Health
  before_filter :find_assembly_environment_platform

  def index
    @platforms = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'ComposedOf',
                                                 :targetClassName   => 'manifest.Platform'}).map(&:toCi)
    render :json => @platforms
  end

  def show
    load_platform_detail

    @clouds = Cms::Relation.all(:params => {:relationName    => 'base.Consumes',
                                            :targetClassName => 'account.Cloud',
                                            :direction       => 'from',
                                            :ciId            => @platform.ciId}).sort_by {|o| o.toCi.ciName}

    respond_to do |format|
      format.html do
        @requires = Cms::DjRelation.all(:params => {:ciId         => @platform.ciId,
                                                    :direction    => 'from',
                                                    :includeToCi  => true,
                                                    :relationName => 'manifest.Requires',
                                                    :attrProps    => 'owner'})

        state_info = Operations::Sensor.component_states(@requires.map(&:toCiId))
        @requires.each do |r|
          comp_state_info = state_info[r.toCiId.to_s]
          comp_state_info.except!('updated') if comp_state_info
          r.toCi.health = comp_state_info
        end

        @instances     = Cms::DjCi.all(:params => {:nsPath => platform_bom_ns_path(@environment, @platform)})
        @bom_release   = Cms::Release.first(:params => {:nsPath => "#{environment_ns_path(@environment)}/bom", :releaseState => 'open'})
        @ops_states    = Operations::Sensor.states(@instances)
        @procedure_cis = get_platform_procedures(@platform)
        @procedures    = Cms::Procedure.all(:params => {:ciId => @platform.ciId, :limit => 100})

        @policy_compliance = Cms::Ci.violates_policies(@requires.map(&:toCi), false, true) if Settings.check_policy_compliance
      end

      format.json do
        if @platform
          @platform.links_to = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                               :direction         => 'from',
                                                               :relationShortName => 'LinksTo',
                                                               :includeToCi       => true}).map { |r| r.toCi.ciName }
          @platform.consumes = @clouds
        end
        render_json_ci_response(true, @platform)
      end
    end
  end

  def graph
    ns_path    = platform_bom_ns_path(@environment, @platform)
    components = Cms::Relation.all(:params => {:ciId         => @platform.ciId,
                                               :direction    => 'from',
                                               :includeToCi  => true,
                                               :relationName => 'manifest.Requires'}).map(&:toCi)
    @instances = Cms::DjCi.all(:params => {:nsPath => ns_path})

    if @instances.size > 500
      @graph = platform_graph(@platform, components)
    else
      cis_bom      = @instances.inject({}) {|h, c| h[c.ciId] = c; h}
      @realized_as = Cms::DjRelation.all(:params => {:nsPath => ns_path, :relationName => 'base.RealizedAs'})
      @ops_states  = Operations::Sensor.states(@instances)
      @graph       = platform_graph(@platform, components, @realized_as, cis_bom, @ops_states)
    end
  end

  def procedures
    render :json =>  get_platform_procedures(@platform).map(&:toCi)
  end

  def autorepair
    if params[:status] == 'enable'
      @platform.ciAttributes.autorepair = 'true'
    elsif params[:status] == 'disable'
      @platform.ciAttributes.autorepair = 'false'
    end
    @platform.attrOwner.autorepair = 'manifest'

    ok = execute(@platform, :save)

    respond_to do |format|
      format.js do
        load_platform_detail
        flash[:error] = 'Failed to update autorepair!' unless ok
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def autoreplace
    ok = true
    if request.put?
      status = params[:status]
      enable = (status == 'enable')
      if enable || (status == 'disable')
        @platform.ciAttributes.autoreplace = enable ? 'true' : 'false'
        @platform.attrOwner.autoreplace = 'manifest'
      end
      %w(replace_after_minutes replace_after_repairs).each do |attr|
        value = params[attr]
        if value.present?
          @platform.ciAttributes.attributes[attr] = value
          @platform.attrOwner.attributes[attr] = 'manifest'
        end
      end
      ok = execute(@platform, :save)
    end

    respond_to do |format|
      format.js do
        load_platform_detail
        flash[:error] = 'Failed to update autoreplace!' unless ok
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def autoscale
    unless @platform.ciAttributes.availability == 'redundant'
      message = 'Autoscale operation is allowed only for platforms with redundant availability.'
      @platform.errors.add(:base, message)

      respond_to do |format|
        format.js do
          flash[:error] = message
          render :js => ''
        end

        format.json { render_json_ci_response(false, @platform) }
      end
      return
    end

    if params[:status] == 'enable'
      @platform.ciAttributes.autoscale = 'true'
    elsif params[:status] == 'disable'
      @platform.ciAttributes.autoscale = 'false'
    end
    @platform.attrOwner.autoscale = 'manifest'

    ok = execute(@platform, :save)

    respond_to do |format|
      format.js do
        load_platform_detail
        flash[:error] = 'Failed to update autoscale!' unless ok
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def autocomply
    if params[:status] == 'enable'
      @platform.ciAttributes.autocomply = 'true'
    elsif params[:status] == 'disable'
      @platform.ciAttributes.autocomply = 'false'
    end
    @platform.attrOwner.autocomply = 'manifest'

    ok = execute(@platform, :save)

    respond_to do |format|
      format.js do
        load_platform_detail
        flash[:error] = 'Failed to update autocomply!' unless ok
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end


  protected

  def search_ns_path
    platform_bom_ns_path(@environment, @platform)
  end


  private

  def find_assembly_environment_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)

    platform_id  = params[:id]
    if platform_id.present?
       @platform = locate_manifest_platform(platform_id, @environment, :attrProps => 'owner')
      unless request.get? || @platform.rfcAction == 'add'
        @platform = locate_manifest_platform(platform_id, @environment, :dj => false, :attrProps => 'owner')
      end
    end
  end

  def load_platform_detail
    @platform_detail = Cms::CiDetail.find(@platform.ciId) unless @platform.is_a?(Cms::DjCi) && @platform.rfcAction == 'add'
  end

  def platform_bom_ns_path(environment, platform)
    "#{environment_ns_path(environment)}/bom/#{platform.ciName}/#{platform.ciAttributes.major_version}"
  end

  def get_platform_procedures(platform)
    template_ci = Cms::Ci.first(:params => {:nsPath      => platform_pack_transition_ns_path(platform),
                                            :ciClassName => 'mgmt.manifest.Platform'})
    return [] unless template_ci
    Cms::Relation.all(:params => {:ciId              => template_ci.ciId,
                                  :relationShortName => 'ControlledBy',
                                  :direction         => 'from',
                                  :includeToCi       => true})
  end

  def platform_graph(p, components, realized_as = nil, instances = nil, ops_states = nil)
    t            = HashWithIndifferentAccess.new
    t[:name]     = p.ciName
    t[:pkg]      = p.ciClassName.split('.').shift
    t[:klass]    = p.ciClassName.split('.').last
    t[:children] = Array.new
    components.each do |c|
      component = {:name     => c.ciName,
                   :pkg      => c.ciClassName.split('.').shift,
                   :klass    => c.ciClassName.split('.').last,
                   :size     => 10,
                   :children => Array.new,
                   :url      => assembly_operations_environment_platform_component_path(@assembly, @environment, @platform, c)}
      if @realized_as
        realized_as.select { |r| r.fromCiId == c.ciId }.each do |i|
          instance = instances[i.toCiId]
          component[:children].push({:name       => instance.ciName,
                                     :pkg        => instance.ciClassName.split('.').shift,
                                     :klass      => instance.ciClassName.split('.').last,
                                     :size       => 10,
                                     :release    => instance.rfcAction,
                                     :deployment => instance.rfcId == instance.lastAppliedRfcId ? 'complete' : 'pending',
                                     :health     => instance.lastAppliedRfcId ? ops_states[instance.ciId] : 'pending',
                                     :url        => assembly_operations_environment_platform_component_instance_path(@assembly, @environment, @platform, c, instance)})
        end
      end
      t[:children].push(component)
    end
    return t
  end
end
