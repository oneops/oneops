class Organization::UsersController < ApplicationController
  before_filter :authorize_admin, :except => [:index, :show]

  def index
    @users = current_user.organization.users.order(:email).all
    @user_teams = current_user.organization.teams.includes(:users).inject({}) do |m, team|
      team.users.each do |user|
        m[user.id] ||= []
        m[user.id] << team.name
      end
      m
    end
    respond_to do |format|
      format.js {render :action => :index}
      format.json {render :json => @users}
    end
  end

  def show
    user_id = params[:id]
    org_scope = params[:org_name].present?
    scope = (org_scope ? current_user.organization.users : User).limit(1)
    if user_id =~ /\D/
      scope = scope.where('users.username = ? OR users.email = ?', user_id, user_id)
    else
      scope = scope.where('users.id = ?', user_id.to_i)
    end

    @user = scope.first
    if @user
      if org_scope
      render :json => {:username        => @user.username,
                       :email           => @user.email,
                       :name            => @user.name,
                       :created_at      => @user.created_at,
                       :last_sign_in_at => @user.last_sign_in_at,
                       :teams           => @user.teams.where(:organization_id => current_user.organization_id).map(&:name)}
    else
        render :json => {:username        => @user.username,
                         :email           => @user.email,
                         :name            => @user.name,
                         :created_at      => @user.created_at,
                         :last_sign_in_at => @user.last_sign_in_at,
                         :organizations   => @user.organizations.map(&:name)}
      end
    else
      render :nothing => true, :status => :not_found
    end
  end

  def new
  end

  def create
    username = params[:username]
    user = User.where(:username => username).first
    if user
      if current_user.organization.users.exists?(user)
        flash[:error] = "User #{username} is already added to organization '#{current_user.organization.name}'."
      else
        team_ids = params[:teams]
        if team_ids.present?
          team_ids.each do |id|
            team = current_user.organization.teams.where('teams.id' => id).first
            team.users << user if team
          end
        end
        user.update_attribute(:organization_id, current_user.organization_id) if user.organization_id.blank?
        flash[:notice] = "Successfully added user #{username} to organization '#{current_user.organization.name}'."
      end
    else
      flash[:error] = "Unknown user: #{username}"
    end

    index
  end

  def edit
    @user = current_user.organization.users.find(params[:id])
  end

  def update
    @user = current_user.organization.users.find(params[:id])
    org_team_ids = current_user.organization.teams.map(&:id)
    @user.team_ids = (@user.team_ids - org_team_ids + (params[:teams] || []).map(&:to_i))

    index
  end

  def destroy
    user = current_user.organization.users.find(params[:id])
    if user
      current_user.organization.teams.each { |team| team.users.delete(user) }
      flash[:notice] = "Successfully removed user #{user.username} from '#{current_user.organization.name}'"
    end

    index
  end
end
