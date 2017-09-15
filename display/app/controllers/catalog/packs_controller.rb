class Catalog::PacksController < ApplicationController
  SUPPORT_PERMISSION_PACK_MANAGEMENT = 'pack_management'
  ORG_VISIBILITY_ALT_NS_TAG = 'enableForOrg'

  before_filter :authorize_pack_owner_group_membership, :only => [:visibility, :password]
  before_filter :find_pack_version, :only => [:show, :update, :visibility, :password, :diff]

  def index
    load_packs_and_versions
    respond_to do |format|
      format.html { redirect_to catalog_path(:anchor => 'packs') }
      format.js do
        pack_map = @pack_cis.to_map {|p| "#{p.nsPath}/#{p.ciName}"}

        @packs = @version_cis.inject({}) do |h, version|
          h[version.nsPath] ||= {:pack => pack_map[version.nsPath], :versions => []}
          h[version.nsPath][:versions] << version
          h
        end
        @packs = @packs.values
      end

      format.json do
        source_pack_map = @pack_cis.group_by {|p| p.nsPath.split('/')[-2]}
        version_map = @version_cis.group_by(&:nsPath)

        packs = source_pack_map.keys.inject({}) do |m, source|
          m[source] = source_pack_map[source].to_map_with_value do |pack|
            pack_name = pack.ciName
            [pack_name, (version_map["#{pack.nsPath}/#{pack_name}"] || []).map(&:ciName)]
          end
          m
        end

        render :json => {:packs => packs}
      end
    end
  end

  def versions
    org_ns_path = organization_ns_path
    pack_name = params[:pack]
    if pack_name.present?
      source         = params[:source]
      @pack          = locate_pack(source, pack_name)
      @pack.versions = build_pack_version_map(locate_pack_versions(source, pack_name), org_ns_path)
      render :json => @pack
    else
      load_packs_and_versions
      source_pack_map = @pack_cis.group_by {|p| p.nsPath.split('/')[-2]}
      version_map     = @version_cis.group_by(&:nsPath)

      packs = source_pack_map.keys.inject({}) do |m, source|
        m[source] = source_pack_map[source].to_map_with_value do |pack|
          pack_name = pack.ciName
          pack.versions = build_pack_version_map(version_map["#{pack.nsPath}/#{pack_name}"] || [], org_ns_path)
          [pack_name, pack]
        end
        m
      end
      render :json => packs
    end
  end

  def show
    respond_to do |format|
      format.js do
        @stats = Search::Pack.count_stats(params[:source], params[:pack], params[:version]) if @version
      end

      format.json do
        if @version
          @version.pack = @pack
          render_json_ci_response(@version.present?, @version)
        else
          render_json_ci_response(@pack.present?, @pack)
        end
      end
    end
  end

  def update
    ok  = false
    admin_password = params[:password]
    password_digest = admin_password && Digest::SHA512.hexdigest(admin_password)
    if password_digest == @pack.ciAttributes.admin_password_digest || has_support_permission?(SUPPORT_PERMISSION_PACK_MANAGEMENT)
      %w(owner description).each do |attr|
        value = params[attr]
        @pack.ciAttributes.attributes[attr] = value if value
      end
      ok = execute(@pack, :save)
      flash.now[:error] = 'Failed to update pack info.' unless ok
    else
      @pack.errors.add(:base, 'Invalid pack admin password.')
    end

    respond_to do |format|
      format.js
      format.json { render_json_ci_response(ok, @pack) }
    end
  end

  def stats
    @stats = Search::Pack.count_stats(params[:source], params[:pack], params[:version])
    respond_to do |format|
      format.js
      format.json { render :json => @stats ? {:count => @stats} : {:errors => ['Failed to fetch stats.']}, :status => :internal_server_error }
    end
  end


  def visibility
    ok = false
    password_digest = Digest::SHA512.hexdigest(params[:password])
    if password_digest == @pack.ciAttributes.admin_password_digest || has_support_permission?(SUPPORT_PERMISSION_PACK_MANAGEMENT)
      orgs = params[:orgs]
      if orgs.present?
        @version.altNs.attributes[ORG_VISIBILITY_ALT_NS_TAG] = orgs.split(/[\s,]/).select(&:present?).uniq.map {|o| "/#{o}"}
        @version.ciAttributes.enabled = 'false'
      else
        @version.altNs.attributes[ORG_VISIBILITY_ALT_NS_TAG] = []
        @version.ciAttributes.enabled = params[:enabled] == 'false' ? 'false' : 'true'
      end
      ok = execute(@version, :save)
      @version = locate_pack_version(params[:source], params[:pack], params[:version]) if ok
    else
      @version.errors.add(:base, 'Invalid pack admin password.')
    end

    respond_to do |format|
      format.js {render :action => :update}
      format.json { render_json_ci_response(ok, @version) }
    end
  end

  def password
    ok = false
    password_digest = Digest::SHA512.hexdigest(params[:password])
    if password_digest == @pack.ciAttributes.admin_password_digest || has_support_permission?(SUPPORT_PERMISSION_PACK_MANAGEMENT)
      new_password = params[:new_password]
      if new_password.blank?
        @pack.errors.add(:base, 'Invalid password/')
      elsif new_password != params[:confirm_password]
        @pack.errors.add(:base, 'Passwords do not match.')
      else
        @pack.ciAttributes.admin_password_digest = Digest::SHA512.hexdigest(new_password)
        ok = execute(@pack, :save)
        flash.now[:notice] = 'Updated pack admin password.' if ok
      end
    else
      @pack.errors.add(:base, 'Invalid pack admin password.')
    end

    respond_to do |format|
      format.js {render :action => :show}
      format.json { render_json_ci_response(ok, @pack) }
    end
  end

  def diff
    source       = params[:source]
    pack_name    = params[:pack]
    version      = params[:version]
    availability = params[:availability]

    if request.format.html?
      @versions = locate_pack_versions(source, pack_name)
      return
    end

    platform = locate_pack_platform(pack_name, source, pack_name, version, availability)
    if platform.blank?
      not_found("pack version #{version} not found.")
      return
    end

    other_version = params[:other_version]
    if other_version.present?
      other_platform = locate_pack_platform(pack_name, source, pack_name, other_version, availability)
      if other_platform.blank?
        not_found("other_depends pack version #{other_version} not found.")
        return
      end

      platform_pack_ns_path       = platform_pack_design_ns_path(platform)
      other_platform_pack_ns_path = platform_pack_design_ns_path(other_platform)

      @diff = []

      # Compare components.
      other_components = Cms::Relation.all(:params => {:ciId              => other_platform.ciId,
                                                       :direction         => 'from',
                                                       :relationShortName => 'Requires'})
      other_components_map = other_components.to_map {|r| r.toCi.ciName}
      other_components_id_map = other_components.map(&:toCi).to_map(&:ciId)

      components = Cms::Relation.all(:params => {:ciId              => platform.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'Requires'})
      components_id_map = components.map(&:toCi).to_map(&:ciId)
      components.inject(@diff) do |diff, r|
        component       = r.toCi
        other_requires  = other_components_map.delete(component.ciName)
        process_ci_diff(component, other_requires && other_requires.toCi, diff)
        process_relation_diff(r, other_requires, diff)
        diff
      end
      other_components_map.values.inject(@diff) do |diff, r|
        component = r.toCi
        component.ciState = 'delete'
        diff << component
      end

      # Compare variables.
      other_var_map = Cms::Relation.all(:params => {:ciId              => other_platform.ciId,
                                                    :direction         => 'to',
                                                    :relationShortName => 'ValueFor',
                                                    :includeFromCi     => true,
                                                    :includeToCi       => false}).map(&:fromCi).to_map(&:ciName)
      Cms::Relation.all(:params => {:ciId              => platform.ciId,
                                    :direction         => 'to',
                                    :relationShortName => 'ValueFor',
                                    :includeFromCi     => true,
                                    :includeToCi       => false}).inject(@diff) do |diff, r|
        variable       = r.fromCi
        other_variable = other_var_map.delete(variable.ciName)
        process_ci_diff(variable, other_variable, diff)
        diff
      end
      other_var_map.values.inject(@diff) do |diff, variable|
        variable.ciState = 'delete'
        diff << variable
      end

      # Compare dependsOn relations.
      other_depends_map = Cms::Relation.all(:params => {:nsPath            => other_platform_pack_ns_path,
                                                        :relationShortName => 'DependsOn',
                                                        :includeFromCi     => false,
                                                        :includeToCi       => false}).to_map { |r| "#{other_components_id_map[r.fromCiId].ciName}**#{other_components_id_map[r.toCiId].ciName}" }
      Cms::Relation.all(:params => {:nsPath            => platform_pack_ns_path,
                                    :relationShortName => 'DependsOn',
                                    :includeFromCi     => false,
                                    :includeToCi       => false}).inject(@diff) do |diff, r|
        fromc_ci      = components_id_map[r.fromCiId]
        to_ci         = components_id_map[r.toCiId]
        other_depends = other_depends_map.delete("#{fromc_ci.ciName}**#{to_ci.ciName}")
        r.fromCi = fromc_ci
        r.toCi   = to_ci
        process_relation_diff(r, other_depends, diff)
        diff
      end
      other_depends_map.values.inject(@diff) do |diff, r|
        r.relationState = 'delete'
        diff << r
      end

      # Compare monitors.
      other_monitors_map = Cms::Relation.all(:params => {:nsPath            => other_platform_pack_ns_path,
                                                         :relationShortName => 'WatchedBy',
                                                         :includeFromCi     => false,
                                                         :includeToCi       => true}).
        to_map {|r| "#{other_components_id_map[r.fromCiId].ciName}**#{r.toCi.ciName}"}

      Cms::Relation.all(:params => {:nsPath            => platform_pack_ns_path,
                                    :relationShortName => 'WatchedBy',
                                    :includeFromCi     => false,
                                    :includeToCi       => true}).inject(@diff) do |diff, r|
        monitor          = r.toCi
        component        = components_id_map[r.fromCiId]
        other_watched_by = other_monitors_map.delete("#{component.ciName}**#{monitor.ciName}")
        other_monitor    = other_watched_by && other_watched_by.toCi
        monitor.component = component
        r.fromCi = component
        process_ci_diff(monitor, other_monitor, diff)
        process_relation_diff(r, other_watched_by, diff) if other_watched_by
        diff
      end
      other_monitors_map.values.inject(@diff) do |diff, r|
        monitor           = r.toCi
        monitor.ciState   = 'delete'
        monitor.component = other_components_id_map[r.fromCiId]
        diff << monitor
      end
    end

    respond_to do |format|
      format.js
      format.json {render :json => @diff}
    end
  end


  private

  def load_packs_and_versions
    @pack_cis    = Cms::Ci.all(:params => {:nsPath      => '/public',
                                          :ciClassName => 'mgmt.Pack',
                                          :recursive   => true})
    @version_cis = Cms::Ci.all(:params => {:nsPath       => '/public',
                                          :ciClassName  => 'mgmt.Version',
                                          :recursive    => true,
                                          :includeAltNs => ORG_VISIBILITY_ALT_NS_TAG})
  end

  def find_pack_version
    @pack    = locate_pack(params[:source], params[:pack])
    version = params[:version]
    @version = locate_pack_version(params[:source], params[:pack], version) if version.present?
  end

  def authorize_pack_owner_group_membership
    unless has_support_permission?(SUPPORT_PERMISSION_PACK_MANAGEMENT) || check_pack_owner_group_membership?
      unauthorized
      return
    end
  end

  def process_ci_diff(target, base, diffs)
    if base
      attr_diff = calculate_attr_diff(target, base)
      return if attr_diff.blank?

      target.ciState        = 'update'
      target.diffCi         = base
      target.diffAttributes = attr_diff
    else
      target.ciState = 'add'
    end
    diffs << target
  end

  def process_relation_diff(target, base, diffs)
    if base
      attr_diff = calculate_attr_diff(target, base)
      return if attr_diff.blank?

      target.relationState  = 'update'
      target.diffRelation   = base
      target.diffAttributes = attr_diff
    else
      target.relationState = 'add'
    end
    diffs << target
  end

  def build_pack_version_map(versions, org_ns_path)
    semver_sort(versions).
      to_map_with_value {|v| [v.ciName, !!(v.ciAttributes.enabled != 'false' || v.altNs.attributes[Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG].try(:include?, org_ns_path))]}
  end
end
