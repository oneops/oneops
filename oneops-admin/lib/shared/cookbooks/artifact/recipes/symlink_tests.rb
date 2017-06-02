#
# Cookbook Name:: artifact_test
# Recipe:: symlink_tests
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
  force true
  symlinks({"directory_that_should_never_exist" => "directory_that_should_never_exist"})

  action :deploy
end

ruby_block "make sure directory_that_should_never_exist exists" do
  block do
    current_version = Chef::Artifact.get_current_deployed_version('/srv/artifact_test')
    shared_directory_exists = ::File.directory?("/srv/artifact_test/shared/directory_that_should_never_exist")
    symlink_exists = ::File.symlink?("/srv/artifact_test/releases/#{current_version}/directory_that_should_never_exist")
    Chef::Application.fatal! "directory /srv/artifact_test/shared/directory_that_should_never_exist does not exist!" unless shared_directory_exists
    Chef::Application.fatal! "a symlink at /srv/artifact_test/#{current_version}/directory_that_should_never_exist does not exist!" unless symlink_exists
  end
end