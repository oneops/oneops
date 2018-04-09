module GlobalAdmin
  def self.included(base)
    base.class_eval do
      helper_method :global_admin_mode?, :is_global_admin?, :violates_admin_limit?
    end
  end

  protected

  def global_admin_mode?
    User.global_admin_mode?
  end

  def is_global_admin?
    current_user.is_global_admin?
  end

  def authorize_global_admin
    unauthorized unless global_admin_mode? ? is_global_admin? : is_admin?
  end

  def violates_admin_limit?(admin_team = current_user.organization.admin_team, incoming_member = nil)
    return false unless admin_team.name == Team::ADMINS

    limit = Settings.max_admin_limit
    return false unless limit.present?

    org = current_user.organization

    global_admin_ids = []
    global_admin_groups = Settings.global_admin_groups
    if global_admin_groups.present?
      global_admin_groups = global_admin_groups.split(',')
      global_admin_ids = Group.where(:name => global_admin_groups.map(&:strip)).joins(:users).pluck('users.id').uniq
    end

    current_admin_ids   = org.admin_users.ids + org.admin_group_users.ids
    current_admin_count = (current_admin_ids - global_admin_ids).uniq.size

    user_ids        = (org.users.ids + org.group_users.ids).uniq
    user_count      = (user_ids - global_admin_ids).size
    max_admin_count = limit < 1 ? (user_count * limit).to_i : limit

    incoming_admin_count = 0
    if incoming_member
      incoming_admin_ids   = incoming_member.is_a?(User) ? [incoming_member.id] : incoming_member.users.ids
      incoming_admin_count = (incoming_admin_ids - current_admin_ids).uniq.size
    end

    return current_admin_count + incoming_admin_count > max_admin_count ? [current_admin_count, incoming_admin_count, max_admin_count, global_admin_groups] : false
  end

  def check_admin_limit(admin_team, incoming_member)
    return nil unless violates_admin_limit?(admin_team, incoming_member)

    return "Can not add this #{incoming_member.class.name.downcase} due to volation of max admin count policy - too many admins!"
  end
end
