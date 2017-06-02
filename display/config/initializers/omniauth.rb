# Rails.application.config.middleware.use OmniAuth::Builder do
#   # OpenSSL::SSL::VERIFY_PEER = OpenSSL::SSL::VERIFY_NONE if Rails.env.development? || Rails.env.shared?
#   if Settings.has_key?('omniauth')
#     #provider :developer unless Rails.env.production?
#     if Settings.omniauth.has_key?('github')
#       provider :github, Settings.omniauth.github.key, Settings.omniauth.github.secret,
#       {
#         :client_options => {
#           :site => "https://#{Settings.omniauth.github['host']}/api/v3",
#           :authorize_url => "https://#{Settings.omniauth.github['host']}/login/oauth/authorize",
#           :token_url => "https://#{Settings.omniauth.github['host']}/login/oauth/access_token",
#         }
#       }
#     end
#     provider :twitter, Settings.omniauth.twitter.key, Settings.omniauth.twitter.secret if Settings.omniauth.has_key?('twitter')
#     provider :linkedin, Settings.omniauth.linkedin.key, Settings.omniauth.linkedin.secret if Settings.omniauth.has_key?('linkedin')
#     provider :google_oauth2, Settings.omniauth.google.key, Settings.omniauth.google.secret if Settings.omniauth.has_key?('google')
#   end
# end
