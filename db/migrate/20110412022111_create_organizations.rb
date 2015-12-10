class CreateOrganizations < ActiveRecord::Migration
  def self.up
    create_table :organizations do |t|
      t.string :name, :limit => 50

      t.timestamps
    end
    add_index :organizations, :name, :unique => true
  end

  def self.down
    drop_table :organizations
  end
end
