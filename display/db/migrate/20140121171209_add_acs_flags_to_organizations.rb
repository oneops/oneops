class AddAcsFlagsToOrganizations < ActiveRecord::Migration
  def up
    add_column :organizations, :assemblies, :boolean, :null => false, :default => true
    add_column :organizations, :catalogs,   :boolean, :null => false, :default => false
    add_column :organizations, :services,   :boolean, :null => false, :default => false
  end

  def down
    remove_column :organizations, :assemblies
    remove_column :organizations, :catalogs
    remove_column :organizations, :services
  end
end
