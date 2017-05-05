class Catalog::PacksController < ApplicationController
  SUPPORT_PERMISSION_PACK_MANAGEMENT = 'pack_management'
  ORG_VISIBILITY_ALT_NS_TAG = 'enableForOrg'

  before_filter :authorize_pack_owner_group_membership, :only => [:visibility, :password]
  before_filter :find_pack_version, :only => [:show, :visibility, :password]

  helper_method :check_pack_owner_group_membership?


  def index
    pack_cis = Cms::Ci.all(:params => {:nsPath      => '/public',
                                       :ciClassName => 'mgmt.Pack',
                                       :recursive   => true})
    if check_pack_owner_group_membership?(current_user) || has_support_permission?(SUPPORT_PERMISSION_PACK_MANAGEMENT)
      version_cis = Cms::Ci.all(:params => {:nsPath       => '/public',
                                            :ciClassName  => 'mgmt.Version',
                                            :recursive    => true,
                                            :includeAltNs => ORG_VISIBILITY_ALT_NS_TAG})
    else
      version_cis = Cms::Ci.all(:params => {:nsPath       => '/public',
                                            :ciClassName  => 'mgmt.Version',
                                            :recursive    => true,
                                            :attr         => 'enabled:neq:false',
                                            :includeAltNs => ORG_VISIBILITY_ALT_NS_TAG})
      version_cis += Cms::Ci.all(:params => {:nsPath       => '/public',
                                             :ciClassName  => 'mgmt.Version',
                                             :recursive    => true,
                                             :attr         => 'enabled:eq:false',
                                             :altNsTag     => ORG_VISIBILITY_ALT_NS_TAG,
                                             :altNs        => organization_ns_path})
    end

    respond_to do |format|
      format.html { redirect_to catalog_path(:anchor => 'packs') }
      format.js do
        pack_map = pack_cis.to_map {|p| "#{p.nsPath}/#{p.ciName}"}

        @packs = version_cis.inject({}) do |h, version|
          h[version.nsPath] ||= {:pack => pack_map[version.nsPath], :versions => []}
          h[version.nsPath][:versions] << version
          h
        end
        @packs = @packs.values
      end

      format.json do
        source_pack_map = pack_cis.inject({}) do |m, pack|
          root, public, source = pack.nsPath.split('/')
          (m[source] ||= []) << pack
          m
        end

        version_map = version_cis.inject({}) do |m, version|
          (m[version.nsPath] ||= []) << version
          m
        end

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

  def show
    respond_to do |format|
      format.js do
        @stats = Search::Pack.count_stats(params[:source], params[:pack], params[:version]) if @version
      end

      format.json do
        @version.pack = @pack
        render_json_ci_response(@version.present?, @version)
      end
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
      format.js
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
        flash.now[:notice] = 'Updated pack admin password.'
      end
    else
      @pack.errors.add(:base, 'Invalid pack admin password.')
    end

    respond_to do |format|
      format.js {render :action => :visibility}
      format.json { render_json_ci_response(ok, @pack) }
    end
  end

  def check_pack_owner_group_membership?(user = current_user)
    auth_group = Settings.pack_management_auth
    # 'pack_management_auth' is assumed to the name of the user group whose memebers are allowed to manage pack visibility.
    auth_group.present? && user.in_group?(auth_group)
  end


  private

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
end
