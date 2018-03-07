if !is_propagate_update
  Chef::Log.info("adding monitor")
  include_recipe "monitor::add"
else
  Chef::Log.info("Skipping monitor add")
end