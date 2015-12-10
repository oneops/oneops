class CreateUserGroups < ActiveRecord::Migration
  def up
    create_table :groups do |t|
      t.string :name, :limit => 50
      t.string :created_by, :limit => 40, :null => false
      t.text :description

      t.timestamps
    end
    add_index :groups, :name

    create_table :group_members do |t|
      t.belongs_to :group
      t.belongs_to :user
      t.boolean :admin, :null => false, :default => false
      t.string :created_by, :limit => 40, :null => false

      t.timestamps
    end

    create_table :groups_teams do |t|
      t.belongs_to :group
      t.belongs_to :team
    end

    # Clean-up: delete orphaned teams.
    orphaned_org_ids = Team.pluck(:organization_id).uniq
    org_ids = Organization.pluck(:id)
    (orphaned_org_ids - org_ids).each do |org_id|
      Team.where(:organization_id => org_id).each do |team|
        if team
          puts "Found orphaned team: id=#{team.id} name='#{team.name}' org_id=#{org_id} - deleting..."
          team.destroy
        end
      end
    end
  end

  def down
    drop_table :groups
    drop_table :group_members
    drop_table :groups_teams
  end
end
