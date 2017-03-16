class Catalog::PacksController < ApplicationController
  ORG_VISIBILITY_ALT_NS_TAG = 'enableForOrg'

  before_filter :authorize_pack_owner_group_membership, :only => [:visibility, :password]
  before_filter :find_pack_version, :only => [:show, :visibility, :password]

  helper_method :check_pack_owner_group_membership?


  def index
    pack_cis    = Cms::Ci.all(:params => {:nsPath      => '/public',
                                          :ciClassName => 'mgmt.Pack',
                                          :recursive   => true})
    version_cis = Cms::Ci.all(:params => {:nsPath       => '/public',
                                          :ciClassName  => 'mgmt.Version',
                                          :recursive    => true,
                                          :includeAltNs => ORG_VISIBILITY_ALT_NS_TAG})

    version_map = version_cis.inject({}) do |m, version|
      (m[version.nsPath] ||= []) << version
      m
    end

    respond_to do |format|
      format.html { redirect_to catalog_path(:anchor => 'packs') }
      format.js do
        @packs = pack_cis.inject([]) do |a, pack|
          (version_map["#{pack.nsPath}/#{pack.ciName}"] || []).each { |version| a << {:pack => pack, :version => version} }
          a
        end
      end

      format.json do
        source_pack_map = pack_cis.inject({}) do |m, pack|
          root, public, source = pack.nsPath.split('/')
          (m[source] ||= []) << pack
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
    @pack  = locate_pack(params[:source], params[:pack])

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
    counts = Search::Pack.count_stats(@platform.ciAttributes.source, @platform.ciAttributes.pack, @platform.ciAttributes.version)
    if counts
      render :json => {:count => counts}
    else
      render :json => {:errors => ['Failed to fetch stats.']}, :status => :internal_server_error
    end
  end


  def visibility
    ok = false
    password_digest = Digest::SHA512.hexdigest(params[:password])
    if password_digest == @version.ciAttributes.admin_password_digest
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
    if password_digest == @version.ciAttributes.admin_password_digest
      new_password = params[:new_password]
      if new_password.blank?
        @version.errors.add(:base, 'Invalid password/')
      elsif new_password != params[:confirm_password]
        @version.errors.add(:base, 'Passwords do not match.')
      else
        @version.ciAttributes.admin_password_digest = Digest::SHA512.hexdigest(new_password)
        ok = execute(@version, :save)
        flash.now[:notice] = 'Updated pack admin password.'
      end
    else
      @version.errors.add(:base, 'Invalid pack admin password.')
    end

    respond_to do |format|
      format.js {render :action => :visibility}
      format.json { render_json_ci_response(ok, @version) }
    end
  end

  def check_pack_owner_group_membership?(user = current_user)
    auth_group = Settings.pack_management_auth
    # 'pack_management_auth' is assumed to the name of the user group whose memebers are allowed to manage pack visibility.
    auth_group.present? && user.in_group?(auth_group)
  end


  private

  def find_pack_version
    @version = locate_pack_version(params[:source], params[:pack], params[:version])
  end

  def authorize_pack_owner_group_membership
    unless check_pack_owner_group_membership?
      unauthorized
      return
    end
  end
end
