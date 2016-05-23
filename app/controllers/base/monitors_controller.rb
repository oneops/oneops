class Base::MonitorsController < ApplicationController
  include ::RfcHistory

  helper_method :custom_monitors_allowed?, :is_custom_monitor?

  def custom_monitors_allowed?
    @environment.ciAttributes.custom_monitor == 'true'
  end

  def is_custom_monitor?(monitor = @monitor)
    monitor.ciAttributes.custom == 'true'
  end

  protected

  def ci_resource
    @monitor
  end
end
