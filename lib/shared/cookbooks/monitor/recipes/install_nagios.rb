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
    # execute 'choco install nagios-core-setup'
    Chef::Log.info('WINDOWS!!!!!  Need to download the package, compile and install')

    # get nexus url from OO_CLOUD_VARS
    cloud_vars = node.workorder.payLoad.OO_CLOUD_VARS
    nexus_url = ''
    cloud_vars.each do |var|
      if var[:ciName] == 'nexus'
        nexus_url = var[:ciAttributes][:value]
      end
    end

    if nexus_url.nil? || nexus_url.size == 0
      msg = 'Could not find nexus url in OO Cloud Vars!!'
      puts"***FAULT:FATAL=#{msg}"
      e=Exception.new("#{msg}")
      e.set_backtrace('')
      raise e
    end

    # download tar from Nexus
    execute "wget -P /cygdrive/c/cygwin64 #{nexus_url}/nexus/content/repositories/thirdparty/nagios/nagios/3.5.1/nagios-3.5.1.tar.gz"
    # un tar it in /cygdrive/c/cygwin64/opt/nagios/
    execute 'tar xzf /cygdrive/c/cygwin64/nagios-3.5.1.tar.gz -C /cygdrive/c/cygwin64/opt'

    # check to make sure it isn't already there
    `cygrunsrv --list | grep nagios`
    if $? != 0  # service not installed, install it.
      execute 'cygrunsrv --install nagios -d "Nagios" -p /cygdrive/c/cygwin64/opt/nagios/bin/nagios.exe'
    end

    #execute 'cygrunsrv --start nagios'

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

if node.platform =~ /windows/
  execute "rm -fr /cygdrive/c/cygwin64/etc/nagios3"

  execute "ln -s /cygdrive/c/cygwin64/etc/nagios /cygdrive/c/cygwin64/etc/nagios3"

  execute "mkdir -p /cygdrive/c/cygwin64/opt/nagios /cygdrive/c/cygwin64/var/cache/nagios3 /cygdrive/c/cygwin64/var/log/nagios3/archives /cygdrive/c/cygwin64/var/run/nagios3 /cygdrive/c/cygwin64/var/run/nagios /cygdrive/c/cygwin64/var/lib/nagios3/rw /cygdrive/c/cygwin64/var/log/nagios3/rw /cygdrive/c/cygwin64/var/lib/nagios3/spool/checkresults"
  # execute "chown nagios /cygdrive/c/cygwin64/var/run/nagios3"
  # execute "chown nagios /cygdrive/c/cygwin64/var/run/nagios"
  # execute "chown nagios /cygdrive/c/cygwin64/var/cache/nagios3"
  # execute "chown -R nagios /cygdrive/c/cygwin64/var/lib/nagios3"
  # execute "chown -R nagios /cygdrive/c/cygwin64/var/log/nagios3"

  execute "mkdir -p c:/cygwin64/etc/logrotate.d/nagios"
  # execute "chown nagios c:/cygwin64/etc/logrotate.d/nagios"

  # remove bad symlink from old monitor::add
  execute "rm -f /cygdrive/c/cygwin64/opt/nagios/libexec/plugins"
  execute "cp /home/oneops/shared/cookbooks/monitor/files/default/* /cygdrive/c/cygwin64/opt/nagios/libexec/"
  execute "chmod +x /cygdrive/c/cygwin64/opt/nagios/libexec/*"

  template "c:/cygwin64/etc/logrotate.d/nagios/logrotate.erb" do
    source "logrotate.erb"
    cookbook "monitor"
    mode 0644
  end
else
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
end

