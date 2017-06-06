class AddLastSignInAtToTeamUsers < ActiveRecord::Migration
  def change
    add_column :teams_users, :last_sign_in_at, :datetime
  end
end
