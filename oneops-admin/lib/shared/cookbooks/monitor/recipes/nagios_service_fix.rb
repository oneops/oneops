
template '/etc/init.d/nagios' do
	source 'nagios_service_3.5.1.erb'
	mode '0755'
end
      
service 'nagios' do
	action :reload
end
