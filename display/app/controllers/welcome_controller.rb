class WelcomeController < ApplicationController
  skip_before_filter :authenticate_user!, :check_organization, :check_eula, :check_reset_password, :except => [:index]
  before_filter :check_signed_in, :except => [:api_docs]
  skip_filter *_process_action_callbacks.map(&:filter), :only => [:api_docs]

  IMAGE_STUB = Rails.root.join('public', 'images', 'cms', 'ci_stub.png')

  def api_docs
    render :action => :api_docs, :layout => false
  end

  def error
    @message = params[:message].presence
    respond_to do |format|
      format.html

      format.js do
        flash[:error] = @message
        render :js => ''
      end

      format.json {render :json => {:code => 500, :exception => @message}, :status => :internal_server_error}
      format.any {render :status => :internal_server_error}
    end
  end

  def server_error
    @exception = env['action_dispatch.exception']
    respond_to do |format|
      format.html {render :status => :internal_server_error, :layout => false}
      format.js {render :js => '', :status => :internal_server_error}
      format.json {render :json => {:code => 500, :exception => @exception.to_s}, :status => :internal_server_error}
    end
  end

  def not_found_error
    respond_to do |format|
      format.html
      format.js {render :js => '', :status => :not_found}
      format.json {render :json => {:errors => ['not found']}, :status => :not_found}
      format.any {render :text => 'not found', :status => :not_found}
    end
  end

  def custom_log_info
    {:exception => @exception.to_s} if @exception
  end

  def image_not_found
    send_file(IMAGE_STUB, :type => 'image/png', :disposition => 'inline')
  end


  private

  def check_signed_in
    redirect_to new_user_session_path unless user_signed_in?
  end
end
