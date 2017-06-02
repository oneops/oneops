#
# Cookbook Name:: artifact_test
# Recipe:: removal_causes_redeploy
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

execute "delete the entire artifact release directory" do
  command "rm -rf /srv/artifact_test/releases/#{node[:artifact_test][:version]}"
  action :run
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

ruby_block "make sure files actually exist in the installed artifact directory" do
  block do
    current_version = Chef::Artifact.get_current_deployed_version('/srv/artifact_test')
    entries_size = Dir.entries("/srv/artifact_test/releases/#{current_version}").size
    Chef::Application.fatal! "no files in installed artifact directory!" unless entries_size > 2
  end
end

# This might be slightly hacky depending on the artifact being installed
files = []
ruby_block "delete a file from the installed directory" do
  block do
    current_version = Chef::Artifact.get_current_deployed_version('/srv/artifact_test')
    files = Dir["/srv/artifact_test/releases/#{current_version}/**"].sort
    `rm -rf #{files.first}`
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

ruby_block "make sure that the deleted file is back" do
  block do
    Chef::Application.fatal! "#{files.first} was not re-extracted!" unless ::File.exists?(files.first)
  end
end