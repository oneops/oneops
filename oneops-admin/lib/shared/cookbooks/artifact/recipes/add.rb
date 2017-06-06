#
# Cookbook Name:: artifact
# Recipe:: add
# 
# Copyright 2013, Walmartlabs
#

def to_boolean(str)
  str == 'true'
end

if node[:artifact][:install_dir] =~ /\s/
  Chef::Log.warn "Install Directory Contains White Spaces. Current Element value is:"+node[:artifact][:install_dir]
  node.set[:artifact][:install_dir]=node[:artifact][:install_dir].gsub!(/\s/, "")
  Chef::Log.info "Removed whitepsaces from installation directory. New Element value is:"+node[:artifact][:install_dir]
end

directory "#{node[:artifact][:install_dir]}/tmp" do
  owner 'root'
  group 'root'
  recursive true
  mode 0777
  action :create
end

ruby_block "set temp #{node[:artifact][:install_dir]}/tmp" do
  begin
    ENV['TEMP'] = "#{node[:artifact][:install_dir]}/tmp"
  end
  action :nothing
end

# oo stores booleans as string and no ruby-built-in to_bool
_should_expand = to_boolean node[:artifact][:should_expand]

cloud_name = node[:workorder][:cloud][:ciName]

if node[:workorder][:services].has_key?(:maven)
        cloud_service = node[:workorder][:services][:maven][cloud_name][:ciAttributes]
	
	if node[:artifact].has_key?(:username) && !node[:artifact][:username].empty?
        	Chef::Log.info("Not using the cloud attributes for nexus username as user has set one in the artifact component")
        	nexus_username = node[:artifact][:username]
	else
        	Chef::Log.info("Using the nexus cloud service attributes for nexus username")
          	nexus_username = cloud_service[:username]
	end

	if node[:artifact].has_key?(:password) && !node[:artifact][:password].empty?
        	Chef::Log.info("Not using the cloud attributes for nexus password as user has set one in the artifact component")
        	nexus_password = node[:artifact][:password]
	else
        	Chef::Log.info("Using the nexus cloud service attributes for nexus password")
          	nexus_password = cloud_service[:password]
	end

	if node[:artifact].has_key?(:url) && !node[:artifact][:url].empty?
        	Chef::Log.info("Not using the cloud attributes for nexus url as user has set one in the artifact component")
        	nexus_url = node[:artifact][:url]
	else
        	Chef::Log.info("Using the nexus cloud service attributes for nexus url")
          	nexus_url = cloud_service[:url]
  end        	
          	
  if node[:artifact].has_key?(:path) && !node[:artifact][:path].empty?
          Chef::Log.info("Using the cloud attributes for repo path from the artifact component")
          nexus_path = node[:artifact][:path]
	end

else
	nexus_username = node[:artifact][:username]
	nexus_password = node[:artifact][:password]
	nexus_url = node[:artifact][:url]
	nexus_path = node[:artifact][:path]
end

node.set[:data_bag] = {
  'artifact' => {
    'nexus' => {
      'username' =>  nexus_username,
      'password' => nexus_password,
      'url' => nexus_url,
      'repository' => node[:artifact][:repository],
      'path' => nexus_path
    },
    'aws' => {
      'access_key_id' => node[:artifact][:username],
      'secret_access_key' => node[:artifact][:password],
      'region' => node[:artifact][:repository]
    }
  }
}

#Chef::Log.info("nexus #{node[:data_bag].inspect}")
#Chef::Log.info("artifact #{node[:artifact].inspect}")

ciName = node.workorder[:rfcCi][:ciName] if node.workorder[:rfcCi]
ciName = node.workorder[:ci][:ciName] if node.workorder[:ci]

artifact_deploy ciName do
  version node[:artifact][:version]
  artifact_location node[:artifact][:location]
  artifact_checksum node[:artifact][:checksum] if !node[:artifact][:checksum].empty?
  deploy_to node[:artifact][:install_dir]
  owner node[:artifact][:as_user]
  group node[:artifact][:as_group]
  environment JSON.parse(node[:artifact][:environment])
  should_expand _should_expand
  # callbacks
  configure Proc.new { eval node[:artifact][:configure] } unless node[:artifact][:configure].empty?
  migrate   Proc.new { eval node[:artifact][:migrate]   } unless node[:artifact][:migrate].empty?
  after_deploy Proc.new { eval node[:artifact][:restart]   } unless node[:artifact][:restart].empty?

  force true
  remove_on_force true

  action :deploy
end

# link "#{node[:tomcat][:home]}/webapps/myface.war" do
  # to "/srv/myface/current/myface.war"
# end
