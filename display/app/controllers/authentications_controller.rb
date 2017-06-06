class AuthenticationsController < ApplicationController
  skip_before_filter :authenticate_user!, :only => [:create]
  before_filter :check_omniauth

  def create
    omniauth = request.env["omniauth.auth"]
    authentication = Authentication.where(:provider => omniauth['provider'], :uid => omniauth['uid']).first
    if authentication
      flash[:notice] = "Signed in successfully"
      sign_in_and_redirect(:user, authentication.user)
    elsif current_user
      current_user.authentications.create!(:provider => omniauth['provider'], :uid => omniauth['uid'])
      flash[:notice] = "Successfully created authentication"
      redirect_to account_profile_path + "#authentication"
    else
      user = User.new
      user.apply_omniauth(omniauth)
      if user.save
        flash[:notice] = "Signed in successfully"
        sign_in_and_redirect(:user, user)
      else
        session[:omniauth] = omniauth.except('extra')
        redirect_to new_registration_path(user)
      end
    end
  end

  def destroy
    @authentication = current_user.authentications.find(params[:id])
    @authentication.destroy
    flash[:warning] = "Successfully destroyed authentication"
    redirect_to account_profile_path + "#authentication"
  end

  private

  def check_omniauth
    if !Settings.omniauth
      flash[:notice] = 'Authentication services not enabled'
      redirect_to new_session_path(User.new)
    end
  end

end
