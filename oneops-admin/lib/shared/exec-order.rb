#!/usr/bin/env ruby
#
# wrapper to chef or puppet - uses impl to get chef or puppet version and setup:
#  create Gemfile, bundle install if different
#
require 'rubygems'
require 'json'
require 'yaml'
require 'optparse'
Dir[File.join(File.expand_path(File.dirname(__FILE__)), 'exec-order','*.rb')].each {|f| require f }

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

impl              = ARGV[0]
json_context      = ARGV[1]
cookbook_path     = ARGV[2] || ''
service_cookbooks = ARGV[3] || ''
gem_sources       = get_gem_sources
ostype            = get_os_type(log_level)

if !(ostype =~ /windows/)
  fast_image = File.exist?('/etc/oneops-tools-inventory.yml')
else
  fast_image = false
end

prefix_root = ''
file_cache_path = '/tmp'
ci = json_context.split("/").last.gsub(".json","")
if ostype =~ /windows/
  prefix_root = 'c:/cygwin64'
  impl = 'oo::chef-12.11.18'
  json_context = prefix_root + json_context
end
lock_file = prefix_root + '/tmp/exec-order.lock'
dsl, version = impl.split('::')[1].split('-') # ex) oo::chef-10.16.6::optional_uri_for_cookbook_or_module
if !ENV['class'].nil? && !ENV['class'].empty?
  component = ENV['class'].downcase
else
  component = json_context.split('/').last.split('.').first.downcase
end

#Custom ruby install
custom_ruby_installed = nil
if ostype !~ /windows/ &&
  %w[objectstore compute volume os].include?(component)

  cr = ExecOrderUtils::CustomRuby.new(json_context, component)
  if cr.custom_ruby_required?
    cr.install_custom_ruby if cr.install_needed?
    custom_ruby_installed = cr.installed_package_version
  end
end

# set cwd to same dir as the exe-order.rb file
Dir.chdir File.dirname(__FILE__)

case dsl
when "chef"
  Dir.chdir "cookbooks"

  if ostype =~ /windows/ && !is_chef_installed?(ostype, version)
    install_chef_windows(version)
    patch_chef_121118 if version == '12.11.18'
  end

  #we want to make sure rubygems sources on the VM match rubygems_proxy env variable from compute cloud service
  #otherwise changes to the cloud service will require updating compute component
  #have to be called after chef is installed on windows, as that's the chef installation that also installs rubygems
  update_gem_sources(gem_sources, log_level)
  unless fast_image || custom_ruby_installed
    #Run bunle to insert/update neccessary gems if needed
    puts 'Installing gems from a gemfile'
    install_using_prebuilt_gemfile(gem_sources, component, dsl, version)
  end

  chef_config = "#{prefix_root}/home/oneops/#{cookbook_path}/components/cookbooks/chef-#{ci}.rb"

  additionalCookbooks = ''
  if ! service_cookbooks.empty?
    additionalCookbooks = service_cookbooks.split(",").map { |e| "\"#{prefix_root}#{e}\"" }.join(', ')
  end

  # generate chef_config

    cookbook_full_path = chef_config.gsub("/chef-#{ci}.rb","")
    # when using alternate cookbooks include base cookbooks
    if cookbook_path.empty?
      config_content = 'cookbook_path "'+cookbook_full_path+"\""
    else
      config_content = "cookbook_path [\"#{cookbook_full_path}\",\"#{prefix_root}/home/oneops/shared/cookbooks\""

      if ! additionalCookbooks.empty?
          config_content += "," + additionalCookbooks
      end
      config_content += "]\n"
    end

    log_level = "info"
    #Chef 12 specific config options - chef-client is used in Chef12 instead of chef-solo,
    #and it's no longer thread safe, i.e. parallel chef runs may step on each other toes
    #when accessing cache or node's json
    #plus for security reasons we do not want chef to preserve attributes coming from WO
    if version.split('.')[0].to_i >= 12
      file_cache_path = "#{file_cache_path}/#{ci}"
      config_content += "normal_attribute_whitelist []\n"
      config_content += "node_path '#{file_cache_path}'\n"
    end
    config_content += "log_level :#{log_level}\n"
    config_content += "formatter :#{formatter}\n"
    config_content += "verify_api_cert true\n"
    config_content += "file_cache_path '#{file_cache_path}'\n"
    config_content += "lockfile \"#{file_cache_path}/#{ci}.lock\"\n"

    ['http','https','no'].each do |proxy|
      proxy_key = proxy + "_proxy"
      if ENV[proxy_key]
        config_content += "#{proxy_key} \""+ENV[proxy_key]+"\"\n"
      end
    end

    puts "chef_config: #{chef_config}"
    File.open(chef_config, 'w') {|f| f.write(config_content) }

  if ostype =~ /windows/
    bindir = 'c:/opscode/chef/embedded/bin'
  else
    # Let's use custom ruby if it is available.
    # This assume that ruby is installed at a fixed location
    # TODO: need to externalize a lot of this to be dynamic
    # base on a config file or something.

    custom_ruby_bindir = '/home/oneops/ruby/2.0.0-p648/bin'
    if File.exist?("#{custom_ruby_bindir}/chef-solo")
      bindir = custom_ruby_bindir
      ENV['GEM_PATH'] = '/home/oneops/ruby/2.0.0-p648/lib/ruby/gems/2.0.0'
      ENV['PATH'] = "#{custom_ruby_bindir}:#{ENV['PATH']}"
      puts "Custom ruby found, bindir :#{bindir}"
    else # fall back to old method
      bindir = `gem env | grep 'EXECUTABLE DIRECTORY' | awk '{print $4}'`.to_s.chomp
    end
  end

  if version.split('.')[0].to_i >= 12
    cmd = "#{bindir}/chef-client --local-mode -c #{chef_config} -j #{json_context}"
  else
    cmd = "#{bindir}/chef-solo -l #{log_level} -F #{formatter} -c #{chef_config} -j #{json_context}"
  end
  puts cmd
  #Obtain shared lock
  File.open(lock_file, File::RDWR|File::CREAT, 0644) do |f|
    f.flock(File::LOCK_SH)
    system cmd
    if $?.exitstatus != 0
      puts "CHEF SOLO failed, #{$?}"
      exit $?.exitstatus
    end
  end

when "puppet"
  Dir.chdir "modules"
  modules_dir = `pwd`.chomp

  #we want to make sure rubygems sources on the VM match rubygems_proxy env variable from compute cloud service
  #otherwise changes to the cloud service will require updating compute component
  update_gem_sources(gem_sources, log_level)

  #Run bunle to insert/update neccessary gems if needed
  gem_list = get_gem_list(dsl, version)
  gen_gemfile_and_install(gem_sources, gem_list, component, dsl, log_level)

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
