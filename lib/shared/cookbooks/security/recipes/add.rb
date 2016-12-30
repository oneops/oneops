#
# Cookbook Name:: security
# Recipe:: add
#
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

filter = nil
override_version = nil
if node.workorder.has_key?('arglist')
  args = ::JSON.parse(node.workorder.arglist)
  compl_list = args["name"]
  if (!compl_list.nil? && compl_list != "*")
    filter = compl_list.split(/\s*,\s*/)
    Chef::Log.info("filter : #{filter}")
  end
  override_version = args["version"].to_s
  Chef::Log.info("override_version : #{override_version}")
end

run_all = !compl_list.nil? && compl_list == "*"
sec_list = nil
if node.workorder.payLoad.has_key?('ExtraRunList')
  sec_list = node.workorder.payLoad.ExtraRunList.select do |runList|
    runList.ciClassName == 'cloud.compliance.Security' && 
         ((run_all && runList.ciAttributes.enabled == 'true') || 
           (!run_all && (filter.nil? || filter.empty? || filter.include?(runList.ciName))))
  end
end

if (sec_list.nil? || sec_list.empty?)
  Chef::Log.info("No ExtraRunList available for security")
  if (!filter.nil? && !filter.empty?)
    Chef::Application.fatal!("Names not matching any available security compliance #{filter}")
  end
  return
end

ci = node.workorder.has_key?('rfcCi') ? node.workorder.rfcCi : node.workorder.ci
ciAttributes = ci.ciAttributes

appliedMap = Hash.new
if ciAttributes.has_key?('applied_compliance')
  appliedMap = JSON.parse(ciAttributes[:applied_compliance])
end

sec_list.each do |security|

  url = security[:ciAttributes][:asset_url]
  name = security[:ciName]
  version = security[:ciAttributes][:version]
  if (!override_version.nil? && !override_version.empty? && override_version.downcase != "latest")
    version = override_version
  end

  curr_version = 'nil'
  if !appliedMap.empty? && appliedMap.has_key?(name)
    curr_version = appliedMap[name]
  end
  Chef::Log.info("name : #{name}, curr_version : #{curr_version}, version to apply : #{version}")

  script_file = "#{name}-#{version}"
  remote_file "/tmp/#{script_file}" do
    source url
    owner "root"
    group "root"
    mode "0755"
  end

  ruby_block 'run_script' do
    block do
      Chef::Log.info("Executing script /tmp/#{script_file}")
      cmd = Mixlib::ShellOut.new("/tmp/#{script_file} #{curr_version} #{version}", :live_stream => Chef::Log::logger, :user => "root")
      cmd.run_command
      cmd.error!
    end
  end

  if !filter.nil?
    filter.delete(name)
  end
  
  if version.downcase == 'delete'
    appliedMap[name] = '0'
  else
    appliedMap[name] = version
  end
  
end

if (!filter.nil? && !filter.empty?)
  Chef::Log.warn("Some of the names provided don't have matching security compliance #{filter}")
end

puts "***RESULT:applied_compliance="+JSON.generate(appliedMap)
