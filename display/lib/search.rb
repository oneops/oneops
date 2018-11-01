module Search
  ApplicationController.before_filter :weak_ci_relation_data_consistency, :only => [:search]

  def self.included(base)
    base.class_eval do
      swagger_api :search do
        summary 'Search API to return CI or relation data for a given CI class name or relation name.'
        notes 'Perform a search of CIs or relations against CMS (source=cms) or ES (source=es) - see <b>source</b> parameter: defaults CMS for ' \
              'json/yaml/txt requestst and to ES for html/ajax requests.  Either <b>class_name</b> or <b>relation_name</b> ' \
              'must be specified. Relation search is not supported for ES. <br/>Examples:' \
              '<br/>1. Lookup IP addresses for a given platform and matching cloud name from ES:<br/>' \
              '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; https://SERVER/ORG/assemblies/ASSEMBLY/operations/environments/ENV/platforms/PLATFORM/search.json?source=es&class_name=Compute&query=workorder.cloud.ciName:dal*&pluck=private_ip' \
              '<br/>2. Get a list of FQDNs for a given environment from CMS:<br/>' \
              '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; https://SERVER/ORG/assemblies/ASSEMBLY/operations/environments/ENV/search.json?source=cms&class_name=Fqdn'
        param_org_name
        controller_name = base.class.name
        if controller_name.end_with?('AssembliesController')
          param_ci_id :assembly
        elsif controller_name.end_with?('EnvironmentsController')
          param_parent_ci_id :assembly
          param_ci_id :environment
        elsif controller_name.end_with?('PlatformsController')
          param_parent_ci_id :assembly
          param_parent_ci_id :environment
          param_ci_id :platform
        end
        param :query, 'ns_path', :string, :optional, 'Namespace to narrow implied namespace scope but can not be "wider" than current org/assembly/env/platform scope.'
        param :query, 'class_name', :string, :optional, 'CI class name. FUll and short names are both supported, i.e. "bom.oneops.1.Compute" vs "Compute"'
        param :query, 'relation_name', :string, :optional, 'Relation name. FUll and short names are both supported, i.e. "manifest.DependsOn"" vs "DependsOn""'
        param :query, 'query', :string, :optional, 'Query string to filter results.  For ES search use standard ES\'s query string syntax. ' \
                                                    'For CMS search the following format is supported: "attr:operator:vlue", where ' \
                                                    ' "attr" is field name (could be a "path", e.g. "fromCi.ciAttributes.private_ip", "locdation", "ciName"), ' \
                                                    ' "operator" - eq|neq|like (can be omitted, defaults to "eq"), "value" - field value to filter on. Exmaples: ' \
                                                    '"private_ip:101.102.103.104", "toCi.ciAttributes.alias:like:some_value"'
        param :query, 'include_from_ci', :string, :optional, 'Include full "fromCi" object in the resultset: applicable to relation searches only.'
        param :query, 'include_to_ci', :string, :optional, 'Include full "toCi" object in the resultset: applicable to relation searches only.'
        param :query, 'from_class_name', :string, :optional, 'Filter based on "fromCi" class name: supported for CMS searches only.'
        param :query, 'to_class_name', :string, :optional, 'Filter based on "toCi" class name: supported for CMS searches only.'
        param :query, 'pluck', :string, :optional, 'Restrict resultset to the values of attributes names or expression (e.g. "private_ip", "toCi", "nsPath,fromCi.ciName,fromCi.ciAttributes.ostype")'
        param :query, 'size', :string, :optional, 'Resutlset record max size: supported for ES searches only.'
        response :unauthorized
        response :unprocessable_entity
      end
    end
  end

  def search
    format = request.format
    @source = params[:source].presence || (format == :html || format == :js  ? 'es' : 'cms')

    return if format == 'text/html'

    min_ns_path = search_ns_path
    ns_path     = params[:ns_path] || min_ns_path
    unless ns_path.start_with?(min_ns_path)
      unauthorized('Invalid namespace!')
      return
    end

    class_name = params[:class_name]
    rel_name   = params[:relation_name]
    attr_name  = params[:attr_name]
    attr_value = params[:attr_value]
    query      = params[:query]

    relation_search = rel_name.present?

    pluck_attr       = params[:pluck]
    pluck_attr       = attr_name if pluck_attr.blank? && attr_name.present? && attr_value.blank? # TODO:deprecated: plucking via 'attr_name' param if 'attr_value' is not passed in
    pluck            = pluck_attr.present?
    pluck_attr_paths = parse_attr_path(pluck_attr, relation_search) if pluck

    @search_results = []

    if @source == 'cms' || @source == 'simple'
      # CMS search.
      clazz = nil
      query_params = {:nsPath => ns_path, :recursive => true}
      if relation_search
        clazz = Cms::Relation
        query_params[rel_name.include?('.') ? :relationName : :relationShortName] = rel_name

        query_params[:includeFromCi]   = true unless params[:include_from_ci] == 'false'
        query_params[:includeToCi]     = true unless params[:include_to_ci] == 'false'

        class_name                     = params[:from_class_name]
        query_params[:fromClassName]   = class_name if class_name.present?

        class_name                     = params[:to_class_name]
        query_params[:targetClassName] = class_name if class_name.present?
      elsif class_name.present?
        clazz = Cms::Ci
        query_params[:ciClassName] = class_name
      end

      if clazz
        filter_attr = nil
        if query.present?
          # Expecting something like: "toCi.ciName:eq:whatever"
          filter_attr, attr_operator, attr_value = query.split(':', 3)
          unless attr_value
            # In case of short-hand form: "toCi.ciName:whatever" assume 'eq'
            attr_value = attr_operator
            attr_operator = 'eq'
          end
        elsif attr_name.present? && attr_value.present?
          # TODO:deprecated: filtering via 'attr_name', 'attr_operator' and 'attr_value'
          filter_attr = attr_name
          attr_operator = params[:attr_operator] || 'eq'
        end

        filter      = filter_attr.present?
        deep_filter = filter && filter_attr.include?('.')
        if filter && !deep_filter
          # CMS should support 'shallow attribute' filtering, otherwise will have to do it on-the-fly later after fetching larger un-filtered result set.
          query_params[:attr] = "#{filter_attr}:#{attr_operator}:#{attr_operator == 'like' ? "%#{attr_value}%" : attr_value}"
        end

        @search_results = clazz.all(:params => query_params)

        if filter && deep_filter
          # Need to do further "deep attribute" filtering on-the-fly filtering.
          filter_attr_path = parse_attr_path(filter_attr, relation_search).first
          @search_results = @search_results.select do |r|
            value = filter_attr_path.inject(r) do |r, name|
              rr = r.try(name.to_sym)
              break nil unless r
              rr
            end
            (attr_operator == 'eq' && value == attr_value) ||
              (attr_operator == 'neq' && value != attr_value) ||
              (attr_operator == 'like' && value.include?(attr_value))
          end
        end
      end
    else
      # ES search.
      max_size = (params[:max_size].presence || 1000).to_i

      if query.present? || class_name.present?
        begin
          search_params = {:nsPath => "#{ns_path}#{'*' unless ns_path.include?('*')}", :size => max_size}
          search_params[:_source] = pluck_attr_paths.map {|p| p.join('.')} if pluck
          if query.present?
            search_params[:query] = {:query => query, :lenient => true}
            search_params[:query][:fields] = %w(ciAttributes.* ciClassName ciName) unless query.match(/\w+:\s?./)   # advanced query string with explicit filed nmaes specified.
          end
          search_params['ciClassName.keyword'] = "#{'*' unless class_name.match(/\W/)}#{class_name}" if class_name.present?
          @search_results = Cms::Ci.search(search_params)
        rescue Exception => e
          @error = e.message
        end
      end
    end

    unless @search_results || is_admin? || has_org_scope?
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

    if pluck
      @search_results = @search_results.map do |r|
        if pluck_attr_paths.size > 1
          pluck_attr_paths.to_map_with_value {|path| [path.join('.'), eval_attr_path(path, r)]}
        else
          eval_attr_path(pluck_attr_paths.first, r)
        end
      end
    end

    respond_to do |format|
      format.js { render 'base/search/search' }

      format.json do
        if @error
          render :json => {:errors => [@error]}, :status => :unprocessable_entity
        else
          render :json => @search_results
        end
      end

      format.yaml do
        if @error
          render :text => @error, :status => :unprocessable_entity
        else
          render :text => JSON.parse(@search_results.to_json).to_yaml, :content_type => 'text/data_string'
        end
      end

      format.text do
        if @error
          render :text => @error, :status => :unprocessable_entity
        else
          render :text => @search_results.join(params[:delimeter] || ' ')
        end
      end
    end
  end


  private

  def parse_attr_path(attr, is_relation)
    properties = %w(nsPath ciClassName ciName ciId ciRelationId created updated createdBy updateBy relationAttributes ciAttributes comments fromCi toCi)
    attr.split(',').inject([]) do |a, path_string|
      path = path_string.split('.')
      if path.size == 1 && !properties.include?(path.first)
        path = path.unshift(is_relation ? 'relationAttributes' : 'ciAttributes')
      end
      a << path
    end
  end

  def eval_attr_path(path, target)
    path.inject(target) do |r, name|
      break r if r.nil?
      r.try(name.to_sym)
    end
  end
end
