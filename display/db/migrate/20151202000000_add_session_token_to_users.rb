class AddSessionTokenToUsers < ActiveRecord::Migration
  def up
    add_column :users, :session_token, :string, :limit => 30
  end

  def down
    remove_column :users, :session_token
  end
end
