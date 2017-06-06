class AddNameToUsers < ActiveRecord::Migration
  def up
    add_column :users, :name, :string, :limit => 50
  end

  def down
    remove_column :users, :name
  end
end
