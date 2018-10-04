class Account::GroupMembersController < ApplicationController
  skip_before_filter :check_organization
  before_filter :find_group
  before_filter :authorize_group_admin, :only => [:create, :update]
  before_filter :find_member, :only => [:show, :update, :destroy]

  def index
    members = @group.members.includes(:user).all
    respond_to do |format|
      format.json {render :json => members.to_json(:include => [:user])}
      format.any do
        members = members.map do |m|
          user = m.user
          {:username        => user.username,
           :name            => user.name,
           :email           => user.email,
           :added_at        => m.created_at,
           :added_by        => m.created_by,
           :last_sign_in_at => user.current_sign_in_at,
           :admin           => m.admin}
        end
        render_csv(members, [:username, :name, :email, :added_at, :added_by, :last_sign_in_at, :admin], [:name, :email])
      end
    end
  end

  def show
    if @member
      render :json => @member.to_json(:include => [:user])
    else
      render :json => {:errors => ['not found']}, :status => :not_found
    end
  end

  def create
    error = nil
    username = params[:username]
    user = User.where(:username => username).first
    if user
      if @group.users.exists?(user)
        error = "User #{username} is already a member of '#{@group.name}'."
      else
        @member = @group.members.create(:user_id => user.id, :admin => false, :created_by => current_user.username)
      end
    else
      error = "Unknown user: #{username}"
    end

    GroupMailer.added_to_group(user, @group, current_user).deliver unless error

    respond_to do |format|
      format.js { flash[:error] = error if error }
      format.json { error ? render_json_ci_response(false, @group, [error]) : show }
    end
  end

  def update
    @member.update_attribute(:admin, !@member.admin) unless @member.user_id == current_user.id

    respond_to do |format|
      format.js
      format.json { error ? render_json_ci_response(false, @member) : show }
    end
  end

  def destroy
    user = @member.user
    unless user.id == current_user.id || @group.is_admin?(current_user)
      unauthorized
      return
    end

    error = nil
    if !current_user.is_global_admin? && @member.admin? && @group.admins.count == 1
      error = 'Cannot remove last admin member from the group.'
    else
      @group.members.delete @member
    end

    GroupMailer.removed_from_group(user, @group, current_user).deliver unless error

    respond_to do |format|
      format.js { flash[:error] = error if error }
      format.json { error ? render_json_ci_response(false, @member, [error]) : show }
    end
  end


  private

  def find_group
    group_id = params[:group_id]
    @group = (is_global_admin? ? Group : current_user.groups).where((group_id =~ /\D/ ? 'groups.name' : 'groups.id') => group_id).first
  end

  def authorize_group_admin
    unauthorized unless @group.is_admin?(current_user)
  end

  def find_member
    user_id = params[:id]
    @member = @group.members.includes(:user).where((user_id =~ /\D/ ? 'users.username' : 'users.id') => user_id).first
  end
end
