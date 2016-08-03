#!/usr/bin/env ruby
#
# wrapper to chef or puppet - uses impl to get chef or puppet version and setup:
#  create Gemfile, bundle install if different
#
require 'rubygems'
require 'json'
require 'yaml'
require 'optparse'

log_level = "info"
formatter = "null"

options = {}
OptionParser.new do |opts|
  opts.banner = "Usage: exec-order.rb [options]"

  opts.on("-d", "--[no-]debug", "Run in debug") do |d|
    log_level = "debug"
    formatter = "doc"
  end
end.parse!


impl = ARGV[0]
json_context = ARGV[1]
cookbook_path = ARGV[2] || ''
ostype = ARGV[3]

if ostype =~ /windows/
 impl = "oo::chef-12.11.18"
end

def gen_gemfile_and_install (gems, dsl, ostype)

    rubygems_proxy = ENV['rubygems_proxy']
    gemfile_content = "source 'https://rubygems.org'\n"
	  if !rubygems_proxy.nil?
	    gemfile_content = "source '#{rubygems_proxy}'\n"
	  end
    gems.each do |gem_set|
      if gem_set.size > 1
        gemfile_content += "gem '#{gem_set[0]}', '#{gem_set[1]}'\n"
      else
        gemfile_content += "gem '#{gem_set[0]}'\n"
      end
    end

pwd= system("pwd")
puts pwd
    File.open('Gemfile', 'w') {|f| f.write(gemfile_content) }
    method = "install"
    result = ""
if ostype =~ /windows/
 `c:\opscode\chef\embedded\bin\gem list | Select-String -Pattern #{dsl}`
else
  `gem list | grep #{dsl}`
end
if $?.to_i == 0
      method = "update"
    end
    cmd = ""
if ostype =~ /windows/
    cmd = "c:/opscode/chef/embedded/bin/bundle #{method}"
else
    cmd = "bundle #{method}"
end
    puts "running: #{cmd}"
    start_time = Time.now.to_i
    system cmd
    duration = Time.now.to_i - start_time

    puts "took: #{duration} sec"

    if $?.to_i != 0
      puts "result #{$?}"
      exit 1
    end

    puts "change gem source back to rubygems_proxy"
    rubygems_proxy = `cat /opt/oneops/rubygems_proxy`.chomp
    system("gem source --add #{rubygems_proxy}")

    sources = `gem source | egrep -v "CURRENT SOURCES|#{rubygems_proxy}"`.split("\n")
    sources.each do |source|
      cmd = "gem source --remove #{source}"
      puts cmd
      system(cmd)
    end
    system("gem source")

end

# set cwd to same dir as the exe-order.rb file
Dir.chdir File.dirname(__FILE__)
gem_config = YAML::load(File.read('exec-gems.yaml'))

# ex) oo::chef-10.16.6::optional_uri_for_cookbook_or_module
dsl, version = impl.split("::")[1].split("-")


case dsl
when "chef"
  Dir.chdir "cookbooks"

  # check version
  current_version = `bundle list | grep chef`.to_s.chomp
  if $?.to_i != 0 || current_version.to_s.index(version).nil?
    puts "current: #{current_version}, expected: #{version} - updating Gemfile"
    version_gems = [["chef",version]]
    if !gem_config["chef-#{version}"].nil?
       version_gems += gem_config["chef-#{version}"]
    end
    gem_list = gem_config["common"] + version_gems
    gem_list.push(['chef', version])
   if ostype !~ /windows/
    gen_gemfile_and_install(gem_list, dsl, ostype)
   else
    start_time = Time.now.to_i
    chef_install_cmd = "c:/programdata/chocolatey/choco.exe install -y --allow-downgrade chef-client"
    result=system(chef_install_cmd)
    duration = Time.now.to_i - start_time
    puts "installed chef-client in #{duration} seconds"
    gen_gemfile_and_install(gem_list,dsl,ostype)
   end
end

  # used to create specific chef config for cookbook_path and lockfile
  ci = json_context.split("/").last.gsub(".json","")

if ostype !~ /windows/
    chef_config = "/home/oneops/#{cookbook_path}/components/cookbooks/chef-#{ci}.rb"
  else
    chef_config = "C:/cygwin64/home/admin/#{cookbook_path}/components/cookbooks/chef-#{ci}.rb"
    json_context = "C:/cygwin64" + json_context
  end

  # generate chef_config if doesn't exist
  if !File::exist?(chef_config)
     cookbook_full_path = chef_config.gsub("/chef-#{ci}.rb","")
     # when using alternate cookbooks include base cookbooks
     if cookbook_path.empty?
     	config_content = 'cookbook_path "'+cookbook_full_path+"\"\n"
     else
	 if ostype !~ /windows/
        	config_content = "cookbook_path [\"#{cookbook_full_path}\",\"/home/oneops/shared/cookbooks\"]\n"
        else
          config_content = "cookbook_path [\"#{cookbook_full_path}\",\"C:/cygwin64/home/admin/shared/cookbooks\"]\n"
        end
   end
     log_level = "info"
     config_content += "log_level :#{log_level}\n"
     config_content += "formatter :#{formatter}\n"
     config_content += "verify_api_cert true\n"
     config_content += "file_cache_path \"/tmp\"\n"
     config_content += "lockfile \"/tmp/#{ci}.lock\"\n"

	 ['http','https','no'].each do |proxy|
	   proxy_key = proxy + "_proxy"
       if ENV[proxy_key]
         config_content += "#{proxy_key} \""+ENV[proxy_key]+"\"\n"
       end
     end

     puts "chef_config: #{chef_config}"
     File.open(chef_config, 'w') {|f| f.write(config_content) }
  end

 if ostype !~ /windows/
    bindir = `gem env | grep 'EXECUTABLE DIRECTORY' | awk '{print $4}'`.to_s.chomp
    cmd = "#{bindir}/chef-solo -l #{log_level} -F #{formatter} -c #{chef_config} -j #{json_context}"
  else
    cmd = "c:/opscode/chef/bin/chef-solo.bat -l error -F #{formatter} -c #{chef_config} -j #{json_context}"
  end
  ec = system(cmd)
  if ec == -1
   exit -1
  else
   exit ec
  end


when "puppet"
  Dir.chdir "modules"
  modules_dir = `pwd`.chomp

  # check version/install
  current_version = `bundle exec puppet --version`.to_s.chomp
  if $?.to_i != 0 || current_version != version
    puts "current version: #{current_version} ... expecting #{version}"
    puts "updating Gemfile and running bundle install."
    gem_list = gem_config["common"] + gem_config["puppet"]
    gem_list.push(['puppet', version])
    gen_gemfile_and_install(gem_list,dsl)
  end

  # run puppet apply for each item in the run_list
  context = JSON.parse(File.read(json_context))
  context["run_list"].each do |class_action|

    module_name, action = class_action.gsub(/recipe\[|\]/,"").split("::")

    # TODO: implement puppet attachment and monitor modules or route to chef
    next if module_name =~ /attachment|monitor/

    # hiera setup
    json_wo = json_context.split("/").last.gsub(".json","")

    cmd = "FACTER_wo=#{json_wo} puppet apply --hiera_config ../hiera.yaml "
    cmd += "--modulepath #{modules_dir} -e 'include #{module_name}'"
    puts cmd
    ec = system cmd
    exit ec
  end
end

