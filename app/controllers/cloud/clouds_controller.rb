class Cloud::CloudsController < ApplicationController
  include ::TeamAccess

  before_filter :find_cloud, :only => [:search, :show, :edit, :update, :destroy, :operations, :instances, :procedures,
                                       :reports, :teams, :update_teams]
  before_filter :authorize_create, :only => [:new, :create]
  before_filter :authorize_update, :only => [:update, :destroy, :update_teams]
  before_filter :authorize_support, :only => [:operations, :instances, :procedures, :reports]
  before_filter :find_proxy, :only => [:destroy, :teams]

  def index
    @clouds = Cms::Ci.all(:params => {:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'}).sort_by(&:ciName)
    respond_to do |format|
      format.html { render :action => :index }
      format.json { render :json => @clouds }
    end
  end

  def show
    render :json => @cloud, :status => @cloud ? :ok : :not_found
  end

  def new
    @cloud = Cms::Ci.build({:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'})

    respond_to do |format|
      format.html { load_available_clouds }
      format.json { render_json_ci_response(true, @cloud) }
    end
  end

  def create
    Cms::Namespace.create(:nsPath => clouds_ns_path) unless Cms::Namespace.first(:params => {:nsPath => clouds_ns_path})
    @cloud = Cms::Ci.build(params[:cms_ci].merge(:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'))
    ok = check_auth_key
    ok = execute(@cloud, :save) if ok

    process_update_teams(@cloud) if ok

    respond_to do |format|
      format.html do
        if ok
          render(:action => :edit)
        else
          load_available_clouds
          render(:action => :new)
        end
      end
      format.json { render_json_ci_response(ok, @cloud) }
    end
  end

  def edit
    respond_to do |format|
      format.html
      format.json { render_json_ci_response(true, @cloud) }
    end
  end

  def update
    ok = check_auth_key
    if ok
      cloud_hash                           = params[:cms_ci]
      cloud_hash[:ciAttributes][:location] = @cloud.ciAttributes.location
      ok                                   = execute(@cloud, :update_attributes, cloud_hash)
    end

    respond_to do |format|
      format.html { render :action => :edit }
      format.js
      format.json { render_json_ci_response(ok, @cloud) }
    end
  end

  def destroy
    cis = Cms::Relation.all(:params => {:relationName => 'base.Consumes',
                                        :direction    => 'to',
                                        :ciId         => @cloud.ciId})

    if cis.blank?
      ok = execute(@cloud, :destroy)
      flash[:error] = "Cannot delete cloud '#{@cloud.ciName}': #{@cloud.errors.full_messages.join(';')}." unless ok
      @proxy.destroy if ok && @proxy
    else
      ok = false
      flash[:error] = "Cannot delete cloud '#{@cloud.ciName}': #{cis.size} existing #{'environment/platform'.pluralize(cis.size)} using this cloud."
    end

    respond_to do |format|
      format.html { index }
      format.json { render_json_ci_response(ok, @cloud) }
    end
  end

  def locations
    render :json => load_available_clouds.inject({}) { |m, c| m[c.ciName] = "#{c.nsPath}/#{c.ciName}"; m }
  end

  def operations
    @environments = Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                                  :direction         => 'to',
                                                  :relationShortName => 'Consumes',
                                                  :targetClassName   => 'manifest.Environment',
                                                  :includeFromCi     => true}).map(&:fromCi)

    respond_to do |format|
      format.js
      format.json { render :json => @environments }
    end
  end

  def instances
    @instances = Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                               :direction         => 'to',
                                               :relationShortName => 'DeployedTo',
                                               :includeFromCi     => true}).map(&:fromCi)

    @state     = params[:instances_state]
    @state = nil if @state == 'all'
    ops_states = @instances.blank? ? {} : Operations::Sensor.states(@instances)
    @instances = @instances.select do |i|
      state      = ops_states[i.ciId]
      i.opsState = state
      @state.blank? || state == @state
    end

    @instance_procedures = Cms::Procedure.all(:params => {:nsPath    => organization_ns_path,
                                                          :recursive => true,
                                                          :actions   => true,
                                                          :state     => 'active',
                                                          :limit     => 10000}).inject({}) do |m, p|
      p.actions.each {|a| m[a.ciId] = p}
      m
    end

    respond_to do |format|
      format.js
      format.json { render :json => @instances }
    end
  end

  def procedures
    @procedures = Cms::Procedure.all(:params => {:ciId => @cloud.ciId})

    respond_to do |format|
      format.js
      format.json { render :json => @procedures }
    end
  end

  def teams
    @teams = @proxy ? @proxy.teams : []
    respond_to do |format|
      format.js   { render :action => :teams }
      format.json { render :json => @teams }
    end
  end

  def update_teams
    @teams = process_update_teams(@cloud)

    respond_to do |format|
      format.js   { render :action => :teams }
      format.json { render :json => @teams }
    end
  end


  protected

  def search_ns_path
    cloud_ns_path(@cloud)
  end


  private

  def find_cloud
    @cloud = locate_cloud(params[:id])
  end

  def authorize_create
    unauthorized unless manages_access?
  end

  def authorize_update
    unauthorized unless @cloud && manages_access_for_cloud?(@cloud.ciId)
  end

  def authorize_support
    unauthorized unless @cloud && has_cloud_support?(@cloud.ciId)
  end

  def find_proxy
    @proxy = locate_proxy(params[:id], clouds_ns_path)
  end

  def load_available_clouds
    @available_clouds = Cms::Ci.all(:params => {:nsPath => '/public', :ciClassName => 'mgmt.Cloud', :recursive => true}).sort_by(&:ciName) unless @available_clouds
  end

  def check_auth_key
    location = @cloud.ciAttributes.attributes['location']
    if location.blank?
      @cloud.errors.add(:base, 'Location is required.')
    elsif location.start_with?(@cloud.nsPath)
      @cloud.errors.add(:base, 'Authorization Key must be set for custom clouds.') if @cloud.ciAttributes.attributes['auth'].blank?
      @cloud.ciAttributes.location = "#{clouds_ns_path}/#{@cloud.ciName}"
    else
      load_available_clouds
      @cloud.errors.add(:base, 'Invalid location.') unless @available_clouds.find { |c| "#{c.nsPath}/#{c.ciName}" == location }
    end

    return @cloud.errors.empty?
  end
end
