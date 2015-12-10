class DeviseCreateUsers < ActiveRecord::Migration
  def self.up
    create_table(:users) do |t|
      t.string :email, :null => false, :default => ""
      t.string :encrypted_password, :null => false, :default => "", :limit => 128

      t.string :reset_password_token
      t.datetime :reset_password_sent_at

      t.string :remember_token
      t.string :remember_created_at

      t.integer :sign_in_count, :default => 0
      t.datetime :current_sign_in_at
      t.datetime :last_sign_in_at
      t.string :current_sign_in_ip
      t.string :last_sign_in_ip

      t.string :confirmation_token
      t.datetime :confirmed_at
      t.datetime :confirmation_sent_at

      t.integer :failed_attempts, :default => 0
      t.string :unlock_token
      t.datetime :locked_at

      t.datetime :eula_accepted_at

      t.integer :organization_id, :null => false

      t.timestamps
    end

    add_index :users, :email, :unique => true
    add_index :users, :reset_password_token, :unique => true
    add_index :users, :confirmation_token, :unique => true
  end

  def self.down
    drop_table :users
  end
end
