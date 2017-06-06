class ConfirmationsController < Devise::ConfirmationsController
  # Override default behaviour on new user confirmation to force user to sign in explicitly.
  protected

  def sign_in(resource_or_scope, *args)
    # Do nothing.
  end

  def after_confirmation_path_for(resource_name, resource)
    new_user_session_path(:email => resource.email)
  end
end
