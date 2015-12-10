class AddOrgScopeToTeams < ActiveRecord::Migration
  def up
    add_column :teams, :org_scope, :boolean, :null => false, :default => false
  end

  def down
    remove_column :teams, :org_scope
  end
end
