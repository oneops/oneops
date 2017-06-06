#
# Cookbook Name:: artifact_test
# Recipe:: skip_manifest_check
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
user "artifacts"

artifact_deploy "artifact_test" do
  version node[:artifact_test][:version]
  artifact_location node[:artifact_test][:location]
  artifact_checksum node[:artifact_test][:checksum]
  deploy_to node[:artifact_test][:deploy_to]
  owner "artifacts"
  group "artifact"
  skip_manifest_check true
  action :deploy
end

ruby_block "make sure manifest.yaml does not exist" do
  block do
    manifest_file = ::File.join(node[:artifact_test][:deploy_to], "current", "manifest.yaml")
    Chef::Application.fatal! "Manifest file exists!" if ::File.exists?(manifest_file)
  end
end