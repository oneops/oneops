class AssembliesController < ApplicationController
  include ::TeamAccess

  before_filter :find_assembly, :only => [:search, :show, :edit, :update, :destroy, :new_clone, :clone,
                                          :teams, :update_teams, :reports, :notifications, :cost_rate]

  before_filter :authorize_create, :only => [:new, :create, :new_clone, :clone]
  before_filter :authorize_update, :only => [:update, :destroy, :update_teams]

  before_filter :find_proxy, :only => [:destroy, :teams]

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

  def new
    @assembly = Cms::Ci.build({:nsPath => organization_ns_path, :ciClassName => 'account.Assembly'})

    respond_to do |format|
      format.html { load_catalog_templates }
      format.json { render_json_ci_response(true, @assembly) }
    end
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
      format.json { render_json_ci_response(ok, @assembly) }
    end
  end

  def destroy
    ok = true
    Cms::Relation.all(:params => {:ciId              => @assembly.ciId,
                                  :direction         => 'from',
                                  :relationShortName => 'RealizedIn',
                                  :targetClassName   => 'manifest.Environment'}).each do |e|
      ok = Cms::Ci.count("#{environment_ns_path(e.toCi)}/bom", true) == 0
      break unless ok
    end

    if ok
      ok = execute(@assembly, :destroy)
      if ok && @proxy
        @proxy.watched_by_users.clear
        @proxy.destroy
      end
    else
      message = 'Cannot delete assembly with deployments.  Please disable all platforms in all environments before deleting the assembly.'
      flash[:error] = message
      @assembly.errors.add(:base, message)
    end

    respond_to do |format|
      format.html { index }
      format.json { render_json_ci_response(ok, @assembly) }
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

  def notifications
    @notifications = Search::Notification.find_by_ns("#{assembly_ns_path(@assembly)}/", :size => 50)
  end


  def cost_rate
    @cost_rate = Search::Cost.cost_rate("#{assembly_ns_path(@assembly)}/*/bom")
    respond_to do |format|
      format.js
      format.json {render :json => @cost_rate}
    end
  end

  def cost
    begin
      @start = Date.parse(params[:start_date])
    rescue Exception => e
      @start = Date.today.prev_month(2).beginning_of_month
    end
    begin
      @end = Date.parse(params[:end_date])
    rescue Exception => e
      @end = Date.today.end_of_month
    end
    @end = @start.end_of_month if @start > @end

    groupings = [{:name => :service_type, :label => 'By Service Type'},
                 {:name => :cloud, :label => 'By Cloud'}]

    grouping_values = {:service_type => %w(compute storage dns), :cloud => %w(dal1 dfw1 dfw2 ndc)}
    months = (@start.month..@end.month).to_a
    y = months.inject([]) do |a, m|
      month_cost = rand(20) + 10
      a << groupings.inject({}) do |h, g|
        grouping_name = g[:name]
        vals = []
        grouping_cost = month_cost
        grouping_values[grouping_name].each_with_index() do |v, i|
          cost = i == grouping_values[grouping_name].size - 1 ?  grouping_cost : rand(grouping_cost)
          grouping_cost -= cost
          vals << {:label => v, :value => cost} if cost > 0
        end
        h[grouping_name] = vals.sort! {|v1, v2| v2[:value] <=> v1[:value]}
        h
      end
    end
    @cost = {:title     => 'Monthly Cost',
             :labels    => {:x => 'Month', :y => 'Cost (USD)'},
             :groupings => groupings,
             :x         => months.map {|m| Date::ABBR_MONTHNAMES[m]},
             :y         => y}

    # @cost = {:title     => 'Monthly Cost',
    #          :labels    => {:x => 'Month', :y => 'Cost (USD)'},
    #          :groupings => [{:name => :service_type, :label => 'By Service Type'},
    #                         {:name => :cloud, :label => 'By Cloud'}],
    #          :x         => %w(Aug Sep Oct Nov),
    #          :y         => [{:service_type => [{:label => 'compute', :value => 7},
    #                                            {:label => 'dns', :value => 1},
    #                                            {:label => 'storage', :value => 4}],
    #                          :cloud        => [{:label => 'dal1', :value => 16},
    #                                            {:label => 'dfw1', :value => 13},
    #                                            {:label => 'dfw2', :value => 3}]},
    #                         {:service_type => [{:label => 'compute', :value => 2.7},
    #                                            {:label => 'dns', :value => 1.4},
    #                                            {:label => 'storage', :value => 1.2}],
    #                          :cloud        => [{:label => 'dal1', :value => 3.7},
    #                                            {:label => 'dfw1', :value => 2.4},
    #                                            {:label => 'dfw3', :value => 1.5}]},
    #                         {:service_type => [{:label => 'compute', :value => 23},
    #                                            {:label => 'dns', :value => 6},
    #                                            {:label => 'storage', :value => 4}],
    #                          :cloud        => [{:label => 'dal1', :value => 13},
    #                                            {:label => 'dfw1', :value => 7},
    #                                            {:label => 'dfw2', :value => 2},
    #                                            {:label => 'dfw3', :value => 5}]},
    #                         {:service_type => [{:label => 'compute', :value => 15},
    #                                            {:label => 'whatever', :value => 5.5},
    #                                            {:label => 'dns', :value => 2.5},
    #                                            {:label => 'storage', :value => 5.5}],
    #                          :cloud        => [{:label => 'dal1', :value => 8},
    #                                            {:label => 'dfw1', :value => 7},
    #                                            {:label => 'dfw2', :value => 6}]}]}
    # @cost = {:title  => 'Monthly Cost',
    #          :labels => {:x => 'Month', :y => 'Cost (USD)'},
    #          :x      => %w(Aug Sep Oct Nov),
    #          :y      => [12, 5, 19, 25]}
    respond_to do |format|
      format.html {render 'assemblies/_cost'}
      format.js
      format.json {render :json => @cost_rate}
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
      #a.release      = Cms::Release.latest(:nsPath => assembly_ns_path(a))
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
