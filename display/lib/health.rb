module Health
  ApplicationController.before_filter :weak_ci_relation_data_consistency, :only => [:health]

  def self.included(base)
    base.class_eval do
      swagger_api :health do
        summary 'Get health state counts for components in a given namespace'
        notes <<-NOTE
Returns counts of instances by health state for all componnets in the implied (by current scope) namespace or in a given namepsace if explicitly specified.
Response example:
<pre>
[
  {
    "id": 12345671,
    "ns": "/some-org/some-assembly/some-env/manifest/some-platform/1",
    "name": "compute",
    "class": "manifest.oneops.1.Compute",
    "health": {
      "total": 10,
      "unhealthy": 1,
      "notify": 3,
      "good": 6
    }
  },
  {
    "id": 12345672,
    "ns": "/some-org/some-assembly/some-env/manifest/some-platform/1",
    "name": "logstash",
    "class": "manifest.oneops.1.Logstash",
    "health": {
      "total": 10,
      "notify": 3,
      "good": 7
    }
  },
  {
    "id": 12345673,
    "ns": "/oneops/core/mgmt-1410/manifest/daq/1",
    "name": "ntpd",
    "class": "manifest.oneops.1.Daemon",
    "health": {
      "total": 10,
      "good": 10
    }
  },
  ...
]
</pre>
NOTE
        controller_name = base.name
        if controller_name.end_with?('OrganizationController')
          param_org_name
        elsif controller_name.end_with?('AssembliesController')
          param_path_parent_ids :assembly
        elsif controller_name.end_with?('EnvironmentsController')
          param_path_parent_ids :assembly
          param_path_ci_id :environment
        elsif controller_name.end_with?('PlatformsController')
          param_path_parent_ids :assembly, :environment
          param_path_ci_id :platform
        end
        param :query, 'ns_path', :string, :optional, 'Namespace to narrow implied namespace scope but can not be "wider" than current org/assembly/env/platform scope.'
        param :query, 'state', :string, :optional, 'Filter results only by entries with non-zeo counts of a given state'
        param :query, 'profiles', :string_array, :optional, 'When called for org or assembly namespace filter results only for components in enviroments of given profiles'
      end
    end
  end

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
