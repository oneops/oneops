class Organization::TeamMembersController < ApplicationController
  before_filter :find_team
  before_filter :authorize_update, :except => [:index]

  def index
    respond_to do |format|
      format.json {render :json => {:users => @team.users.all, :groups => @team.groups.all}}

      format.csv do
        delimiter = params[:delimiter].presence || ','

        csv = to_csv(@team.users.all, [:id, :username, :email, :name, :created_at, :last_sign_in_at], 'Users', delimiter)
        csv << "\n"
        csv << to_csv(@team.groups.all, [:id, :name, :created_at], 'Groups', delimiter)

        render :text => csv   #, :content_type => 'text/data_string'
      end
    end
  end

  def show
    def show
      if @member
        render :json => @member
      else
        render :json => {:errors => ['not found']}, :status => :not_found
      end
    end
  end

  def create
    @error = nil
    user_name  = params[:user]
    group_name = params[:group]
    if user_name.present?
      @member = User.where(:username => user_name).first
      if @member
        if @team.users.exists?(@member)
          @error = "User #{user_name} is already a member of team '#{@team.name}'."
        else
          @error = check_admin_limit(@team, @member)
          @team.users << @member if @error.blank?
        end
      else
        @error = "Unknown user: #{user_name}"
      end
    elsif group_name.present?
      @member = Group.where(:name => group_name).first
      if @member
        if @team.groups.exists?(@member)
          @error = "Group #{group_name} is already added to team '#{@team.name}'."
        else
          @error = check_admin_limit(@team, @member)
          @team.groups << @member if @error.blank?
        end
      else
        @error = "Unknown user: #{group_name}"
      end
    else
      @error = 'No member identifier specified.'
    end

    OrganizationMailer.added_to_team(@member, @team, current_user).deliver if @error.blank? && @member.is_a?(User)

    respond_to do |format|
      format.js
      format.json { @error ? render_json_ci_response(false, @team, [@error]) : show }
    end
  end

  def destroy
    error = nil
    member_id = params[:id]
    if params[:type] == 'group'
      @member = @team.groups.where((member_id =~ /\D/ ? 'groups.name' : 'groups.id') => member_id).first
      @team.groups.delete @member if @member
    else
      @member = @team.users.where((member_id =~ /\D/ ? 'users.username' : 'users.id') => member_id).first
      if @member
        if @team.name == Team::ADMINS && @team.users.count == 1
          error = 'Can not leave organizatio with no admins.'
        else
          @team.users.delete @member
        end
      end
    end
    error = "Team member '#{member_id}' not found" unless @member

    OrganizationMailer.removed_from_team(@member, @team, current_user).deliver if error.blank? && @member.is_a?(User)

    respond_to do |format|
      format.js {flash[:error] = error if error}
      format.json { error ? render_json_ci_response(false, @member, [error]) : show }
    end
  end


  protected

  def authorize_update
    if @team && @team.name == Team::ADMINS && action_name == 'create'
      return unauthorized('Unauthorized to manage admins!') unless manages_admins?
    else
      return unauthorized('Unauthorized to manage this team members!') unless manages_team_members?(@team)
    end
  end


  private

  def find_team
    team_qualifier = params[:team_id]
    if team_qualifier.present?
      @team = team_qualifier  =~ /\D/ ? current_user.organization.teams.where(:name => team_qualifier).first : current_user.organization.teams.find(team_qualifier)
    end
  end

  private

  def to_csv(data, fields, header = nil, delimiter = ',')
    csv = header.present? ? "#{header}\n" : ''
    csv << fields.join(delimiter) << "\n"
    data.each do |u|
      csv << fields.inject([]) {|a, k| a << u.send(k)}.join(delimiter) << "\n"
    end
    return csv
  end
end
