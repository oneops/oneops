#!/usr/bin/env ruby

# Script arguments
service_name = ARGV[0]
use_script_status = ARGV[1]
pattern = ARGV[2]
secondary_down = ARGV[3]

# Check if the node is secondary or standby
adminstatus = `grep ONEOPS_CLOUD_ADMINSTATUS /etc/profile.d/oneops.sh`.split('=').last
secondary_down = adminstatus.include?('secondary') && (secondary_down == 'true')

if use_script_status == 'true'
  `service #{service_name} status 2>&1`
else
  `ps auxwww| grep -v grep | grep -v check_process | egrep #{pattern} 2>&1`
end

exit_status = $?.to_i

if exit_status == 0
  puts secondary_down ? "#{service_name} down |up=0" : "#{service_name} up |up=100"
else
  puts secondary_down ? "#{service_name} up |up=100" : "#{service_name} down |up=0"
end
