# install lsof for some port and fd monitors to get as nagios user
package "lsof"

case node.platform
when "redhat","centos","fedora"
  execute "chmod +s /usr/sbin/lsof"
when "suse","ubuntu"
  execute "chmod +s /usr/bin/lsof"
end

nagios_service = "nagios"
case node.platform
when "redhat","centos","fedora","suse"
  package "nagios"    
  package "nagios-plugins"
  
  unless node.platform == "suse"
    package "nagios-devel"  
    package "nagios-plugins-load"
    package "nagios-plugins-http"
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
else
  nagios_service = "nagios3"
  package "nagios3-core"

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


execute "rm -fr /etc/nagios3"
  
link "/etc/nagios3" do
  to "/etc/nagios"
end    

execute "mkdir -p /opt/nagios /var/cache/nagios3 /var/log/nagios3/archives /var/run/nagios3 /var/run/nagios /var/lib/nagios3/rw /var/log/nagios3/rw /var/lib/nagios3/spool/checkresults"
execute "chown nagios:nagios /var/run/nagios3"
execute "chown nagios:nagios /var/run/nagios"
execute "chown nagios:nagios /var/cache/nagios3"
execute "chown -R nagios:nagios /var/lib/nagios3"
execute "chown -R nagios:nagios /var/log/nagios3"

# remove bad symlink from old monitor::add
execute "rm -f /opt/nagios/libexec/plugins"
execute "cp /home/oneops/shared/cookbooks/monitor/files/default/* /opt/nagios/libexec/"
execute "chmod +x /opt/nagios/libexec/*"

template "/etc/logrotate.d/nagios" do
  source "logrotate.erb"
  owner "root"
  group "root"
  mode 0644
end
