require 'json'

if node.workorder.payLoad[:Environment][0][:ciAttributes][:logging] == "false"
  Chef::Log.info("logging disabled for the env")
  return  
end

if !node.workorder.payLoad.has_key?("LoggedBy")
  Chef::Log.info("no LoggedBy - skipping log::add")
  return
end

service 'logstash-forwarder' do
  action :stop
end

ZONE = `cat /opt/oneops/cloud`
MGMT_DOMAIN = `cat /opt/oneops/mgmt_domain`

destination="#{ZONE.strip}.logs.#{MGMT_DOMAIN.strip}:35854"

#Create sub directories
`mkdir -p /etc/customlogs/logstash/cert/`
`mkdir -p /etc/logstash/conf.d/`
`mkdir -p /etc/customlogs/logstash-forwarder/`
 
# Update the mgmt certificate file 
cert_path = "/etc/customlogs/logstash/cert/logstash-forwarder.crt"
cert_content = node.mgmt_cert
File.open(cert_path,"w") do |f|
  f.write(cert_content)
end

config_dir = "/etc/logstash/conf.d/"

logs = node.workorder.payLoad[:LoggedBy]
ci_id = node.workorder.rfcCi.ciId

path_hash = {}
logs.each do |log|
  #Add the paths for the component
  path_arr = JSON.parse(log.ciAttributes.paths)
  path_hash[:paths] = path_arr
  path_hash[:fields] = {}
  path_hash[:fields][:ciId] = ci_id.to_s
  path_hash[:fields][:ciName] = log.ciName.to_s
  path_hash[:fields][:nsPath] = log.nsPath.to_s
  
  File.open("/etc/logstash/conf.d/logstash-#{ci_id}-#{log.ciName}.conf","w") do |f|
    f.write(path_hash.to_json)
  end
end


#Merge all the config files for CIs
logs = Array.new
Dir.glob('/etc/logstash/conf.d/logstash-*.conf') do |conf_file|
  logs.push(File.read(conf_file))
end

template "/etc/customlogs/logstash-forwarder/logstash-forwarder.conf" do
  source "logstash-forwarder.conf.erb"
  mode 0600
  variables({
    :destination => destination,
    :cert_path=> cert_path,
    :logs => logs
  })
  owner "root"
  group "root"
end

cookbook_file "logstash-forwarder" do
  path "/usr/local/bin/logstash-forwarder"
  source "logstash-forwarder"
  action :create_if_missing
  owner "root"
  group "root"
  mode 0700
end

template "/etc/init.d/logstash-forwarder" do
  source "initd.erb"
  owner "root"
  group "root"
  mode 0700
end
     
     
#execute "/sbin/service logstash-forwarder restart"     
#ensure the service is running
service 'logstash-forwarder' do
  action :start
end
