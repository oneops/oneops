class Transition::LogsController < ApplicationController
  before_filter :find_parent_cis
  before_filter :find_log, :only => [:show, :edit, :update, :destroy]

  def index
    @logs = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                            :direction         => 'from',
                                            :relationShortName => 'LoggedBy',
                                            :targetClassName   => 'manifest.Log'}).map(&:toCi)
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @logs }
    end
  end

  def show
    render_json_ci_response(@log.present?, @log)
    respond_to do |format|
      format.html { redirect_to edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, @component, :anchor => "logging/list_item/#{@log.ciId}") }
      format.json { render_json_ci_response(@log.present?, @log) }
    end
  end

  def new
    @log = Cms::DjCi.build(:ciClassName => 'manifest.Log',
                           :nsPath      => @component.nsPath)

    render :action => :edit
  end

  def create
    ns_path  = @component.nsPath
    @log     = Cms::DjCi.build(params[:cms_dj_ci].merge(:ciClassName => 'manifest.Log', :nsPath => ns_path))
    relation = Cms::DjRelation.build(:nsPath       => ns_path,
                                     :relationName => 'manifest.LoggedBy',
                                     :fromCiId     => @component.ciId,
                                     :toCi         => @log)
    ok = execute_nested(@log, relation, :save)
    @log = relation.fromCi if ok

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @log) }
    end
  end

  def edit
    render :action => :edit
  end

  def update
    ok = execute(@log, :update_attributes, params[:cms_dj_ci])

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @log) }
    end
  end

  def destroy
    ok = execute(@log, :destroy)

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @log) }
    end
  end


  protected

  def find_parent_cis
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
    @component   = Cms::DjCi.locate(params[:component_id], @platform.nsPath)
  end

  def find_log
    @log = Cms::DjCi.locate(params[:id], @component.nsPath, 'manifest.Log')
  end
end
