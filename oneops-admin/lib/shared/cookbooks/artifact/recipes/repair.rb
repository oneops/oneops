#removing the older artifact binaries to cleanup disk. This is a temporary code until we get rid of disk space issue for some applications
Chef::Log.info("deleting all older versions from under #{node[:artifact][:install_dir]}/artifact_deploys/")
execute "rm -rf #{node[:artifact][:install_dir]}/artifact_deploys/*"
include_recipe "artifact::update"
