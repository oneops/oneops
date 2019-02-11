#
# Cookbook Name:: monitor
# Recipe:: add
#
# Copyright 2016, Walmart Stores, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# gens nagios config using WatchedBy payload and reloads/starts nagios and perf-agent
#
require 'fileutils'

cloud_name = node.workorder.cloud.ciName

ostype = ''
begin
  ostype = node.workorder.payLoad.os[0].ciAttributes['ostype']
rescue
  begin
    ostype = node.workorder.rfcCi.ciAttributes['ostype']
  rescue
    ostype = node.platform
  end
end

if ostype.nil? || ostype.length == 0
  # Using RUBY_PLATFORM is not the best here, because this recipe will run from the inductor
  # and from the VM being created.  If it has to use this, you may not get thre results
  # you want.
  Chef::Log.info('OSTYPE IS STILL NOT SET, TRY TO USE RUBY_PLATFORM')
  Chef::Log.info("RUBY_PLATFORM IN MONITOR IS: #{RUBY_PLATFORM}")
  case RUBY_PLATFORM
  when /mingw/
    ostype = 'windows'
    Chef::Log.info('Setting ostype to windows')
  when /linux/
    ostype = 'linux'
    Chef::Log.info('Setting ostype to linux')
  else
    Chef::Log.info('leaving ostype as nil')
  end
end

Chef::Log.info("*** OS_PLATFORM => #{ostype} ***")

dir_prefix = ''
if ostype =~ /windows/
  dir_prefix = 'c:/cygwin64'
end

cloud_service = nil

if !node.workorder.services['monitoring'].nil? &&
   !node.workorder.services['monitoring'][cloud_name].nil?

  cloud_service = node.workorder.services['monitoring'][cloud_name]
end

# skip if no managed via without monitoring cloud service
if cloud_service.nil? && (!node.workorder.payLoad.has_key?('ManagedVia') &&
  !node.workorder.rfcCi.ciClassName.include?('Compute') )

  Chef::Log.info('no monitoring service provided, no managed via, nor compute. ')
  Chef::Log.info('will skip monitor creation. services: '+node.workorder.services.inspect)
  return
end

# delegate to monitoring service to config
if !cloud_service.nil?
  recipe_name = cloud_service
  Chef::Log.info('including cloud monitor recipe: ' + cloud_service.ciClassName.split('.').last.downcase + '::monitor')
  include_recipe cloud_service.ciClassName.split('.').last.downcase + '::monitor'
  return
end

if !node.workorder.payLoad.has_key?('WatchedBy')
  Chef::Log.info('no WatchedBy - skipping monitor::add')
  return
end

if node.workorder.payLoad[:Environment][0][:ciAttributes][:monitoring] == 'false'

  FileUtils::mkdir_p "#{dir_prefix}/opt/nagios/libexec"

  Chef::Log.info('monitoring disabled for the env')
  return
end

is_new_compute = false
if node.workorder.rfcCi.ciClassName =~ /bom\..*\.Compute/
  is_new_compute = true
else
  include_recipe 'monitor::install_nagios'
end

Chef::Log.info("Is new compute: #{is_new_compute}")

conf_dir = "#{dir_prefix}/etc/nagios"
perf_dir = "#{dir_prefix}/opt/oneops/perf"
if is_new_compute
  puuid = (0..32).to_a.map{|a| rand(32).to_s(32)}.join
  conf_dir = "/tmp/#{puuid}"
  perf_dir = "#{conf_dir}/perf"
  FileUtils::mkdir_p "#{conf_dir}/conf.d"
end

node.set['nagios_conf_dir'] = conf_dir
node.set['perf_conf_dir'] = perf_dir

template "#{conf_dir}/nagios.cfg" do
    source 'nagios.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/conf.d/generic-service.cfg" do
    source 'generic-service.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/conf.d/generic-host.cfg" do
    source 'generic-host.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/resource.cfg" do
    source 'resource.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/conf.d/contacts.cfg" do
    source 'contacts.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/conf.d/timeperiods.cfg" do
    source 'timeperiods.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

template "#{conf_dir}/conf.d/command_perf_process.cfg" do
    source 'perf_process_command.cfg.erb'
    cookbook 'monitor'
    mode 0644
end

if node.workorder.payLoad.has_key?('WatchedBy')
  node.workorder.payLoad.WatchedBy.each do |monitor|
    config = ''
    config_map = {}
    metrics = JSON.parse(monitor['ciAttributes']['metrics'])
    metrics.each_pair do |k,v|
      dstype = 'gauge'
      if v.has_key?('dstype')
        dstype = v['dstype'].downcase
      end
      config += "#{k}-dstype=#{dstype}\n"
    end

    if monitor['ciAttributes'].has_key?('sample_interval')
      config += 'interval='+ monitor['ciAttributes']['sample_interval']
    else
      config += 'interval=60'
    end

    metric_config_dir = "#{perf_dir}/#{node.workorder.rfcCi.ciId}-#{monitor['ciName']}"
    FileUtils::mkdir_p "#{metric_config_dir}"
    metric_config_file = "#{metric_config_dir}/config"
    # write simple dstype file
    File.open(metric_config_file, 'w') { |file| file.write(config) }

  end

end

ruby_block 'setup nagios' do
  block do
    Chef::Resource::RubyBlock.send(:include, Chef::Mixin::ShellOut)
    $conf_d = "#{node.nagios_conf_dir}/conf.d"

    def delete_monitor_ciid(ci_id)
       Chef::Log.info("Delete all the files for the monitor cid= service_#{ci_id}")
       `rm -rf #{$conf_d}/service_#{ci_id}* `
    end

    def set_service(monitor)
      ci_id = node.workorder.rfcCi.ciId
      cmd_options = Mash.new(JSON.parse(monitor[:ciAttributes][:cmd_options]))
      cmd = eval('"' + monitor[:ciAttributes][:cmd] + '"')
      source = monitor[:ciName]
      monitor_name = ci_id.to_s+"-"+source

      enableAttr = 1
      if(monitor[:ciAttributes][:enable] == 'false')
         enableAttr = 0
      end

      template = 'define service{\n'
      template += '  use                 generic-service\n'
      template += '  host_name           :::ci_id:::\n'
      template += '  service_description :::monitor_name:::\n'
      template += '  display_name        :::source:::\n'
      template += '  check_command       :::cmd:::\n'
      template += '  register            :::enableAttr:::\n' # 1: enable the monitor 0:disable the monitor
      if monitor['ciAttributes'].has_key?('sample_interval')
        template += '  normal_check_interval '+ monitor['ciAttributes']['sample_interval']+"\n"
      end
      template += get_additional_attributes(monitor,'service')
      template += '}\n\n'

      template.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
      new_block = eval( '"' + template + '"' )

      # sub wo variables
      new_block.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
      final_block = eval( '"' + new_block + '"' )

      Chef::Log.info("adding: #{final_block}")
      service_file = $conf_d + '/service_' + monitor_name + '.cfg'
      ::File.open(service_file, 'w') {|f| f.write(final_block) }
      return 1
    end

    def get_additional_attributes(monitor, attr_type)
      attrs = ""
      monitor.each do |key, value|
        if key =~ /^#{attr_type}_/ && key !~ /^#{attr_type}_name/
          key = key.gsub(/^#{attr_type}_/,'')
          Chef::Log.info("--- CUSTOM #{attr_type} : #{key} #{value}\n")
          attrs += "  #{key} #{value}\n"
        end
      end
      return attrs
    end

    def set_host(monitor)
      ci_id = node.workorder.rfcCi.ciId
      cloud_ci_id = node.workorder.cloud.ciId

      template = 'define host{\n'
      template += '  use       generic-host\n'
      template += '  host_name :::ci_id:::\n'
      template += '  alias     :::cloud_ci_id:::\n'
      template += '  address   127.0.0.1\n'
      template += get_additional_attributes(monitor,'host')
      template += '}\n\n'

      template.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
      new_block = eval( '"' + template + '"' )
      Chef::Log.info('adding: '+new_block)
      host_file = $conf_d + '/host_' + ci_id.to_s + '.cfg'
      ::File.open(host_file, 'w') {|f| f.write(new_block) }
      return 1
    end

    def set_hostgroup(monitor)
      ci_id = node.workorder.rfcCi.ciId
      ci_name = node.workorder.rfcCi.ciName

      template = 'define hostgroup{\n'
      template += '  hostgroup_name :::ci_name:::\n'
      template += '  alias          local\n'
      template += '  members        :::ci_id:::\n'
      template += get_additional_attributes(monitor,'hostgroup')
      template += '}\n\n'

      template.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
      new_block = eval( '"' + template + '"' )
      Chef::Log.info('adding: '+new_block)
      hostgroup_file = $conf_d + '/hostgroup_' + ci_name + '.cfg'
      ::File.open(hostgroup_file, 'w') {|f| f.write(new_block) }
      return 1
    end

    def set_command(monitor)
      command_name = monitor[:ciAttributes][:cmd].split('!')[0]
      command_line = monitor[:ciAttributes][:cmd_line]

      template = 'define command{\n'
      template += '  command_name :::command_name:::\n'
      template += '  command_line :::command_line:::\n'
      template += get_additional_attributes(monitor,'command')
      template += '}\n\n'

      template.gsub!( /:::(.*?):::/ ) { '#{'+$1+'}' }
      new_block = eval( '"' + template + '"' )
      Chef::Log.info('adding: '+new_block )
      command_file = $conf_d + '/command_' + command_name + '.cfg'
      ::File.open(command_file, 'w') {|f| f.write(new_block) }
      return 1
    end

    def process_monitor(monitor)
      Chef::Log.info("--- add monitor #{monitor['ciAttributes']['name']} ---")
      changes = 0
      changes += set_host(monitor)
      changes += set_command(monitor)
      changes += set_service(monitor)
      # remove hostgroup - will move to config.d next
      #changes += set_hostgroup(monitor)
      return changes
    end

    ########################
    ########################

    changes = 0

    # start my clearing previous config for ci
    ci_id = node.workorder.rfcCi.ciId
    delete_monitor_ciid(ci_id)

    nagios_service = 'nagios'
    if node.platform == 'ubuntu'
      nagios_service = 'nagios3'
    end

    # gets a list of monitors
    monitors = node.workorder.payLoad[:WatchedBy]
    monitors.each do |monitor|
      name = monitor[:ciAttributes][:name]
      changes += process_monitor(monitor)
    end

    if is_new_compute

      # sync and restart
      # node rsync and ssh set from compute::base
      cmd = node.rsync_cmd.gsub('SOURCE',conf_dir).gsub('DEST','/tmp').gsub('IP',node.ip)
      result = shell_out(cmd)
      Chef::Log.info("#{cmd} returned: #{result.stdout}")
      result.error!

      dirs = ["#{dir_prefix}/var/run/nagios3","#{dir_prefix}/opt/nagios/libexec"]
      dirs += ["#{dir_prefix}/var/log/nagios3","#{dir_prefix}/opt/oneops/perf"]
      # not going to chown for windows
      if ostype =~ /windows/
        cmd = node.ssh_cmd.gsub('IP',node.ip) + '"' + 'sudo mkdir -p '+dirs.join(' ')+';' +
         'sudo chown -R oneops:Administrators /var/run/nagios3 /var/log/nagios3 /opt/oneops/perf' + '"'
      else
        cmd = node.ssh_cmd.gsub('IP',node.ip) + '"' + 'sudo mkdir -p '+dirs.join(' ')+';' +
         'sudo chown -R nagios:nagios /var/run/nagios3 /var/log/nagios3 /opt/oneops/perf' + '"'
      end
      result = shell_out(cmd)
      Chef::Log.info("#{cmd} returned: #{result.stdout}")
      result.error!

      # copy default plugins
      nagios_plugins = File.join(
        File.expand_path('../../', __FILE__), '/files/default/', '*'
      )
      cmd = node.rsync_cmd.gsub('SOURCE',nagios_plugins).gsub('DEST','~/nagios_libexec/').gsub('IP',node.ip)
      result = shell_out(cmd)
      Chef::Log.info("#{cmd} returned: #{result.stdout}")
      result.error!

      cmd = node.ssh_cmd.gsub('IP',node.ip) + '"' + "sudo mv ~/nagios_libexec/* #{dir_prefix}/opt/nagios/libexec/" + '"'
      result = shell_out(cmd)
      Chef::Log.info("#{cmd} returned: #{result.stdout}")
      result.error!

      cmd = node.ssh_cmd.gsub('IP',node.ip) + '"' + "sudo cp -r #{conf_dir}/* #{dir_prefix}/etc/nagios/; sudo cp -r #{conf_dir}/perf #{dir_prefix}/opt/oneops/; " +
          "sudo chmod +x #{dir_prefix}/opt/nagios/libexec/* " + '"'
      result = shell_out(cmd)
      Chef::Log.info("#{cmd} returned: #{result.stdout}")
      result.error!

      `rm -fr #{conf_dir}`

    else
      # standard way
      # for flume agent use in connecting to collectors
      mgmt_domain = node.mgmt_domain
      `echo #{mgmt_domain} > #{dir_prefix}/opt/oneops/mgmt_domain`

      cloud = node.workorder.cloud.ciName
      `echo #{cloud} > #{dir_prefix}/opt/oneops/cloud`

      env = node.workorder.payLoad[:Environment][0]
      has_monitoring = true
      Chef::Log.debug('env: '+env.inspect)
      if env[:ciAttributes].has_key?('monitoring')
        Chef::Log.info('monitoring: '+env[:ciAttributes][:monitoring])
        if env[:ciAttributes][:monitoring] == 'false'
          has_monitoring = false
        end
      end

      Chef::Log.info("total of #{changes} changes")
      # path to initd is diff for windows
      if ostype =~ /windows/
        `chown -R oneops:Administrators /etc/nagios /opt/oneops/perf`
		# restart nagios & forwarder
        # `c:/cygwin64/etc/rc.d/init.d/nagios restart && c:/cygwin64/etc/init.d/perf-agent restart`
      else
        `chown -R nagios:nagios /etc/nagios /opt/oneops/perf`
        # restart nagios & forwarder
	# Not required to restart the perf-agent and forwarder as the perf-agent recovers
	# TODO: IF clause needs to be removed .      
        #`/etc/init.d/nagios restart && /etc/init.d/perf-agent restart`
      end

    end

  end
end


if is_new_compute
  include_recipe 'compute::ssh_key_file_rm'
else
  if ostype =~ /windows/
    perf_dir = '/opt/oneops/perf'
	    
    #grant permissions to all subfolders and files to SYSTEM
    directory perf_dir do
      rights :modify, 'SYSTEM'
      rights :full_control, 'oneops'
      rights :full_control, 'Administrators'
      inherits false
      action :create
    end
  
    ps_code = "
    $Path = '#{perf_dir}'
    $acl = Get-Acl $Path
    $Objects = Get-ChildItem -Path $Path -Recurse | % { $_.FullName }
    ForEach ($Object in $Objects)  {Set-Acl -Path $Object -AclObject $acl }"

    powershell_script 'Assign Permissions' do
      code ps_code
    end
  end

  nagios_service = 'nagios'
  if node.platform == 'ubuntu'
    nagios_service = 'nagios3'
  end

  if node.workorder.payLoad.Environment[0][:ciAttributes].has_key?('monitoring') &&
     node.workorder.payLoad.Environment[0][:ciAttributes][:monitoring] == 'true'

    if File.exist?('/etc/init.d/nagios') && node[:platform_family].include?('rhel')
      provider = Chef::Provider::Service::Redhat
    elsif node[:platform_family] == 'rhel' && node[:platform_version].to_i >= 7
      provider = Chef::Provider::Service::Systemd
    else
      provider = nil
    end

    service nagios_service do
      provider provider if provider
      supports [ :restart, :enable ]
      action [ :restart, :enable ]
    end

  else
    service nagios_service do
      action [ :stop, :disable ]
    end
    service 'perf-agent' do
      action [ :stop, :disable ]
    end
  end
end

include_recipe 'monitor::nagios_service_fix' unless node.workorder.rfcCi.ciClassName =~ /bom\..*\.Compute/
