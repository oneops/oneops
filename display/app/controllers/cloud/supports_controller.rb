class Cloud::SupportsController < ApplicationController
  SUPPORT_PERMISSION_CLOUD_SUPPORT_MANAGEMENT = 'cloud_support_management'

  before_filter :find_cloud_and_support
  before_filter :authorize_write, :only => [:new, :create, :update, :destroy]

  def index
    @supports = Cms::Relation.all(:params => {:ciId         => @cloud.ciId,
                                              :direction    => 'from',
                                              :relationName => 'base.SupportedBy'}).map(&:toCi)
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @supports }
    end
  end

  def show
    render_json_ci_response(@support.present?, @support)
  end

  def new
    @support = Cms::Ci.build(:nsPath => cloud_ns_path(@cloud), :ciClassName => 'cloud.Support')

    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(true, @support) }
    end
  end

  def create
    @support = Cms::Ci.build(params[:cms_ci].merge(:ciClassName => 'cloud.Support', :nsPath => cloud_ns_path(@cloud)))
    relation = Cms::Relation.build(:nsPath       => cloud_ns_path(@cloud),
                                   :relationName => 'base.SupportedBy',
                                   :fromCiId     => @cloud.ciId,
                                   :toCi         => @support)
    ok       = execute_nested(@support, relation, :save)
    @support = relation.fromCi if ok

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @support) }
    end
  end

  def update
    ok = execute(@support, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @support) }
    end
  end

  def destroy
    ok = execute(@support, :destroy)

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @support) }
    end
  end


  private

  def find_cloud_and_support
    @cloud     = locate_cloud(params[:cloud_id])
    support_id = params[:id]
    @support   = Cms::Ci.locate(support_id, cloud_ns_path(@cloud)) if support_id.present?
  end

  def authorize_write
    unauthorized unless @cloud && has_cloud_support?(@cloud.ciId) && has_support_permission?(SUPPORT_PERMISSION_CLOUD_SUPPORT_MANAGEMENT)
  end
end
