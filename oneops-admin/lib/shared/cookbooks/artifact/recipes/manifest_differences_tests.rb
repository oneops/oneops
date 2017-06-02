#
# Cookbook Name:: artifact_test
# Recipe:: manifest_differences_tests
#
# Author:: Kyle Allan (<kallan@riotgames.com>)
# 
# Copyright 2013, Riot Games
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
#


group "artifact"
user "artifact" do
  group "artifact"
end

artifact_deploy "artifact_test" do
  version node[:artifact_test][:version]
  artifact_location node[:artifact_test][:location]
  artifact_checksum node[:artifact_test][:checksum]
  deploy_to "/srv/artifact_test"
  owner "artifact"
  group "artifact"

  action :deploy
end

# define a configure and restart proc -> configure will always run and will add a new file to the deployed directory
# which will cause the restart proc to execute, giving us something testable
artifact_deploy "artifact_test" do
  version node[:artifact_test][:version]
  artifact_location node[:artifact_test][:location]
  artifact_checksum node[:artifact_test][:checksum]
  deploy_to "/srv/artifact_test"
  owner "artifact"
  group "artifact"

  configure Proc.new {
    file "/srv/artifact_test/releases/#{Chef::Artifact.get_current_deployed_version('/srv/artifact_test')}/configured.txt" do
      owner "artifact"
      group "artifact"
      mode 0755
      content "Hello World!"
      action :create
    end
  }

  restart Proc.new {
    directory "/restart_directory" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end
  }

  action :deploy
end

ruby_block "make sure the restart proc executed" do
  block do
    Chef::Application.fatal! "restart proc didn't execute when it should have!" unless ::File.directory?("/restart_directory")
  end
end

artifact_deploy "artifact_test" do
  version node[:artifact_test][:version]
  artifact_location node[:artifact_test][:location]
  artifact_checksum node[:artifact_test][:checksum]
  deploy_to "/srv/artifact_test"
  owner "artifact"
  group "artifact"

  action :deploy
end

ruby_block "configured files should stay and not cause a redeploy" do
  block do
    Chef::Application.fatal! "configured files were removed!" unless ::File.exists?("/srv/artifact_test/releases/#{Chef::Artifact.get_current_deployed_version('/srv/artifact_test')}/configured.txt")
  end
end
