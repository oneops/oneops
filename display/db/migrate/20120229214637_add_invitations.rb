class AddInvitations < ActiveRecord::Migration
  def self.up
    create_table :invitations do |t|
      t.string  :email, :null => :false
      t.string  :token, :limit => 8, :null => :false
      t.string  :comment, :limit => 200

      t.timestamps
    end
  end

  def self.down
    drop_table :invitations
  end
end
