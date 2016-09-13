class NotificationsController < ApplicationController
  before_filter :find_notification, :only => [:show, :edit, :update, :destroy]

  def index
    load_notifications

    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @notifications }
    end
  end

  def show
    render_json_ci_response(true, @notification)
  end

  def new
    load_available_types
    @notification_type = params[:notification_type]
    @notification = Cms::Ci.build(:ciClassName => @notification_type, :nsPath => organization_ns_path) if @available_types.find { |t| t.className == @notification_type }

    respond_to do |format|
      format.js { render(:action => :new) }
      format.json { render_json_ci_response(true, @notification) }
    end
  end

  def create
    ns_path = organization_ns_path
    @notification = Cms::Ci.build(params[:cms_ci].merge(:nsPath => ns_path))
    forwards_to = Cms::Relation.build(:relationName => 'base.ForwardsTo',
                                      :nsPath       => ns_path,
                                      :fromCiId     => current_user.organization.ci.ciId,
                                      :toCi         => @notification)

    ok = execute_nested(@notification, forwards_to, :save)
    respond_to do |format|
      format.js do
        if ok
          load_notifications
          render :action => :index
        else
          @notification_type = @notification.ciClassName
          load_available_types
          render(:action => :new)
        end
      end

      format.json { render_json_ci_response(ok, @notification) }
    end
  end

  def edit
    respond_to do |format|
      format.js { render(:action => :edit) }
      format.json { render_json_ci_response(true, @notification) }
    end
  end

  def update
    ok = execute(@notification, :update_attributes, params[:cms_ci])
    if ok
      index
    else
      respond_to do |format|
        format.js { render(:action => :edit) }
        format.json { render_json_ci_response(true, @notification) }
      end
    end
  end

  def destroy
    ok = execute(@notification, :destroy)
    if ok
      index
    else
      respond_to do |format|
        format.js do
          flash[:error] = 'Failed to delete notification.'
          render(:action => :edit)
        end

        format.json { render_json_ci_response(true, @notification) }
      end
    end
  end


  private

  def load_notifications
    @notifications = Cms::Relation.all(:params => {:ciId              => current_user.organization.ci.ciId,
                                                   :relationShortName => 'ForwardsTo',
                                                   :direction         => 'from',
                                                   :nsPath            => organization_ns_path}).map(&:toCi)
  end

  def find_notification
    @notification = Cms::Ci.find(params[:id])
  end

  def load_available_types
    @available_types = Cms::CiMd.all(:params => {:package => 'account.notification'})
  end
end
