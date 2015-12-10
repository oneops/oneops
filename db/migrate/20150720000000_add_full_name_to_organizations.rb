class AddFullNameToOrganizations < ActiveRecord::Migration
  def up
    add_column :organizations, :full_name, :string, :limit => 100
  end

  def down
    remove_column :organizations, :full_name
  end
end
