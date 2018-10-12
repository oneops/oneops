class ApplicationController < ActionController::Base
  include ::GlobalAdmin

  GRAPHVIZ_IMG_STUB = '/images/cms/graphviz.png'
  CI_IMG_STUB       = '/images/cms/ci_stub.png'

  before_filter :set_no_cache

  if Settings.allow_cors
    before_filter :cors_check, :only => [:cors_options]
    before_filter :cors_headers, :except => [:cors_options]
  end

  rescue_from DeviseLdapAuthenticatable::LdapException do |exception|
    render :text => exception, :status => 500
  end

  protect_from_forgery

  before_action :configure_permitted_parameters, :if => :devise_controller?
  before_filter :authenticate_user_from_token
  before_filter :authenticate_user!
  before_filter :check_username
  before_filter :check_reset_password unless Settings.authentication == 'ldap'
  before_filter :check_eula if Settings.eula
  before_filter :check_organization
  before_filter :set_active_resource_headers, :set_cms_data_consistency

  after_filter :process_flash_messages

  class UnauthorizedException < Exception
  end

  rescue_from(Exception, :with => :handle_generic_exception)
  rescue_from(UnauthorizedException, :with => :handle_unauthorized)
  rescue_from(Cms::Ci::NotFoundException, :with => :handle_ci_not_found)

  helper_method :in_catalog?, :in_design?, :in_transition?, :in_operations?,
                :is_admin?, :creates_clouds?, :manages_cloud?, :creates_assemblies?, :manages_assembly?,
                :has_org_scope?, :dto_allowed?, :locate_assemblies,
                :has_design?, :has_transition?, :has_operations?,
                :has_cloud_services?, :has_cloud_compliance?, :has_cloud_support?,
                :manages_org?, :manages_admins?, :manages_team_members?, :allowed_to_settle_approval?,
                :path_to_ci, :path_to_ci!, :path_to_ns, :path_to_ns!, :path_to_release, :path_to_deployment,
                :ci_image_url, :ci_class_image_url, :platform_image_url, :pack_image_url,
                :graphvis_sub_ci_remote_images, :packs_info, :pack_versions, :design_platform_ns_path,
                :bom_platform_ns_path, :has_support_permission?, :organization_ns_path, :check_pack_owner_group_membership?,
                :semver_sort

  AR_CLASSES_WITH_HEADERS = [Cms::Ci, Cms::DjCi, Cms::Relation, Cms::DjRelation, Cms::RfcCi, Cms::RfcRelation,
                             Cms::Release, Cms::ReleaseBom, Cms::Procedure, Transistor,
                             Cms::Deployment, Cms::DeploymentCi, Cms::DeploymentRecord,
                             Cms::DeploymentRelation, Cms::DeploymentApproval]

  def in_catalog?
    self.class.name.include?('Catalog')
  end

  def in_design?
    self.class.name.include?('Design')
  end

  def in_transition?
    self.class.name.include?('Transition')
  end

  def in_operations?
    self.class.name.include?('Operations')
  end

  def cors_options
    render :nothing => true, :status => :ok
  end


  protected

  def ci_resource
    # Should be overwritten by subclasses.
    raise Exception.new("Controller #{self.class.name} did not define target resource.")
  end

  def locate_org(identifier)
    (is_global_admin? ? Organization : current_user.organizations).
      where("organizations.#{identifier.to_s =~ /\D/ ? 'name' : 'id'}" => identifier).first
  end

  def locate_proxy(qualifier, ns_path)
    find_params = {'ci_proxies.ns_path' => ns_path}
    if qualifier =~ /\D/
      # Must be a ciName, look it up by ciName and namespace.
      find_params['ci_proxies.ci_name'] = qualifier
    else
      # All digits, must be a ciId, look it up by ID.
      find_params['ci_proxies.ci_id'] = qualifier
    end
    current_user.organization.ci_proxies.where(find_params).first
  end

  def find_or_create_proxy(ci)
    current_user.organization.ci_proxies.find_or_create_by(:organization_id => current_user.organization_id,
                                                           :ci_id           => ci.ciId,
                                                           :ci_name         => ci.ciName,
                                                           :ns_path         => ci.nsPath,
                                                           :ci_class_name   => ci.ciClassName)
  end

  def search_ns_path
    organization_ns_path
  end

  def custom_log_info
    @exception && {:exception => @exception.to_s, :backtrace => @exception.respond_to?(:backtrace) && @exception.backtrace}
  end

  helper_method :organization_ns_path,
                :assembly_ns_path,
                :design_ns_path,
                :environment_ns_path,
                :environment_manifest_ns_path,
                :environment_bom_ns_path,
                :search_ns_path

  def organization_ns_path(org = current_user.organization.name)
    "/#{org}"
  end

  def services_ns_path
    "#{organization_ns_path}/_services"
  end

  def clouds_ns_path(org = current_user.organization.name)
    "#{organization_ns_path(org)}/_clouds"
  end

  def cloud_ns_path(cloud)
    "#{organization_ns_path}/_clouds/#{cloud.ciName}"
  end

  def cloud_zone_ns_path(zone)
    "#{zone.nsPath}/#{zone.ciName}"
  end

  def cloud_service_ns_path(service)
    "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}"
  end

  def private_catalog_designs_ns_path
    "#{organization_ns_path}/_catalogs"
  end

  def catalog_designs_ns_path
    '/public/packer/catalogs'
  end

  def catalog_design_ns_path(design_ci)
    "#{design_ci.nsPath}/#{design_ci.ciName}"
  end

  def catalog_design_platform_ns_path(design_ci, platform_ci)
    "#{catalog_design_ns_path(design_ci)}/_design/#{platform_ci.ciName}"
  end

  def assembly_ns_path(assembly_ci)
    "#{organization_ns_path}/#{assembly_ci.ciName}"
  end

  def design_ns_path(assmebly_ci)
    "#{organization_ns_path}/#{assmebly_ci.ciName}/_design"
  end

  def environment_ns_path(environment_ci)
    "#{environment_ci.nsPath}/#{environment_ci.ciName}"
  end

  def environment_manifest_ns_path(environment_ci)
    "#{environment_ns_path(environment_ci)}/manifest"
  end

  def environment_bom_ns_path(environment_ci)
    "#{environment_ns_path(environment_ci)}/bom"
  end

  def pack_design_ns_path(source, pack, version)
    "/public/#{source}/packs/#{pack}/#{version}"
  end

  def platform_pack_design_ns_path(platform)
    return platform.nsPath if platform.ciClassName.start_with?('mgmt.')
    platform_attr = platform.ciAttributes
    pack_design_ns_path(platform_attr.source,  platform_attr.pack, platform_attr.version)
  end

  def platform_pack_ns_path(platform)
    ci_class_name = platform.ciClassName
    if ci_class_name.start_with?('mgmt.')
      platform.nsPath
    elsif ci_class_name.end_with?('catalog.Platform')
      platform_pack_design_ns_path(platform)
    else
      platform_pack_transition_ns_path(platform)
    end
  end

  def pack_transition_ns_path(source, pack, version, availability)
    "/public/#{source}/packs/#{pack}/#{version}/#{availability.downcase}"
  end

  def platform_pack_transition_ns_path(platform)
    platform_attr = platform.ciAttributes
    pack_transition_ns_path(platform_attr.source, platform_attr.pack, platform_attr.version, platform.ciAttributes.availability.downcase)
  end

  def design_platform_ns_path(assembly_ci, platform_ci)
    "#{assembly_ns_path(assembly_ci)}/_design/#{platform_ci.ciName}"
  end

  def transition_platform_ns_path(environment_ci, platform_ci)
    "#{environment_manifest_ns_path(environment_ci)}/#{platform_ci.ciName}/#{platform_ci.ciAttributes.major_version}"
  end

  def bom_platform_ns_path(environment_ci, platform_ci)
    "#{environment_bom_ns_path(environment_ci)}/#{platform_ci.ciName}/#{platform_ci.ciAttributes.major_version}"
  end

  def token_ns_path(token_ci)
    "#{token_ci.nsPath}/#{token_ci.ciName}"
  end

  def locate_catalog_design(design_id)
    Cms::Ci.locate(design_id, catalog_designs_ns_path, 'account.Design') ||
      Cms::Ci.locate(design_id, private_catalog_designs_ns_path, 'account.Design')
  end

  def locate_cloud(id)
    cloud = Cms::Ci.locate(id, clouds_ns_path, 'account.Cloud')
    cloud = nil if cloud && cloud.ciClassName != 'account.Cloud'
    return cloud
  end

  def locate_assemblies
    assemblies = Cms::Relation.all(:params => {:targetClassName => 'account.Assembly',
                                               :direction       => 'from',
                                               :ciId            => current_user.organization.cms_id}).map(&:toCi)

    return assemblies if is_admin? || has_org_scope?

    allowed = current_user.organization.ci_proxies.joins(:teams).
      where(:ns_path => organization_ns_path).
      where('teams.id IN (?)', current_user.all_team_ids).pluck(:ci_id).inject({}) do |m, ci_id|
      m[ci_id] = true
      m
    end
    return assemblies.select { |a| allowed[a.ciId] }
  end

  def locate_assembly(id)
    assembly = Cms::Ci.locate(id, organization_ns_path, 'account.Assembly')

    if read_only_request?
      unless is_admin? || has_org_scope? ||
        current_user.organization.ci_proxies.joins(:teams).
          where(:ns_path => organization_ns_path, :ci_id => assembly.ciId).
          where('teams.id IN (?)', current_user.all_team_ids).first
        raise UnauthorizedException.new
      end
    else
      raise UnauthorizedException.new unless dto_allowed?(assembly.ciId)
    end

    return assembly
  end

  # This is default implementation to indicate which request considered read access only.  Overwrite this
  # in controllers to provide custom logic in lieu of default which treats all GET requests as read-only.
  # E.g. some actions may be defined as POST (e.g. due URL length limitation of GET requests) so you can
  # use this method to mark them read-only.
  def read_only_request?
    request.get?
  end

  def locate_environment(id, assembly)
    Cms::Ci.locate(id, assembly_ns_path(assembly), 'manifest.Environment')
  end

  def locate_catalog_design_platform(qualifier, design, opts = {})
    Cms::Ci.locate(qualifier, catalog_design_ns_path(design), 'catalog.Platform', opts)
  end

  def locate_pack_platform(qualifier, source, pack, version, availability = nil, opts = {})
    if availability.blank?
      Cms::Ci.locate(qualifier, pack_design_ns_path(source, pack, version), 'mgmt.catalog.Platform', opts)
    else
      Cms::Ci.locate(qualifier, pack_transition_ns_path(source, pack, version, availability), 'mgmt.manifest.Platform', opts)
    end
  end

  def locate_pack_for_platform(platform)
    attrs = platform.ciAttributes
    locate_pack(attrs.source, attrs.pack)
  end

  def locate_pack(source, pack)
    Cms::Ci.first(:params => {:nsPath       => "/public/#{source}/packs",
                              :ciClassName  => 'mgmt.Pack',
                              :ciName       => pack})
  end

  def locate_pack_versions(source, pack)
    Cms::Ci.all(:params => {:nsPath       => "/public/#{source}/packs/#{pack}",
                            :ciClassName  => 'mgmt.Version',
                            :includeAltNs => Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG})
  end

  def locate_pack_version_for_platform(platform)
    attrs = platform.ciAttributes
    locate_pack_version(attrs.source, attrs.pack, attrs.version)
  end

  def locate_pack_version(source, pack, version)
    Cms::Ci.first(:params => {:nsPath       => "/public/#{source}/packs/#{pack}",
                              :ciClassName  => 'mgmt.Version',
                              :ciName       => version,
                              :includeAltNs => Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG})
  end

  def locate_design_platform(qualifier, assembly, opts = {})
    Cms::DjCi.locate(qualifier, assembly_ns_path(assembly), 'catalog.Platform', opts)
  end

  def locate_manifest_platform(qualifier, environment, opts = {})
    dj    = opts.delete(:dj)
    dj    = true if dj.nil?
    clazz = dj ? Cms::DjCi : Cms::Ci
    if qualifier =~ /\D/
      ci_name, version = qualifier.split('!')
      if version.present?
        result = clazz.locate(ci_name,
                              "#{environment_manifest_ns_path(environment)}/#{ci_name}/#{version}",
                              'manifest.Platform',
                              opts)
      else
        result = clazz.locate(ci_name,
                              "#{environment_manifest_ns_path(environment)}/#{ci_name}",
                              'manifest.Platform',
                              opts.merge(:recursive => true)) {|a| a.find {|p| p.ciAttributes.is_active == 'true'}}
      end
    else
      # ciId
      result = clazz.locate(qualifier, nil, nil, opts)
    end
    result
  end

  def locate_ci_in_platform_ns(qualifier, platform, ci_class_name = nil, opts = {}, &block)
    ns_path = (in_design? || in_catalog?) ? "#{platform.nsPath}/_design/#{platform.ciName}" : platform.nsPath
    ci = Cms::DjCi.locate(qualifier, ns_path, ci_class_name, opts, &block)
    ci.add_policy_locations(platform_pack_ns_path(platform))
    return ci
  end

  def locate_component_in_manifest_ns(component_id, platform, class_name = nil, opts = {})
    locate_ci_in_platform_ns(component_id, platform, class_name, opts) do |results|
      results.find { |ci| !%w(Platform Attachment Monitor Localvar).include?(ci.ciClassName.split('.').last) }
    end
  end

  def locate_cloud_service_template(service)
    service_class_name = service.ciClassName
    if service_class_name.start_with?('cloud.zone.service')
      Cms::Ci.build(:ciClassName => service_class_name)
    else
      Cms::Ci.first(:params => {:nsPath      => '/public/',
                                :recursive   => true,
                                :ciClassName => "mgmt.#{service_class_name}",
                                :ciName      => service.ciName})

    end
  end

  def calculate_attr_diff(target, base)
    attributes_key = target.is_a?(Cms::Ci) ? :ciAttributes : :relationAttributes
    base_attrs     = base ? base.send(attributes_key).attributes : {}
    attrs          = target.send(attributes_key).attributes
    target.meta.attributes[:mdAttributes].inject([]) do |diff, a|
      attr_name       = a.attributeName
      attr_value      = attrs[attr_name]
      pack_attr_value = base_attrs[attr_name]
      unless attr_value == pack_attr_value || ((attr_value.nil? || attr_value.empty?) && (pack_attr_value.nil? || pack_attr_value.empty?))
        diff << {:attribute => attr_name, :value => attr_value, :base_value => pack_attr_value}
      end
      diff
    end
  end

  def execute(model, operation, *args)
    execute_nested(model, model, operation, *args)
  end

  def execute_nested(parent_model, model, operation, *args)
    begin
      result = model.send(operation, *args)
    rescue Exception => e
      handle_error e, parent_model
      result = false
    end
    model.errors.full_messages.each {|e| parent_model.errors.add(:base, e)} unless parent_model == model
    return result
  end

  def handle_error(exception, record)
    message = exception.message
    body = exception.respond_to?(:response) ? exception.response.body : nil
    if body
      begin
        message_hash = ActiveSupport::JSON.decode(body)
        message = "#{message_hash['message'].presence || exception.message} (#{message_hash['code']})"
      rescue Exception => e
        message = 'unknown reason.'
      end
    end
    record.errors.add(:base, message.size > 300 ? "#{message[0...300]} ..." : message)
    logger.error '-------------- cms EXCEPTION --------------'
    logger.error(body) if body
    logger.error(exception)
    logger.error(exception.backtrace.join("\n\t"))
    logger.error '-------------------------------------------'
  end

  def handle_generic_exception(exception)
    if exception.is_a?(SocketError) || exception.is_a?(Errno::ECONNREFUSED)
      redirect_to error_url(:message => 'CMS is not accessible.')
      return
    end

    @exception = exception

    redirect = Rails.application.config.consider_all_requests_local
    if redirect
      raise(exception)
    else
      logger.error '-------------- EXCEPTION --------------'
      logger.error(exception)
      logger.error(exception.backtrace.join("\n\t"))
      logger.error '-------------------------------------------'

      if request.xhr?
        render :js => '', :status => :internal_server_error
      elsif request.format == :html
        redirect_to (user_signed_in? ? error_redirect_path : new_user_session_path)
        # redirect_to (user_signed_in? ? '/500' : new_user_session_path)
      elsif request.format == :json
        render :json => {:code => 500, :exception => exception.to_s}, :status => :internal_server_error
      else
        render :status => :internal_server_error
      end
    end
  end

  def set_no_cache
    response.headers['Cache-Control'] = 'no-cache, no-store, max-age=0, must-revalidate'
    response.headers['Pragma']        = 'no-cache'
    response.headers['Expires']       = 'Thu, 01 Jan 1970 00:00:00 GMT'
  end

  def cors_check
    cors_headers
    render :nothing => true, :status => :ok
  end

  def cors_headers
    headers['Access-Control-Allow-Origin']  = '*'
    headers['Access-Control-Allow-Methods'] = 'POST, GET, PUT, DELETE, OPTIONS'
    headers['Access-Control-Allow-Headers'] = 'Origin, X-Requested-With, Token, Authorization, Content-Type, Accept'
    headers['Access-Control-Max-Age']       = '86400'
    # headers['Access-Control-Max-Age']       = '1'
  end

  def configure_permitted_parameters
    devise_parameter_sanitizer.for(:sign_up) { |u| u.permit(:username, :email, :password, :password_confirmation, :remember_me, :show_wizard, :name) }
    devise_parameter_sanitizer.for(:sign_in) { |u| u.permit(:username, :email, :password, :password_confirmation, :remember_me, :show_wizard, :name) }
    devise_parameter_sanitizer.for(:account_update) { |u| u.permit(:username, :email, :password, :password_confirmation, :current_password, :show_wizard, :name) }
  end

  def authenticate_user_from_token
    return unless request.authorization.present? && request.authorization.split(' ', 2).first == 'Basic'
    @auth_token, _ = Base64.decode64(request.authorization.split(' ', 2).last || '').split(/:/, 2)
    user = @auth_token.present? && User.where(:authentication_token => @auth_token.to_s).first

    if user
      # do not update user record with "trackable" stats (i.e. sign_in_count, last_sign_in_at, etc...) for API requests.
      request.env["devise.skip_trackable"] = true
      # Passing in store => false, so the user is not actually stored in the session and a token is needed for every request.
      sign_in(user, store: false)
      request.env["devise.skip_trackable"] = false
      # However, Let update user's sign_in timestamps periodically - so API only accounts will show as "active".
      now = Time.now
      user.update_attributes(:current_sign_in_at => now, :last_sign_in_at => now) if now - user.current_sign_in_at > 24.hours
    end
  end

  def check_username
    return if request.authorization.present?

    if user_signed_in?
      if current_user.username != session[:username]
        logger.error "Current username '#{current_user.username}' doesn't match session: #{session.inspect}"
        sign_out
        message = 'Please verify your identity by signing in.'
        flash.now[:alert] = message
        respond_to do |format|
          format.html { redirect_to new_user_session_url}
          format.js   { render :js => "window.location = '#{new_user_session_url}'" }
          format.json { render :json => {:errors => [message]}, :status => :unauthorized }
        end
        return
      end

      # Limit to only one GUI session per user.
      unless request.format == :json || session[:token] == current_user.session_token
        sign_out
        message = 'You are already logged in somewhere else. Please start new session by signing in if you would like to continue here.'
        if request.xhr?
          redirect_to new_user_session_path(:message => message)
        else
          flash[:error] = message
          redirect_to new_user_session_path, :status => :see_other
        end
      end
    end
  end

  def check_reset_password
    if user_signed_in?
      user = current_user
      if user.reset_password_token?
        sign_out
        flash[:notice] = 'Please reset your password.'
        redirect_to edit_password_url(user, :reset_password_token => user.reset_password_token), :status => :see_other
      end
    end
  end

  def set_active_resource_headers(enable = true)
    if enable && user_signed_in?
      cms_user = current_user.username
      AR_CLASSES_WITH_HEADERS.each do |clazz|
        clazz.headers['X-Cms-User']   = cms_user
        clazz.headers['X-Cms-Client'] = ENV['RAILS_ENV']
        clazz.headers['X-Cms-Scope']  = organization_ns_path if current_user.organization
      end
    else
      AR_CLASSES_WITH_HEADERS.each do |clazz|
        clazz.headers.delete('X-Cms-User')
        clazz.headers.delete('X-Cms-Client')
        clazz.headers.delete('X-Cms-Scope')
      end
    end
  end

  def clear_active_resource_headers
    set_active_resource_headers(false)
  end

  def set_cms_data_consistency(value = nil, *classes)
    (classes.presence || AR_CLASSES_WITH_HEADERS).each do |clazz|
      if value.blank?
        clazz.headers.delete('X-Cms-Data-Consistency')
      else
        clazz.headers['X-Cms-Data-Consistency'] = value
      end
    end
    if value.present? && Rails.env.shared?
      Rails.logger.info '========================= '
      Rails.logger.info "== X-Cms-Data-Consistency #{value.blank? ? 'default' : "#{value} for: #{classes.present? ? classes.map(&:name).join(', ') : 'all'}"}"
      Rails.logger.info '========================= '
    end
  end

  def weak_ci_relation_data_consistency
    set_cms_data_consistency('weak', Cms::Ci, Cms::Relation)
  end

  def check_eula
    return unless user_signed_in? && current_user.eula_accepted_at.blank?
    respond_to do |format|
      format.html { redirect_to show_eula_account_profile_path}
      format.js   { render :js => "window.location = '#{show_eula_account_profile_path}'" }
      format.json { render :json => {:errors => ['EULA not accepted']}, :status => :unauthorized }
    end
  end

  def check_organization
    if user_signed_in?
      org_name = params[:org_name]
      if org_name.present? && !(current_user.organization && current_user.organization.name == org_name)
        org = locate_org(org_name)
        if org
          current_user.change_organization(org)
        else
          org = Organization.where('organizations.name' => org_name).first
          if org
            unauthorized("You are not part of organization '#{org_name}'.", organization_public_profile_path(:org_name => org_name))
          else
            redirect_to not_found_path
          end
        end
      end
    end
  end

  include ActionView::Helpers::JavaScriptHelper
  def process_flash_messages
    return unless request.xhr?

    response.body += ";flash('#{escape_javascript(flash[:notice])}', '#{escape_javascript(flash[:error])}', '#{escape_javascript(flash[:alert])}');" if flash[:notice].present? || flash[:error].present? || flash[:alert].present?
    flash.discard
  end

  def rfc_action_to_color(action = '')
    case action
      when 'add'
        '#468847'
      when 'replace'
        '#468847'
      when 'update'
        '#F89406'
      when 'delete'
        '#B94A48'
      else
        '#777777'
    end
  end

  def platforms_diagram(platforms, links_to, path, size = nil)
    graph = GraphViz::new(:G)
    graph[:truecolor   => true,
          :rankdir     => 'TB',
          :ratio       => 'fill',
          :size        => size || '6,3',
          :compound    => true,
          :concentrate => true,
          :bgcolor     => 'transparent']

    graph.node[{:fontsize  => 8,
                :fontname  => 'ArialMT',
                :color     => 'black',
                :fontcolor => 'black',
                :fillcolor => 'whitesmoke',
                :fixedsize => true,
                :width     => '2.50',
                :height    => '0.66',
                :style     => 'rounded,filled'}]

    platforms.each do |r|
      platform = r.toCi
      attrs  = platform.ciAttributes
      label  = "<<table border='0' cellspacing='2' fixedsize='true' width='175' height='44'>"
      label << "<tr><td fixedsize='true' rowspan='2' cellpadding='4' width='40' height='36' align='center'>"
      label << "<img scale='both' src='#{GRAPHVIZ_IMG_STUB}'></img></td>"
      label << "<td align='left' cellpadding='0'><font point-size='12'><b>#{platform.ciName.truncate(18)}</b></font></td></tr>"
      label << "<tr><td align='left' cellpadding='0'><font point-size='10'>version #{"#{attrs.major_version}"}</font></td></tr>"
      label << "</table>>"

      graph.add_node(platform.ciId.to_s,
                     :target    => '_parent',
                     :URL       => "#{path}/platforms/#{platform.ciId}",
                     :tooltip   => "#{attrs.source}/#{attrs.pack}/#{attrs.version}",
                     :label     => label,
                     :shape     => "#{'double' if platform.ciAttributes.try(:availability) == 'redundant'}octagon",
                     :color     => rfc_action_to_color(platform.try(:rfcAction)),
                     :fillcolor => platform.ciAttributes.try(:is_active) == 'false' ? '#CCCCCC' : r.relationAttributes.try(:enabled) == 'false' ? '#FFDDDD' : 'white')
    end

    links_to.each { |r| graph.add_edges(r.fromCiId.to_s,
                                        r.toCiId.to_s,
                                        :color => rfc_action_to_color(r.try(:rfcAction))) }

    return graph
  end

  def packs_info(org = current_user.organization.name)
    pack_versions = {}
    packs         = {}
    pack_sources  = Cms::Ci.all(:params => {:nsPath => '/public', :ciClassName => 'mgmt.Source'}).map(&:ciName)

    pack_map = Cms::Ci.all(:params => {:nsPath      => '/public',
                                       :ciClassName => 'mgmt.Pack',
                                       :recursive   => true}).inject({}) do |m, c|
      root, public, source = c.nsPath.split('/')
      m[source] ||= []
      m[source] << c
      m
    end
    versions = Cms::Ci.all(:params => {:nsPath      => '/public',
                                       :ciClassName => 'mgmt.Version',
                                       :recursive   => true,
                                       :attr        => 'enabled:neq:false'})
    versions += Cms::Ci.all(:params => {:nsPath      => '/public',
                                        :ciClassName => 'mgmt.Version',
                                        :recursive   => true,
                                        :attr        => 'enabled:eq:false',
                                        :altNsTag    => Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG,
                                        :altNs       => organization_ns_path(org)})
    version_map = versions.inject({}) do |m, version|
      m[version.nsPath] ||= []
      m[version.nsPath] << version.ciName
      m
    end
    pack_sources.each do |source|
      pack_versions[source] = {}
      packs[source] = (pack_map[source] || []).inject({}) do |ch, pack|
        versions = version_map["#{pack.nsPath}/#{pack.ciName}"]
        if versions.present?
          pack_versions[source][pack.ciName] = semver_sort(versions)
          category = pack.ciAttributes.category
          ch[category] = [] unless ch.include?(category)
          ch[category] << pack.ciName
        end
        ch
      end
    end
    packs = packs.inject({}) do |m, source_packs|
      m[source_packs.first] = source_packs.last.to_a.sort_by {|e| e.first}
      m
    end
    return pack_sources, pack_versions, packs
  end

  # Returns only pack versions enabled for current org.
  def pack_versions(source, pack_name, major_version = nil)
    pack_ns_path = "/public/#{source}/packs/#{pack_name}"
    versions = Cms::Ci.all(:params => {:nsPath       => pack_ns_path,
                                        :ciClassName  => 'mgmt.Version',
                                        :recursive    => true,
                                        :attr         => 'enabled:neq:false',
                                        :includeAltNs => Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG})
    versions += Cms::Ci.all(:params => {:nsPath      => pack_ns_path,
                                         :ciClassName => 'mgmt.Version',
                                         :recursive   => true,
                                         :attr        => 'enabled:eq:false',
                                         :altNsTag    => Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG,
                                         :altNs       => organization_ns_path})
    versions = versions.select {|v| v.ciName == major_version || v.ciName.start_with?("#{major_version}.")} if major_version.present?
    semver_sort(versions)
  end

  def check_pack_owner_group_membership?(user = current_user)
    auth_group = Settings.pack_management_auth
    # 'pack_management_auth' is assumed to the name (or comas separated list of names) of the user group whose members
    # are allowed to manage pack visibility.
    auth_group.present? && user.in_group?(auth_group)
  end

  def render_json_ci_response(ok, ci, errors = nil, status = nil)
    errors = [errors] if errors.present? && !errors.is_a?(Array)
    if ok && ci.present?
      render :json => ci, :status => :ok
    elsif ci
      render :json => {:errors => errors || ci.errors.full_messages}, :status => status || :unprocessable_entity
    else
      render :json => {:errors => errors || ['not found']}, :status => status || :not_found
    end
  end

  def render_json_cis_response(cis, errors = nil, status = nil)
    if errors.blank? && cis
      render :json => cis, :status => :ok
    elsif
      render :json => {:errors => (errors.present? && !errors.is_a?(Array) ? [errors] : errors)}, :status => status || :unprocessable_entity
    end
  end

  def default_url_options(options = {})
    (user_signed_in? && current_user.organization) ? {:org_name => current_user.organization.name} : {}
  end

  def unauthorized(message = 'Unauthorized access!', redirect_path = root_path)
    respond_to do |format|
      format.html {redirect_to redirect_path, :alert => message}
      format.js   {render :js => %($j('.modal').modal('hide'); flash(null, "#{escape_javascript(message)}"))}
      format.json {render :json => {:errors => [message]}, :status => :unauthorized}
    end
  end

  def handle_unauthorized(exception)
    unauthorized
  end

  def handle_ci_not_found(exception)
    not_found(exception.message)
  end

  def not_found(message)
    Rails.logger.warn "CI not found: #{message}"
    respond_to do |format|
      format.html {redirect_to not_found_url}
      format.js   {render :status => :not_found}
      format.json {render :json => {:errors => [message]}, :status => :not_found}
    end
  end

  def is_admin?(org = nil)
    current_user.is_admin?(org)
  end

  def has_org_scope?(org = nil)
    current_user.has_org_scope?(org)
  end

  def creates_clouds?(org_id = nil)
    current_user.creates_clouds?(org_id)
  end

  def manages_cloud?(cloud_id)
    current_user.manages_cloud?(cloud_id)
  end

  def creates_assemblies?(org_id = nil)
    current_user.creates_assemblies?(org_id)
  end

  def manages_assembly?(assembly_id)
    current_user.manages_assembly?(assembly_id)
  end

  def has_design?(assembly_id = @assembly.try(:ciId))
    current_user.has_design?(assembly_id)
  end

  def has_transition?(assembly_id = @assembly.try(:ciId))
    current_user.has_transition?(assembly_id)
  end

  def has_operations?(assembly_id = @assembly.try(:ciId))
    current_user.has_operations?(assembly_id)
  end

  def has_cloud_services?(cloud_id = @cloud.try(:ciId))
    current_user.has_cloud_services?(cloud_id)
  end

  def has_cloud_compliance?(cloud_id = @cloud.try(:ciId))
    current_user.has_cloud_compliance?(cloud_id)
  end

  def has_cloud_support?(cloud_id = @cloud.try(:ciId))
    current_user.has_cloud_support?(cloud_id)
  end

  def manages_admins?
    current_user.manages_admins?
  end

  def manages_team_members?(team)
    current_user.manages_team_members?(team)
  end

  def manages_org?(org = nil)
    User.global_admin_mode? ? is_global_admin? : is_admin?(org)
  end

  def allowed_to_settle_approval?(approval)
    # SUPPORT_PERMISSION_CLOUD_SUPPORT_MANAGEMENT allows to designate users which can settle approvals
    # in general. And then "has_cloud_support?" allows to further "fine tune" it on cloud by cloud basis.
    if support_auth_config[Cloud::SupportsController::SUPPORT_PERMISSION_CLOUD_SUPPORT_MANAGEMENT].present? &&
       !has_support_permission?(Cloud::SupportsController::SUPPORT_PERMISSION_CLOUD_SUPPORT_MANAGEMENT, true)
      return false
    end

    govern_ci  = approval.govern_ci
    cloud      = govern_ci.nsPath.split('/')[3]
    class_name = govern_ci.ciClassName
    return is_admin? || (class_name == 'cloud.Support' ? has_cloud_support?(cloud) : has_cloud_compliance?(cloud))
  end

  def authorize_admin
    unauthorized unless is_admin?
  end

  def dto_allowed?(assembly_id = nil)
    return false unless user_signed_in?

    if in_design?
      return current_user.has_design?(assembly_id || @assembly.try(:ciId))
    elsif in_transition?
      current_user.has_transition?(assembly_id || @assembly.try(:ciId))
    elsif in_operations?
      current_user.has_operations?(assembly_id || @assembly.try(:ciId))
    else
      return true
    end
  end

  def path_to_ci!(ci, dto_area = nil)
    begin
      return path_to_ci(ci, dto_area)
    rescue Exception => e
      return nil
    end
  end

  def path_to_ci(ci, dto_area = nil)
    ns_path    = ci.nsPath
    class_name = ci.ciClassName
    name       = ci.ciName
    ci_id      = ci.ciId
    if ns_path.include?('/_clouds')
      root, org, _clouds, cloud, zone_or_service_class, service_name = ns_path.split('/')
      if cloud.present?
        if class_name == 'account.Cloudvar'
          return edit_cloud_path(:org_name  => org,
                                 :id        => cloud,
                                 :anchor    => "variables/list_item/#{ci_id}")
        elsif class_name == 'cloud.Zone'
          return edit_cloud_path(:org_name => org,
                                 :id       => cloud,
                                 :anchor   => "zones/list_item/#{ci_id}")
        elsif class_name.start_with?('cloud.service.')
          return edit_cloud_service_path(:org_name => org,
                                         :cloud_id  => cloud,
                                         :id       => ci_id)
        elsif class_name.start_with?('cloud.zone.service.')
          return edit_cloud_service_path(:org_name => org,
                                         :cloud_id => cloud,
                                         :zone_id  => zone_or_service_class,
                                         :id       => ci_id)
        elsif class_name.start_with?('cloud.compliance.')
          return edit_cloud_path(:org_name => org,
                                 :id       => cloud,
                                 :anchor   => "compliance/list_item/#{ci_id}")
        elsif class_name == 'cloud.Support'
          return edit_cloud_path(:org_name => org,
                                 :id       => cloud,
                                 :anchor   => "support/list_item/#{ci_id}")
        elsif class_name == 'cloud.Offering'
          return edit_cloud_service_path(:org_name => org,
                                         :cloud_id => cloud,
                                         :id       => service_name,
                                         :anchor   => "offerings/list_item/#{ci_id}")
        else
          return edit_cloud_service_path(:org_name => org, :cloud_id => cloud, :id => name)
        end
      elsif class_name == 'account.Cloud'
        return edit_cloud_path(ci_id)
      else
        return clouds_path(:org_name => org)
      end
    elsif class_name == 'account.Design'
      root, org, _catalog = ns_path.split('/')
      return catalog_design_path(:org_name => org, :id => ci_id)
    elsif class_name.start_with?('catalog.')
      if ns_path.include?('/_catalogs/')
        root, org, _catalogs, design, _design, platform = ns_path.split('/')
        if platform.present?
          if class_name == 'catalog.Monitor'
            return catalog_design_platform_monitor_path(:org_name    => org,
                                                        :design_id   => design,
                                                        :platform_id => platform,
                                                        :id          => ci_id)
          else
            return catalog_design_platform_component_path(:org_name    => org,
                                                          :design_id   => design,
                                                          :platform_id => platform,
                                                          :id          => name)
          end
        elsif class_name == 'catalog.Platform'
          return catalog_design_platform_path(:org_name => org, :design_id => design, :id => name)
        else
          return catalog_design_path(:org_name => org, :id => design)
        end
      else
        root, org, assembly, _design, platform = ns_path.split('/')
        if platform.present?
          if class_name == 'catalog.Localvar'
            return assembly_design_platform_path(:org_name    => org,
                                                 :assembly_id => assembly,
                                                 :id          => platform,
                                                 :anchor      => "variables/list_item/#{ci_id}")
          elsif class_name == 'catalog.Attachment'
            return assembly_design_platform_attachment_path(:org_name    => org,
                                                            :assembly_id => assembly,
                                                            :platform_id => platform,
                                                            :id          => ci_id)
          elsif class_name == 'catalog.Monitor'
            return assembly_design_platform_monitor_path(:org_name    => org,
                                                         :assembly_id => assembly,
                                                         :platform_id => platform,
                                                         :id          => ci_id)
          else
            return edit_assembly_design_platform_component_path(:org_name => org, :assembly_id => assembly, :platform_id => platform, :id => ci_id)
          end
        else
          if class_name == 'catalog.Platform'
            return assembly_design_platform_path(:org_name => org, :assembly_id => assembly, :id => name)
          elsif class_name == 'catalog.Globalvar'
            return assembly_design_path(:org_name    => org,
                                        :assembly_id => assembly,
                                        :anchor      => "variables/list_item/#{ci_id}")
          else
            return assembly_design_path(:org_name => org, :assembly_id => assembly)
          end
        end
      end
    elsif class_name.start_with?('manifest.')
      root, org, assembly, env, manifest, platform, platform_version = ns_path.split('/')
      if platform.present? && platform_version.present?
        if class_name == 'manifest.Platform'
          if dto_area == 'design'
            return assembly_design_platform_path(:org_name    => org,
                                                 :assembly_id => assembly,
                                                 :id          => name)
          elsif dto_area == 'operations'
            return assembly_operations_environment_platform_path(:org_name       => org,
                                                                 :assembly_id    => assembly,
                                                                 :environment_id => env,
                                                                 :id             => "#{platform}!#{platform_version}")
          else
            return assembly_transition_environment_platform_path(:org_name       => org,
                                                                 :assembly_id    => assembly,
                                                                 :environment_id => env,
                                                                 :id             => "#{platform}!#{platform_version}")
          end
        elsif class_name == 'manifest.Localvar'
          return assembly_transition_environment_platform_path(:org_name       => org,
                                                               :assembly_id    => assembly,
                                                               :environment_id => env,
                                                               :id             => "#{platform}!#{platform_version}",
                                                               :anchor         => "variables/list_item/#{ci_id}")
        elsif class_name == 'manifest.Attachment'
          return assembly_transition_environment_platform_attachment_path(:org_name       => org,
                                                                          :assembly_id    => assembly,
                                                                          :environment_id => env,
                                                                          :platform_id    => "#{platform}!#{platform_version}",
                                                                          :id             => ci_id)
        elsif class_name == 'manifest.Monitor'
          return assembly_transition_environment_platform_monitor_path(:org_name       => org,
                                                                       :assembly_id    => assembly,
                                                                       :environment_id => env,
                                                                       :platform_id    => "#{platform}!#{platform_version}",
                                                                       :id             => ci_id)
        else
          if dto_area == 'design'
            return edit_assembly_design_platform_component_path(:org_name    => org,
                                                                :assembly_id => assembly,
                                                                :platform_id => platform,
                                                                :id          => name,
                                                                :class_name  => "catalog.#{class_name.split('.', 2).last}")
          elsif dto_area == 'operations'
            return assembly_operations_environment_platform_component_path(:org_name       => org,
                                                                           :assembly_id    => assembly,
                                                                           :environment_id => env,
                                                                           :platform_id    => "#{platform}!#{platform_version}",
                                                                           :id             => ci_id)
          else
            return edit_assembly_transition_environment_platform_component_path(:org_name       => org,
                                                                                :assembly_id    => assembly,
                                                                                :environment_id => env,
                                                                                :platform_id    => "#{platform}!#{platform_version}",
                                                                                :id             => ci_id)
          end
        end
      else
        if class_name == 'manifest.Globalvar'
          return assembly_transition_environment_path(:org_name       => org,
                                                      :assembly_id    => assembly,
                                                      :id             => env,
                                                      :anchor         => "variables/list_item/#{ci_id}")
        elsif class_name.start_with?('manifest.relay')
          return assembly_transition_environment_path(:org_name    => org,
                                                      :assembly_id => assembly,
                                                      :id          => env,
                                                      :anchor      => "relays/list_item/#{ci_id}")
        else
          if dto_area == 'operations'
            return assembly_operations_environment_path(:org_name => org, :assembly_id => assembly, :id => ci_id)
          else
            return assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => ci_id)
          end
        end
      end
    elsif class_name.start_with?('bom.')
      root, org, assembly, env, bom, platform, platform_version = ns_path.split('/')
      if platform.present? && platform_version.present?
        return assembly_operations_environment_platform_instance_path(:org_name       => org,
                                                                      :assembly_id    => assembly,
                                                                      :environment_id => env,
                                                                      :platform_id    => "#{platform}!#{platform_version}",
                                                                      :id             => ci_id)
      else
        return assembly_operations_environment_path(:org_name => org, :assembly_id => assembly, :environment_id => env)
      end
    else
      root, org = ns_path.split('/')
      if class_name == 'account.Cloud'
        return edit_cloud_path(:org_name => org, :id => name)
      elsif class_name == 'account.Assembly'
        return assembly_path(:org_name => org, :id => name)
      elsif class_name == 'account.Environment'
        return edit_organization_path(:org_name => org, :anchor => "environments/list_item/#{ci_id}")
      elsif class_name == 'account.Policy'
        return edit_organization_path(:org_name => org, :anchor => "policies/list_item/#{ci_id}")
      elsif class_name == 'account.Organization'
        return edit_organization_path(:org_name => name)
      elsif class_name.start_with?('account.notification')
        return edit_organization_path(:org_name => org, :anchor => "notifications/list_item/#{ci_id}")
      end
    end
  end

  def path_to_ns!(ns_path, dto_area = nil)
    begin
      path_to_ns(ns_path, dto_area)
    rescue Exception => e
      return nil
    end
  end

  def path_to_ns(ns_path, dto_area = nil)
    if ns_path.include?('/_clouds')
      root, org, _clouds, cloud, zone = ns_path.split('/')
      if zone.present?
        return edit_cloud_path(:org_name => org,
                               :id       => cloud,
                               :anchor   => 'zones')
      elsif cloud.present?
        return edit_cloud_path(:org_name => org, :id => cloud)
      else
        return clouds_path(:org_name => org)
      end
    elsif ns_path.include?('/_services')
      root, org, _clouds, service = ns_path.split('/')
      if service.present?
        return edit_service_path(:org_name => org, :id => service)
      else
        return services_path(:org_name => org)
      end
    elsif ns_path.include?('/_catalogs')
      root, org, _catalogs, design, _design, platform = ns_path.split('/')
      if platform.present?
        return catalog_design_platform_path(:org_name => org, :design_id => design, :id => platform)
      else
        return catalog_design_path(:org_name => org, :design_id => design)
      end
    elsif ns_path.include?('/_design/')
      root, org, assembly, _design, platform = ns_path.split('/')
      if platform.present?
        return assembly_design_platform_path(:org_name => org, :assembly_id => assembly, :id => platform)
      else
        return assembly_design_path(:org_name => org, :assembly_id => assembly)
      end
    elsif ns_path.include?('/manifest/')
      root, org, assembly, env, manifest, platform, platform_version = ns_path.split('/')
      if platform.present? && platform_version.present?
        return assembly_transition_environment_platform_path(:org_name       => org,
                                                             :assembly_id    => assembly,
                                                             :environment_id => env,
                                                             :id             => "#{platform}!#{platform_version}")
      elsif env.present?
        return assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env)
      else
        return assembly_transition_path(:org_name => org, :assembly_id => assembly)
      end
    elsif ns_path =~ (/\/bom(\/|$)/)
      root, org, assembly, env, bom, platform, platform_version = ns_path.split('/')
      if platform.present? && platform_version.present?
        return assembly_operations_environment_platform_path(:org_name       => org,
                                                             :assembly_id    => assembly,
                                                             :environment_id => env,
                                                             :id             => "#{platform}!#{platform_version}")
      elsif env.present?
        return assembly_operations_environment_path(:org_name => org, :assembly_id => assembly, :id => env)
      else
        return assembly_operations_path(:org_name => org, :assembly_id => assembly)
      end
    else
      root, org, assembly, env = ns_path.split('/')
      if env.present?
        if ns_path.end_with?('/bom') || dto_area == 'operations'
          return assembly_operations_environment_path(:org_name => org, :assembly_id => assembly, :id => env)
        else
          return assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env)
        end
      elsif assembly.present?
        return assembly_path(:org_name => org, :id => assembly)
      elsif org.present?
        return organization_path(:org_name => org)
      else
        '#'
      end
    end
  end

  def path_to_release(release)
    root, org, assembly, env, manifest = release.nsPath.split('/')
    if env.present?
      assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env, :anchor => "timeline/timeline_list/release_#{release.releaseId}")
    else
      assembly_design_path(:org_name => org, :assembly_id => assembly, :anchor => "timeline/timeline_list/release_#{release.releaseId}")
    end
  end

  def path_to_deployment(deployment)
    root, org, assembly, env, bom = deployment.nsPath.split('/')
    assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env, :anchor => "timeline/timeline_list/deployment_#{deployment.deploymentId}")
  end


  def set_pagination_response_headers(data)
    add_pagination_response_headers(data.info[:total], data.info[:size], data.info[:offset]) if data
  end

  def add_pagination_response_headers(total_count, page_size, offset)
    response.headers['oneops-list-total-count'] = total_count.to_s
    response.headers['oneops-list-page-size']   = page_size.to_s
    response.headers['oneops-list-offset']      = offset.to_s
  end

  def ci_image_url(ci)
    ci_class_image_url(ci.ciClassName)
  end

  def ci_class_image_url(ci_class_name)
    if Cms::CiMd.look_up!(ci_class_name)
      split = ci_class_name.split('.')
      split = split[1..-1] if split.first == 'mgmt'
      "#{asset_url_prefix}#{split[-[split.size - 1, 3].min..-1].join('.')}/#{split.last}.png"
    else
      CI_IMG_STUB
    end
  end

  def platform_image_url(platform)
    ci_attrs = platform.ciAttributes
    pack_image_url(ci_attrs.source, ci_attrs.pack, ci_attrs.version)
  end

  def pack_image_url(source, pack, version)
    "#{asset_url_prefix}#{source}/packs/#{pack}/#{version}/#{pack}.png"
  end

  def graphvis_sub_ci_remote_images(svg, img_stub = GRAPHVIZ_IMG_STUB)
    svg.scan(/(?<=xlink:title=)"[^"]+\.[^"]+"/).inject(svg) do |r, c|
      r.sub(img_stub, ci_class_image_url(CGI.unescape_html(c[1..-2])))
    end
  end

  def graphvis_sub_pack_remote_images(svg, img_stub = GRAPHVIZ_IMG_STUB)
    svg.scan(/(?<=xlink:title=").+\/.+\/\d+/).inject(svg) do |r, c|
      r.sub(img_stub, pack_image_url(*c.split('/')))
    end
  end

  def asset_url_prefix
    Settings.asset_url.presence || '/cms/'
  end

  def browser_timezone_offset(default = 0)
    session[:browser_timezone] || default
  end

  def convert_json_attrs_from_string(attrs, ci_class_name)
    return attrs if attrs.blank?

    types = %w(array hash struct)
    ci_md = Cms::CiMd.look_up(ci_class_name)
    attrs.each_pair do |k, v|
      if v.present?
        attr_md = ci_md.md_attribute(k)
        if attr_md
          if types.include?(attr_md.dataType)
            begin
              attrs[k] = JSON.parse(v)
            rescue Exception => e
              # Do nothing - leave as string.
            end
          end
        end
      end
    end
  end

  def support_auth_config
    return @support_auth if @support_auth

    @support_auth = {}
    config = Settings.support_auth
    if config.present?
      begin
        @support_auth = JSON.parse(config)
      rescue Exception => e
        # If it is not json, assume "simple" form, i.e. comas separated group names for all permissions
        @support_auth = {'*' => config}
      end
    end
    @support_auth
  end

  def support_permissions
    return @permissions if @permissions

    @permissions = {}
    config = support_auth_config
    if config.present?
      user_groups = current_user.groups.pluck(:name).to_map
      @permissions = config.inject({}) do |h, (perm, groups)|
        ok = (groups.is_a?(Array) ? groups : groups.to_s.split(',')).any? { |g| user_groups[g.strip] }
        h[perm] = ok if ok
        h
      end
    end
    @permissions
  end

  def has_support_permission?(permission, explicit = false)
    return true if is_global_admin?

    permissions = support_permissions
    (!explicit && permissions['*']) || permissions[permission]
  end

  def semver_sort(versions, ascending = false)
    asc = ascending ? 1 : -1
    name = versions.first.respond_to?(:ciName) ? :ciName : :to_s
    versions.sort_by {|v| s = v.send(name).split('.'); asc * (s[0].to_i * 10000000 + s[1].to_i * 10000 + s[2].to_i)}
  end


  def render_csv(data, fields, fields_to_escape = nil)
    delimiter = params[:delimiter].presence || ','
    csv = fields.join(delimiter) << "\n"
    data.each do |o|
      fields_to_escape.each {|f| o[f] = %("#{o[f]}")} if fields_to_escape.present?
      csv << fields.inject([]) {|a, k| a << o[k]}.join(delimiter) << "\n"
    end
    render :text => csv #, :content_type => 'text/data_string'
    end
end
