class CreateUserWatches < ActiveRecord::Migration
  def up
    create_table :user_watches do |t|
      t.belongs_to :user
      t.belongs_to :ci_proxy
    end

  end

  def down
    drop_table :user_watches
  end
end
