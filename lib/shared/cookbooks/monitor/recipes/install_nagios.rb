require 'fileutils'

Chef::Log.info("PLATFORM IS: #{node.platform}")

if node.platform !~ /windows/
  # install lsof for some port and fd monitors to get as nagios user
  package "lsof" do
    not_if { ::File.exists?("/usr/sbin/lsof") }
  end
end

case node.platform
when "redhat","centos","fedora"
  execute "chmod +s /usr/sbin/lsof"
when "suse","ubuntu"
  execute "chmod +s /usr/bin/lsof"
end

nagios_service = "nagios"
case node.platform
when "redhat","centos","fedora","suse"
  package "nagios" do
    not_if "rpm -qa | egrep -qe 'nagios-[0-9]+'"
  end
  package "nagios-plugins" do
    not_if "rpm -qa | egrep -qe 'nagios-plugins-[0-9]+'"
  end

  unless node.platform == "suse"
    package "nagios-devel" do
      not_if "rpm -qa | egrep -qe 'nagios-devel-[0-9]+'"
    end
    package "nagios-plugins-load" do
      not_if "rpm -qa | egrep -qe 'nagios-plugins-load-[0-9]+'"
    end
    package "nagios-plugins-http" do
      not_if "rpm -qa | egrep -qe 'nagios-plugins-http-[0-9]+'"
    end
  end

  execute "cp /usr/lib64/nagios/plugins/check_load /opt/nagios/libexec/" do
    not_if "test -f /opt/nagios/libexec/check_load"
    only_if "test -d /usr/lib64/nagios/plugins"
  end
  execute "cp /usr/lib/nagios/plugins/check_load /opt/nagios/libexec/" do
    not_if "test -f /opt/nagios/libexec/check_load"
    only_if "test -d /usr/lib/nagios/plugins"
  end
  execute "cp /usr/lib64/nagios/plugins/check_http /opt/nagios/libexec/" do
    not_if "test -f /opt/nagios/libexec/check_http"
    only_if "test -d /usr/lib64/nagios/plugins"
  end
  execute "cp /usr/lib/nagios/plugins/check_http /opt/nagios/libexec/" do
    not_if "test -f /opt/nagios/libexec/check_http"
    only_if "test -d /usr/lib/nagios/plugins"
  end

  # in case conf.d doesn't exist
  directory "/etc/nagios/conf.d"

  # remove default stuff added by redhat
  execute "rm -f internet.cfg" do
   cwd "/etc/nagios3/conf.d/"
   returns [0,1]
  end
when "windows"

  #Download and install only if not already installed, should happen in OS step
  if !(::Win32::Service.exists?('nagios'))
    cloud_name = node[:workorder][:cloud][:ciName]
    services = node[:workorder][:services]
    if services.has_key?(:mirror)
      nagios_for_win = JSON.parse(node[:workorder][:services][:mirror][cloud_name][:ciAttributes][:mirrors])['nagios-windows']
    else
      msg = 'Mirror service required for OS!!!'
      puts"***FAULT:FATAL=#{msg}"
      e=Exception.new("#{msg}")
      e.set_backtrace('')
      raise e
    end

    if nagios_for_win.nil? || nagios_for_win.size == 0
      msg = 'Could not find nexus url in mirror service!!'
      puts"***FAULT:FATAL=#{msg}"
      e=Exception.new("#{msg}")
      e.set_backtrace('')
      raise e
    end

    # download tar from Nexus
    execute "wget -P c:/cygwin64 #{nagios_for_win}"

    # get the filename from the mirror url
    file_array = nagios_for_win.split('/')
    file_name = file_array.last
    Chef::Log.info("Nagios Filename is: #{file_name}")
    # un tar it in /cygdrive/c/cygwin64/opt/nagios/
    execute "tar xzf /cygdrive/c/cygwin64/#{file_name} -C /cygdrive/c/cygwin64/opt"
    execute "chmod +x /opt/nagios/bin/*"
	
    #Install service for windows  
    powershell_script 'Install nagios service' do
      code "C:/Cygwin64/bin/cygrunsrv.exe -I nagios -d Nagios -p /opt/nagios/bin/nagios.exe -a /etc/nagios/nagios.cfg"
      not_if {::Win32::Service.exists?("nagios")}
    end
  end #if !(::Win32::Service.exists?('nagios'))
else
  nagios_service = "nagios3"
  package "nagios3-core" do
    not_if "dpkg -l | egrep -qe 'nagios3-core'"
  end

  # ubuntu 12.04 + nagios 3.2.3 has issue with not setting p1_file, but ok with it empty
  execute "mkdir -p /usr/lib/nagios3 ; touch /usr/lib/nagios3/p1.pl"
  execute "rm -f /etc/nagios-plugins/config/ping.cfg" do
    returns [0,1]
  end

  # remove default stuff added by debian pkg
  execute "rm -f localhost_nagios2.cfg hostgroups_nagios2.cfg services_nagios2.cfg extinfo_nagios2.cfg generic-service_nagios2.cfg generic-host_nagios2.cfg contacts_nagios2.cfg timeperiods_nagios2.cfg" do
    cwd "/etc/nagios3/conf.d/"
    returns [0,1]
  end

end


dir_prefix = ''
root_user = 'root'
root_group = 'root'
if node.platform =~ /windows/
  dir_prefix = 'c:/cygwin64'
  root_user = 'oneops'
  root_group = 'Administrators'
  
  #configure directory permissions Windows way, plus make sure child folders will inherit rights from parents
  ['/var/lib/nagios3','/var/log/nagios3'].each do |dir_name1|
    directory dir_name1 do
      rights :full_control, 'oneops'
      rights :full_control, 'Administrators'
	  rights :modify, 'SYSTEM', :applies_to_children => true
	  inherits true
    end
  end

  #mount /opt on MinGW so perl scripts from /opt/nagios/libexec can be executed using mingw.perl
  file "C:/tools/DevKit2/etc/fstab" do
    action :create
	content "c:/opt /opt"
	only_if{File.directory?("C:/tools/DevKit2/etc")}
  end
  
  #Adding check_load for windows - in linux it's coming from nagios plugin, but for windows we write a simple bash script, that currently only returns 0s
  cookbook_file "/opt/nagios/libexec/check_load" do
    cookbook 'monitor'
	source "check_load"
  end
end

#Create necessary directories
dirs = ['/var/lib/nagios3/spool/checkresults','/var/lib/nagios3/rw','/var/log/nagios3/archives','/var/log/nagios3/rw']
dirs += ['/var/cache/nagios3', '/var/run/nagios3', '/var/run/nagios', '/etc/logrotate.d']
dirs.each do |dir_name2|
  directory dir_name2 do
    recursive true
  end
end

case node.platform
when "ubuntu"
  execute "rm -rf /etc/nagios3" do
   cwd "/etc/"
   returns [0,1]
  end
end

#Create symlinks
link "#{dir_prefix}/etc/nagios3" do
  to "#{dir_prefix}/etc/nagios"
end

directory '/var/log/nagios' do
  action :delete
  recursive true
  only_if{::File.directory?('/var/log/nagios')}
end

link "#{dir_prefix}/var/log/nagios" do
  to "#{dir_prefix}/var/log/nagios3"
end

# for windows we won't change owners
if node.platform !~ /windows/
  FileUtils::mkdir_p "/opt/nagios"
  execute "chown nagios:nagios /var/run/nagios3 /var/run/nagios /var/cache/nagios3"
  execute "chown -R nagios:nagios /var/lib/nagios3 /var/log/nagios3"
end

# remove bad symlink from old monitor::add
execute "rm -f /opt/nagios/libexec/plugins"
execute "cp /home/oneops/shared/cookbooks/monitor/files/default/* /opt/nagios/libexec/"
execute "chmod +x /opt/nagios/libexec/*"

template '/etc/logrotate.d/nagios' do
  cookbook 'monitor'
  source 'logrotate.erb'
  owner root_user
  group root_group
  mode 0644
end
