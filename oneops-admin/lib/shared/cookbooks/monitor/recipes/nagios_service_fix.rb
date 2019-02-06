
#fix for this change before we upgrade to a newer nagios package version https://github.com/NagiosEnterprises/nagioscore/commit/f7c6118c794c18b84ce73faa7b2767f847616582

nagios_version = `yum info nagios | grep 'Version'`.gsub(/\s+/, '').gsub('Version:', '')

if nagios_version == '3.5.1' || nagios_version == '4.4.3' && node.platform_family == 'rhel'
	template '/etc/init.d/nagios' do
		source 'nagios_service_3.5.1.erb'
		mode '0755'
		notifies :reload, 'service[nagios]', :immediately
		notifies :restart, 'service[nagios]', :immediately
	end
	service 'nagios' do
                provider Chef::Provider::Service::Redhat if File.exist?('/etc/init.d/nagios') && node[:platform_family].include?("rhel")
      		supports [ :restart, :enable ]
      		action [ :restart, :enable ]
	end
end
