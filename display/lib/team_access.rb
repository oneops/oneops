module TeamAccess
  def process_update_teams(ci)
    proxy = find_or_create_proxy(ci)
    team_ids = params[:teams]
    if team_ids
      proxy.team_ids = team_ids.blank? ? [] : validate_team_ids(team_ids)
    else
      add_team_ids = params[:add]
      proxy.team_ids = proxy.team_ids + validate_team_ids(add_team_ids) if add_team_ids.present?
      remove_team_ids = params[:remove]
      proxy.team_ids = proxy.team_ids - validate_team_ids(remove_team_ids) if remove_team_ids.present?
    end
    proxy.teams
  end


  protected

  def validate_team_ids(team_ids)
    current_user.organization.teams.where('teams.id in (?)', team_ids.select(&:present?).map(&:to_i)).pluck('teams.id')
  end
end
