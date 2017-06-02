# Be sure to restart your server when you modify this file.

Display::Application.config.session_store :cookie_store,
                                          :key    => '_oneops-display_session',
                                          :secure => !(Rails.env.development? || Rails.env.shared?)
