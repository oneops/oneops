class Design::ReleasesController < Base::ReleasesController
  before_filter :find_assembly

  def index
    size   = (params[:size].presence || 1000).to_i
    offset = (params[:offset].presence || 0).to_i
    sort   = params[:sort].presence || {'created' => 'desc'}
    filter = params[:filter]
    search_params = {:nsPath       => assembly_ns_path(@assembly),
                     :releaseState => %w(closed canceled),
                     :size         => size,
                     :from         => offset,
                     :sort         => sort,
                     :_silent      => []}
    search_params[:query] = filter if filter.present?
    source = params[:source]
    if source == 'cms' || source == 'simple'
      @releases = Cms::Release.all(:params => {:nsPath => assembly_ns_path(@assembly)})
    else
      @releases = Cms::Release.search(search_params)
    end
    respond_to do |format|
      format.js   { render :action => :index }
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
    @release = Cms::Release.locate(params[:id], assembly_ns_path(@assembly))
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@release.present?, @release)}
    end
  end

  def latest
    @release = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))
    render_json_ci_response(@release.present?, @release)
  end

  def commit
    @release = Cms::Release.locate(params[:id], assembly_ns_path(@assembly))
    execute(@release, :commit, :desc => params[:desc])
    @release = Cms::Release.locate(params[:id], assembly_ns_path(@assembly))
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@release.present?, @release) }
    end
  end

  def discard
    @release = Cms::Release.locate(params[:id], assembly_ns_path(@assembly))
    execute(@release, :discard)
    @release = Cms::Release.locate(params[:id], assembly_ns_path(@assembly))
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@release.present?, @release) }
    end
  end


  private

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
  end
end
