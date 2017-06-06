#
# Cookbook Name:: monitor
# Recipe:: delete
# Copyright 2016, Walmart Stores, Inc.
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
# only in place to call remote monitoring service if there is a delete work order


cloud_name = node.workorder.cloud.ciName
cloud_service = nil

if !node.workorder.services["monitoring"].nil? &&
   !node.workorder.services["monitoring"][cloud_name].nil?
  
  cloud_service = node.workorder.services["monitoring"][cloud_name]
end

# skip if no managed via without monitoring cloud service
if cloud_service.nil? && (!node.workorder.payLoad.has_key?("ManagedVia") && 
  !node.workorder.rfcCi.ciClassName.include?("Compute") )
  Chef::Log.info("no monitoring service provided and no managed via, will skip monitor creation. services: "+node.workorder.services.inspect)
  return
end

if !cloud_service.nil?
  recipe_name = cloud_service
  Chef::Log.info("including cloud monitor recipe: " + cloud_service.ciClassName.split(".").last.downcase + "::monitor")
  include_recipe cloud_service.ciClassName.split(".").last.downcase + "::monitor"
  return
end
