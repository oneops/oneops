class FixPermissionsForAdmins < ActiveRecord::Migration
  def up
    Team.where(:name => Team::ADMINS).update_all(:design           => true,
                                                 :transition       => true,
                                                 :operations       => true,
                                                 :cloud_services   => true,
                                                 :cloud_compliance => true,
                                                 :cloud_support    => true,
                                                 :manages_access   => true,
                                                 :org_scope        => true)

  end

  def down
  end
end
