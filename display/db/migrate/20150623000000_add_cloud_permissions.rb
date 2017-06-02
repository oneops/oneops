class AddCloudPermissions < ActiveRecord::Migration
  def up
    add_column :teams, :manages_access,   :boolean, :null => false, :default => false
    add_column :teams, :cloud_services,   :boolean, :null => false, :default => false
    add_column :teams, :cloud_compliance, :boolean, :null => false, :default => false
    add_column :teams, :cloud_support,    :boolean, :null => false, :default => false

    Team.reset_column_information
    Team.where(:name => Team::ADMINS).each {|t| t.update_attributes(:cloud_services => true, :cloud_compliance => true, :cloud_support => true)}
  end

  def down
    remove_column :teams, :manages_access
    remove_column :teams, :cloud_services
    remove_column :teams, :cloud_compliance
    remove_column :teams, :cloud_support
  end
end
