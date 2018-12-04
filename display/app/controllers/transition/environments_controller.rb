class Transition::EnvironmentsController < Base::EnvironmentsController
  include ::Search
  before_filter :find_assembly_and_environment

  swagger_controller :environments, 'Transition Environment Management'

  def index
    @environments = Cms::Relation.all(:params => {:ciId              => @assembly.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'RealizedIn',
                                                  :targetClassName   => 'manifest.Environment'}).map(&:toCi)
    render :json => @environments
  end

  def show
    respond_to do |format|
      format.html { render_show }
      format.js { render_show }

      format.json do
        if @environment
          load_consumes_relations
          @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m}
        end
        render_json_ci_response(true, @environment)
      end
    end
  end

  def new
    @environment = Cms::Ci.build(:nsPath => assembly_ns_path(@assembly), :ciClassName => 'manifest.Environment')

    available_clouds_ok = check_available_clouds

    closed_design_ok = check_closed_design_release

    if available_clouds_ok && closed_design_ok
      @clouds = []

      profile_id = params[:profile_id]
      profile    = profile_id.present? ? Cms::Ci.locate(profile_id, organization_ns_path) : nil
      if profile
        attributes            = @environment.ciAttributes.attributes
        attributes['profile'] = profile.ciName
        profile.ciAttributes.attributes.each_pair { |name, value| attributes[name] = value }
        @clouds = Cms::Relation.all(:params => {:relationName    => 'base.Consumes',
                                                :ciId            => profile.ciId,
                                                :direction       => 'from',
                                                :targetClassName => 'account.Cloud'}).sort_by { |o| o.relationAttributes.priority }.inject([]) do |m, c|
          m << Cms::Relation.build({:relationName       => 'base.Consumes',
                                    :nsPath             => @environment.nsPath,
                                    :toCiId             => c.toCiId,
                                    :relationAttributes => c.relationAttributes.attributes}) if c.toCi.ciAttributes.adminstatus == 'active'
          m
        end
      end
    end

    respond_to do |format|
      format.html do
        unless available_clouds_ok
          redirect_to clouds_url
          return
        end

        unless closed_design_ok
          redirect_to assembly_design_path(@assembly)
          return
        end

        load_design_platforms

        ensure_domain

        load_profiles
      end

      format.json { render_json_ci_response(available_clouds_ok && closed_design_ok, @environment) }
    end
  end

  def create
    ns_path      = assembly_ns_path(@assembly)
    cms_ci       = params[:cms_ci]
    cloud_map    = params[:clouds] || cms_ci.delete(:clouds)
    @environment = Cms::Ci.build(cms_ci.merge(:nsPath => ns_path, :ciClassName => 'manifest.Environment'))

    ensure_domain

    ok = check_available_clouds && check_closed_design_release && prepare_consumes_relations(cloud_map)

    load_design_platforms
    availability_map = build_platform_availability(@design_platforms)
    unless availability_map
      @environment.errors.add(:base, "Specify platform availability for: #{@design_platforms.map(&:ciName).join(', ')}")
      ok = false
    end

    if ok
      relation = Cms::Relation.build(:relationName => 'base.RealizedIn',
                                     :nsPath       => ns_path,
                                     :fromCiId     => @assembly.ciId,
                                     :toCi         => @environment)
      ok = execute_nested(@environment, relation, :save)
      @environment = relation.toCi if ok
    end

    ok = save_consumes_relations if ok

    if ok
      ok, error = pull_design(availability_map, params[:async] == 'true')
      @environment.errors.add(:base, "Failed to automatically pull design: #{error}") unless ok

      environment_ns_path = environment_ns_path(@environment)
      relay = Cms::Ci.build(:ciClassName => 'manifest.relay.email.Relay',
                            :nsPath      => environment_ns_path,
                            :ciName      => 'default')
      relay.ciAttributes.emails = @assembly.ciAttributes.owner
      relation = Cms::Relation.build(:relationName => 'manifest.Delivers',
                                     :nsPath       => environment_ns_path,
                                     :fromCiId     => @environment.ciId,
                                     :toCi         => relay)
      unless execute(relation, :save)
        @environment.errors.add(:base, 'Failed to create default notification relay.')
      end
    end

    respond_to do |format|
      format.html do
        if ok
          errors = @environment.errors.full_messages
          flash[:error] = errors.join('. ') if errors.present?
          redirect_to assembly_transition_environment_path(@assembly, @environment)
        else
          load_profiles
          flash[:error] = 'Failed to create environment.'
          render(:action => :new)
        end
      end

      format.json do
        @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m} if @environment
        render_json_ci_response(ok, @environment)
      end
    end
  end

  def edit
    respond_to do |format|
      format.html do
        load_profiles
        load_consumes_relations
        load_available_clouds
        render '_configuration'
      end

      format.json { render_json_ci_response(true, @environment) }
    end
  end

  def update
    load_available_clouds
    load_consumes_relations
    cms_ci = params[:cms_ci]

    # Only admins are allowed to change env profile.
    unless manages_org?
      ci_attrs = cms_ci['ciAttributes']
      ci_attrs.delete('profile') if ci_attrs
    end

    cloud_map = params[:clouds] || cms_ci.delete(:clouds)
    if cloud_map.nil?
      ok = execute(@environment, :update_attributes, cms_ci)
    else
      # Clouds could be passed in both ways (top level or as a part of environment ci.)
      ok = prepare_consumes_relations(cloud_map) &&
           execute(@environment, :update_attributes, cms_ci) &&
           save_consumes_relations
    end

    respond_to do |format|
      format.js { render_show }

      format.json do
        @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m}
        render_json_ci_response(ok, @environment)
      end
    end
  end

  def destroy
    ok = Cms::Ci.count("#{environment_ns_path(@environment)}/bom", true) == 0
    @environment.errors.add(:base, 'Cannot delete evironment with deployments.  Please disable all platforms before deleting the envrionment.') unless ok

    @deployment = Cms::Deployment.latest(:nsPath => environment_bom_ns_path(@environment))
    ok = (!@deployment || @deployment.deploymentState == 'complete' || @deployment.deploymentState == 'canceled')
    @environment.errors.add(:base, 'Cannot delete evironment with open deployment.  Please cancel open deployment before deleting the envrionment.') unless ok

    ok = @environment.errors.blank?
    if ok
      ok = execute(@environment, :destroy)
      if ok
        proxy = CiProxy.where(:ci_id => @environment.ciId).first
        if proxy
          proxy.watched_by_users.clear
          proxy.destroy
        end
      end
    end
    flash[:error] = "Failed to delete environment: #{@environment.errors.full_messages.join(' ')}" unless ok

    respond_to do |format|
      format.html { redirect_to assembly_transition_path(@assembly) }

      format.js do
        if ok
          render :js => "window.location = '#{assembly_transition_path(@assembly)}'"
        else
          render :js => 'hide_modal();'
        end
      end
      format.json { render_json_ci_response(ok, @environment) }
    end
  end


  swagger_api :pull do
    summary 'Pull design.'
    param_path_parent_ids :assembly
    param_path_ci_id :environment
    param :form, :platform_availability, :string, :optional,
          'Platform availability map for new platfroms coming from design. '\
            'Keys are platform ciId or ciName, values are availability type: single|redundant|default.'
    param :form, :async, :string, :optional,
          "This request blocks until pull is complete, unless this set to 'true' to make this request asynchronous (preferred way). "\
          "If not specified this request is blocking."
    param :body, :body, :json, :optional
    notes <<-NOTE
Pull design is not available when there is open environment release. Use <em>'pull_status'</em> request to poll on status when this request is async
(design pull can take up to few minutes for large environments).<br>
JSON body payload example - 2 new platforms are coming from design:
<pre>
{
  "platform_availability": {
    "plat1": "single",
    "plat2": "redundant"
  }
}
</pre>
NOTE
  end
  def pull
    load_design_platforms
    transition_platforms = load_platforms.map(&:toCi)

    @design_platforms.reject! do |dp|
      transition_platforms.detect { |tp| tp.ciName == dp.ciName && tp.ciAttributes.major_version == dp.ciAttributes.major_version }
    end

    availability_map = build_platform_availability(@design_platforms)

    if availability_map
      ok, error = pull_design(availability_map, params[:async] == 'true')
      unless ok
        flash[:error] = "Failed to pull design. #{error.presence || 'Please try again later.'}"
        @environment.errors.add(:base, 'Failed to pull design.')
      end

      respond_to do |format|
        format.js
        format.json { render_json_ci_response(ok, @environment) }
      end
    else
      @environment.errors.add(:base, "Specify platform availability for: #{@design_platforms.map(&:ciName).join(', ')}")

      respond_to do |format|
        format.js { render 'platform_availability' }
        format.json { render_json_ci_response(false, @environment) }
      end
    end
  end

  swagger_api :pull do
    summary 'Check pull design status.'
    param_path_parent_ids :assembly
    param_path_ci_id :environment
    notes <<-NOTE
Ths will return environment CI json.  Check the value of <em>ciState</em> field - it will be set to <b>manifest_locked</b>
while pull is still in progress, it will reset to <em>default</em> once pull design is complete.
NOTE
  end
  def pull_status
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(true, @environment) }
    end
  end

  # TODO deprected - use deployments_controller#bom for in-memory bom generation and preview
  def commit
    generate_bom(true)
  end

  # TODO deprected - use deployments_controller#bom for in-memory bom generation and preview
  def force_deploy
    generate_bom(false)
  end

  def discard
    release_id, message = Transistor.discard_manifest(@environment.ciId)

    respond_to do |format|
      format.js do
        flash[:error] = message if release_id.blank?
      end
      format.json do
        @release = Cms::Release.find(release_id.presence) if release_id.present?
        render_json_ci_response(release_id.present?, @release, [message])
      end
    end
  end

  def enable
    toggle_enabled(true)
  end

  def disable
    toggle_enabled(false)
  end

  def diagram
    send_data(prepare_platform_diagram, :type => 'image/svg+xml', :disposition => 'inline')
  end

  def extract
    respond_to do |format|
      format.json { render :json => process_extract }
      format.yaml { render :text => process_extract.to_yaml, :content_type => 'text/data_string' }
    end
  end

  def load
    data_file = params[:data_file]
    loaded, message = Transistor.import_environment(@environment, (data_file && data_file.read).presence || params[:data])

    respond_to do |format|
      format.json do
        if loaded
          render(:json => @environment, :status => :ok)
        else
          render(:json   => {:errors => [message]}, :status => :unprocessable_entity)
        end
      end
    end
  end


  protected

  def search_ns_path
    environment_manifest_ns_path(@environment)
  end

  private

  def find_assembly_and_environment
    @assembly      = locate_assembly(params[:assembly_id])
    environment_id = params[:id]
    @environment = locate_environment(environment_id, @assembly) if environment_id
  end

  def render_show
    manifest_ns_path = environment_manifest_ns_path(@environment)
    bom_ns_path      = environment_bom_ns_path(@environment)

    @catalog     = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly), :releaseState => 'closed')
    @release     = Cms::Release.latest(:nsPath => manifest_ns_path)
    @manifest    = @release && @release.releaseState == 'canceled' ? Cms::Release.latest(:nsPath => manifest_ns_path, :releaseState => 'closed') : @release
    @bom_release = Cms::Release.first(:params => {:nsPath => bom_ns_path, :releaseState => 'open'})
    @deployment  = Cms::Deployment.latest(:nsPath => bom_ns_path)
    if @deployment && @deployment.deploymentState == 'pending'
      @pending_approvals = Cms::DeploymentApproval.all(:params => {:deploymentId => @deployment.deploymentId}).select {|a| a.state == 'pending'}
    end

    @platforms = load_platforms
    @diagram   = prepare_platform_diagram(@platforms)

    if @manifest && @manifest.parentReleaseId == @catalog.releaseId
      begin
        @design_pull_releasse = Cms::Release.search(:nsPath          => manifest_ns_path,
                                                    :parentReleaseId => @catalog.releaseId,
                                                    :sort            => 'created',
                                                    :size            => 1).first
      rescue Exception => e
        # Not a big deal.
      end
    end

    load_consumes_relations

    load_platform_instances_info

    load_profiles

    load_available_clouds

    render :action => :show
  end

  def load_platforms
    Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                    :direction         => 'from',
                                    :relationShortName => 'ComposedOf',
                                    :targetClassName   => 'manifest.Platform'})
  end

  def load_design_platforms
    @design_platforms = Cms::Relation.all(:params => {:ciId            => @assembly.ciId,
                                                      :direction       => 'from',
                                                      :targetClassName => 'catalog.Platform',
                                                      :relationName    => 'base.ComposedOf'}).map(&:toCi)
  end

  # TODO: disabled temporary for on-premise version
  # need to implement configurable settings.yml option later
  def ensure_domain
    #domain = current_user.organization.ci.ciAttributes.domain
    #@environment.ciAttributes.domain = domain if domain.present?
    #@environment.ciAttributes.subdomain = "#{@environment.ciName}.#{@assembly.ciName}.#{current_user.organization.name}" if domain.blank? || @environment.ciAttributes.subdomain.blank?
    #@environment.ciAttributes.subdomain = "#{@environment.ciName}.#{@assembly.ciName}.#{current_user.organization.name}" if @environment.ciAttributes.subdomain.blank?
  end

  def build_platform_availability(new_design_platforms)
    return {} if new_design_platforms.blank?

    platform_availability = params[:platform_availability] || {}

    return nil if platform_availability.blank?

    return new_design_platforms.inject({}) do |m, p|
      availability = platform_availability[p.ciId.to_s] || platform_availability[p.ciName]
      m[p.ciId]    = availability.presence || 'default'
      m
    end
  end

  def load_available_clouds
    @available_clouds ||= Cms::Ci.all(:params => {:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'}).sort_by(&:ciName)
  end

  def load_consumes_relations
    @clouds = Cms::DjRelation.all(:params => {:relationName    => 'base.Consumes',
                                              :targetClassName => 'account.Cloud',
                                              :direction       => 'from',
                                              :ciId            => @environment.ciId}).sort_by { |o| o.relationAttributes.priority }
  end

  def prepare_consumes_relations(cloud_map)
    old_consumes_rel_map = @environment.persisted? ? @clouds.to_map(&:toCiId) : {}
    @clouds              = []

    return true unless cloud_map

    available_cloud_id_map   = @available_clouds.to_map(&:ciId)
    available_cloud_name_map = @available_clouds.to_map(&:ciName)

    errors = []
    has_primary_cloud = false
    cloud_map.each_pair do |id, cloud_attr|
      cloud_attr = {:priority => cloud_attr} unless cloud_attr.is_a?(Hash)   # For backward compatibility (1/22/2015) - TODO: remove at some point.
      priority = cloud_attr[:priority]
      if priority == '1' || priority == '2'
        cloud = available_cloud_id_map[id.to_i] || available_cloud_name_map[id]
        if cloud
          cloud_status = cloud.ciAttributes.adminstatus
          consumes_rel = old_consumes_rel_map[cloud.ciId]
          if cloud_status != 'active' && !consumes_rel
            errors << "Administrative status for cloud '#{cloud.ciName}' is #{cloud_status}, adding cloud to environment is not allowed."
          else
            @clouds << Cms::Relation.build({:ciRelationId       => consumes_rel ? consumes_rel.ciRelationId : nil,
                                            :relationName       => 'base.Consumes',
                                            :nsPath             => @environment.nsPath,
                                            :fromCiId           => @environment.persisted? ? @environment.ciId : nil,
                                            :toCiId             => cloud.ciId,
                                            :relationAttributes => cloud_attr})
          end
          has_primary_cloud ||= priority == '1'
        end
      end
    end

    errors << 'At least one primary cloud must be selected.' unless has_primary_cloud
    errors.each {|e| @environment.errors.add(:base, e)}

    return errors.blank?
  end

  def save_consumes_relations
    @clouds.each { |c| c.fromCiId = @environment.ciId }
    ok, message = Transistor.set_environment_clouds(@environment.ciId, @clouds)
    unless ok
      @environment.errors.add(:base, message)
      flash[:error] = "Failed to update environment. #{message}"
    end
    return ok
  end

  def check_available_clouds
    load_available_clouds
    if @available_clouds.present?
      return true
    else
      message       = 'Please set up a cloud before creating an environment.'
      flash[:error] = message
      @environment.errors.add(:base, message) if @environment
      return false
    end
  end

  def check_closed_design_release
    if Cms::Release.latest(:nsPath => assembly_ns_path(@assembly), :releaseState => 'closed')
      return true
    else
      message       = 'Please ensure you have a committed design before creating an environment.'
      flash[:error] = message
      @environment.errors.add(:base, message) if @environment
      return false
    end
  end

  def load_profiles
    @profiles = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                        :ciClassName => 'account.Environment'}).sort_by(&:ciName)
  end

  def generate_bom_allowed?(action)
    if @environment.ciState == 'locked'
      return false, "Cannot #{action} while deployment plan is being generated."
    end

    @deployment = Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")
    if @deployment && %w(active paused failed pending).include?(@deployment.deploymentState)
      return false, "Cannot #{action} while deployment is in progress."
    end

    return true
  end

  def generate_bom(commit = true)
    ok, message = generate_bom_allowed?(commit ? 'commit' : 'force deploy')

    ok, message = Transistor::generate_bom(@environment.ciId, params[:desc], commit, params[:exclude_platforms]) if ok
    if ok
      # Reload environment since its "ciState" and "comments" should have changed.
      @environment = locate_environment(@environment.ciId, @assembly)
    end

    respond_to do |format|
      format.js do
        if ok
          @platforms = load_platforms
          render :action => :commit
        else
          flash[:error] = message
          render :js => 'hide_modal();'
        end
      end

      format.json { render_json_ci_response(ok, @environment, message) }
    end
  end

  def toggle_enabled(enabled)
    platform_ci_ids = params[:platformCiIds] || load_platforms(&:toCiId)
    ok, message = Transistor.toggle_platforms(platform_ci_ids, enabled)

    respond_to do |format|
      format.js do
        flash[:error] = message unless ok
        render :action => :enable
      end

      format.json { render_json_ci_response(ok, @environment, [message]) }
    end
  end

  def prepare_platform_diagram(platforms = nil)
    platforms ||= load_platforms
    links_to  = Cms::DjRelation.all(:params => {:nsPath => [@environment.nsPath, @environment.ciName, 'manifest'].join('/'), :relationShortName => 'LinksTo'})
    begin
      return graphvis_sub_pack_remote_images(platforms_diagram(platforms, links_to, assembly_transition_environment_path(@assembly, @environment), params[:size]).output(:svg => String))
    rescue
      return nil
    end
  end

  def pull_design(availability_map, async)
    release_id, error = Transistor.pull_design(@environment.ciId, availability_map)
    return false, error unless release_id

    @environment.reload
    return true if async

    start_time = Time.now
    while @environment.ciState == 'manifest_locked' && (Time.now - start_time < Transistor.timeout)
      sleep(5)
      @environment.reload
    end

    if @environment.ciState == 'manifest_locked'
      return false, 'Timed out pulling design, design pull is still in progress.'
    else
      return true
    end
  end

  def process_extract
    platform_ids = params[:platform_id]
    params[:raw].blank? ? export_environment(params[:collapse] == 'false' ? false : true, platform_ids) : export_environment_raw(platform_ids)
  end

  def export_environment_raw(platform_id = nil)
    Transistor.export_environment(@environment, platform_id && [platform_id])
  end

  def export_environment(collapse, platform_id = nil)
    data   = export_environment_raw(platform_id)
    result = {}

    environment = data['environment']['attributes'].delete_blank
    convert_json_attrs_from_string(environment, 'manifest.Environment') unless collapse
    result['environment'] = environment

    consumes = data['consumes']
    result['clouds'] = consumes.sort_by {|c| c['name']}.to_map_with_value {|c| [c['name'], (c['attributes'] || {}).delete_blank]} if consumes.present?

    relays = data['relays']
    if relays.present?
      result['relays'] = relays.sort_by { |c| c['name'] }.to_map_with_value do |r|
        relay = (r['attributes'] || {}).delete_blank
        convert_json_attrs_from_string(relay, 'manifest.relay.email.Relay') unless collapse
        [r['name'], relay]
      end
    end


    manifest = data['manifest']
    if manifest.present?
      data.copy_if(manifest, 'variables')

      platforms = manifest['platforms']
      result['platforms'] = (platforms || []).sort_by {|p| p['name']}.inject({}) do |plats, p|
        attrs = p['attributes']
        plat = {'pack' => "#{attrs.delete('source')}/#{attrs.delete('pack')}:#{attrs.delete('version')}"}
        attrs.delete('description') if attrs['description'].blank?
        plat.merge!(attrs)

        consumes = p['consumes']
        plat['clouds'] = consumes.sort_by {|c| c['name']}.to_map_with_value {|c| [c['name'], (c['attributes'] || {}).delete_blank]} if consumes.present?

        p.copy_if(plat, 'links', 'variables')

        components = p.delete('components')
        if components.present?
          plat['components'] = components.group_by {|c| "#{c['template']}/#{c['type'].sub(/^manifest\./, '')}"}.inject({}) do |templates_hash, (template_name, template_components)|
            templates_hash[template_name] = template_components.sort_by {|c| c['name']}.to_map_with_value do |c|
              comp = c['attributes'].presence || {}
              comp = convert_json_attrs_from_string(comp, c['type']) unless collapse

              c.copy_if(comp, 'depends')

              attachments = c['attachments']
              if attachments.present?
                comp['attachments'] = attachments.sort_by {|a| a['name']}.to_map_with_value do |a|
                  attrs = a['attributes']
                  attrs = convert_json_attrs_from_string(attrs, 'manifest.Attachment') unless collapse
                  [a['name'], attrs]
                end
              end

              monitors = c['monitors']
              watched_bys = c['watchedBy']
              if watched_bys.present?
                monitor_map = monitors.blank? ? {} : monitors.to_map {|w| w['name']}
                monitor_map = watched_bys.inject(monitor_map) do |mm, w|
                  monitor_name = w['name']
                  monitor = mm[monitor_name]
                  if monitor
                    monitor['attributes'].merge!(w['attributes'])
                  else
                    mm[monitor_name] = w
                  end
                  mm
                end
                monitors = monitor_map.values
              end
              if monitors.present?
                comp['monitors'] = monitors.sort_by {|a| a['name']}.to_map_with_value do |a|
                  attrs = a['attributes']
                  attrs = convert_json_attrs_from_string(attrs, 'manifest.Monitor') unless collapse
                  [a['name'], attrs]
                end
              end

              scaling = c['scaling']
              if scaling.present?
                comp['scaling'] = scaling.values.inject({}) do |s, rels|
                  rels.each_pair do |to_name, rel|
                    scaling_attrs = rel['attributes']
                    s[to_name] = scaling_attrs.delete_blank if scaling_attrs.present?
                  end
                  s
                end
              end

              [c['name'], comp]
            end
            templates_hash
          end
        end
        plats[p['name']] = plat
        plats
      end
    end

    result
  end
end
