class AssignUsersToTeams < ActiveRecord::Migration
  def up
    Organization.all.each do |org|
      team = org.teams.create(:name => Team::ADMINS)
      User.where(:organization_id => org.id).each {|user| team.users << user}
    end
  end

  def down
  end
end
