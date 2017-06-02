class CreateTeams < ActiveRecord::Migration
  def up
    create_table :teams do |t|
      t.string :name, :limit => 50
      t.integer :organization_id, :null => false

      t.timestamps
    end
    add_index :teams, :organization_id

    create_table :teams_users do |t|
      t.belongs_to :team
      t.belongs_to :user
    end
  end

  def down
    drop_table :teams
    drop_table :teams_users
  end
end
