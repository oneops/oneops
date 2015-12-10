class Organization::TeamsController < ApplicationController
  before_filter :authorize_admin, :except => [:index, :show, :edit]
  before_filter :find_team, :only => [:show, :edit, :update, :destroy]

  def index
    @teams = current_user.organization.teams.order(:name)
    @teams = @teams.joins(:users).where('users.id = ? OR teams.name = ?', current_user.id, Team::ADMINS).uniq unless is_admin?
    @user_count = current_user.organization.teams.joins(:users).select('teams.id, count(users.id) as user_count').group('teams.id').inject({}) do |m, team|
      m[team.id] = team.user_count.to_i
      m
    end
    @group_count = current_user.organization.teams.joins(:groups).select('teams.id, count(groups.id) as group_count').group('teams.id').inject({}) do |m, team|
      m[team.id] = team.group_count.to_i
      m
    end

    respond_to do |format|
      format.js { render :action => :index }
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
