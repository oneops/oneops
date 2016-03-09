class DesignController < ApplicationController
  before_filter :find_assembly
  before_filter :find_latest_release, :only => [:show, :edit, :update]
  before_filter :check_open_release, :only => [:edit, :update]

  def show
    respond_to do |format|
      format.html do
        platforms = Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                    :direction       => 'from',
                                                    :targetClassName => 'catalog.Platform',
                                                    :relationName    => 'base.ComposedOf'})
        @platforms = platforms.map(&:toCi)
        @diagram = prepare_platforms_diagram(platforms)

        render :action => :show
      end

      format.json do
        design = Transistor.export_design(@assembly)
        render :json => design
      end

      format.yaml do
        design = Transistor.export_design(@assembly)
        render :text => design.to_yaml, :content_type => 'text/data_string'
      end
    end
  end

  def edit
  end

  def update
    data = nil
    data_file = params[:data_file]
    data_string = (data_file && data_file.read).presence || params[:data]
    begin
      data = YAML.load(data_string)
    rescue
      begin
        data = JSON.parse(data_string)
      rescue
      end
    end

    if data.present?
      ok, message = Transistor.import_design(@assembly, data)
    else
      ok = false
      message = 'Please specify proper design coonfguration in YAML format.'
    end

    respond_to do |format|
      format.html do
        if ok
          flash[:notice] = 'Successfully updated design from import.'
          redirect_to assembly_design_url(@assembly)
        else
          flash.now[:error] = "Failed to update design from import: #{message}"
          render :action => :edit
        end
      end

      format.json do
        if ok
          show
        else
          render_json_ci_response(false, nil, [message])
        end
      end
    end
  end

  def diagram
    send_data(prepare_platforms_diagram, :type => 'image/svg+xml', :disposition => 'inline')
  end


  private

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
  end

  def find_latest_release
    @release = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))
  end

  def check_open_release
    if @release && @release.releaseState == 'open'
      message = 'Design import is not allowed when there is an open release. Please commit or discard current release before proceeding with import.'
      respond_to do |format|
        format.html do
          flash.now[:error] = message
          show
        end

        format.json {render_json_ci_response(false, nil, [message])}
      end
    end
  end

  def prepare_platforms_diagram(platforms = nil)
    platforms ||= Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                  :direction       => 'from',
                                                  :targetClassName => 'catalog.Platform',
                                                  :relationName    => 'base.ComposedOf',
                                                  :includeToCi     => true})
    links_to = Cms::DjRelation.all(:params => {:nsPath            => [@assembly.nsPath, @assembly.ciName].join('/'),
                                               :relationShortName => 'LinksTo'})
    begin
      return graphvis_sub_pack_remote_images(platforms_diagram(platforms, links_to, assembly_design_path(@assembly), params[:size]).output(:svg => String))
    rescue
      return nil
    end
  end
end
