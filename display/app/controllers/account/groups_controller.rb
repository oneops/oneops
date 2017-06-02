class Account::GroupsController < ApplicationController
  skip_before_filter :check_organization
  before_filter :find_group, :only => [:show, :edit, :update, :destroy, :confirm_delete]
  before_filter :authorize_group_admin, :only => [:update, :destroy, :confirm_delete]

  def index
    @groups = current_user.groups.select('groups.*, group_members.admin').order(:name).all

    respond_to do |format|
      format.js do
        scope = Group.joins(:members).group('groups.id').where('groups.id IN (?)', @groups.map(&:id))
        @member_counts = scope.select('groups.id, count(distinct group_members.id) as member_count').to_map(&:id)
        @team_counts   = scope.joins(:teams => [:organization]).select('groups.id, count(distinct teams.id) as team_count, count(distinct organizations.id) as org_count').to_map(&:id)
        render :action => :index
      end
      format.json { render :json => @groups }
    end
  end

  def show
    render :json => @group
  end

  def new
    @group = current_user.groups.build
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render :json => @group }
    end
  end

  def create
    @group = Group.new(strong_params.merge(:created_by => current_user.username))
    @group.members.build(:user_id => current_user.id, :admin => true, :created_by => current_user.username)
    ok = @group.save

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render :json => render_json_ci_response(ok, @group) }
    end
  end

  def edit
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render :json => render_json_ci_response(true, @group) }
    end
  end

  def update
    ok = @group.update_attributes(strong_params)
    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render :json => render_json_ci_response(ok, @group) }
    end
  end

  def destroy
    ok = @group.destroy

    respond_to do |format|
      format.js { index }
      format.json { render :json => render_json_ci_response(ok, @group) }
    end
  end

  def confirm_delete
    #  usage stats.
  end

  def lookup
    name = "%#{params[:name]}%"
    render :json => Group.joins(:members).
      select('groups.name, count(group_members.user_id) as user_count').
      where('name ILIKE ?', name).
      group('groups.id').
      limit(20).map {|g| "#{g.name} (#{g.user_count} #{'user'.pluralize(g.user_count)})"}
  end


  private

  def find_group
    group_id = params[:id]
    @group = current_user.groups.where((group_id =~ /\D/ ? 'groups.name' : 'groups.id') => group_id).first
  end

  def authorize_group_admin
    unauthorized unless @group.is_admin?(current_user)
  end

  def strong_params
    params[:group].permit(:name, :created_by, :description)
  end
end
