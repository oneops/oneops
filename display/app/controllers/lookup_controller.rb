class LookupController < ApplicationController
  before_filter :clear_active_resource_headers

  def ci
    ci = nil
    begin
      ci = Cms::DjCi.find(params[:id])
    rescue
    end

    unless ci
      render :json => {}, :status => :not_found
      return
    end

    attribute_name_param = params[:attribute_name]
    if attribute_name_param.present?
      render_attribute(ci.nsPath, ci.ciAttributes.attributes[attribute_name_param], ci.meta.md_attribute(attribute_name_param))
    else
      authorize_and_respond(ci)
    end
  end

  def relation
    rel = nil
    begin
      rel = Cms::DjRelation.find(params[:id])
    rescue
    end

    unless rel
      render :json => {}, :status => :not_found
      return
    end

    attribute_name_param = params[:attribute_name]
    if attribute_name_param.present?
      render_attribute(rel.nsPath, rel.relationAttributes.attributes[attribute_name_param], rel.meta.md_attribute(attribute_name_param))
    else
      authorize_and_respond(rel)
    end
  end

  def release
    begin
      release = Cms::Release.find(params[:id])
    rescue
    end

    authorize_and_respond(release)
  end

  def deployment
    begin
      deployment = Cms::Deployment.find(params[:id])
    rescue
    end

    authorize_and_respond(deployment)
  end

  def procedure
    begin
      procedure = Cms::Procedure.find(params[:id])
      ci = procedure && Cms::DjCi.find(procedure.ciId)
    rescue
    end

    authorize_and_respond(procedure, ci && ci.nsPath)
  end


  def counterparts
    # Saving an additional lookup - construct a barebone Ci on-the-fly.
    ci        = params[:ci]
    ci[:ciId] = ci[:ciId].to_i
    @ci       = Cms::Ci.new(ci, true)
    unless @ci
      render :text => 'Ci not found', :status => :not_found
      return
    end

    root, org, assembly, rest = @ci.nsPath.split('/')
    unless current_user.has_any_dto?(assembly)
      unauthorized
      return
    end

    ci_class_name        = @ci.ciClassName
    find_params          = {:nsPath      => "/#{org}/#{assembly}",
                            :recursive   => true,
                            :ciClassName => ci_class_name}
    find_params[:ciName] = @ci.ciName unless ['Assembly', 'Environment', 'Platform'].include?(ci_class_name.split('.').last)
    @counterparts        = Cms::Ci.all(:params => find_params)
    split                = ci_class_name.split('.')
    if split[0] == 'catalog'
      split[0]                  = 'manifest'
      find_params[:ciClassName] = split.join('.')
      @counterparts             += Cms::Ci.all(:params => find_params)
    end

    respond_to do |format|
      format.js
      format.json { render :json => @counterparts }
    end
  end

  def policy_violations
    clazz = nil
    attrs = params[:cms_ci]
    if attrs.present?
      clazz = Cms::Ci
    else
      attrs = params[:cms_dj_ci]
      clazz = Cms::DjCi if attrs.present?
    end

    unless clazz
      respond_to do |format|
        format.js
        format.json { render :json => {:errors => ["Invalid payload: 'cms_ci' || 'cms_dj_ci' structure is expected."]}, :status => :unprocessable_entity }
      end

      return
    end

    ci = clazz.new(attrs, true)
    unless authorize(ci.nsPath)
      respond_to do |format|
        format.js
        format.json { render :json => {}, :status => :unauthorized }
      end
      return
    end

    policy_locations    = params[:locations]
    ci.policy_locations = policy_locations if policy_locations.present?
    @violations         = ci.violates_policies
    respond_to do |format|
      format.js
      format.json { render :json => @violations }
    end
  end

  def variables
    result      = {}
    assembly_id = params[:assembly_id]

    if assembly_id.present?
      assembly = locate_assembly(assembly_id)
      if authorize(assembly.nsPath)
        env_id      = params[:environment_id]
        platform_id = params[:platform_id]
        if env_id.present?
          result[:global] = var_list('GLOBAL',
                                     Cms::DjRelation.all(:params => {:ciId              => env_id,
                                                                     :direction         => 'to',
                                                                     :relationShortName => 'ValueFor',
                                                                     :targetClassName   => 'manifest.Globalvar'}))
          result[:local]  = var_list('LOCAL',
                                     Cms::DjRelation.all(:params => {:ciId              => platform_id,
                                                                     :direction         => 'to',
                                                                     :relationShortName => 'ValueFor',
                                                                     :targetClassName   => 'manifest.Localvar'})) if platform_id.present?
        else
          result[:global] = var_list('GLOBAL',
                                     Cms::DjRelation.all(:params => {:ciId              => assembly.ciId,
                                                                     :direction         => 'to',
                                                                     :relationShortName => 'ValueFor',
                                                                     :targetClassName   => 'catalog.Globalvar'}))
          result[:local]  = var_list('LOCAL',
                                     Cms::DjRelation.all(:params => {:ciId              => platform_id,
                                                                     :direction         => 'to',
                                                                     :relationShortName => 'ValueFor',
                                                                     :targetClassName   => 'catalog.Localvar'})) if platform_id.present?
        end
        result[:cloud] = Cms::Ci.all(:params => {:nsPath      => clouds_ns_path,
                                                 :ciClassName => 'account.Cloudvar',
                                                 :recursive   => true}).map(&:ciName).uniq.map {|v| {:name => v, :type => 'CLOUD'}}

        begin
          system_cloud_vars_var = Cms::Var.find('CLOUD_SYSTEM_VARS')
          system_cloud_vars     = JSON.parse(system_cloud_vars_var.value).values.inject([]) do |a, providder_vars|
            providder_vars.keys.inject(a) {|aa, var| aa << var}
          end
          result[:cloud] += system_cloud_vars.uniq.map {|v| {:name => v, :type => 'CLOUD'}}
        rescue Exception
        end
      end
    end

    render :json => result, :status => result.blank? ? :unauthorized : :ok
  end

  def ci_lookup
    org = params[:org]
    query = params[:query]
    if org.blank? || query.blank?
      render :json => []
      return
    end

    dto_scope = params[:dto]

    tokens = query.split(/\s+/).inject([]) do |a, t|
      case t
        when 'd', 't', 'o'
          dto_scope = t
        else
          a << t
      end
      a
    end

    query_string = {:query  => tokens.map { |t| %("#{t}") }.join(' AND '),
                    :fields => %w(ciName nsPath)}
    must = [{:wildcard => {'nsPath.keyword' => "/#{org}/*"}},
            {:query_string => query_string}]

    case dto_scope
      when 'd'
        must << {:wildcard => {'ciClassName.keyword' =>  'catalog.*'}}
      when 't'
        must << {:wildcard => {'ciClassName.keyword' =>  'manifest.*'}}
      when 'o'
        query_string[:fields] << 'workorder.cloud.ciName'
        must << {:wildcard => {'ciClassName.keyword' =>  'bom.*'}}
    end

    should = []
    assembly = params[:assembly]
    if assembly.present?
      environment = params[:environment]
      should = [{:wildcard => {'nsPath.keyword' => "/#{org}/#{assembly}/#{"#{environment}/" if environment.present?}*"}}]
    end

    search_params = {:size    => (params[:max_size] || 20).to_i,
                     :_query => {:bool => {:must => must, :should => should, :minimum_should_match => 0}},
                     :_source => %w(ciName ciClassName nsPath ciId),
                     :_silent => []}
    result = Cms::Ci.search(search_params)
    render :json => result
  end


  private

  def render_attribute(ns_path, attribute, md_attribute)
    if authorize(ns_path)
      if md_attribute.present?
        data_type = md_attribute.dataType
        if data_type == 'hash' || data_type == 'array' || data_type == 'struct'
          begin
            render :json => JSON.parse(attribute)
          rescue
            render :text => attribute
          end
        else
          render :text => attribute
        end
      else
        render :nothing => true, :status => :not_found
      end
    else
      render :nothing => true, :status => :unauthorized
    end
  end

  def authorize_and_respond(target, ns_path = nil)
    unless target
      render :json => {}, :status => :not_found
      return
    end

    if authorize(ns_path || target.nsPath)
      render_json_ci_response(true, target)
    else
      render :json => {}, :status => :unauthorized
    end
  end

  def authorize(ns_path)
    return true if ns_path.start_with?('/public/')

    _, org_name = ns_path.split('/')
    org = locate_org(org_name)
    return false unless org

    unless is_admin?(org) || has_org_scope?(org)
      paths = org.ci_proxies.where(:ns_path => "/#{org_name}").joins(:teams).where('teams.id IN (?)', current_user.all_team_ids(org.id)).pluck(:ns_path)
      return false unless paths.find { |p| ns_path.start_with?(p) }
    end

    return true
  end

  def var_list(type, relations)
    relations.map do |r|
      var = r.fromCi
      {:name => var.ciName,
       :type => type,
       :value => var.ciAttributes.secure == 'true' ? var.ciAttributes.encrypted_value : var.ciAttributes.value}
    end
  end
end
