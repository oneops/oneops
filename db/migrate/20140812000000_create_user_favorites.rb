class CreateUserFavorites < ActiveRecord::Migration
  def up
    create_table :user_favorites do |t|
      t.belongs_to :user
      t.belongs_to :ci_proxy
    end

  end

  def down
    drop_table :user_favorites
  end
end
