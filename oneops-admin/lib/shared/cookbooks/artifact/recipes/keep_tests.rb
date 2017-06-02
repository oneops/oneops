#
# Cookbook Name:: artifact_test
# Recipe:: keep_tests
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

first_artifact_location  = node[:artifact_test][:first_location]
first_artifact_version   = "1.0.0"

second_artifact_location = node[:artifact_test][:second_location]
second_artifact_version  = "2.0.0"

third_artifact_location  = node[:artifact_test][:third_location]
third_artifact_version   = "3.0.0"

fourth_artifact_location = node[:artifact_test][:fourth_location]
fourth_artifact_version  = "4.0.0"

artifact_deploy "artifact_test" do
  version           first_artifact_version
  artifact_location first_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"

  action :deploy
end

artifact_deploy "artifact_test" do
  version           second_artifact_version
  artifact_location second_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"

  action :deploy
end

artifact_deploy "artifact_test" do
  version           third_artifact_version
  artifact_location third_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"

  action :deploy
end

artifact_deploy "artifact_test" do
  version           fourth_artifact_version
  artifact_location fourth_artifact_location
  deploy_to         "/srv/artifact_test"
  owner             "artifact"
  group             "artifact"

  action :deploy
end

ruby_block "ensure that version 1.0.0 was deleted" do
  block do
    deleted_release_path = "/srv/artifact_test/releases/1.0.0"
    deleted_cache_path = "#{Chef::Config[:file_cache_path]}/artifact_deploys/artifact_test/1.0.0"
    Chef::Application.fatal! "the artifacts release was not deleted!" if ::File.exists?(deleted_release_path)
    Chef::Application.fatal! "the artifacts cached file was not deleted!" if ::File.exists?(deleted_cache_path)
  end
end