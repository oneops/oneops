class ApplicationController < ActionController::Base
  GRAPHVIZ_IMG_STUB = '/images/cms/graphviz.png'

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
  before_filter :set_active_resource_headers

  after_filter :process_flash_messages

  class UnauthorizedException < Exception
  end

  class CiNotFoundException < Exception
    attr_accessor :locateId, :nsPath, :ciType

    def initialize(id, ns_path = nil, ci_type = nil)
      super("Could not locate #{ci_type || 'CI'} with id/name '#{id}'#{" in namespace '#{ns_paths}'" if nsPath.present?}.")
    end
  end


  rescue_from(Exception, :with => :handle_generic_exception)
  rescue_from(UnauthorizedException, :with => :handle_unauthorized)
  rescue_from(CiNotFoundException, :with => :handle_ci_not_found)

  helper_method :in_design?, :in_transition?, :in_operations?, :packs_info, :is_admin?,
                :manages_access?, :manages_access_for_cloud?, :manages_access_for_assembly?,
                :has_org_scope?, :dto_allowed?, :locate_assemblies,
                :has_design?, :has_transition?, :has_operations?,
                :has_cloud_services?, :has_cloud_compliance?, :has_cloud_support?, :allowed_to_settle_approval?,
                :path_to_ci, :path_to_ci!, :path_to_ns, :path_to_ns!, :path_to_release, :path_to_deployment,
                :ci_image_url, :ci_class_image_url, :platform_image_url, :graphvis_sub_ci_remote_images

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

  def search
    @source = request.format == params[:source].presence || ('application/json' ? 'cms' : 'es')

    return if request.format == 'text/html'

    min_ns_path     = search_ns_path
    ns_path         = params[:ns_path] || min_ns_path
    ns_path         = min_ns_path unless ns_path.start_with?(min_ns_path)
    class_name      = params[:class_name]
    @search_results = []

    if @source == 'cms' || @source == 'simple'
      # CMS search.
      query_params = {:nsPath => ns_path, :recursive => true}

      rel_name   = params[:relation_name]
      attr_name  = params[:attr_name]
      attr_value = params[:attr_value]

      query_params[:ciClassName] = class_name if class_name.present?
      query_params[rel_name.include?('.') ? :relationName : :relationShortName] = rel_name if rel_name.present?

      if attr_name.present? && attr_value.present?
        attr_operator       = params[:attr_operator] || 'eq'
        attr_value          = "%#{attr_value}%" if attr_operator == 'like'
        query_params[:attr] = "#{attr_name}:#{attr_operator}:#{attr_value}"
      end

      if rel_name.present?
        query_params[:includeFromCi]   = true unless params[:include_from_ci] == 'false'
        query_params[:includeToCi]     = true unless params[:include_to_ci] == 'false'
        class_name                     = params[:from_class_name]
        query_params[:fromClassName]   = class_name if class_name.present?
        class_name                     = params[:to_class_name]
        query_params[:targetClassName] = class_name if class_name.present?
        clazz                          = Cms::Relation
      else
        clazz = Cms::Ci
      end

      @search_results = clazz.all(:params => query_params) if query_params.size > 2
    else
      # ES search.
      query    = params[:query].presence
      max_size = (params[:max_size].presence || 1000).to_i

      if query.present? || class_name.present?
        begin
          search_params                        = {:nsPath => "#{ns_path}/*", :size => max_size}
          search_params[:query]                = {:query => query, :fields => %w(ciAttributes.* ciClassName ciName)} if query.present?
          # search_params[:query]                = query if query.present?
          search_params['ciClassName.keyword'] = class_name if class_name.present?
          @search_results                      = Cms::Ci.search(search_params)
        rescue Exception => e
          @error = e.message
        end
      end
    end

    unless is_admin? || has_org_scope?
      org_ns_path = organization_ns_path
      prefixes = current_user.organization.ci_proxies.where(:ns_path => org_ns_path).joins(:teams).where('teams.id IN (?)', current_user.all_team_ids).pluck(:ci_name).inject([]) do |a, ci_name|
        a << "#{org_ns_path}/#{ci_name}"
      end
      if prefixes.present?
        reg_exp = /^(#{prefixes.join(')|(')})/
        @search_results = @search_results.select {|r| r.nsPath =~ reg_exp}
      else
        @search_results = []
      end
    end

    respond_to do |format|
      format.js   {render 'base/search/search'}
      format.json {render :json => @search_results}
    end
  end


  protected

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

  def scope
    if in_design? || in_catalog?
      'catalog'
    elsif in_transition?
      'manifest'
    elsif in_operations?
      'operations'
    end
  end

  def search_ns_path
    organization_ns_path
  end

  def custom_log_info
    @exception && {:exception => @exception.to_s, :backtrace => @exception.respond_to?(:backtrace) && @exception.backtrace}
  end

  helper_method :organization_ns_path,
                :assembly_ns_path,
                :environment_ns_path,
                :environment_manifest_ns_path,
                :environment_bom_ns_path,
                :search_ns_path

  def organization_ns_path(org = current_user.organization.name)
    "/#{org}"
  end

  def catalogs_ns_path
    '/public/packer/catalogs'
  end

  def services_ns_path
    "#{organization_ns_path}/_services"
  end

  def clouds_ns_path
    "#{organization_ns_path}/_clouds"
  end

  def cloud_ns_path(cloud)
    "#{organization_ns_path}/_clouds/#{cloud.ciName}"
  end

  def cloud_service_ns_path(service)
    "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}"
  end

  def private_catalogs_ns_path
    "#{organization_ns_path}/_catalogs"
  end

  def catalog_ns_path(catalog_ci)
    "#{catalog_ci.nsPath}/#{catalog_ci.ciName}"
  end

  def catalog_platform_ns_path(catalog_ci, platform_ci)
    "#{catalog_ns_path(catalog_ci)}/_design/#{platform_ci.ciName}"
  end

  def assembly_ns_path(assembly_ci)
    "#{organization_ns_path}/#{assembly_ci.ciName}"
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

  def platform_pack_design_ns_path(platform)
    platform_attr = platform.ciAttributes
    return "/public/#{platform_attr.source}/packs/#{platform_attr.pack}/#{platform_attr.version}"
  end

  def platform_pack_transition_ns_path(platform)
    platform_attr = platform.ciAttributes
    return "/public/#{platform_attr.source}/packs/#{platform_attr.pack}/#{platform_attr.version}/#{platform.ciAttributes.availability.downcase}"
  end

  def design_platform_ns_path(assembly_ci, platform_ci)
    "#{assembly_ns_path(assembly_ci)}/_design/#{platform_ci.ciName}"
  end

  def transition_platform_ns_path(environment_ci, platform_ci)
    "#{environment_manifest_ns_path(environment_ci)}/#{platform_ci.ciName}/#{platform_ci.ciAttributes.major_version}"
  end

  def bom_platform_ns_path(environment_ci, platform_ci)
    "#{environment_bom_ns_path(environment_ci)}#{platform_ci.ciName}/#{platform_ci.ciAttributes.major_version}"
  end

  def token_ns_path(token_ci)
    "#{token_ci.nsPath}/#{token_ci.ciName}"
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
    raise CiNotFoundException.new(id, organization_ns_path, 'account.Assembly') unless assembly && assembly.ciClassName == 'account.Assembly'

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
    env = Cms::Ci.locate(id, assembly_ns_path(assembly), 'manifest.Environment')
    raise CiNotFoundException.new(id, organization_ns_path, 'manifest.Environment') unless env && env.ciClassName == 'manifest.Environment'
    return env
  end

  def locate_manifest_platform(qualifier, environment, opts = {})
    dj    = opts.delete(:dj)
    dj    = true if dj.nil?
    clazz = dj ? Cms::DjCi : Cms::Ci
    result = nil
    if qualifier =~ /\D/
      ci_name, version = qualifier.split('!')
      if version.present?
        result = clazz.locate(ci_name, "#{environment_manifest_ns_path(environment)}/#{ci_name}/#{version}", 'manifest.Platform', opts)
      else
        result = clazz.locate(ci_name, "#{environment_manifest_ns_path(environment)}/#{ci_name}", 'manifest.Platform', opts.merge(:recursive => true))
        result = result.find {|p| p.ciAttributes.is_active == 'true'} if result.is_a?(Array)
      end
      raise CiNotFoundException.new(qualifier, environment_manifest_ns_path(environment), 'manifest.Platform') unless result && result.ciClassName == 'manifest.Platform'
    else
      # ciId
      result = clazz.locate(qualifier, nil, nil, opts)
    end
    result
  end

  def locate_cloud_service_template(service)
    Cms::Ci.first(:params => {:nsPath      => '/public/',
                              :recursive   => true,
                              :ciClassName => "mgmt.#{service.ciClassName}",
                              :ciName      => service.ciName})

  end

  def calculate_ci_diff(ci, base_ci)
    base_attrs = base_ci ? base_ci.ciAttributes.attributes : {}
    attrs      = ci.ciAttributes
    diff       = []
    ci.meta.attributes[:mdAttributes].each do |a|
      attr_name       = a.attributeName
      attr_value      = attrs.attributes[attr_name]
      pack_attr_value = base_attrs[attr_name]
      unless attr_value == pack_attr_value || ((attr_value.nil? || attr_value.empty?) && (pack_attr_value.nil? || pack_attr_value.empty?))
        diff << {:attribute => attr_name, :value => attr_value, :base_value => pack_attr_value}
      end
    end
    diff
  end

  def execute(model, operation, *args)
    execute_nested(model, model, operation, *args)
  end

  def execute_nested(parent_model, model, operation, *args)
    begin
      result = model.send(operation, *args)
      model.errors.full_messages.each {|e| parent_model.errors.add(:base, e)} unless parent_model == model
    rescue Exception => e
      handle_error e, parent_model
      result = false
    end
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
    token, foo = Base64.decode64(request.authorization.split(' ', 2).last || '').split(/:/, 2)
    user  = token.present? && User.where(:authentication_token => token.to_s).first

    # We are passing store => false, so the user is not actually stored in the session and a token is needed for every request.
    sign_in(user, store: false) if user
  end

  def check_username
    return if request.authorization.present?

    if user_signed_in?
      if current_user.username != session[:username]
        logger.error "Current username '#{current_user.username}' doesn't match session: #{session.inspect}"
        sign_out
        flash[:warning] = 'Please verify your identity by signing in.'
        redirect_to new_user_session_path
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
          redirect_to new_user_session_path
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
        redirect_to edit_password_url(user, :reset_password_token => user.reset_password_token)
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

  def check_eula
    return unless user_signed_in? && current_user.eula_accepted_at.blank?
    redirect_to show_eula_account_profile_path
  end

  def check_organization
    if user_signed_in?
      if current_user.organization_id.blank?
        redirect_to account_profile_path
        return
      end

      org_name = params[:org_name]
      if org_name.present? && !(current_user.organization && current_user.organization.name == org_name)
        org = current_user.organizations.where('organizations.name' => org_name).first
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

    response.body += ";flash('#{escape_javascript(flash[:notice])}', '#{escape_javascript(flash[:error])}');" if flash[:notice].present? || flash[:error].present?
    flash.discard
  end

  def to_color(action = '')
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
        '#999999'
    end
  end

  def platforms_diagram(platforms, links_to, path)
    nodes = Array.new
    edges = Array.new


    platform_index = platforms.index_by{ |p| p.toCi.ciId }
    platform_group = platforms.group_by{ |p| p.toCi.ciName }

    platform_group.each do |platform,versions|
      active = versions.find { |v| v.toCi.ciAttributes.attributes.has_key?(:is_active) && v.toCi.ciAttributes.is_active == 'false' } || versions.first
      nodes << {:id       => active.toCiId.to_s,
                :name     => platform,
                :target   => "_parent",
                :icon     => GRAPHVIZ_IMG_STUB,
                :tooltip  => platform_image_url(active.toCi),
                :url      => "#{path}/platforms/#{active.toCi.ciId}",
                :footer   => "version #{active.toCi.ciAttributes.major_version}",
                :color    => active.toCi.respond_to?('rfcAction') ? to_color(active.toCi.rfcAction) : 'gray',
                :versions => versions}
    end

    links_to.each { |edge| edges << {:from  => platform_index[edge.fromCiId].toCiId.to_s,
                                     :to    => platform_index[edge.toCiId].toCiId.to_s,
                                     :color => edge.respond_to?('rfcAction') ? to_color(edge.rfcAction) : 'gray'} }
    graph = _diagram(nodes, edges)
    return graph
  end

  def _diagram(nodes,edges,options = {})
    graph = GraphViz::new(:G)
    graph[:truecolor   => true,
          :rankdir     => 'TB',
          :ratio       => 'fill',
          :size        => '6,3',
          :compound    => true,
          :concentrate => true,
          :bgcolor     => 'transparent']

    graph.node[options.merge(:fontsize  => 8,
                             :fontname  => 'ArialMT',
                             :color     => 'black',
                             :fontcolor => 'black',
                             :fillcolor => 'whitesmoke',
                             :fixedsize => true,
                             :width     => '2.50',
                             :height    => '0.66',
                             :shape     => 'ractangle',
                             :style     => 'rounded')]

    nodes.each do |node|
      label = "<<table border='0' cellspacing='2' fixedsize='true' width='175' height='48'>"
      label << "<tr><td fixedsize='true' rowspan='#{node[:versions].count + 1}' cellpadding='4' width='40' height='40' align='center'>"
      label << "<img scale='both' src='#{node[:icon]}'></img></td>"
      label << "<td align='left' cellpadding='0'><font point-size='12'><b>#{node[:name].size > 18 ? "#{node[:name][0..16]}..." : node[:name]}</b></font></td></tr>"
      label << "<tr><td align='left' cellpadding='0'><font point-size='10'>#{node[:footer]}</font></td></tr>"
      label << "</table>>"
      graph.add_node(node[:id],
                      :target  => node[:target],
                      :URL     => node[:url],
                      :tooltip => node[:tooltip],
                      :label   => label,
                      :color   => node[:color])
    end

    edges.each { |edge| graph.add_edges(edge[:from], edge[:to], :color => edge[:color]) }

    return graph
  end

  def packs_info
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
    version_map = Cms::Ci.all(:params => {:nsPath      => '/public',
                                          :ciClassName => 'mgmt.Version',
                                          :recursive   => true}).inject({}) do |m, version|
      enabled = version.ciAttributes.attributes['enabled']
      unless enabled && enabled == 'false'
        m[version.nsPath] ||= []
        m[version.nsPath] << version.ciName
      end
      m
    end
    pack_sources.each do |source|
      pack_versions[source] = {}
      packs[source] = (pack_map[source] || []).inject({}) do |ch, pack|
        versions = version_map["#{pack.nsPath}/#{pack.ciName}"]
        if versions.present?
          pack_versions[source][pack.ciName] = versions.sort.reverse
          category = pack.ciAttributes.category
          ch[category] = [] unless ch.include?(category)
          ch[category] << [pack.ciAttributes.description, pack.ciName]
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

  def render_json_ci_response(ok, ci, errors = nil)
    errors = [errors] if errors.present? && !errors.is_a?(Array)
    if ok && ci.present?
      render :json => ci, :status => :ok
    elsif ci
      render :json => {:errors => errors || ci.errors.full_messages}, :status => :unprocessable_entity
    else
      render :json => {:errors => errors || ['not found']}, :status => :not_found
    end
  end

  def default_url_options(options = {})
    (user_signed_in? && current_user.organization) ? {:org_name => current_user.organization.name} : {}
  end

  def unauthorized(message = 'Unauthorized access!', redirect_path = root_path)
    respond_to do |format|
      format.html { redirect_to redirect_path, :alert => message }
      format.js   { render :js => "$j('.modal').modal('hide'); flash(null, 'Unauthorized access!')" }
      format.json { render :json => '', :status => :unauthorized }
    end
  end

  def handle_unauthorized(exception)
    unauthorized
  end

  def handle_ci_not_found(exception)
    respond_to do |format|
      format.html {redirect_to not_found_url}
      format.js {render :status => :not_found}
      format.json {render :json => {:errors => [exception.message]}, :status => :not_found}
    end
  end

  def is_admin?(org = nil)
    current_user.is_admin?(org)
  end

  def has_org_scope?(org = nil)
    current_user.has_org_scope?(org)
  end

  def manages_access?(org_id = nil)
    current_user.manages_access?(org_id)
  end

  def manages_access_for_cloud?(cloud_id)
    current_user.manages_access_for_cloud?(cloud_id)
  end

  def manages_access_for_assembly?(assembly_id)
    current_user.manages_access_for_assembly?(assembly_id)
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

  def allowed_to_settle_approval?(approval)
    govern_ci  = approval.govern_ci
    cloud      = govern_ci.nsPath.split('/')[3]
    class_name = govern_ci.ciClassName
    return class_name == 'cloud.Support' ? has_cloud_support?(cloud) : has_cloud_compliance?(cloud)
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
      root, org, _clouds, cloud, service_class, service_name = ns_path.split('/')
      if cloud.present?
        if class_name == 'account.Cloudvar'
          return edit_cloud_path(:org_name  => org,
                                 :id        => cloud,
                                 :anchor    => "variables/list_item/#{ci_id}")
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
      return catalog_path(:org_name => org, :id => ci_id)
    elsif class_name.start_with?('catalog.')
      if ns_path.include?('/_catalogs/')
        root, org, _catalogs, catalog, _design, platform = ns_path.split('/')
        if platform.present?
          return catalog_platform_path(:org_name => org, :catalog_id => catalog, :id => platform)
        elsif class_name == 'catalog.Platform'
          return catalog_platform_path(:org_name => org, :catalog_id => catalog, :id => name)
        else
          return catalog_path(:org_name => org, :id => catalog)
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
                                                                :class_name  => class_name.split('.').last)
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
      end
    end
  end

  def path_to_ns!(ns_path)
    begin
      path_to_ns(ns_path)
    rescue Exception => e
      return nil
    end
  end

  def path_to_ns(ns_path)
    if ns_path.include?('/_clouds')
      root, org, _clouds, cloud = ns_path.split('/')
      if cloud.present?
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
      root, org, _catalogs, catalog, _design, platform = ns_path.split('/')
      if platform.present?
        return catalog_platform_path(:org_name => org, :catalog_id => catalog, :id => platform)
      else
        return catalogs_path(:org_name => org)
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
    elsif ns_path.include?('/bom/')
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
        if ns_path.end_with?('/bom')
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
      assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env, :anchor => "releases/release_list/#{release.releaseId}")
    else
      assembly_design_path(:org_name => org, :assembly_id => assembly, :anchor => "releases/release_list/#{release.releaseId}")
    end
  end

  def path_to_deployment(deployment)
    root, org, assembly, env, bom = deployment.nsPath.split('/')
    assembly_transition_environment_path(:org_name => org, :assembly_id => assembly, :id => env, :anchor => "deployments/deployment_list/#{deployment.deploymentId}")
  end


  def set_pagination_response_headers(data)
    return unless data
    response.headers['oneops-list-total-count'] = (data.info[:total] || 0).to_s
    response.headers['oneops-list-page-size']   = (data.info[:size] || 0).to_s
    response.headers['oneops-list-offset']      = (data.info[:offset] || 0).to_s
  end


  def ci_image_url(ci)
    ci_class_image_url(ci.ciClassName)
  end

  def ci_class_image_url(ci_class_name)
    split = ci_class_name.split('.')
    "#{asset_url_prefix}#{split[1..-1].join('.')}/#{split.last}.png"
  end

  def platform_image_url(platform)
    ci_attrs = platform.ciAttributes
    pack = ci_attrs.pack
    "#{asset_url_prefix}public/#{ci_attrs.source}/packs/#{pack}/#{ci_attrs.version}/#{pack}.png"
  end

  def graphvis_sub_ci_remote_images(svg, img_stub = GRAPHVIZ_IMG_STUB)
    svg.scan(/(?<=xlink:title=")\w+\.[\.\w]+/).inject(svg) {|r, c| r.sub(img_stub, ci_class_image_url(c))}
  end

  def graphvis_sub_pack_remote_images(svg, img_stub = GRAPHVIZ_IMG_STUB)
    prefix = Settings.asset_url.present? ? 'http' : '\/cms'
    svg.scan(/(?<=xlink:title=")#{prefix}.*\.png/).inject(svg) do |r, c|
      r.sub(img_stub, c).sub(c, c.split('/')[-3..-2].join('/'))
    end
  end

  def asset_url_prefix
    Settings.asset_url.presence || '/cms/'
  end
end
