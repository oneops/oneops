class Transition::ReleasesController < Base::ReleasesController
  layout nil
  before_filter :find_assembly_and_environment

  def index
    size   = (params[:size].presence || 1000).to_i
    offset = (params[:offset].presence || 0).to_i
    sort   = params[:sort].presence || {'created' => 'desc'}
    filter = params[:filter]
    search_params = {:nsPath       => environment_manifest_ns_path(@environment),
                     :releaseState => %w(closed canceled),
                     :size         => size,
                     :from         => offset,
                     :sort         => sort,
                     :_silent      => []}
    search_params[:query] = filter if filter.present?
    # @releases = Cms::Release.all(:params => {:nsPath => environment_manifest_ns_path(@environment)})
    @releases = Cms::Release.search(search_params)
    respond_to do |format|
      format.html {render 'transition/environments/_releases'}
      format.js { render :action => :index }
      format.json do
        set_pagination_response_headers(@releases)
        render :json => @releases
      end
    end
  end

  def show
    edit
  end

  def edit
    @release = Cms::Release.find(params[:id])
    @deployment = Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom") unless request.format.json?
    respond_to do |format|
      format.html { render '_release_details', :locals => {:release => @release} }
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@release.present?, @release) }
    end
  end

  def latest
    manifest_ns_path = environment_manifest_ns_path(@environment)
    @release = Cms::Release.latest(:nsPath => manifest_ns_path)
    @release = Cms::Release.latest(:nsPath => manifest_ns_path, :releaseState => 'closed') if @release && @release.releaseState == 'canceled'
    render_json_ci_response(@release.present?, @release)
  end

  def bom
    if params[:latest] == 'true'
      @release = Cms::Release.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")
    else
    @release = Cms::Release.first(:params => {:nsPath => "#{environment_ns_path(@environment)}/bom", :releaseState => 'open'})
    end

    if @release
      include_rfcs = params[:include_rfcs]
      unless include_rfcs.blank? || include_rfcs == 'false'
        rfcs = {}
        rfcs[:cis] = @release.rfc_cis unless include_rfcs == 'relations'
        rfcs[:relations] = @release.rfc_relations unless include_rfcs == 'cis'
        @release.rfcs = rfcs
      end
    end

    render_json_ci_response(@release.present?, @release)
  end

  def discard
    release_id, message = Transistor.discard_bom(@environment.ciId)

    respond_to do |format|
      format.js do
        flash[:error] = message if release_id.blank?
      end
      format.json do
        @release = Cms::ReleaseBom.find(release_id.presence || params[:id])
        render_json_ci_response(release_id.present?, @release, [message])
      end
    end
  end

  private

  def find_assembly_and_environment
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
  end
end
