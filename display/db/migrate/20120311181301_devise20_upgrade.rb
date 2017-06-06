class Devise20Upgrade < ActiveRecord::Migration
  def up
# 12.05.31 - mjs - fix for fresh install    
#    add_column :users, :reset_password_sent_at, :datetime
    add_column :users, :password_salt, :string
    add_column :users, :unconfirmed_email, :string

  end

  def down
#    remove_column :users, :reset_password_sent_at, :datetime
    remove_column :users, :password_salt, :string
    remove_column :users, :unconfirmed_email, :string
  end
end
