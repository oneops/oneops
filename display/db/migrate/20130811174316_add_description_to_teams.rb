class AddDescriptionToTeams < ActiveRecord::Migration
  def up
    add_column :teams, :description, :text
    Team.reset_column_information
    Team.where(:name => Team::ADMINS).update_all(['description = ?', 'Most powerful users with full spectrum of permissions.'])
    add_index :teams, [:organization_id, :name], :unique => true
  end

  def down
    remove_column :teams, :description
    remove_index :teams, [:organization_id, :name]
  end
end
