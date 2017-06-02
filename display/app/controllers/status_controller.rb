class StatusController < ApplicationController
  SHUTDOWN_SIGNAL_FILE_NAME = 'public/shut.down'
  skip_before_filter :authenticate_user!, :check_username, :check_reset_password,
                     :check_eula, :check_organization, :set_active_resource_headers


  def ecv
    if File.exist?(SHUTDOWN_SIGNAL_FILE_NAME)
      begin
        text = File.read(SHUTDOWN_SIGNAL_FILE_NAME)
      rescue
        text = ''
      end
      render :text => text, :status => :service_unavailable
    else
      render :text => 'ok', :status => :ok
    end
  end
end
