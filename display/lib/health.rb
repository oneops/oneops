module Health
  ApplicationController.before_filter :weak_ci_relation_data_consistency, :only => [:health]

  def health
    ns_path = params[:ns_path] || search_ns_path
    if ns_path.present? && !ns_path.start_with?(organization_ns_path)
      unauthorized
      return
    end

    watched_component_ids = Cms::Relation.count(:nsPath       => ns_path.sub(/\/bom(\/|$)/) {|x| x.sub('bom', 'manifest')},
                                                :relationName => 'manifest.WatchedBy',
                                                :direction    => 'from',
                                                :recursive    => true,
                                                :groupBy      => 'ciId').keys.map(&:to_i)

    realized_component_ids = Cms::Relation.count(:nsPath       => ns_path.sub(/\/manifest(\/|$)/) {|x| x.sub('manifest', 'bom')},
                                                 :relationName => 'base.RealizedAs',
                                                 :direction    => 'from',
                                                 :recursive    => true,
                                                 :groupBy      => 'ciId').keys.map(&:to_i)

    component_ids = watched_component_ids & realized_component_ids

    if component_ids.blank?
      render :json => []
      return
    end

    components = Cms::Ci.list(component_ids)

    if ns_path.split('/').size < 3   # Org or assembly level.
      profiles = params[:profiles]
      if profiles.present?
        profiles = profiles.to_map
        envs     = Cms::Ci.all(:params => {:nsPath      => ns_path,
                                           :recursive   => true,
                                           :ciClassName => 'manifest.Environment'}).
          select { |e| profiles[e.ciAttributes.profile] }.
          to_map { |e| environment_ns_path(e) }

        component_ids = components.inject([]) do |a, c|
          a << c['ciId'] if envs.has_key?(c['nsPath'].split('/').shift(4).join('/'))
          a
        end
      end
    end

    if component_ids.blank?
      render :json => []
      return
    end

    components = components.to_map_with_value do |c|
      ci_id = c['ciId']
      [ci_id, {:ciId => ci_id, :nsPath => c['nsPath'], :ciName => c['ciName'], :ciClassName => c['ciClassName']}]
    end

    # Sensor seem to choke on too big of requests.  So we will try to get health info in trenches each no bigger
    # than 6000 components.
    state_info = {}
    component_ids.each_slice(6000) do |ids|
      state_info = state_info.merge(Operations::Sensor.component_states(ids))
    end

    if state_info.blank?
      render :nothing => true, :status => 204
      return
    end

    result = component_ids.map do |ci_id|
      component = components[ci_id]
      data = {:id    => ci_id,
              :ns    => component[:nsPath],
              :name  => component[:ciName],
              :class => component[:ciClassName]}
      health_info = state_info[ci_id.to_s]
      data[:health] = health_info if health_info
      data
    end

    state = params[:state]
    result = result.select { |c| c[:health] && c[:health][state].to_i > 0 } if state.present?
    render :json => result
  end
end
