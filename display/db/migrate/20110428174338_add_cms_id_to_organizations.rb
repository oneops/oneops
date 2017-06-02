class AddCmsIdToOrganizations < ActiveRecord::Migration
  def self.up
    add_column :organizations, :cms_id, :integer
  end

  def self.down
    remove_column :organizations, :cms_id
  end
end
