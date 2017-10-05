require 'thor'

# prevent ArgumentError: invalid byte sequence in US-ASCII from play2app under 1.9.3
ENV['LANG'] = "en_US.UTF-8"

class Circuit < Thor
  include Thor::Actions

  desc "create", "Setup Environment for cookbook/Pack creation"
  method_option :path, :default => File.expand_path('circuit', Dir.pwd)
  method_option :force, :default => true
  def create
    directory File.expand_path('templates/circuit', File.dirname(__FILE__)), options[:path]
    empty_directory "#{options[:path]}/components/cookbooks"
    empty_directory "#{options[:path]}/packs"
    empty_directory "#{options[:path]}/clouds"
    empty_directory "#{options[:path]}/.chef"

    config = <<-EOH
  log_level                :info
  log_location             STDOUT
  print_after              true
  admin                    true
  node_name                'oneops'
  register                 'oneops'
  version                  '1.0.0'
  nspath                   '/public'
  client_key               'client_key.pem'
  validation_client_name   'chef-validator'
  validation_key           'validation.pem'
  chef_server_url          'http://localhost:4000'
  cache_type               'BasicFile'
  cache_options( :path => '.chef/checksums' )
  cookbook_path [ 'components/cookbooks' ]
  publish_path 'pkgs'
  pack_path [ 'packs' ]
  service_path [ 'services' ]
  cloud_path [ 'clouds' ]
  catalog_path [ 'catalogs' ]
  default_impl 'oo::chef-11.18.12'
    EOH

    open(File.join("#{options[:path]}/.chef", 'knife.rb'), 'w') do |file|
      file.puts config
    end

    puts 'Created empty circuit seed. Next Step: provide component, packs, cloud content. Run Run "cd circuit; circuit install" to load.'
  end

  desc 'init', 'Initialize the circuit'
  method_option :cookbook_path, :default => '-o base:shared/cookbooks'
  def init
    pwd = Dir.pwd
    Dir.chdir File.expand_path('', File.dirname(__FILE__))
    cookbook_path = options[:cookbook_path]
    cookbook_path = "#{'-o' unless cookbook_path.include?('-o')} #{cookbook_path}" if cookbook_path
    models(cookbook_path)
    Dir.chdir pwd
  end

  desc 'install', 'Install the circuit'
  def install
    models

    # Metadata cache TTL in seconds. Used to sleep before
    # doing pack sync to clear the adapter metadata cache.
    # Default value is 5 seconds. The default value can be
    # overridden by "export MD_CACHE_VAR_TTL=<TTL in sec>".
    sleep((ENV['MD_CACHE_VAR_TTL'] || 5).to_i)

    register
    packs
    clouds

    say 'Circuit installed!', :green
  end

  desc 'models', 'Model Sync'
  def models(cookbook_paths = nil)
    cmd = "knife model sync -a --classes --relations #{cookbook_paths}"
    say_status('running', cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "Failed: #{cmd}\nExit code: #{exit_code}", :red
      exit exit_code
    end
    puts "Models synced!\n\n"
  end

  desc "register", "Register source"
  def register
    cmd = "knife register"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "Failed: #{cmd}\nExit code: #{exit_code}", :red
      exit exit_code
    end
    puts "Source registered!\n\n"
  end

  desc "packs", "Pack Sync"
  def packs
    cmd = "knife pack sync -a"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "Failed: #{cmd}\nExit code: #{exit_code}", :red
      exit exit_code
    end
    puts "Packs synced!\n\n"
  end

  desc "clouds", "Clouds Sync"
  def clouds
    cmd = "knife cloud sync -a"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "Failed: #{cmd}\nExit code: #{exit_code}", :red
      exit exit_code
    end
    puts "Cloud synced!\n\n"
  end
end

Circuit.start
