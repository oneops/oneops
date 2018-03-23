class Organization::TeamsController < ApplicationController
  before_filter :authorize_admin, :except => [:index, :show, :edit]
  before_filter :find_team, :only => [:show, :edit, :update, :destroy]

  def index
    org = current_user.organization
    if is_admin?
      @teams = org.teams.order(:name).all
      scope = org.teams
    else
      @teams = current_user.all_teams
      manage_access_team_ids = @teams.select {|t| t.manages_access}.map(&:id)

      # Allow to browse the teams assigned to assemblies which current user manages.
      manage_assembly_ids = org.ci_proxies.joins(:teams).
        where(:ns_path => organization_ns_path).
        where('teams.id IN (?)', manage_access_team_ids).pluck(:ci_id)
      if manage_access_team_ids.present?
        @teams += Team.joins(:ci_proxies).where('ci_proxies.ci_id in (?)', manage_assembly_ids).all
      end

      # Allow to browse admin team.
      @teams << org.admin_team
      @teams.uniq!(&:id)
      scope = Team.where(:id => @teams.map(&:id))
    end

    respond_to do |format|
      format.js do
        @user_count = scope.joins(:users).
          select('teams.id, count(users.id) as user_count').
          group('teams.id').
          inject({}) do |m, team|
            m[team.id] = team.user_count.to_i
            m
        end
        @group_count = scope.joins(:groups).
          select('teams.id, count(groups.id) as group_count').
          group('teams.id').
          inject({}) do |m, team|
            m[team.id] = team.group_count.to_i
            m
        end

        render :action => :index
      end

      format.json { render :json => @teams }
    end
  end

  def show
    render :json => @team
  end

  def new
    @team = current_user.organization.teams.build
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render :json => @team }
    end
  end

  def create
    @team = current_user.organization.teams.build(strong_params)
    ok = @team.save

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render :json => render_json_ci_response(ok, @team) }
    end
  end

  def edit
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render :json => @team }
    end
  end

  def update
    ok = @team.update_attributes(strong_params)
    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render :json => render_json_ci_response(ok, @team) }
    end
  end

  def destroy
    ok = true
    if @team.name == Team::ADMINS
      @team.errors.add(:base, "Not allowed to delete team '#{Team::ADMINS}'.")
      ok = false
    end

    if ok
      ok = @team.destroy
      @team.errors.add(:base, "Cannot delete team: #{@team.errors.full_messages.join(';')}.") unless ok
    end

    flash[:error] = "Cannot delete team: #{@team.errors.full_messages.join(';')}." unless ok

    respond_to do |format|
      format.js { index }
      format.json { render :json => render_json_ci_response(ok, @team) }
    end
  end


  private

  def find_team
    @team = current_user.organization.teams.find(params[:id])
  end

  def strong_params
    params[:team].permit(:organization, :organization_id, :name, :description,
                         :manages_access, :org_scope,
                         :design, :transition, :operations,
                         :cloud_services, :cloud_compliance, :cloud_support)
  end
end
