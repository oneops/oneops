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

if File.file?("C:/cygwin64" + json_context)
  wo_file = File.read("C:/cygwin64" + json_context)
else File.file?(json_context)
  wo_file = File.read(json_context)
end
wo_hash = JSON.parse(wo_file)
ostype = wo_hash['workorder']['rfcCi']['ciAttributes']['ostype']
if ostype.nil?
  # check for a DependsOn relationship with Os
  if wo_hash['workorder']['payLoad'].has_key?('DependsOn')
    os = wo_hash['workorder']['payLoad']['DependsOn'].select { |d| d['ciClassName'] =~ /Os/ }.first
    if !os.nil?
      ostype = os['ciAttributes']['ostype']
    end
  end
end
# if os type is still nil, will default to linux way of doing things.
# not every wo will have the ostype in the rfcCi section
puts "OS TYPE IS: #{ostype}"

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

    current_dir = `pwd`.chomp
    puts "CURRENT DIR IS: #{current_dir}"

    File.open('Gemfile', 'w') {|f| f.write(gemfile_content) }
    method = "install"
    result = ""
    if ostype =~ /windows/
      `c:/opscode/chef/embedded/bin/gem list | grep #{dsl}`
    else
      `gem list | grep #{dsl}`
    end
    if $?.to_i == 0
      method = "update"
    end
    cmd = ""
    start_time = Time.now.to_i
    if ostype =~ /windows/
      puts "running: c:/opscode/chef/embedded/bin/bundle #{method}"
      ec = system("c:/opscode/chef/embedded/bin/bundle #{method}")
    else
      puts "running: bundle #{method}"
      ec = system("bundle #{method}")
    end
    if !ec || ec.nil?
      puts "bundle #{method} failed with, #{$?}"
      exit 1
    end
    duration = Time.now.to_i - start_time
    puts "took: #{duration} sec"

    puts "change gem source back to rubygems_proxy"
    rubygems_proxy = `cat /opt/oneops/rubygems_proxy`.chomp
    puts "RUBYGEMS PROXY IS: #{rubygems_proxy}"
    if ostype =~ /windows/
      system("c:/opscode/chef/embedded/bin/gem source --add #{rubygems_proxy}")
      sources = `c:/opscode/chef/embedded/bin/gem source | egrep -v "CURRENT SOURCES|#{rubygems_proxy}"`.split("\n")
      sources.each do |source|
        system("c:/opscode/chef/embedded/bin/gem source --remove #{source}")
      end
      system("c:/opscode/chef/embedded/bin/gem source")
    else
      `gem source --add #{rubygems_proxy}`
      sources = `gem source | egrep -v "CURRENT SOURCES|#{rubygems_proxy}"`.split("\n")
      sources.each do |source|
        `gem source --remove #{source}`
      end
      `gem source`
    end

end

# set cwd to same dir as the exe-order.rb file
Dir.chdir File.dirname(__FILE__)
gem_config = YAML::load(File.read('exec-gems.yaml'))

# ex) oo::chef-10.16.6::optional_uri_for_cookbook_or_module
dsl, version = impl.split("::")[1].split("-")

case dsl
when "chef"
  Dir.chdir "cookbooks"

  if ostype =~ /windows/
    start_time = Time.now.to_i
    ec = system("c:/programdata/chocolatey/choco.exe install -y --allow-downgrade --allowEmptyChecksums chef-client -version 12.11.18")
    if !ec || ec.nil?
      puts "choco install result #{$?}"
      exit 1
    end
    duration = Time.now.to_i - start_time
    puts "installed chef-client in #{duration} seconds"

    # patch the bug in chef 12.11.18
    # https://github.com/chef/chef/issues/5027
    # fixed here https://github.com/chef/chef/blame/master/lib/chef/chef_fs/file_system/multiplexed_dir.rb#L44
    # but the chef-client msi was built and uploaded to chocolatey before it was fixed, so we need to temporarily add this
    # until we can get a new version of the msi uploaded to chocolatey.
    puts "Patch the bug in chef client!!!"
    puts "run a substitute command and put the source back"
    puts "SED command is: sed -i '44s/unless.*//' c:/opscode/chef/embedded/lib/ruby/gems/2.1.0/gems/chef-12.11.18-universal-mingw32/lib/chef/chef_fs/file_system/multiplexed_dir.rb"
    rc = system("sed -i '44s/unless.*//' c:/opscode/chef/embedded/lib/ruby/gems/2.1.0/gems/chef-12.11.18-universal-mingw32/lib/chef/chef_fs/file_system/multiplexed_dir.rb")
    if !rc || rc.nil?
      puts "SED command failed with #{$?}"
      exit 1
    else
      puts "SED Command Success!, #{$?}"
    end

    puts "DONE PATCHING THE CHEF BUG"
  end

  # check version
  if ostype =~ /windows/
    current_version = `c:/opscode/chef/embedded/bin/bundle list | grep chef`.to_s.chomp
  else
    current_version = `bundle list | grep chef`.to_s.chomp
  end
  if $?.to_i != 0 || current_version.to_s.index(version).nil?
    puts "current: #{current_version}, expected: #{version} - updating Gemfile"
    version_gems = [["chef",version]]
    puts "VERSION GEMS IS: #{version_gems}"
    if !gem_config["chef-#{version}"].nil?
      puts "FOUND CHEF VERSION IN GEM CONFIG!!!"
      version_gems += gem_config["chef-#{version}"]
    end
    gem_list = gem_config["common"] + version_gems
    gem_list.push(['chef', version])
    gen_gemfile_and_install(gem_list, dsl, ostype)
  end

  # used to create specific chef config for cookbook_path and lockfile
  ci = json_context.split("/").last.gsub(".json","")

  if ostype =~ /windows/
    chef_config = "c:/cygwin64/home/oneops/#{cookbook_path}/components/cookbooks/chef-#{ci}.rb"
    json_context = "c:/cygwin64" + json_context
  else
    chef_config = "/home/oneops/#{cookbook_path}/components/cookbooks/chef-#{ci}.rb"
  end

  puts "CHEF CONFIG IS: #{chef_config}"

  # generate chef_config if doesn't exist
  if !File::exist?(chef_config)
    puts "CONFIG DOESN'T EXIST"
    cookbook_full_path = chef_config.gsub("/chef-#{ci}.rb","")
    # when using alternate cookbooks include base cookbooks
    if cookbook_path.empty?
     	config_content = 'cookbook_path "'+cookbook_full_path+"\"\n"
    else
      if ostype =~ /windows/
        config_content = "cookbook_path [\"#{cookbook_full_path}\",\"c:/cygwin64/home/oneops/shared/cookbooks\"]\n"
      else
        config_content = "cookbook_path [\"#{cookbook_full_path}\",\"/home/oneops/shared/cookbooks\"]\n"
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

  if ostype =~ /windows/
    puts "RUNNING: c:/opscode/chef/embedded/bin/chef-solo.bat -l #{log_level} -F #{formatter} -c #{chef_config} -j #{json_context}"
    ec = system("c:/opscode/chef/embedded/bin/chef-solo.bat -l #{log_level} -F #{formatter} -c #{chef_config} -j #{json_context}")
  else
    bindir = `gem env | grep 'EXECUTABLE DIRECTORY' | awk '{print $4}'`.to_s.chomp
    ec = system("#{bindir}/chef-solo -l #{log_level} -F #{formatter} -c #{chef_config} -j #{json_context}")
  end
  if !ec || ec.nil?
    puts "CHEF SOLO failed, #{$?}"
    exit 1
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
