class TransitionController < ApplicationController
  def show
    @assembly     = locate_assembly(params[:assembly_id])
    @environments = Cms::Relation.all(:params => {:ciId              => @assembly.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'RealizedIn',
                                                  :targetClassName   => 'manifest.Environment'}).map(&:toCi)

    composedof_rels = Cms::Relation.all(:params => {:nsPath            => assembly_ns_path(@assembly),
                                                    :relationShortName => 'ComposedOf',
                                                    :targetClassName   => 'manifest.Platform',
                                                    :recursive         => true})

    consumes_rels = Cms::Relation.all(:params => {:nsPath        => assembly_ns_path(@assembly),
                                                  :relationName  => 'base.Consumes',
                                                  :fromClassName => 'manifest.Environment',
                                                  :includeToCi   => true,
                                                  :recursive     => true})

    @catalog = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly), :releaseState => 'closed')

    # We try to get deployment and bom release data from ES for performance reasons. But if it fails (ES is unavailable)
    # then we fall back to old inefficient way from CMS.

    manifest_releases = nil
    begin
      manifest_releases = Cms::Release.search_latest_by_ns(@environments.map {|e| environment_manifest_ns_path(e)}).to_map(&:nsPath)
    rescue Exception => e
    end

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
      manifest_ns_path = environment_manifest_ns_path(e)
      e.release = manifest_releases ? manifest_releases[manifest_ns_path] : Cms::Release.latest(:nsPath => manifest_ns_path)
      if e.release && e.release.releaseState == 'canceled'
        e.manifest  = Cms::Release.latest(:nsPath => manifest_ns_path, :releaseState => 'closed')
      else
        e.manifest  = e.release
      end

      bom_ns_path = "#{environment_ns_path(e)}/bom"
      e.deployment  = deployments ? deployments[bom_ns_path] : Cms::Deployment.latest(:nsPath => bom_ns_path)
      unless e.deployment && %w(active failed).include?(e.deployment.deploymentState)
        e.bom_release = bom_releases ? bom_releases[bom_ns_path] : Cms::Release.first(:params => {:nsPath => bom_ns_path, :releaseState => 'open'})
      end
      e.platforms = composedof_rels.select { |r| r.fromCiId == e.ciId }
      e.clouds    = consumes_rels.select { |r| r.fromCiId == e.ciId }
    end

    @profiles = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                        :ciClassName => 'account.Environment'}).sort_by(&:ciName)
  end
end
