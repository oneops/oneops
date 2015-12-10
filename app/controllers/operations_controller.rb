class OperationsController < ApplicationController
  before_filter :authorize_admin, :except => [:show]

  def show
    @assembly = locate_assembly(params[:assembly_id])

    @environments = Cms::Relation.all(:params => {:ciId              => @assembly.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'RealizedIn',
                                                  :targetClassName   => 'manifest.Environment'}).map(&:toCi)

    assembly_ns_path = assembly_ns_path(@assembly)

    composedof_rels = Cms::Relation.all(:params => {:nsPath            => assembly_ns_path,
                                                    :relationShortName => 'ComposedOf',
                                                    :recursive         => true}).inject({}) do |m, rel|
      m[rel.fromCiId] ||= []
      m[rel.fromCiId] << rel
      m
    end

    consumes_rels = Cms::Relation.all(:params => {:nsPath            => assembly_ns_path,
                                                  :relationShortName => 'Consumes',
                                                  :fromClassName     => 'manifest.Environment',
                                                  :includeToCi       => true,
                                                  :recursive         => true}).inject({}) do |m, rel|
      m[rel.fromCiId] ||= []
      m[rel.fromCiId] << rel
      m
    end

    # We try to get deployment and bom release data from ES for performance reasons. But if it fails (ES is unavailable)
    # then we fall back to old inefficient way from CMS.

    bom_ns_paths = @environments.map { |e| environment_bom_ns_path(e) }

    bom_releases = nil
    begin
      bom_releases = Cms::Release.search(:nsPath => bom_ns_paths, :releaseState => 'open').to_map(&:nsPath)
    rescue Exception => e
    end

    deployments = nil
    begin
      deployments = Cms::Deployment.search_latest_by_ns(bom_ns_paths).to_map(&:nsPath)
    rescue Exception => e
    end

    @environments.each do |e|
      bom_ns_path = "#{environment_ns_path(e)}/bom"

      e.bom_release = bom_releases ? bom_releases[bom_ns_path] : Cms::Release.first(:params => {:nsPath => bom_ns_path, :releaseState => 'open'})

      e.deployment  = deployments ? deployments[bom_ns_path] : Cms::Deployment.latest(:nsPath => bom_ns_path)

      e.platforms   = composedof_rels[e.ciId] || []
      e.clouds      = consumes_rels[e.ciId] || []
    end
  end

  def health
    watched_component_ids = Cms::Relation.count(:nsPath       => search_ns_path,
                                                :relationName => 'manifest.WatchedBy',
                                                :direction    => 'from',
                                                :recursive    => true,
                                                :groupBy      => 'ciId').keys.map(&:to_i)

    realized_component_ids = Cms::Relation.count(:nsPath       => search_ns_path,
                                                 :relationName => 'base.RealizedAs',
                                                 :direction    => 'from',
                                                 :recursive    => true,
                                                 :groupBy      => 'ciId').keys.map(&:to_i)

    component_ids = watched_component_ids & realized_component_ids

    components = Cms::Ci.list(component_ids)

    profiles = params[:profiles]
    if profiles.present?
      profiles = profiles.to_map
      envs     = Cms::Ci.all(:params => {:nsPath      => search_ns_path,
                                         :recursive   => true,
                                         :ciClassName => 'manifest.Environment'}).
        select { |e| profiles[e.ciAttributes.profile] }.
        to_map { |e| environment_ns_path(e) }

      component_ids = components.inject([]) do |a, c|
        a << c['ciId'] if envs.has_key?(c['nsPath'].split('/').shift(4).join('/'))
        a
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

  def charts
    req_set = params[:request_set]
    data = req_set && Daq.charts(req_set)
    render :json => data || []
  end
end
