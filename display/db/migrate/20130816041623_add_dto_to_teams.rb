class AddDtoToTeams < ActiveRecord::Migration
  def up
    add_column :teams, :design,     :boolean, :null => false, :default => false
    add_column :teams, :transition, :boolean, :null => false, :default => false
    add_column :teams, :operations, :boolean, :null => false, :default => false

    Team.reset_column_information
    Team.where(:name => Team::ADMINS).each {|t| t.update_attributes(:design => true, :transition => true, :operations => true)}
  end

  def down
    remove_column :teams, :design
    remove_column :teams, :transition
    remove_column :teams, :operations
  end
end
