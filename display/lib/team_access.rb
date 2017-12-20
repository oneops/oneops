module TeamAccess
  def teams
    load_teams
    respond_to do |format|
      format.js   { render :action => :teams }
      format.json { render :json => @teams }
    end
  end

  def update_teams
    process_update_teams(ci_resource)
    teams
  end


  protected

  def load_teams
    @proxy ||= locate_proxy(ci_resource.ciId, ci_resource.nsPath)
    @teams = current_user.organization.teams.where(:org_scope => true).all
    @teams |= @proxy.teams if @proxy
  end

  def process_update_teams(ci)
    @proxy = find_or_create_proxy(ci)
    team_ids = params[:teams]
    if team_ids
      @proxy.team_ids = team_ids.blank? ? [] : validate_team_ids(team_ids)
    else
      add_team_ids = params[:add]
      @proxy.team_ids = @proxy.team_ids + validate_team_ids(add_team_ids) if add_team_ids.present?
      remove_team_ids = params[:remove]
      @proxy.team_ids = @proxy.team_ids - validate_team_ids(remove_team_ids) if remove_team_ids.present?
    end
  end

  def validate_team_ids(team_ids)
    current_user.organization.teams.where('teams.id in (?)', team_ids.select(&:present?).map(&:to_i)).pluck('teams.id')
  end
end
