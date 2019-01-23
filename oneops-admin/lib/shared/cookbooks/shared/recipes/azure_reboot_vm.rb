
include_recipe "shared::set_provider_new"

vm_svc = node[:iaas_provider]

ci_attributes = node[:workorder][:payLoad][:ManagedVia][0][:ciAttributes]
vm_name = ci_attributes[:instance_name]
resource_group_name = ci_attributes[:instance_id].split('/')[4]

if resource_group_name.nil? || resource_group_name.empty?
  Chef::Log.info("Resource group not found for the VM #{vm_name}")
  return
end

Chef::Log.info("Rebooting VM name #{vm_name} in resource group #{resource_group_name}.")


begin
  server = vm_svc.servers(resource_group: resource_group_name).get(resource_group_name, vm_name)
  server.restart
  Chef::Log.info("VM #{vm_name} has been successfully rebooted.")
rescue Exception => e
  msg = "VM #{vm_name} in resource group #{resource_group_name} failed to reboot, Exception #{e}"
  puts "***FATAL: #{msg}"
  raise(msg)
end
