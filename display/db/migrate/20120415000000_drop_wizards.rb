class DropWizards < ActiveRecord::Migration
  def up
    drop_table :wizards
  end

  def down
    create_table "wizards" do |t|
      t.integer  "organization_id"
      t.string   "wizard_type", :limit => 50
      t.integer  "resource_id"
      t.integer  "status"
      t.datetime "created_at"
      t.datetime "updated_at"
    end
  end
end
