class NotificationMailer < ActionMailer::Base
  logger.level = Logger::INFO unless Rails.env.development? || Rails.env.shared?
  default :from => "\"OneOps Support\" <#{Settings.support_email}>"

  def notification(recipients, data, component_health = nil)
    @data = data

    @component_health = nil
    if @data[:source] == 'ops'
      # Find out new component state based on the remaining highest severity non-zero counter.
      payload = @data[:payload]
      if payload.present?
        @component_health = 'good'
        %w(unhealthy overutilized notify underutilized).each do |s|
          if payload[s].to_i > 0
            @component_health = s
            break
          end
        end
      end
    end
    profile = data[:environmentProfileName]
    mail(:to => recipients, :subject => "#{"#{profile} " if profile.present?}#{data[:subject]}")
  end
end
