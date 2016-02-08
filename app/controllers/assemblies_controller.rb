class AssembliesController < ApplicationController
  include ::TeamAccess, ::CostSummary, ::NotificationSummary

  before_filter :find_assembly, :only => [:search, :show, :edit, :update, :destroy, :new_clone, :clone,
                                          :teams, :update_teams, :reports, :notifications, :cost_rate, :cost]

  before_filter :authorize_create, :only => [:new, :create, :new_clone, :clone]
  before_filter :authorize_update, :only => [:update, :destroy, :update_teams]

  before_filter :find_proxy, :only => [:destroy, :teams]

  swagger_controller :assemblies, 'Assembly Management'

  swagger_api :index do
    summary 'Fetches all org assemblies accessible by current user.'
    notes 'This lists all organization assemblies subject to access right.  Users with admin and organization ' \
           'scope team priviliges will see all assemblies. Other users will see only assemblies that are assoociated ' \
           'with any of the teams they belong to.'
    param_org_name
    response :unauthorized
  end

  def index
    @assemblies = locate_assemblies.sort_by { |o| o.created_timestamp }

    respond_to do |format|
      format.html do
        load_assembly_list
        render :action => :index
      end

      format.js do
        load_assembly_list
        render :action => :index
      end

      format.json { render :json => @assemblies }
    end
  end

  swagger_api :show do
    summary 'Fetches an assembly.'
    notes 'This fetches an assembly by CI id or name.'
    param_org_name
    param_ci_id :assembly
    response :unauthorized
    response :not_found
  end

  def show
    respond_to do |format|
      format.html do
        @environments = Cms::Relation.all(:params => {:ciId              => @assembly.ciId,
                                                      :direction         => 'from',
                                                      :relationShortName => 'RealizedIn',
                                                      :targetClassName   => 'manifest.Environment'}).map(&:toCi)

        assembly_ns_path = assembly_ns_path(@assembly)
        consumes_rels = Cms::Relation.all(:params => {:nsPath            => assembly_ns_path,
                                                      :relationShortName => 'Consumes',
                                                      :includeToCi       => true,
                                                      :recursive         => true}).inject({}) do |m, rel|
          m[rel.fromCiId] ||= []
          m[rel.fromCiId] << rel
          m
        end

        @environments.each { |e| e.clouds = consumes_rels[e.ciId] || [] }
        @catalog = Cms::Release.latest(:nsPath => assembly_ns_path)
      end

      format.json { render(:json => @assembly, :status => @assembly ? :ok : :not_found) }
    end
  end

  swagger_api :new do
    summary 'Builds a new assembly CI with default attributes.'
    param_org_name
    response :unauthorized
  end

  def new
    @assembly = Cms::Ci.build({:nsPath => organization_ns_path, :ciClassName => 'account.Assembly'})

    respond_to do |format|
      format.html { load_catalog_templates }
      format.json { render_json_ci_response(true, @assembly) }
    end
  end

  swagger_api :create do
    summary 'Creates a new assembly.'
    notes 'If <b>catalog_template</b> is specified it will try to find a corresponding catalog and create ' \
          'a design defined by the catalog.'
    param_org_name
    param :form, 'cms_ci', :string, :required, 'Assembly CI object.'
    param :form, 'catalog_template', :string, :optional, 'Name of exisiting catalog defining a design that will be automatically created in this new assembly.'
    response :unauthorized
    response :unprocessable_entity
  end

  def create
    catalog_id = params[:catalog_template]
    if catalog_id.present?
      assembly_id = Transistor.create_assembly_from_catalog(catalog_id, params[:cms_ci].merge(:nsPath => organization_ns_path, :ciClassName => 'account.Assembly'))
      @assembly = Cms::Ci.find(assembly_id) if assembly_id.present?
      ok = @assembly.present?
    else
      relation = Cms::Relation.build({:relationName => 'base.Manages',
                                      :nsPath       => organization_ns_path,
                                      :fromCiId     => current_user.organization.cms_id,
                                      :toCi         => Cms::Ci.build(params[:cms_ci].merge(:nsPath      => organization_ns_path,
                                                                                           :ciClassName => 'account.Assembly'))})
      ok = execute_nested(relation.toCi, relation, :save)
      @assembly = relation.toCi
    end

    process_update_teams(@assembly) if ok

    respond_to do |format|
      format.html do
        if ok
          redirect_to assembly_design_path(@assembly)
        else
          load_catalog_templates
          render :action => :new
        end
      end

      format.json { render_json_ci_response(ok, @assembly) }
    end
  end

  def edit
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(true, @assembly) }
    end
  end

  def update
    ok = execute(@assembly, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js {render :action => :edit}
      format.json {render_json_ci_response(ok, @assembly)}
    end
  end

  swagger_api :destroy do
    summary 'Deletes an assembly.'
    notes 'This deletes an assembly by CI id or name. Deteling is allowed only if assembly has no deployed instances.'
    param_org_name
    param_ci_id :assembly
    response :unauthorized
    response :not_found
    response :unprocessable_entity
  end

  def destroy
    count = Cms::Relation.count(:nsPath            => assembly_ns_path(@assembly),
                                :recursive         => true,
                                :relationShortName => 'DeployedTo',
                                :direction         => 'to',
                                :groupBy           => 'ciId')
    cloud_count = count.size
    instance_count = count.values.sum
    ok = instance_count == 0

    if ok
      ok = execute(@assembly, :destroy)
      if ok && @proxy
        @proxy.watched_by_users.clear
        @proxy.destroy
      end
    else
      message = "Cannot delete assembly with deployments: there are #{instance_count} #{'instance'.pluralize(instance_count)} deployed to #{cloud_count} #{'cloud'.pluralize(cloud_count)}. Please disable all platforms in all environments before deleting the assembly."
      flash[:error] = message
      @assembly.errors.add(:base, message)
    end

    respond_to do |format|
      format.js { render(:json => '') unless ok }
      format.json {render_json_ci_response(ok, @assembly)}
    end
  end

  def new_clone
    @release = Cms::Release.latest( :nsPath => assembly_ns_path(@assembly) )
  end

  def clone
    errors = nil
    ci = {:ciName => params[:ciName], :ciAttributes => {:description => params[:description]}}
    export = params[:export]
    if export.present?
      action = 'save'
      ci[:nsPath]      = private_catalogs_ns_path
      ci[:ciClassName] = 'account.Design'
    else
      action   = 'clone'
      org_name = params[:to_org].presence || current_user.organization.name
      org      = current_user.organizations.where('organizations.name = ?', org_name).first
      team     = current_user.manages_access?(org.id)
      if org && team
        ci[:nsPath]      = organization_ns_path(org.name)
        ci[:ciClassName] = 'account.Assembly'
        ci[:ciAttributes][:owner] = @assembly.ciAttributes.owner
      else
        errors = ["No permission to create assembly in organization '#{org_name}'."]
      end
    end

    if errors.blank?
      ci_id, message = Transistor.clone_assembly(@assembly.ciId, ci)
      if ci_id
        Cms::Ci.headers['X-Cms-Scope'] = ci[:nsPath] if action == 'clone'
        @new_ci = Cms::Ci.find(ci_id)
        if export.blank? && !is_admin?(org)
          current_user.update_attribute(:organization_id, org.id)
          proxy = find_or_create_proxy(@new_ci)
          proxy.team_ids = [team.id]
        end
      else
        errors = ["Failed to #{action} assembly.", message]
      end
    end

    respond_to do |format|
      format.js { flash[:error] = errors.join('.') if errors.present? }
      format.json { render_json_ci_response(@new_ci.present?, @new_ci, errors) }
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
    unless @assembly && manages_access_for_assembly?(@assembly.ciId)
      unauthorized
      return
    end

    @teams = process_update_teams(@assembly)

    respond_to do |format|
      format.js   { render :action => :teams }
      format.json { render :json => @teams }
    end
  end


  protected

  def search_ns_path
    assembly_ns_path(@assembly)
  end


  private

  def find_assembly
    id = params[:id]
    @assembly = locate_assembly(id) if id
  end

  def authorize_create
    unauthorized unless manages_access?
  end

  def authorize_update
    unauthorized unless @assembly && manages_access_for_assembly?(@assembly.ciId)
  end

  def load_assembly_list
    platforms = Cms::Ci.all(:params => {:nsPath => "#{organization_ns_path}/", :ciClassName => 'catalog.Platform', :recursive => true}).inject({}) do |m, p|
      name    = p.nsPath.split('/').last
      m[name] = (m[name] || []) << p
      m
    end

    environments = Cms::Ci.all(:params => {:nsPath => "#{organization_ns_path}/", :ciClassName => 'manifest.Environment', :recursive => true}).inject({}) do |m, p|
      name    = p.nsPath.split('/').last
      m[name] = (m[name] || []) << p
      m
    end

    @assemblies.each do |a|
      #a.release      = Cms::Release.latest(:nsPath => ns_path(a))
      a.release      = nil
      a.platforms    = platforms[a.ciName] || []
      a.environments = environments[a.ciName] || []
    end
  end

  def find_proxy
    @proxy = locate_proxy(params[:id], organization_ns_path)
  end

  def load_catalog_templates
    @catalog_templates = Cms::Ci.all(:params => {:nsPath => catalogs_ns_path,         :ciClassName => 'account.Design'}) +
                         Cms::Ci.all(:params => {:nsPath => private_catalogs_ns_path, :ciClassName => 'account.Design'})
  end
end
