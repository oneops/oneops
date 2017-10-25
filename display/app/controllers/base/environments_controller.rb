class Base::EnvironmentsController < ApplicationController
  protected

  def load_platform_instances_info
    @platform_instance_counts = Cms::Ci.count_and_group_by_ns(environment_bom_ns_path(@environment))
  end
end
