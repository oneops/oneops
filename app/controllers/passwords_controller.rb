class PasswordsController < Devise::PasswordsController
  # Override these actions as we do not support user-driven self password reset for now due to security concerns.
  def new
    if Settings.authentication == 'ldap'
      flash[:error] = 'System is configured to authenticate users via LDAP. Changing passwords is not supported'
    else
      super
    end
  end

  def create
    if Settings.authentication == 'ldap'
      flash[:error] = 'System is configured to authenticate users via LDAP. Changing passwords is not supported'
    else
      super
    end
  end
end
