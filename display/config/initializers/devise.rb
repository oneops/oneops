module Devise
  mattr_accessor :ldap_search_filter_builder
  @@ldap_search_filter_builder = Proc.new() { |ldap| false }

  module LdapAdapter
    class LdapConnect
      # def search_for_login(login = nil)
      #   @login = login if login
      #   DeviseLdapAuthenticatable::Logger.send("LDAP search for login: #{@attribute}=#{@login}")
      #   filter = Net::LDAP::Filter.eq(@attribute.to_s, @login.to_s)
      #   search_filter = ::Devise.ldap_search_filter_builder.call(@ldap)
      #   if search_filter
      #     DeviseLdapAuthenticatable::Logger.send("LDAP search with filter #{search_filter.to_s}")
      #     filter = Net::LDAP::Filter.join(filter, search_filter)
      #   end
      #   ldap_entry = nil
      #   @ldap.search(:filter => filter) {|entry| ldap_entry = entry}
      #   ldap_entry
      # end

      def search(filter)
        ldap_entry = nil
        @ldap.search(:filter => filter) {|entry| ldap_entry = entry}
        ldap_entry
      end

      def search_for_logins(logins)
        search_attr = @attribute.to_s
        filter = logins.inject(nil) do |f, l|
          ff = Net::LDAP::Filter.eq(search_attr, l.to_s)
          f ? (f | ff) : ff
        end
        # search_filter = ::Devise.ldap_search_filter_builder.call(@ldap)
        # if search_filter
        #   filter = Net::LDAP::Filter.join(filter, search_filter)
        # end
        login_map = logins.to_map {|l| l.downcase}
        ldap_entries = {}
        @ldap.search(:filter => filter) {|entry| ldap_entries[login_map[entry[search_attr].first.downcase]] = entry}
        ldap_entries
      end
    end
  end

  # This is a fix for a problem with HTTP Auth with ldap authenticatable.
  # See; https://github.com/cschiewek/devise_ldap_authenticatable/issues/145
  Models::LdapAuthenticatable
  class Strategies::LdapAuthenticatable < Devise::Strategies::Authenticatable
    def authenticate!
      resource = valid_password? && mapping.to.authenticate_with_ldap(authentication_hash.merge(:password => password))
      if validate(resource)
        success!(resource)
      else
        fail(:invalid)
      end
    end
  end
end

class CustomFailureApp < Devise::FailureApp
  def redirect_url
    if request.xhr?
      flash[:timedout] = true
      send(:"new_#{scope}_session_path")
    else
      super
    end
  end
end

Devise.setup do |config|
  config.secret_key = '01814cf031762c13c5e92f159082de344e6896e082e9da1da1cbd0ad028be6baeefe0d6ee648f150613f6d55f31b1a85a8fe2b04fb090f89769e70304ef51bef'

  # In Devise 3.1, we store an encrypted token in the database and the actual token is sent only via e-mail to the user.
  # We recommend users upgrading to set this option on production only for a couple days
  # TODO Remove after 6/1/2015.
  # config.allow_insecure_token_lookup = true

  # ==> LDAP Configuration
  config.ldap_logger = true
  config.ldap_create_user = true
  config.ldap_update_password = false
  #config.ldap_config = "#{Rails.root}/config/ldap.yml"
  config.ldap_check_group_membership = false
  config.ldap_check_attributes = false
  config.ldap_use_admin_to_bind = true
  #config.ldap_ad_group_check = false
  config.ldap_auth_username_builder = Proc.new() { |attribute, login, ldap| Settings.ldap_domain ? "#{login}@#{Settings.ldap_domain}" : login }
  # config.ldap_search_filter_builder = Proc.new() { |ldap| Net::LDAP::Filter.present('mail') }

  # ==> Mailer Configuration
  # Configure the e-mail address which will be shown in DeviseMailer.
  config.mailer_sender = Settings.support_email || 'support@oneops.com'

  # Configure the class responsible to send e-mails.
  # config.mailer = "Devise::Mailer"

  # ==> ORM configuration
  # Load and configure the ORM. Supports :active_record (default) and
  # :mongoid (bson_ext recommended) by default. Other ORMs may be
  # available as additional gems.
  require 'devise/orm/active_record'

  # ==> Configuration for any authentication mechanism
  # Configure which keys are used when authenticating a user. The default is
  # just :email. You can configure it to use [:username, :subdomain], so for
  # authenticating a user, both parameters are required. Remember that those
  # parameters are used only when authenticating and not when retrieving from
  # session. If you need permissions, you should implement that in a before filter.
  # You can also supply a hash where the value is a boolean determining whether
  # or not authentication should be aborted when the value is not present.
  config.authentication_keys = [:username]

  # Configure parameters from the request object used for authentication. Each entry
  # given should be a request method and it will automatically be passed to the
  # find_for_authentication method and considered in your model lookup. For instance,
  # if you set :request_keys to [:subdomain], :subdomain will be used on authentication.
  # The same considerations mentioned for authentication_keys also apply to request_keys.
  # config.request_keys = []

  # Configure which authentication keys should be case-insensitive.
  # These keys will be downcased upon creating or modifying a user and when used
  # to authenticate or find a user. Default is :email.
  config.case_insensitive_keys = [:email, :username]

  # Configure which authentication keys should have whitespace stripped.
  # These keys will have whitespace before and after removed upon creating or
  # modifying a user and when used to authenticate or find a user. Default is :email.
  config.strip_whitespace_keys = [:email, :username]

  # Tell if authentication through request.params is enabled. True by default.
  # It can be set to an array that will enable params authentication only for the
  # given strategies, for example, `config.params_authenticatable = [:database]` will
  # enable it only for database (email + password) authentication.
  config.params_authenticatable = true

  # Tell if authentication through HTTP Auth is enabled. False by default.
  # It can be set to an array that will enable http authentication only for the
  # given strategies, for example, `config.http_authenticatable = [:database]` will
  # enable it only for database authentication. The supported strategies are:
  # :database      = Support basic authentication with authentication key + password
  config.http_authenticatable = true

  # If http headers should be returned for AJAX requests. True by default.
  config.http_authenticatable_on_xhr = false

  # The realm used in Http Basic Authentication. "Application" by default.
  # config.http_authentication_realm = "Application"

  # It will change confirmation, password recovery and other workflows
  # to behave the same regardless if the e-mail provided was right or wrong.
  # Does not affect registerable.
  # config.paranoid = true

  # By default Devise will store the user in session. You can skip storage for
  # particular strategies by setting this option.
  # Notice that if you are skipping storage for all authentication paths, you
  # may want to disable generating routes to Devise's sessions controller by
  # passing skip: :sessions to `devise_for` in your config/routes.rb
  config.skip_session_storage = [:http_auth]

  # By default, Devise cleans up the CSRF token on authentication to
  # avoid CSRF token fixation attacks. This means that, when using AJAX
  # requests for sign in and sign up, you need to get a new CSRF token
  # from the server. You can disable this option at your own risk.
  # config.clean_up_csrf_token_on_authentication = true

  # ==> Configuration for :database_authenticatable
  # For bcrypt, this is the cost for hashing the password and defaults to 10. If
  # using other encryptors, it sets how many times you want the password re-encrypted.
  #
  # Limiting the stretches to just one in testing will increase the performance of
  # your test suite dramatically. However, it is STRONGLY RECOMMENDED to not use
  # a value less than 10 in other environments. Note that, for bcrypt (the default
  # encryptor), the cost increases exponentially with the number of stretches (e.g.
  # a value of 20 is already extremely slow: approx. 60 seconds for 1 calculation).
  config.stretches = 10

  # Setup a pepper to generate the encrypted password.
  # config.pepper = "6b8740e999721ac14a37f0c606c7b4a351ab2e598b7a63cb3f5003a86f7396f1b9e62ee716628bd947c63de78e1fc83c9c2971f419155b1da15ac6c920d5ba49"

  # ==> Configuration for :confirmable
  # The time you want to give your user to confirm his account. During this time
  # he will be able to access your application without confirming. Default is 0.days
  # When allow_unconfirmed_access_for is zero, the user won't be able to sign in without confirming.
  # You can use this to let your user access some features of your application
  # without confirming the account, but blocking it after a certain period
  # (ie 2 days).
  config.allow_unconfirmed_access_for = 0.days

# A period that the user is allowed to confirm their account before their
  # token becomes invalid. For example, if set to 3.days, the user can confirm
  # their account within 3 days after the mail was sent, but on the fourth day
  # their account can't be confirmed with the token any more.
  # Default is nil, meaning there is no restriction on how long a user can take
  # before confirming their account.
  # config.confirm_within = 3.days

  # If true, requires any email changes to be confirmed (exactly the same way as
  # initial account confirmation) to be applied. Requires additional unconfirmed_email
  # db field (see migrations). Until confirmed, new email is stored in
  # unconfirmed_email column, and copied to email column on successful confirmation.
  config.reconfirmable = true

  # Defines which key will be used when confirming an account
  # config.confirmation_keys = [ :email ]

  # ==> Configuration for :rememberable
  # The time the user will be remembered without asking for credentials again.
  # config.remember_for = 2.weeks

  # If true, extends the user's remember period when remembered via cookie.
  # config.extend_remember_period = false

  # Options to be passed to the created cookie. For instance, you can set
  # secure: true in order to force SSL only cookies.
  # config.rememberable_options = {}

  # ==> Configuration for :validatable
  # Range for password length. Default is 6..20.
  # config.password_length = 6..20

  # Regex to use to validate the email address
  # config.email_regexp = /\A([\w\.%\+\-]+)@([\w\-]+\.)+([\w]{2,})\z/i

  # ==> Configuration for :timeoutable
  # The time you want to timeout the user session without activity. After this
  # time the user will be asked for credentials again. Default is 30 minutes.
  config.timeout_in = Settings.session_inactivity_timeout.minutes

  # If true, expires auth token on session timeout.
  # config.expire_auth_token_on_timeout = false

  # ==> Configuration for :lockable
  # Defines which strategy will be used to lock an account.
  # :failed_attempts = Locks an account after a number of failed attempts to sign in.
  # :none            = No lock strategy. You should handle locking by yourself.
  # config.lock_strategy = :failed_attempts

  # Defines which key will be used when locking and unlocking an account
  # config.unlock_keys = [ :email ]

  # Defines which strategy will be used to unlock an account.
  # :email = Sends an unlock link to the user email
  # :time  = Re-enables login after a certain amount of time (see :unlock_in below)
  # :both  = Enables both strategies
  # :none  = No unlock strategy. You should handle unlocking by yourself.
  # config.unlock_strategy = :both

  # Number of authentication tries before locking an account if lock_strategy
  # is failed attempts.
  # config.maximum_attempts = 20

  # Time interval to unlock the account if :time is enabled as unlock_strategy.
  # config.unlock_in = 1.hour

  # Warn on the last attempt before the account is locked.
  # config.last_attempt_warning = true

  # ==> Configuration for :recoverable
  # Defines which key will be used when recovering the password for an account
  # config.reset_password_keys = [ :email ]

  # Time interval you can reset your password with a reset password key.
  # Don't put a too small interval or your users won't have the time to
  # change their passwords.
  config.reset_password_within = 10.years

  # ==> Configuration for :encryptable
  # Allow you to use another encryption algorithm besides bcrypt (default). You can use
  # :sha1, :sha512 or encryptors from others authentication tools as :clearance_sha1,
  # :authlogic_sha512 (then you should set stretches above to 20 for default behavior)
  # and :restful_authentication_sha1 (then you should set stretches to 10, and copy
  # REST_AUTH_SITE_KEY to pepper)
  # config.encryptor = :sha512

  # ==> Scopes configuration
  # Turn scoped views on. Before rendering "sessions/new", it will first check for
  # "users/sessions/new". It's turned off by default because it's slower if you
  # are using only default views.
  # config.scoped_views = false

  # Configure the default scope given to Warden. By default it's the first
  # devise role declared in your routes (usually :user).
  # config.default_scope = :user

  # Configure sign_out behavior.
  # Sign_out action can be scoped (i.e. /users/sign_out affects only :user scope).
  # The default is true, which means any logout action will sign out all active scopes.
  # config.sign_out_all_scopes = true

  # ==> Navigation configuration
  # Lists the formats that should be treated as navigational. Formats like
  # :html, should redirect to the sign in page when the user does not have
  # access, but formats like :xml or :json, should return 401.
  #
  # If you have any extra navigational formats, like :iphone or :mobile, you
  # should add them to the navigational formats lists.
  #
  # The :"*/*" and "*/*" formats below is required to match Internet
  # Explorer requests.
  config.navigational_formats = [:"*/*", "*/*", :html, :js]

  # The default HTTP method used to sign out a resource. Default is :delete.
  config.sign_out_via = :get

  # ==> OmniAuth
  # Add a new OmniAuth provider. Check the wiki for more information on setting
  # up on your models and hooks.
  # config.omniauth :github, 'APP_ID', 'APP_SECRET', :scope => 'user,public_repo'

  # ==> Warden configuration
  # If you want to use other strategies, that are not supported by Devise, or
  # change the failure app, you can configure them inside the config.warden block.
  #
  config.warden do |manager|
    manager.failure_app   = CustomFailureApp
    # manager.intercept_401 = false
  #   manager.default_strategies(:scope => :user).unshift :some_external_strategy
  end

  # ==> Mountable engine configurations
  # When using Devise inside an engine, let's call it `MyEngine`, and this engine
  # is mountable, there are some extra configurations to be taken into account.
  # The following options are available, assuming the engine is mounted as:
  #
  #     mount MyEngine, at: '/my_engine'
  #
  # The router that invoked `devise_for`, in the example above, would be:
  # config.router_name = :my_engine
  #
  # When using OmniAuth, Devise cannot automatically set OmniAuth path,
  # so you need to do it manually. For the users scope, it would be:
  # config.omniauth_path_prefix = '/my_engine/users/auth'
end
