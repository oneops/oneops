#
# Cookbook Name:: artifact_test
# Recipe:: artifact_rollback_tests
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

first_artifact_location = node[:artifact_test][:first_location]
first_artifact_version  = "1.0.0"

second_artifact_location = node[:artifact_test][:second_location]
second_artifact_version = "2.0.0"

artifact_deploy "artifact_test" do
  version           first_artifact_version
  artifact_location first_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"
  action            :deploy
end

artifact_deploy "artifact_test" do
  version           second_artifact_version
  artifact_location second_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"
  action            :deploy
end

ruby_block "make sure get_current_deployed_version library call works and is correct" do
  block do
    Chef::Application.fatal! "get_current_deployed_version is broken or incorrect!" unless Chef::Artifact.get_current_deployed_version("/srv/artifact_test") == "2.0.0"
  end
end

artifact_deploy "artifact_test" do
  version           first_artifact_version
  artifact_location first_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"
  action            :deploy
end

ruby_block "make sure get_current_deployed_version library call works and is correct" do
  block do
    Chef::Application.fatal! "get_current_deployed_version is broken or incorrect!" unless Chef::Artifact.get_current_deployed_version("/srv/artifact_test") == "1.0.0"
  end
end

artifact_deploy "artifact_test" do
  version           second_artifact_version
  artifact_location second_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"

  restart Proc.new {
    file "/tmp/#{artifact_filename}" do
      owner "artifact"
      group "artifact"
      mode 0755
      content "Test!"
      action :create
    end
    
  }

  action            :deploy
end