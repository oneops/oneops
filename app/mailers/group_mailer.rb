class GroupMailer < ActionMailer::Base
  logger.level = Logger::INFO unless Rails.env.development? || Rails.env.shared?
  default :from => "\"OneOps Support\" <#{Settings.support_email}>"

  def added_to_group(user, group, admin_user)
    @user       = user
    @group      = group
    @admin_user = admin_user
    mail(:to => user.email, :subject => "Added to group '#{@group.name}'")
  end

  def removed_from_group(user, group, admin_user)
    @user       = user
    @group      = group
    @admin_user = admin_user
    mail(:to => user.email, :subject => "Removed from group '#{@group.name}'")
  end
end
