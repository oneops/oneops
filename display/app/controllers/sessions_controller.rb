class SessionsController < Devise::SessionsController
  skip_before_filter :check_username, :only => [:create]
  skip_before_filter :check_eula, :only => [:destroy, :create]
  skip_before_filter :check_organization, :only => [:destroy, :create]

  after_filter :session_username, :only => [:create]

  append_view_path(File.join(Rails.root, 'app', 'views', 'devise'))

  skip_after_filter :process_flash_messages, :only => [:new]

  def new
    if request.xhr?
      flash[:error] = params[:message].presence || I18n.t('devise.failure.timeout')
      render :js => "window.location = '#{new_user_session_url}?return_to=' + window.location.href"
    else
      return_to = params[:return_to]
      session['user_return_to'] = return_to if return_to.present?
      super
    end
  end

  def create
    super
    token = Devise.friendly_token
    session[:token] = token
    current_user.update_attribute(:session_token, token)
    current_user.team_users.joins(:team).where("teams.organization_id" => current_user.organization_id).update_all(last_sign_in_at: DateTime.now) if current_user.organization_id
    browser_timezone = params[:browser_timezone]
    session[:browser_timezone] = browser_timezone.to_i if browser_timezone.present?
  end

  def after_sign_in_path_for(resource_or_scope)
    return_to = session['user_return_to']
    if return_to.present? && return_to.include?(new_user_session_path)
      return_to = nil
      session['user_return_to'] = nil
    end
    current_user.organization ? (return_to.presence || organization_path) : root_path
  end


  private

  def build_resource(hash = nil, options = {})
    super
    resource.email = params[:email].presence || params[:user].try {|u| u[:email]} if action_name == 'new'
  end

  def session_username
    session[:username] = current_user.username if current_user
  end
end
