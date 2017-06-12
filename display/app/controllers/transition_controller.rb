class TransitionController < ApplicationController
  before_filter :find_assembly

  def show
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


    # We try to get deployment and bom release data from ES for performance reasons. But if it fails (ES is unavailable)
    # then we fall back to old inefficient way from CMS.

    manifest_releases = nil
    bom_releases = nil
    deployments = nil
    if @manifest.present?
      bom_ns_paths = @environments.map {|e| environment_bom_ns_path(e)}

      begin
        manifest_releases = Cms::Release.search_latest_by_ns(@environments.map {|e| environment_manifest_ns_path(e)}).to_map(&:nsPath)
      rescue Exception => e
      end

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

    respond_to do |format|
      format.html
      format.js { render :action => :show }
    end

  end

  def pull
    environment_ids = params["ciIds"]

    environments_to_pull = []
    env_messages = []

    environment_ids.each do |environment_id|
      environment = locate_environment(environment_id, @assembly)
      manifest_ns_path = environment_manifest_ns_path(environment)
      release  = Cms::Release.latest(:nsPath => manifest_ns_path)
      manifest = release && release.releaseState == 'canceled' ? Cms::Release.latest(:nsPath => manifest_ns_path, :releaseState => 'closed') : release

      design_pull_in_progress = environment.ciState == 'manifest_locked'
      open_manifest_release = manifest && manifest.releaseState == 'open'
      design_latest = manifest && manifest.parentReleaseId == @catalog.releaseId

      if design_pull_in_progress
        env_messages << {:environment => environment.ciName, :message => 'Design pull already in progress', :status => :warning}
      elsif open_manifest_release
        env_messages << {:environment => environment.ciName, :message => 'Open manifest release', :status => :warning}
      elsif design_latest
        env_messages << {:environment => environment.ciName, :message => 'Design already latest', :status => :warning}
      else
        environments_to_pull << environment
      end
    end

    if environments_to_pull
      success, error = Transistor.pull_design(environments_to_pull.map{|e| e.ciId}.join(','))
      environments_to_pull.each do |e|
        if error
          env_messages << {:environment => e.ciName, :message => 'Error pulling', :status => :error}
        else
          env_messages << {:environment => e.ciName, :message => 'Design pull initiated', :status => :success}
        end
      end
    end
    message = env_messages.map{|e| "#{e[:environment]}: #{e[:message]}"}.join("<br/>").html_safe
    if env_messages.any? {|message| message[:status] == :error}
      flash[:error] = message
    elsif env_messages.any? {|message| message[:status] == :warning}
      flash[:alert] = message
    elsif env_messages.all? {|message| message[:status] == :success}
      flash[:notice] = message
    else
      flash[:error] = message
    end

    show
  end

  private

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
    @catalog = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly), :releaseState => 'closed')
  end

end
