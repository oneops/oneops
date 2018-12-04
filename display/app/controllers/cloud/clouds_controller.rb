class Cloud::CloudsController < ApplicationController
  include ::TeamAccess, ::Search

  before_filter :find_cloud, :only => [:search, :show, :edit, :update, :destroy, :operations, :instances, :procedures,
                                       :reports, :teams, :update_teams]
  before_filter :authorize_create, :only => [:new, :create]
  before_filter :authorize_update, :only => [:update, :destroy, :update_teams]
  before_filter :authorize_support, :only => [:operations, :instances, :procedures, :reports]

  def index
    @clouds = Cms::Ci.all(:params => {:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'}).sort_by(&:ciName)
    respond_to do |format|
      format.html { render :action => :index }
      format.json { render :json => @clouds }
    end
  end

  def show
    edit
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
          load_teams unless global_admin_mode?
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
    load_teams unless global_admin_mode?
    respond_to do |format|
      format.html {render :action => :edit}
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
      if ok
        @proxy = locate_proxy(params[:id], clouds_ns_path)
        @proxy.destroy if @proxy
      else
        flash[:error] = "Cannot delete cloud '#{@cloud.ciName}': #{@cloud.errors.full_messages.join(';')}."
      end
    else
      ok = false
      flash[:error] = "Cannot delete cloud '#{@cloud.ciName}': #{cis.size} existing #{'environment/platform'.pluralize(cis.size)} using this cloud."
    end

    respond_to do |format|
      format.html { index }
      format.js
      format.json { render_json_ci_response(ok, @cloud) }
    end
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

    @state = params[:instances_state]
    @state = nil if @state == 'all'
    ops_states = @instances.blank? ? {} : Operations::Sensor.states(@instances)
    @instances = @instances.select do |i|
      state = ops_states[i.ciId]
      i.opsState = state
      @state.blank? || state == @state
    end

    respond_to do |format|
      format.js do
        @instance_procedures = Cms::Procedure.all(:params => {:nsPath    => organization_ns_path,
                                                              :recursive => true,
                                                              :actions   => true,
                                                              :state     => 'active,pending',
                                                              :limit     => 1000}).inject({}) do |m, p|
          p.actions.each {|a| m[a.ciId] = p}
          m
        end
      end

      format.json { render :json => @instances }
    end
  end

  def procedures
    @procedures = Cms::Procedure.all(:params => {:ciId => @cloud.ciId, :limit => 100})

    respond_to do |format|
      format.js
      format.json { render :json => @procedures }
    end
  end

  def locations
    render :json => load_available_clouds.to_map_with_value {|c| [c.ciName, "#{c.nsPath}/#{c.ciName}"]}
  end

  def services
    services_ns_path, provides_ns_path = deduce_ns_path
    return if services_ns_path.blank?

    services = Cms::Relation.all(:params => {:nsPath            => provides_ns_path,
                                             :relationShortName => 'Provides',
                                             :includeToCi       => true,
                                             :recursive         => true}).inject({}) do |m, r|
      service = r.toCi
      (m[r.relationAttributes.service] ||= []) << service if r.toCi.nsPath.start_with?(services_ns_path)
      m
    end

    render :json => services
  end

  def offerings
    ns_path, provides_ns_path = deduce_ns_path
    return if ns_path.blank?

    services  = Cms::Relation.all(:params => {:nsPath            => provides_ns_path,
                                              :relationShortName => 'Provides',
                                              :includeToCi       => true,
                                              :recursive         => true}).inject({}) do |h, r|
      h[r.toCiId] = r.relationAttributes.service
      h
    end

    offerings = Cms::Relation.all(:params => {:nsPath            => ns_path,
                                              :relationShortName => 'Offers',
                                              :includeToCi       => true,
                                              :recursive         => true}).inject({}) do |h, r|
      (h[services[r.fromCiId]] ||= []) << r.toCi
      h
    end
    render :json => offerings
  end


  protected

  def ci_resource
    @cloud
  end

  def search_ns_path
    cloud_ns_path(@cloud)
  end


  private

  def find_cloud
    @cloud = locate_cloud(params[:id])
  end

  def authorize_create
    unauthorized unless creates_clouds?
  end

  def authorize_update
    unauthorized unless @cloud && manages_cloud?(@cloud.ciId)
  end

  def authorize_support
    unauthorized unless @cloud && has_cloud_support?(@cloud.ciId)
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

  def deduce_ns_path
    org_name = params[:org_name]
    ns_path  = params[:ns_path]
    if ns_path.blank?
      ns_path = org_name.blank? ? '/public' : clouds_ns_path(org_name)
      provides_ns_path = ns_path
    elsif !ns_path.start_with?("#{org_name.blank? ? '/public' : organization_ns_path(org_name)}/")
      unauthorized
      return nil
    else
      # Unfortunately, we have inconsistency with Provides relations living in the org "_clouds" namespace instead of its
      # cloud name space.   So we will have to pull all of them and filter further in-memory.
      provides_ns_path = clouds_ns_path(org_name)
    end
    return ns_path, provides_ns_path
  end
end
