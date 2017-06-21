class OperationsController < ApplicationController
  include ::Health
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

    bom_ns_paths = @environments.map {|e| environment_bom_ns_path(e)}

    bom_releases = nil
    deployments = nil
    if @environments.present?
      begin
        bom_releases = Cms::Release.search(:nsPath => bom_ns_paths, :releaseState => 'open').to_map(&:nsPath)
      rescue Exception => e
      end

      begin
        deployments = Cms::Deployment.search_latest_by_ns(bom_ns_paths).to_map(&:nsPath)
      rescue Exception => e
      end
    end

    @environments.each do |e|
      bom_ns_path = "#{environment_ns_path(e)}/bom"

      e.bom_release = bom_releases ? bom_releases[bom_ns_path] : Cms::Release.first(:params => {:nsPath => bom_ns_path, :releaseState => 'open'})

      e.deployment  = deployments ? deployments[bom_ns_path] : Cms::Deployment.latest(:nsPath => bom_ns_path)

      e.platforms   = composedof_rels[e.ciId] || []
      e.clouds      = consumes_rels[e.ciId] || []
    end
  end

  def charts
    req_set = params[:request_set]
    data = req_set && Daq.charts(req_set)
    render :json => data || []
  end
end
