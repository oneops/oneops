class AddAnnouncementToOrganizations < ActiveRecord::Migration
  def up
    add_column :organizations, :announcement, :text
  end

  def down
    remove_column :organizations, :announcement
  end
end
