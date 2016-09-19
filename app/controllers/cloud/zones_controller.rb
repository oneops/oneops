class Cloud::ZonesController < ApplicationController
  before_filter :find_cloud_and_zone
  before_filter :authorize_write, :only => [:new, :create, :update, :destroy]

  def index
    @zones = Cms::Ci.all(:params => {:nsPath      => cloud_ns_path(@cloud),
                                     :ciClassName => 'cloud.Zone'})
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @zones }
    end
  end

  def show
    render_json_ci_response(@zone.present?, @zone)
  end

  def new
    @zone = Cms::Ci.build(:nsPath => cloud_ns_path(@cloud), :ciClassName => 'cloud.Zone')

    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(true, @zone) }
    end
  end

  def create
    @zone = Cms::Ci.build(params[:cms_ci].merge(:ciClassName => 'cloud.Zone', :nsPath => cloud_ns_path(@cloud)))
    ok    = execute(@zone, :save)

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @zone) }
    end
  end

  def edit
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(true, @zone) }
    end
  end

  def update
    ok = execute(@zone, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @zone) }
    end
  end

  def destroy
    ok = execute(@zone, :destroy)

    respond_to do |format|
      format.js do
        flash[:error] = 'Failed to delete zone.' unless ok
        index
      end

      format.json { render_json_ci_response(ok, @zone) }
    end
  end


  private

  def find_cloud_and_zone
    @cloud  = locate_cloud(params[:cloud_id])
    zone_id = params[:id]
    @zone   = Cms::Ci.locate(zone_id, cloud_ns_path(@cloud)) if zone_id.present?
  end

  def authorize_write
    unauthorized unless @cloud && has_cloud_services?(@cloud.ciId)
  end
end
