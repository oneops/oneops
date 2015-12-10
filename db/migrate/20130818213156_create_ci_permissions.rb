class CreateCiPermissions < ActiveRecord::Migration
  def up
    create_table :ci_proxies do |t|
      t.integer :organization_id, :null => false
      t.integer :ci_id, :null => false
      t.string :ci_name, :limit => 100, :null => false
      t.string :ns_path, :limit => 250, :null => false

      t.timestamps
    end

    create_table :ci_proxies_teams do |t|
      t.belongs_to :ci_proxy
      t.belongs_to :team
    end

    add_index :ci_proxies, :organization_id
    add_index :ci_proxies, :ci_id, :unique => true
    add_index :ci_proxies, :ci_name

  end

  def down
    drop_table :ci_proxies
    drop_table :ci_proxies_teams
  end
end
