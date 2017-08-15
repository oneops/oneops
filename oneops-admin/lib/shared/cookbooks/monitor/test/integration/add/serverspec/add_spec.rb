require 'spec_helper'

describe service('nagios') do
 	it { should be_enabled }
  	it { should be_running }
  	it { should be_running.under('systemd') }
end

['nagios', 'nagios-plugins', 'nagios-devel', 'nagios-plugins-load', 'nagios-plugins-http'].each do |p|
	describe package("#{p}") do
  		it { should be_installed }
	end
end

['/var/log/nagios/nagios.log', '/var/log/nagios/host.perflog'].each do |f|
	describe file("#{f}") do
  		it { should be_file }
  		it { should exist }
  		it { should be_owned_by 'nagios' }
  		it { should be_grouped_into 'nagios' }
  		it { should be_readable }
	end
end
