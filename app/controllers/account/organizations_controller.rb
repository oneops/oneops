class Account::OrganizationsController < ApplicationController
  before_filter :find_organization, :only => [:show, :destroy, :leave]
  skip_before_filter :check_organization

  def index
    render :json => current_user.organizations.all
  end

  def show
    render :json => @organization
  end

  def create
    org = Organization.new(:name => params[:name])
    team = org.teams.build(:name             => Team::ADMINS,
                           :design           => true,
                           :transition       => true,
                           :operations       => true,
                           :cloud_services   => true,
                           :cloud_compliance => true,
                           :cloud_support    => true,
                           :manages_access   => true,
                           :org_scope        => true)
    team.users << current_user
    if org.save
      current_user.update_attribute(:organization_id, org.id)
      flash[:notice] = "Successfully created organization '#{org.name}'."
      current_user.update_attribute(:organization_id, org.id) unless current_user.organization_id
      respond_to do |format|
        format.js {render :action => :create}
        format.json {render_json_ci_response(true, org)}
      end
    else
      respond_to do |format|
        format.js do
          flash[:error] = "Failed to create organization '#{org.name}': #{org.errors.full_messages.join('; ')}"
          render :action => :index
        end

        format.json { render_json_ci_response(false, org) }
      end
    end
  end

  def destroy
    ok = true
    team = @organization.teams.where(:name => Team::ADMINS).first
    admin_group_count = team.groups.count
    if admin_group_count > 0
      ok = false
      @organization.errors.add(:base, "organization has #{admin_group_count} admin #{'group'.pluralize(admin_group_count)}.")
    end

    if ok
      admin_user_count = team.users.count
      if admin_user_count > 1
        ok = false
        @organization.errors.add(:base, "organization has #{admin_user_count} other admin #{'user'.pluralize(admin_user_count)}.")
      end
    end


    if ok
      old_org_scope = Cms::Relation.headers['X-Cms-Scope']
      ns_path = "/#{@organization.name}"
      Cms::Relation.headers['X-Cms-Scope'] = ns_path
      instance_count = Cms::Relation.count(:nsPath            => ns_path,
                                           :recursive         => true,
                                           :relationShortName => 'DeployedTo',
                                           :direction         => 'to',
                                           :groupBy           => 'ciId').values.sum
      if instance_count > 0
        ok = false
        @organization.errors.add(:base, "Cannot delete organization with deployed instances (#{instance_count}).")
      end
      Cms::Relation.headers['X-Cms-Scope'] = old_org_scope
    end

    if ok
      old_org_scope = Cms::Ci.headers['X-Cms-Scope']
      Cms::Ci.headers.delete('X-Cms-Scope')
      ci = Cms::Ci.first(:params => {:nsPath => '/', :ciClassName => 'account.Organization', :ciName => @organization.name})
      ok = execute(ci, :destroy)
      Cms::Ci.headers['X-Cms-Scope'] = old_org_scope
    end

    if ok
      ActiveRecord::Base.transaction do
        @organization.destroy
        current_user.update_attribute(:organization_id, current_user.organizations.count > 0 ? current_user.organizations.first.id : nil) if current_user.organization_id == @organization.id
      end
    end

    if ok
      respond_to do |format|
        format.js do
          flash[:notice] = "Successfully deleted organization '#{@organization.name}'."
          render :action => :index
        end

        format.json { render_json_ci_response(true, @organization) }
      end
    else
      respond_to do |format|
        format.js do
          flash[:error] = "Failed to delete organization '#{@organization.name}': #{@organization.errors.full_messages.join('; ')}"
          render :js => ''
        end

        format.json { render_json_ci_response(false, @organization) }
      end
    end
  end

  def current_organization
    render :json => current_user.organization
  end

  def leave
    if leave_organization
      respond_to do |format|
        format.js do
          flash[:notice] = 'Successfully left organization.'
          render :action => :index
        end

        format.json { render_json_ci_response(true, @organization) }
      end
    else
      respond_to do |format|
        format.js do
          flash[:error] = "Failed to leave organization '#{@organization.name}': : #{@organization.errors.full_messages.join('; ')}."
          render :action => :index
        end

        format.json { render_json_ci_response(false, @organization) }
      end
    end
  end


  private

  def find_organization
    id = params[:id]
    if id =~ /\D/
      @organization = current_user.organizations.where('organizations.name' => id).first
    else
      @organization = current_user.organizations.where('organizations.id' => id).first
    end
  end

  def leave_organization
    team = @organization.teams.where(:name => Team::ADMINS).first
    if team.users.count > 1 || (team.users.first.id != current_user.id)
      ActiveRecord::Base.transaction do
        @organization.teams.each {|t| t.users.delete(@current_user)}
        current_user.update_attribute(:organization_id, current_user.organizations.count > 0 ? current_user.organizations.first.id : nil) if current_user.organization_id == @organization.id
      end
      return true
    else
      @organization.errors.add(:base, 'Can not orphan existing organization - you are the only admin for this organization.')
      return false
    end
  end
end
