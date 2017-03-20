Display::Application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  # Code is not reloaded between requests.
  config.cache_classes = true

  # Eager load code on boot. This eager loads most of Rails and
  # your application in memory, allowing both threaded web servers
  # and those relying on copy on write to perform better.
  # Rake tasks automatically ignore this option for performance.
  config.eager_load = true

  # Full error reports are disabled and caching is turned on.
  config.consider_all_requests_local       = false
  config.action_controller.perform_caching = true

  # Enable Rack::Cache to put a simple HTTP cache in front of your application
  # Add `rack-cache` to your Gemfile before enabling this.
  # For large-scale production use, consider using a caching reverse proxy like nginx, varnish or squid.
  # config.action_dispatch.rack_cache = true

  # Disable Rails's static asset server (Apache or nginx will already do this).
  config.serve_static_assets = false

  # Compress JavaScripts and CSS.
  config.assets.js_compressor = :uglifier
  # config.assets.css_compressor = :sass

  # Do not fallback to assets pipeline if a precompiled asset is missed.
  config.assets.compile = false

  # Generate digests for assets URLs.
  config.assets.digest = true

  # Specifies the header that your server uses for sending files.
  config.action_dispatch.x_sendfile_header = "X-Sendfile" # for apache
  # config.action_dispatch.x_sendfile_header = 'X-Accel-Redirect' # for nginx

  # Force all access to the app over SSL, use Strict-Transport-Security, and use secure cookies.
  # config.force_ssl = true

  # Set to :debug to see everything in the log.
  config.log_level = :debug

  # Prepend all log lines with the following tags.
  # config.log_tags = [ :subdomain, :uuid ]

  # Use a different logger for distributed setups.
  # config.logger = ActiveSupport::TaggedLogging.new(SyslogLogger.new)

  # Use a different cache store in production
  config.cache_store = :memory_store

 # Enable serving of images, stylesheets, and JavaScripts from an asset server.
  # config.action_controller.asset_host = "http://assets.example.com"

 # Ignore bad email addresses and do not raise email delivery errors.
  # Set this to true and configure the email server for immediate delivery to raise delivery errors.
  # config.action_mailer.raise_delivery_errors = false
  config.action_mailer.perform_deliveries = Settings.mail_perform_deliveries
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

  # Enable locale fallbacks for I18n (makes lookups for any locale fall back to
  # the I18n.default_locale when a translation cannot be found).
  config.i18n.fallbacks = true

  # Send deprecation notices to registered listeners.
  config.active_support.deprecation = :notify

  # Disable automatic flushing of the log to improve performance.
  # config.autoflush_log = false

  # Use default logging formatter so that PID and timestamp are not suppressed.
  config.log_formatter = ::Logger::Formatter.new

  # User "silencer" gem to suppress logging of certain requests (e.g. "/users/sign_in" used for ECV).
  config.middleware.swap Rails::Rack::Logger, Silencer::Logger, :silence => ['/status/ecv']

  if defined?(LogStasher)
    # Enable the logstasher logs for the current environment
    config.logstasher.enabled = true

    # This line is optional if you do not want to suppress app logs in your <environment>.log
    config.logstasher.suppress_app_log = false

    # This line is optional, it allows you to set a custom value for the @source field of the log event
    config.logstasher.source = 'oneops.web'

    # Enable logging of controller params
    config.logstasher.log_controller_parameters = true
  end
end
