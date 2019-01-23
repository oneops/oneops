#
# handles rebooting of a VM
#

provider = nil
sleep_time = 60
max_wait_time = 300
wait_step = 20

#only process when we have compute service
if node[:workorder][:services].has_key?('compute')
  cloud_name = node[:workorder][:cloud][:ciName]
  provider = node[:workorder][:services][:compute][cloud_name][:ciClassName].gsub("cloud.service.","").downcase.split(".").last
else
  Chef::Log.info("No compute service - wait #{sleep_time} seconds.")
  sleep sleep_time
  return
end

Chef::Log.info("Cloud Provider #{provider}")

if provider =~ /azure/
  Chef::Log.info("Calling azure reboot vm recipe")
  include_recipe "shared::azure_reboot_vm"
  return
end

#only process for openstack clouds (TO-DO support other clouds)
if provider !~ /openstack/
  Chef::Log.info("Cloud Provider #{provider} is not supported - wait #{sleep_time} seconds.")
  sleep sleep_time
  return
end

include_recipe "shared::set_provider"

conn = node[:iaas_provider]
instance_id = node[:workorder][:payLoad][:ManagedVia][0][:ciAttributes][:instance_id]

def is_reboot_successful? (reboot_type, max_wait_time, wait_step, conn, instance_id)
  reboot_succeeded = false
  start_time = Time.now.to_i
  i = 0

  while Time.now.to_i - start_time < max_wait_time do
    status = conn.servers.get(instance_id).state

    if status =~ /ACTIVE/
      reboot_succeeded = true
      Chef::Log.info( "VM has been successfully #{reboot_type} rebooted.")
      break
    end

    Chef::Log.info( "#{reboot_type} Rebooting - waited:#{i*wait_step} seconds, VM status was: #{status}, waiting #{wait_step} seconds...")
    sleep wait_step
    i += 1
  end #while

  return reboot_succeeded
end

  #test
  conn.servers.get(instance_id).reboot
  sleep 2

#1 wait for regular reboot to finish, if not successful try hard reboot
if !is_reboot_successful?('', max_wait_time, wait_step, conn, instance_id)
  Chef::Log.info( "Soft reboot was unsuccessful, trying a HARD reboot now.")
  conn.servers.get(instance_id).reboot('HARD')
  sleep 5
else
  return
end

#2 wait for hard reboot to finish
if !is_reboot_successful?('HARD', max_wait_time, wait_step, conn, instance_id)
  msg = "VM #{instance_id} failed to reboot"
  puts "***FATAL: #{msg}"
  raise(msg)
end
