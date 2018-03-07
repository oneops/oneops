module AdminLimit

  protected

  def check_max_admin_limit(team, member)
    return nil unless team.name == Team::ADMINS

    limit = Settings.max_admin_limit
    return nil unless limit.present?

    org = current_user.organization

    global_admin_ids = []
    global_admin_groups = Settings.global_admin_groups
    if global_admin_groups.present?
      global_admin_ids = team.groups.where(:name => global_admin_groups.split(',').map(&:strip)).joins(:users).pluck('users.id').uniq
    end

    current_admin_ids   = org.admin_users.ids + org.admin_group_users.ids
    current_admin_count = (current_admin_ids - global_admin_ids).uniq.size

    incoming_admin_ids = member.is_a?(User) ? [member.id] : member.users.ids
    incoming_admin_count = (incoming_admin_ids - current_admin_ids).uniq.size

    user_ids        = (org.users.ids + org.group_users.ids).uniq
    user_count      = (user_ids - global_admin_ids).size
    max_admin_count = limit < 1 ? (user_count * limit).to_i : limit

    return nil if current_admin_count + incoming_admin_count <= max_admin_count

    return "Too many admins: adding this #{member.class.name.downcase} will exceed max allowed admin limit."
  end
end
