#
# Cookbook Name:: artifact
# Resource:: file
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

actions :create
default_action :create

attribute :path, :kind_of => String, :required => true, :name_attribute => true
attribute :location, :kind_of => String
attribute :checksum, :kind_of  => String
attribute :owner, :kind_of => String, :required => true, :regex => Chef::Config[:user_valid_regex]
attribute :group, :kind_of => String, :required => true, :regex => Chef::Config[:user_valid_regex]
attribute :download_retries, :kind_of => Integer, :default => 1
