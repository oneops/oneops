class Design::PlatformsController < Base::PlatformsController
  before_filter :find_assembly_and_platform

  swagger_controller :platforms, 'Design Platform Management'

  swagger_api :index do
    summary 'Fetches all platforms in the design of assembly.'
    notes 'This fetches all platforms from design including new platforms from open release.'
    param_org_name
    param_parent_ci_id :assembly
    response :unauthorized
  end

  def index
    @platforms = Cms::DjRelation.all(:params => {:ciId              => @assembly.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'ComposedOf',
                                                 :targetClassName   => 'catalog.Platform'}).map(&:toCi)
    render :json => @platforms
  end

  def show
    respond_to do |format|
      format.html do
        build_component_groups
        build_linkable_platform_map

        @policy_compliance = Cms::Ci.violates_policies(@components, false, true) if Settings.check_policy_compliance

        @release = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))
        @rfcs = @release.releaseState == 'open' ? Transistor.design_platform_rfcs(@platform.ciId, :attrProps => 'owner') : {}


        @platform_detail = Cms::CiDetail.find(@platform.ciId) unless @platform.rfcAction == 'add'

        platform_attributes = @platform.ciAttributes
        source              = platform_attributes.source
        pack_name           = platform_attributes.pack
        version             = platform_attributes.version

        @pack_minor_versions = pack_versions(source, pack_name, version.split('.').first)
        @pack_version = @pack_minor_versions.find {|v| v.ciName == version} || locate_pack_version(source, pack_name, version)

        render(:action => :show)
      end

      format.json do
        @platform.links_to = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                             :direction         => 'from',
                                                             :relationShortName => 'LinksTo',
                                                             :includeToCi       => true}).map { |r| r.toCi.ciName } if @platform
        render_json_ci_response(true, @platform)
      end
    end
  end


  def new
    @platform = Cms::DjCi.build({:nsPath      => assembly_ns_path(@assembly),
                                 :ciClassName => 'catalog.Platform'},
                                {:owner => {}})

    respond_to do |format|
      format.html do
        build_linkable_platform_map
        render :action => :new
      end

      format.json { render_json_ci_response(true, @platform) }
    end
  end

  def create
    platform_hash = params[:cms_dj_ci].merge(:nsPath => assembly_ns_path(@assembly), :ciClassName => 'catalog.Platform')
    attrs = platform_hash[:ciAttributes]
    attrs[:major_version] = attrs[:version]
    attrs[:description] ||= ''
    attr_props = platform_hash.delete(:ciAttrProps)

    @platform = Cms::DjCi.build(platform_hash, attr_props)
    pack_ver = locate_pack_version_for_platform(@platform)

    if pack_ver.blank?
      @platform.errors.add(:base, 'Pack not found.')
    elsif pack_ver.ciAttributes.enabled == 'false'
      visibility = pack_ver.altNs.attributes[Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG]
      @platform.errors.add(:base, 'Pack is disabled.') unless visibility.present? && visibility.include?(organization_ns_path)
    end

    @platform = Transistor.create_platform(@assembly.ciId, @platform) if @platform.errors.blank?
    ok = @platform.errors.blank?

    save_platform_links if ok

    respond_to do |format|
      format.html do
        if ok
          show
        else
          flash.now[:alert] = 'Failed to create platform.'
          setup_linkable_platform_map
          render :action => :new
        end
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def edit
    respond_to do |format|
      format.json { render_json_ci_response(true, @platform) }
    end
  end

  def update
    ok = execute(@platform, :update_attributes, params[:cms_dj_ci])
    ok = save_platform_links if ok

    respond_to do |format|
      format.js do
        if ok
          setup_linkable_platform_map()
        end
        render :action => :edit
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def destroy
    ok = Transistor.delete_platform(@assembly.ciId, @platform)
    respond_to do |format|
      format.html do
        flash[:error] = "Failed to delete platform. #{@platform.errors.full_messages.join('. ')}" unless ok
        redirect_to(assembly_design_path(@assembly))
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def new_clone
    @assemblies = locate_assemblies
  end

  def clone
    @to_assembly  = locate_assembly(params[:to_assembly_id])
    id = Transistor.clone_platform(@platform.ciId, {:nsPath => assembly_ns_path(@to_assembly), :ciClassName => 'catalog.Platform', :ciName => params[:to_ci_name]})
    @to_platform  = Cms::DjCi.find(id) if id
    ok = @to_platform.present?
    flash[:error] = "Failed to clone platform '#{@platform.ciName}'." unless ok

    respond_to do |format|
      format.js
      format.json { render_json_ci_response(ok, @to_platform, ['Failed to clone.']) }
    end
  end

  def component_types
    exising_map = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'Requires'}).inject({}) do |m, r|
      m[r.relationAttributes.template] = (m[r.relationAttributes.template] || 0) + 1
      m
    end

    result = get_platform_requires_relation_temlates(@platform).inject({}) do |m, r|
      template_name    = r.toCi.ciName.split('::').last
      cardinality      = r.relationAttributes.constraint.gsub('*', '999').split('..')
      existing_count = exising_map[template_name] || 0
      m[template_name] = {:min => cardinality.first.to_i,
                          :max => cardinality.last.to_i,
                          :current => existing_count} unless r.toCi.ciState == 'pending_deletion' && existing_count == 0
      m
    end

    render :json => result
  end

  def diff
    clazz = params[:committed] == 'true' ? Cms::Relation : Cms::DjRelation
    changes_only = params[:changes_only] != 'false'

    # Compare components.
    platform_pack_ns_path = platform_pack_design_ns_path(@platform)
    pack_components = Cms::Relation.all(:params => {:nsPath            => platform_pack_ns_path,
                                                    :relationShortName => 'Requires',
                                                    :includeFromCi     => false,
                                                    :includeToCi       => true}).map(&:toCi).to_map(&:ciName)

    component_id_map = {}
    @diff = clazz.all(:params => {:ciId              => @platform.ciId,
                                  :direction         => 'from',
                                  :relationShortName => 'Requires',
                                  :attrProps         => 'owner'}).inject([]) do |m, r|
      component                        = r.toCi
      component_id_map[component.ciId] = component
      pack_component                   = pack_components[r.relationAttributes.template]
      diff                             = calculate_attr_diff(component, pack_component)
      # The last condition below is to ensure that we include current component as diff when it is not required by
      # the pack (lower bound of cardinality constraint is zero) even it did not override any defaults when added by user.
      if !changes_only || diff.present? || r.relationAttributes.constraint.split('..').first.to_i == 0
        component.diffCi         = pack_component
        component.diffAttributes = diff
        m << component
      end
      m
    end

    # Compare variables.
    pack_variables = Cms::Relation.all(:params => {:nsPath            => platform_pack_ns_path,
                                                   :relationShortName => 'ValueFor',
                                                   :includeFromCi     => true,
                                                   :includeToCi       => false}).map(&:fromCi).to_map(&:ciName)
    clazz.all(:params => {:ciId              => @platform.ciId,
                          :direction         => 'to',
                          :relationShortName => 'ValueFor',
                          :includeFromCi     => true,
                          :includeToCi       => false,
                          :attrProps         => 'owner'}).inject(@diff) do |m, r|
      variable      = r.fromCi
      pack_variable = pack_variables[variable.ciName]
      diff          = calculate_attr_diff(variable, pack_variable)
      # The last condition below is to ensure that we include current variable as diff when it was in the pack
      # even it does not have any attributes (namely, 'value') set.
      if !changes_only || diff.present? || !pack_variable
        variable.diffCi         = pack_variable
        variable.diffAttributes = diff
        m << variable
      end
      m

    end

    # Compare monitors.
    pack_monitors = Cms::Relation.all(:params => {:nsPath            => platform_pack_ns_path,
                                                  :relationShortName => 'WatchedBy',
                                                  :includeFromCi     => false,
                                                  :includeToCi       => true}).map(&:toCi).to_map(&:ciName)
    clazz.all(:params => {:nsPath            => design_platform_ns_path(@assembly, @platform),
                          :relationShortName => 'WatchedBy',
                          :includeFromCi     => false,
                          :includeToCi       => true,
                          :attrProps         => 'owner'}).inject(@diff) do |m, r|
      monitor      = r.toCi
      component    = component_id_map[r.fromCiId]
      pack_monitor = pack_monitors[monitor.ciName.sub("#{@platform.ciName}-#{component.ciName}-", '')]
      diff         = calculate_attr_diff(monitor, pack_monitor)
      if !changes_only || diff.present? || !pack_monitor
        monitor.diffCi         = pack_monitor
        monitor.diffAttributes = diff
        monitor.component      = component
        m << monitor
      end
      m
    end

    # There are no attachments in the pack so all attachments are added to diff.
    clazz.all(:params => {:nsPath            => design_platform_ns_path(@assembly, @platform),
                          :relationShortName => 'EscortedBy',
                          :includeFromCi     => true,
                          :includeToCi       => true,
                          :attrProps         => 'owner'}).inject(@diff) do |m, r|
      attachment = r.toCi
      attachment.diffCi         = nil
      attachment.diffAttributes = calculate_attr_diff(attachment, Cms::Ci.build(:ciClassName => 'catalog.Attachment'))
      attachment.component      = component_id_map[r.fromCiId]
      @diff << attachment
    end

    respond_to do |format|
      format.js
      format.json {render :json => @diff}
    end
  end

  def commit
    ok, @error = Transistor.commit_design_platform_rfcs(@platform.ciId, params[:desc])
    respond_to do |format|
      format.js
      format.json {render_json_ci_response(ok, @platform, ok ? nil : [@error])}
    end
  end

  def discard
    ok, @error = Transistor.discard_design_platform_rfcs(@platform.ciId)
    respond_to do |format|
      format.js {render :action => :commit}
      format.json {render_json_ci_response(ok, @platform, ok ? nil : [@error])}
    end
  end

  def pack_refresh
    if Cms::Rfc.count(design_platform_ns_path(@assembly, @platform)).values.sum > 0
      ok = false
      message = 'Pull Pack is not allowed with pending platform changes in the current release. Please commit or discard  current platform changes before proceeding with pack pull.'
    else
      ok, message = Transistor.pack_refresh(@platform.ciId)
    end

    respond_to do |format|
      format.html do
        flash[:error] = message unless ok
        redirect_to :action => :show
      end

      format.json {render_json_ci_response(ok, @platform, ok ? nil : [message])}
    end
  end

  def pack_update
    if Cms::Rfc.count(design_platform_ns_path(@assembly, @platform)).values.sum > 0
      ok = false
      message = 'Pack Update is not allowed with pending platform changes in the current release. Please commit or discard  current platform changes before proceeding with pack update.'
    else
      ok, message = Transistor.pack_update(@platform.ciId, params[:version])
    end

    respond_to do |format|
      format.html do
        flash[:error] = message unless ok
        redirect_to :action => :show
      end

      format.json {render_json_ci_response(ok, @platform, ok ? nil : [message])}
    end
  end


  private

  def find_assembly_and_platform
    @assembly = locate_assembly(params[:assembly_id])
    platform_id = params[:id]
    @platform = locate_design_platform(platform_id, @assembly, :attrProps => 'owner') if platform_id.present?
  end

  def build_linkable_platform_map
    if @platform.new_record?
      linkable_platform_map = find_all_platforms.inject({}) { |m, p| m[p] = false; m }
    else
      platform_ci_id = @platform.ciId
      platforms = find_all_platforms.to_map(&:ciId)

      links_to_relations = Cms::DjRelation.all(:params => {:nsPath        => @platform.nsPath,
                                                           :fromClassName => 'catalog.Platform',
                                                           :toClassName   => 'catalog.Platform',
                                                           :relationName  => 'catalog.LinksTo'})
      @links_from = []
      @links_to   = []
      links_to_relations.each do |r|
        @links_from << platforms[r.fromCiId] if r.toCiId == platform_ci_id
        @links_to << platforms[r.toCiId] if r.fromCiId == platform_ci_id
      end

      linked_platform_ids = find_linked_platform_ids(links_to_relations, platform_ci_id)
      linked_platform_ids << platform_ci_id
      linked_platform_ids.uniq!
      linkable_platform_map = platforms.values.reject {|p| linked_platform_ids.include?(p.ciId) }.inject({}) do |m, p|
        m[p] = links_to_relations.find {|r| r.fromCiId == platform_ci_id && r.toCiId == p.ciId}
        m
      end
    end

    @linkable_platform_map =  linkable_platform_map
  end

  def setup_linkable_platform_map
    new_links_to_ids = (params[:links_to].presence || []).map(&:to_i)
    build_linkable_platform_map
    @linkable_platform_map.keys.each { |p| @linkable_platform_map[p] = new_links_to_ids.include?(p.ciId) }
  end

  def find_all_platforms
    Cms::DjCi.all(:params => {:nsPath => @platform.nsPath, :ciClassName => 'catalog.Platform'})
  end

  def find_linked_platform_ids(relations, platform_ids)
    result = []
    platform_ids = [ platform_ids ] unless platform_ids.is_a?(Array)
    platform_ids.each do |p_id|
      relations.each { |r| result << r.fromCiId if r.toCiId == p_id }
    end
    result += find_linked_platform_ids(relations, result) if result.present?
    return result
  end

  def save_platform_links
    new_links_to_ids = (params[:links_to].presence || []).map do |id|
      begin
        Cms::DjCi.locate(id, assembly_ns_path(@assembly), 'catalog.Platform').ciId.to_i
      rescue
        nil
      end
    end
    new_links_to_ids.compact!

    old_links_to_relations = Cms::DjRelation.all(:params => {:ciId => @platform.ciId, :direction => 'from', :relationName => 'catalog.LinksTo'})
    old_links_to_ids       = old_links_to_relations.map(&:toCiId)

    ok = true

    # Destroy relations to platforms that became unlinked.
    (old_links_to_ids - new_links_to_ids).each do |platform_id|
      relation = old_links_to_relations.detect { |r| r.toCiId == platform_id }
      ok       = execute_nested(@platform, relation, :destroy)
      break unless ok
    end

    # Create relations to platforms that became linked.
    if ok
      (new_links_to_ids - old_links_to_ids).each do |platform_id|
        relation = Cms::DjRelation.build({:nsPath       => @platform.nsPath,
                                          :relationName => 'catalog.LinksTo',
                                          :fromCiId     => @platform.ciId,
                                          :toCiId       => platform_id})
        ok = execute_nested(@platform, relation, :save)
        break unless ok
      end
    end

    return ok
  end
end
