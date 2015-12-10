class AddUsernameToUsers < ActiveRecord::Migration
  def up
    add_column :users, :username, :string, :limit => 40
    User.all.each {|u| u.update_attribute(:username, u.email[0..(u.email.index('@') || 0) - 1])}
    add_index :users, :username, :unique => true
  end

  def down
    remove_column :users, :username
  end
end
