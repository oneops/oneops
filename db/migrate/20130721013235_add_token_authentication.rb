class AddTokenAuthentication < ActiveRecord::Migration
  def self.up
    add_column :users, :authentication_token, :string
    add_index  :users, :authentication_token, :unique => true
    User.all.each {|u| u.ensure_authentication_token}
  end

  def self.down
    remove_column :users, :authentication_token
    remove_index  :users, :authentication_token
  end
end
