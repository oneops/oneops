Display::Application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  # In the development environment your application's code is reloaded on
  # every request.  This slows down response time but is perfect for development
  # since you don't have to restart the webserver when you make code changes.
  config.cache_classes = false

  # Do not eager load code on boot.
  config.eager_load = false

  # Show full error reports and disable caching.
  config.consider_all_requests_local       = true
  config.action_controller.perform_caching = false

  # Don't care if the mailer can't send
  config.action_mailer.raise_delivery_errors = true
  config.action_mailer.perform_deliveries = false
  config.action_mailer.delivery_method = Settings.mail_delivery_method.to_sym
  smtp_settings = {:address              => Settings.smtp_address,
                   :enable_starttls_auto => false}
  if Settings.smtp_settings.present?
    begin
      smtp_settings = JSON.parse(Settings.smtp_settings)
    rescue Exception => e
      puts smtp_settings
      puts e
    end
  end
  config.action_mailer.smtp_settings = smtp_settings
  config.action_mailer.default_url_options = { :host => Settings.host, :port => Settings.port, :protocol => Settings.protocol }

  # Expands the lines which load the assets
  config.assets.debug = false
  config.assets.logger = false

  # Print deprecation notices to the Rails logger.
  config.active_support.deprecation = :log

  # Debug mode disables concatenation and preprocessing of assets.
  # This option may cause significant delays in view rendering with a large
  # number of complex assets.
  config.assets.debug = true

  # User memory cache store.
  config.cache_store = :memory_store

  # Adds additional error checking when serving assets at runtime.
  # Checks for improperly declared sprockets dependencies.
  # Raises helpful error messages.
  config.assets.raise_runtime_errors = true

  # Raises error for missing translations
  # config.action_view.raise_on_missing_translations = true

  if defined?(LogStasher)
    # Enable the logstasher logs for the current environment
    config.logstasher.enabled                   = true

    # This line is optional if you do not want to suppress app logs in your <environment>.log
    config.logstasher.suppress_app_log          = false

    # This line is optional, it allows you to set a custom value for the @source field of the log event
    config.logstasher.source                    = 'oneops.web'

    # Enable logging of controller params
    config.logstasher.log_controller_parameters = true
  end
end
