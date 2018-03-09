class Organization::UsersController < ApplicationController
  include ::AdminLimit

  before_filter :authorize_admin, :except => [:index, :show]

  def index
    @organization = current_user.organization
    @users = @organization.users.all
    respond_to do |format|
      format.js do
        user_map = @users.to_map(&:id)
        team_map = @organization.teams.all.to_map(&:id)
        @user_teams = @organization.teams.includes(:team_users).inject({}) do |m, team|
          team.team_users.each do |tu|
            user_id = tu.user_id
            user = user_map[user_id]
            m[user_id] ||= []
            m[user_id] << team_map[tu.team_id].name
            user.last_sign_in_at_for_current_org = tu.last_sign_in_at if user && user.last_sign_in_at_for_current_org.blank? || (tu.last_sign_in_at.present? && user.last_sign_in_at_for_current_org < tu.last_sign_in_at)
          end
          m
        end

        render :action => :index
      end

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
    @user = User.where(:username => username).first
    if @user
      if current_user.organization.users.exists?(@user)
        @user.errors.add(:base, "User #{username} is already a member of organization '#{current_user.organization.name}'.")
      else
        team_ids = params[:teams]
        if team_ids.present?
          User.transaction do
            team_ids.each do |id|
              team = current_user.organization.teams.where('teams.id' => id).first
              if team
                error = check_max_admin_limit(team, @user)
                if error
                  @user.errors.add(:base, error)
                  raise ActiveRecord::Rollback
                else
                  team.users << @user
                end
              end
            end
            @user.update_attribute(:organization_id, current_user.organization_id) if @user.organization_id.blank?
            flash[:notice] = "Successfully added '#{username}' to organization '#{current_user.organization.name}'."
          end
        else
          @user.errors.add(:base, 'Must select a team.')
        end
      end
    end

    respond_to do |format|
      format.js do
        if @user && @user.errors.blank?
          index
        else
          flash[:error] = @user ? @user.errors.full_messages.join(' ') : "Unknown @user: #{username}."
          render :js => ''
        end
      end

      format.json {render_json_ci_response(@user && @user.errors.blank?, @user)}
    end
  end

  def edit
    @user = current_user.organization.users.find(params[:id])
  end

  def update
    org          = current_user.organization
    org_team_ids = org.teams.ids
    new_team_ids = (params[:teams] || []).map(&:to_i)
    @user        = org.users.find(params[:id]) # Safety check
    if @user
      admin_team    = org.admin_team
      admin_team_id = admin_team.id
      all_team_ids  = @user.team_ids
      was_admin     = all_team_ids.include?(admin_team_id)
      will_be_admin = new_team_ids.include?(admin_team_id)
      if admin_team.users.count == 1 && was_admin && !will_be_admin
        @user.errors.add(:base, 'This user is last admin for this organization: can not remove @user from admins.')
      else
        error = !was_admin && will_be_admin && check_max_admin_limit(admin_team, @user)
        if error
          @user.errors.add(:base, error)
        else
          @user.team_ids = all_team_ids - org_team_ids + new_team_ids
          check_reset_org
        end
      end
    end

    respond_to do |format|
      format.js do
        if @user && @user.errors.blank?
          index
        else
          flash[:error] = @user ? @user.errors.full_messages.join(' ') : "Unknown @user."
          render :js => ''
        end
      end

      format.json {render_json_ci_response(@user && @user.errors.blank?, @user)}
    end
  end

  def destroy
    @user = current_user.organization.users.find(params[:id])
    if @user
      current_user.organization.teams.each { |team| team.users.delete(@user) }
      flash[:notice] = "Successfully removed @user '#{@user.username}' from '#{current_user.organization.name}'"

      check_reset_org
    end

    index
  end

  def remove
    users = current_user.organization.users.find(params[:ids]).reject{|user| user.id == current_user.id}
    if current_user.organization.teams.each { |team| team.users.delete(users) }
      flash[:notice] = "Successfully removed users"
    else
      flash[:error] = "Error removing users"
    end
    index
  end

  def confirm_remove
    @users = current_user.organization.users.find(params[:user_ids]).reject{|user| user.id == current_user.id}
  end

  private

  def check_reset_org
    org_id = current_user.organization_id
    if @user.organization_id == org_id && @user.teams.where(:organization_id => org_id).count == 0
      @user.organization = @user.organizations.first
      @user.save
    end
  end
end
