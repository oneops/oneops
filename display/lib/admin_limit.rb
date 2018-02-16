module AdminLimit

  protected

  def check_max_admin_limit(team, member)
    return nil unless team.name == Team::ADMINS

    limit = Settings.max_admin_limit
    return nil unless limit.present?

    org = current_user.organization

    support_admin_ids = []
    admin_group_name = Settings.support_admin_group
    if admin_group_name.present?
      admin_group = team.groups.where(:name => admin_group_name).first
      support_admin_ids = admin_group.users.ids if admin_group
    end

    current_admin_ids   = org.admin_users.ids + org.admin_group_users.ids
    current_admin_count = (current_admin_ids - support_admin_ids).uniq.size

    incoming_admin_ids = member.is_a?(User) ? [member.id] : member.users.ids
    incoming_admin_count = (incoming_admin_ids - current_admin_ids).uniq.size

    user_ids        = (org.users.ids + org.group_users.ids).uniq
    user_count      = (user_ids - support_admin_ids).size
    max_admin_count = limit < 1 ? (user_count * limit).to_i : limit

    return nil if current_admin_count + incoming_admin_count <= max_admin_count

    return "Too many admins: adding this #{member.class.name.downcase} will exceed max allowed admin limit."
  end
end
