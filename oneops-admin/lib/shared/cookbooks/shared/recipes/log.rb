#
# idempotent nagios log add that gens nagios config whats defined in log.yml and reloads/starts nagios
#

# where local logs get configured:
$localhost_cfg = '/opt/nagios/conf/objects/localhost.cfg'

require 'yaml'
require 'json'

log_class = node.app_name

config_file = Chef::Config[:cookbook_path]+"/#{log_class}/recipes/log.yml"

unless File.exists?(config_file)
        Chef::Log.info("no log.yml here: #{config_file} ...skipping")
        return
end
	
yml = YAML::load(File.open(config_file))


def set_instance(log)
  cmd = log['cmd']
	log_name = log['name']
	ci_id = node.workorder.rfcCi.ciId
	text = File.read($localhost_cfg)

	if text.match(/service_description\s+#{log_name}/)
		puts "already has service #{log_name} in #{$localhost_cfg}"
		return 0
	end

	template = 'define service{\n'
	template += '\t use                 local-service\n'
	template += '\t host_name           :::ci_id:::\n'
	template += '\t service_description :::log_name:::\n'
	template += '\t check_command       :::cmd:::\n'
        template += get_additional_attributes(log,'service')
	template += '}\n\n'

    	template.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
    	new_block = eval( '"' + template + '"' )
	puts 'adding: '+new_block
	File.open($localhost_cfg, 'a') {|f| f.write(new_block) }
	return 1
end


def get_additional_attributes(log, attr_type)
	attrs = ""
	log.each do |key, value| 
		if key =~ /^#{attr_type}_/ && key !~ /^#{attr_type}_name/
			key = key.gsub(/^#{attr_type}_/,'') 
			puts "--- CUSTOM #{attr_type} : #{key} #{value}\n"
			attrs += "\t #{key} #{value}\n" 
		end
        end	
	return attrs
end



def process_log(log)
	puts "--- add log #{log['name']} ---\n"
	changes = 0
	changes += set_instance(log)
	return changes
end


########################
########################

changes = 0

# gets a list of logs to flume back to ldb
logs = yml['log']
logs.each do |log|
	changes += process_log(log)
end

puts "changes: "+changes
puts "restart flume..."
system('/opt/flume/bin/agent restart')
