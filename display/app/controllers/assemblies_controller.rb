class AssembliesController < ApplicationController
  include ::TeamAccess, ::CostSummary, ::NotificationSummary, ::Health, ::Search

  before_filter :find_assembly, :only => [:search, :show, :edit, :update, :destroy, :new_clone, :clone,
                                          :teams, :update_teams, :users, :reports, :notifications, :cost_rate, :cost, :health]

  before_filter :authorize_create, :only => [:new, :create, :new_clone, :clone]
  before_filter :authorize_update, :only => [:update, :destroy, :update_teams]

  swagger_controller :assemblies, 'Assembly Management'

  swagger_api :index do
    summary 'Fetch all org assemblies accessible by current user.'
    notes 'This lists all organization assemblies subject to access right.  Users with admin and organization ' \
           'scope team priviliges will see all assemblies. Other users will see only assemblies that are assoociated ' \
           'with any of the teams they belong to.'
    param_org_name
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
    summary 'Fetch assembly.'
    notes 'This fetches an assembly by CI id or name.'
    param_org_name
    param_path_ci_id :assembly
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
        @instance_count = instance_by_cloud_count.values.sum
      end

      format.json { render(:json => @assembly, :status => @assembly ? :ok : :not_found) }
    end
  end

  swagger_api :new do
    summary 'Build a new assembly CI json with default attribute values.'
    param_org_name
  end
  def new
    @assembly = Cms::Ci.build({:nsPath => organization_ns_path, :ciClassName => 'account.Assembly'})

    respond_to do |format|
      format.html { load_catalog_templates }
      format.json { render_json_ci_response(true, @assembly) }
    end
  end

  swagger_api :create do
    summary 'Create a new assembly.'
    notes 'If <b>catalog_template</b> is specified it will try to find a corresponding catalog and create ' \
          'a design defined by the catalog.'
    param_org_name
    param :form, 'cms_ci', :json, :required, 'Assembly CI structure.'
    param :form, 'catalog_template', :string, :optional, 'Name of exisiting catalog defining a design that will be automatically created in this new assembly.'
  end
  def create
    design_id = params[:catalog_template]
    if design_id.present?
      assembly_id = Transistor.create_assembly_from_catalog(design_id, params[:cms_ci].merge(:nsPath => organization_ns_path, :ciClassName => 'account.Assembly'))
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
      format.html {render 'assemblies/_settings'}
      format.js
      format.json { render_json_ci_response(true, @assembly) }
    end
  end

  swagger_api :update, :responses => [:not_found] do
    summary 'Update assembly.'
    param_org_name
    param_path_ci_id :assembly
    param :body, :body, :json, :required, 'Assembly CI structure.'
  end
  def update
    ok = execute(@assembly, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js {render :action => :edit}
      format.json {render_json_ci_response(ok, @assembly)}
    end
  end

  swagger_api :destroy, :responses => [:not_found] do
    summary 'Delete assembly.'
    notes 'This deletes an assembly by CI id or name. Deteling is allowed only if assembly has no deployed instances.'
    param_org_name
    param_path_ci_id :assembly
  end
  def destroy
    count          = instance_by_cloud_count
    cloud_count    = count.size
    instance_count = count.values.sum
    ok             = instance_count == 0

    if ok
      ok = execute(@assembly, :destroy)
      if ok
        @proxy = locate_proxy(params[:id], organization_ns_path)
        if @proxy
          @proxy.watched_by_users.clear
          @proxy.destroy
        end
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
    ci     = nil
    errors = nil
    export = params[:export]
    if export.present?
      action = 'save'
      ci = Cms::Ci.build(:ciName       => params[:ciName],
                         :nsPath       => private_catalog_designs_ns_path,
                         :ciClassName  => 'account.Design',
                         :ciAttributes => {:description => params[:description]})
    else
      action   = 'clone'
      org_name = params[:to_org].presence || current_user.organization.name
      org      = locate_org(org_name)
      team     = current_user.creates_assemblies?(org.id)
      if org && team
        ci = Cms::Ci.build(:ciName       => params[:ciName],
                           :nsPath       => organization_ns_path(org.name),
                           :ciClassName  => 'account.Assembly',
                           :ciAttributes => {:description => params[:description],
                                             :owner       => current_user.email.presence || @assembly.ciAttributes.owner})
      else
        errors = ["No permission to create assembly in organization '#{org_name}'."]
      end
    end

    if ci
      ci.valid?
      errors = ci.errors.full_messages
    end
    if errors.blank?
      ci_id, message = Transistor.clone_assembly(@assembly.ciId, ci)
      if ci_id
        Cms::Ci.headers['X-Cms-Scope'] = ci.nsPath if action == 'clone'
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

  def tags
    render :json => AssemblyTag.new([current_user.organization.ci], locate_assemblies)
  end

  def users
    org = current_user.organization
    users = org.admin_users.inject({})  do |h, u|
      h[u.id] = {:user => u, :dto => Team::DTO_ALL, :teams => {Team::ADMINS => true}}
      h
    end

    aggregator = lambda do |h, u|
      h[u.id] ||= {:user => u, :dto => Team::DTO_NONE, :manages_access =>  false, :teams => {}}
      h[u.id][:dto] |= Team.calculate_dto_permissions(u.design, u.transition, u.operations) if h[u.id][:dto] < Team::DTO_ALL
      h[u.id][:manages_access] ||= u.manages_access
      h[u.id][:teams][u.team] = true
      h
    end

    org_id = org.id
    select = 'users.*, teams.name as team, teams.design as design, teams.transition as transition, teams.operations as operations, teams.manages_access as manages_access'

    where = {'teams.organization_id' => org_id, 'teams.org_scope' => true}
    users = User.joins(:teams).select(select).where(where).inject(users, &aggregator)
    users = User.joins(:teams_via_groups).select(select).where(where).inject(users, &aggregator)

    where = {'teams.organization_id' => org_id, 'ci_proxies.ci_id' => @assembly.ciId}
    users = User.joins(:teams => :ci_proxies).select(select).where(where).inject(users, &aggregator)
    users = User.joins(:teams_via_groups => :ci_proxies).select(select).where(where).inject(users, &aggregator)

    @users = users.values.sort_by {|u| u[:user].username}.map do |r|
      user = r[:user]
      dto  = r[:dto]
      {:id              => user.id,
       :username        => user.username,
       :email           => user.email,
       :name            => user.name,
       :created_at      => user.created_at,
       :last_sign_in_at => user.current_sign_in_at || user.last_sign_in_at,
       :manages_access  => r[:manages_access],
       :design          => dto & Team::DTO_DESIGN > 0,
       :transition      => dto & Team::DTO_TRANSITION > 0,
       :operations      => dto & Team::DTO_OPERATIONS > 0,
       :teams           => r[:teams].keys
      }
    end

    respond_to do |format|
      format.js

      format.csv do
        fields = [:id, :username, :email, :name, :created_at, :last_sign_in_at, :manages_access, :design, :transition, :operations, :teams]
        data = @users.map do |u|
          fields.map do |f|
            value = u[f]
            value.is_a?(Array) ? value.join(' ') : value
          end.join(',')
        end
        render :text => fields.join(',') + "\n" + data.join("\n")   #, :content_type => 'text/data_string'
      end

      format.yaml {render :text => @users.to_yaml, :content_type => 'text/data_string'}

      format.any {render :json => users}
    end
  end


  protected

  def ci_resource
    @assembly
  end

  def search_ns_path
    assembly_ns_path(@assembly)
  end


  private

  def find_assembly
    id = params[:id]
    @assembly = locate_assembly(id) if id
  end

  def authorize_create
    unauthorized unless creates_assemblies?
  end

  def authorize_update
    unauthorized unless @assembly && (manages_assembly?(@assembly.ciId) || (action_name == 'update' && has_design?))
  end

  def load_assembly_list
    platforms = Cms::Ci.all(:params => {:nsPath => "#{organization_ns_path}/", :ciClassName => 'catalog.Platform', :recursive => true}).inject({}) do |m, p|
      root, org, name = p.nsPath.split('/', 3)
      m[name] = (m[name] || []) << p
      m
    end

    environments = Cms::Ci.all(:params => {:nsPath => "#{organization_ns_path}/", :ciClassName => 'manifest.Environment', :recursive => true}).inject({}) do |m, p|
      root, org, name = p.nsPath.split('/', 3)
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

  def load_catalog_templates
    @catalog_designs = Cms::Ci.all(:params => {:nsPath => catalog_designs_ns_path,         :ciClassName => 'account.Design'}) +
                         Cms::Ci.all(:params => {:nsPath => private_catalog_designs_ns_path, :ciClassName => 'account.Design'})
  end

  def instance_by_cloud_count
    Cms::Relation.count(:nsPath            => assembly_ns_path(@assembly),
                        :recursive         => true,
                        :relationShortName => 'DeployedTo',
                        :direction         => 'to',
                        :groupBy           => 'ciId')
  end

  class AssemblyTag < Hash
    def initialize(orgs, assemblies)
      orgs.each do |o|
        if o.is_a?(Hash)
          self[o['ciName']] = ::CiTags.parse_tags(o['ciAttributes'])
        else
          self[o.ciName] = ::CiTags.parse_tags(o.ciAttributes)
        end
      end
      assemblies.each do |a|
        if a.is_a?(Hash)
          self["#{a['nsPath'].split('/')[1]}/#{a['ciName']}"] = ::CiTags.parse_tags(a['ciAttributes'])
        else
          self["#{a.nsPath.split('/')[1]}/#{a.ciName}"] = ::CiTags.parse_tags(a.ciAttributes)
        end
      end
    end

    def get(org, assembly, tag)
      key = "#{org}/#{assembly}"
      (include?(key) && self[key][:tags][tag]) ||
        (include?(org) && self[org][:tags][tag]) ||
        "#{key} -> #{(include?(key) && self[key][:owner]) || (include?(org) &&  self[org][:owner]) || '???'}"
    end
  end
end
