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
    cookbook_path = options[:cookbook_path]
    cookbook_path = "#{'-o' unless cookbook_path.include?('-o')} #{cookbook_path}" if cookbook_path
    models(cookbook_path, File.expand_path('', File.dirname(__FILE__)))
  end

  desc 'install', 'Install the circuit'
  def install
    models

    register
    packs
    clouds

    say 'Circuit installed!', :green
  end

  desc 'models', 'Model Sync'
  def models(cookbook_paths = nil, cd_to = nil)
    %w(classes relations).each do |type|
      cmd = "#{"cd #{cd_to};" if cd_to} knife model sync -a --#{type} #{cookbook_paths}"
      say_status('running', cmd)
      system(cmd)
      exit_code = $?.exitstatus

      if exit_code == 0
        # Metadata cache TTL in seconds. Used to sleep before
        # doing pack sync to clear the adapter metadata cache.
        # The default value can be overridden by:
        # "export MD_CACHE_VAR_TTL=<TTL in sec>".
        ttl = ENV['MD_CACHE_VAR_TTL'] || 6
        print "\nWaiting #{ttl} seconds for CMS metadata cache to clear... "
        sleep((ttl).to_i)
        puts 'done'
      else
        say "Failed: #{cmd}\nExit code: #{exit_code}", :red
        exit exit_code
      end
    end
    say "Models synced!\n\n", :green
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
    say "Source registered!\n\n", :green
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
    say "Packs synced!\n\n", :green
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
    say "Cloud synced!\n\n", :green
  end
end

Circuit.start
