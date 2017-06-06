class OrganizationMailer < ActionMailer::Base
  logger.level = Logger::INFO unless Rails.env.development? || Rails.env.shared?
  default :from => "\"OneOps Support\" <#{Settings.support_email}>"

  def request_access(recipients, organization, user, message)
    @organization = organization
    @user         = user
    @message      = message
    mail(:to => recipients, :subject => 'OneOps join organization request')
  end

  def added_to_team(user, team, admin_user)
    @user       = user
    @group       = team
    @admin_user = admin_user
    mail(:to => user.email, :subject => "Added to team '#{@group.name}' in '#{@group.organization.name}'")
  end

  def removed_from_team(user, team, admin_user)
    @user       = user
    @group       = team
    @admin_user = admin_user
    mail(:to => user.email, :subject => "Removed from team '#{@group.name}' in '#{@group.organization.name}'")
  end
end
