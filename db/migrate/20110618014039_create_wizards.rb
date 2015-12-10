class CreateWizards < ActiveRecord::Migration
  def self.up
    add_column :users, :show_wizard, :boolean, :null => false, :default => true

    create_table :wizards do |t|
      t.integer :organization_id
      t.string  :wizard_type, :limit => 50
      t.integer :resource_id
      t.integer :status

      t.timestamps
    end
  end

  def self.down
    drop_table :wizards
    remove_column :users, :show_wizard
  end
end
