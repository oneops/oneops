class Account::ProfileController < ApplicationController
  prepend_before_filter :allow_params_authentication!, :only => [:authentication_token]
  skip_before_filter :check_username, :only => [:authentication_token, :session_preferences]
  skip_before_filter :check_eula, :only => [:show_eula, :accept_eula, :session_preferences]
  skip_before_filter :check_organization, :only => [:show, :update, :hide_wizard, :change_password, :organizations, :change_organization, :reset_authentication_token, :show_eula, :accept_eula, :session_preferences]
  layout false, :only => [:show_eula]

  def show
    respond_to do |format|
      format.html
      format.js {render :action => :update}
      format.json {render_json_ci_response(true, current_user.to_json(:include => :organization))}
    end
  end

  def update
    current_user.update_attributes(strong_params)
  end

  def hide_wizard
    current_user.update_attribute(:show_wizard, false)
  end

  include Devise::Controllers::Helpers
  def change_password
    if current_user.update_with_password(strong_params)
      current_user.reset_authentication_token!
      sign_in 'user', current_user, :bypass => true
      flash[:notice] = 'Successfully changed password.'
    else
      flash[:error] = 'Failed to update password.'
    end
  end

  def reset_authentication_token
    current_user.reset_authentication_token!
  end

  def change_organization
    org = current_user.organizations.find(params[:org_id])
    current_user.update_attribute(:organization_id, org.id) if org
    current_user.team_users.joins(:team).where("teams.organization_id" => org.id).update_all(last_sign_in_at: DateTime.now)
    respond_to do |format|
      format.html {redirect_to organization_path}

      format.json do
        if org
          render_json_ci_response(true, org)
        else
          render_json_ci_response(false, nil, ['Organization not found'])
        end
      end
    end
  end

  def accept_eula
    if params[:eula_accepted] == 'true'
      current_user.update_attribute('eula_accepted_at', Time.now)
      redirect_to root_path
    else
      redirect_to show_eula_path
    end
  end

  def authentication_token
    current_user.ensure_authentication_token
    render :json => {:token => current_user.authentication_token}
  end

  def organizations
  end

  def session_preferences
    prefs = params[:preferences] || {}
    [:sidebar, :sidebar_theme].each do |pref|
      session[pref] = prefs[pref] if prefs.include?(pref)
    end
    render :js => ''
  end


  private

  def strong_params
    params[:user].permit(:email, :current_password, :password, :password_confirmation, :remember_me, :show_wizard, :name, :username)
  end
end
