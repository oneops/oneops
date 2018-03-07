class RegistrationsController < Devise::RegistrationsController
  skip_before_filter :check_organization, :only => [:destroy]

  respond_to :json, :only => [:create, :update, :destroy]

  def create
    if Settings.invitations
      token = params[:token]
      if token.blank?
        build_resource(strong_params)
        resource.errors.add(:base, 'You can sign up by invitation only! Invitation token is required.')
        render :action => :new
        return
      end

      invitation = Invitation.where(:email => params[:user][:email]).first
      unless invitation && invitation.token == params[:token].strip.downcase
        build_resource(strong_params)
        resource.errors.add(:base, 'Invalid invitation token.')
        render :action => :new
        return
      end
    end

    ok = false
    User.transaction do
      super
      ok = resource && resource.persisted?
      if ok
        invitation.destroy if invitation
      else
        raise ActiveRecord::Rollback
      end
    end

    if ok
      session[:username] = current_user.username if current_user
      session[:omniauth] = nil unless resource.new_record?
      flash.now[:notice] = 'Signed up successfully.'
    else
      flash.now[:error] = 'Failed to sign up.'
    end
  end

  def edit
    @authentications = current_user.authentications if current_user
    super
  end

  def destroy
    user = User.find(params[:id])
    unless user.id == current_user.id
      user.destroy
      flash[:notice] = "User #{user.username} has been deleted."
    end
    index
  end

  def lookup
    login = "%#{params[:login]}%"
    render :json => User.where('username LIKE ? OR name LIKE ?', login, login).limit(20).map {|u| "#{u.username} #{u.name if u.name.present?}"}

    # Exact match look-up
    # login = params[:login].to_s.strip
    # hits  = User.where('username = ?', login).limit(1).map { |u| "#{u.username} #{u.name if u.name.present?}" }
    # render :json => hits
  end


  protected

  def build_resource(hash = nil)
    organization = strong_params.delete(:organization) if params[:user]
    super
    if session[:omniauth]
      resource.apply_omniauth(session[:omniauth])
    end
    if action_name == 'new'
      resource.organization = Organization.new
      resource.email        = params[:email]
    elsif action_name == 'create'
      resource.organization = Organization.create(organization)
    end
  end

  def after_sign_up_path_for(resource)
    nil
  end


  private

  def strong_params
    params[:user].permit(:email, :password, :password_confirmation, :remember_me, :show_wizard, :name, :username,
                         {:organization => [:name, :cms_id, :assemblies, :services, :catalogs, :announcement]})
  end
end
