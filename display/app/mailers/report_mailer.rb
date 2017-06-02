class ReportMailer < ActionMailer::Base
  default :from => "\"OneOps Support\" <#{Settings.support_email}>"

  def compute(recipients, opts)
    @data = opts[:data]
    @note = opts[:note]
    options = {:to => recipients}
    options[:subject] = opts[:title] || "#{data[:subject]} [#{data[:nsPath]}]"
    user = opts[:user]
    options[:from] = "\"#{user ? (user.name.presence || user.username.presence) : 'OneOps'}\" <#{Settings.support_email}>"
    mail(options)
  end
end
