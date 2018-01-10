require 'thor'

# prevent ArgumentError: invalid byte sequence in US-ASCII from play2app under 1.9.3
ENV['LANG'] = 'en_US.UTF-8'

class Circuit < Thor
  include Thor::Actions

  # Metadata cache TTL in seconds - amount of time to wait for the adapter cache to
  # clear after model sync. The default value can be overridden by:
  # "export MD_CACHE_VAR_TTL=<TTL in sec>".
  MD_CACHE_VAR_TTL = (ENV['MD_CACHE_VAR_TTL'] || 6).to_i

  def self.command_help(shell, command_name)
    super
    if %w(model pack cloud test).include?(command_name)
      system("knife #{command_name} sync -h")
    elsif command_name == 'test'
      system('circuit test -h')
    end
  end

  desc 'create [OPTIONS]', 'Seeds a new circuit directory structure with default config (.chef/knife.rb)'
  method_option :path, :default => File.expand_path('circuit', Dir.pwd)
  method_option :source, :default => 'newcircuit', :type => :string, :desc => 'circuit name, i.e. source'
  def create
    circuit_dir = options[:path]
    %w(components/cookbooks packs clouds .chef).each {|s| empty_directory "#{circuit_dir}/#{s}"}

    source = options[:source]
    config = <<-EOH
log_level :info
log_location STDOUT
print_after true
admin false
node_name '#{source}'
register '#{source}'
version '1.0.0'
nspath '/public'
client_key 'client_key.pem'
validation_client_name 'chef-validator'
validation_key 'validation.pem'
chef_server_url 'http://localhost:4000'
cache_type 'BasicFile'
cache_options(:path => '.chef/checksums')
cookbook_path ['components/cookbooks']
publish_path 'pkgs'
pack_path ['packs']
service_path ['services']
cloud_path ['clouds']
catalog_path ['catalogs']
default_impl 'oo::chef-11.18.12'
EOH

    open(File.join("#{circuit_dir}/.chef", 'knife.rb'), 'w') do |file|
      file.puts config
    end

    say 'Created empty circuit seed.', :green
    say <<-HELP
Next steps:
  - "cd #{source}"
  - provide component, packs, cloud content
  - set 'CMSAPI' environment variable to point to oneops CMS server, e.g.
      export CMSAPI=http://localhost:8080/
  - run "circuit install" to load circuit content
HELP
  end

  desc 'init [OPTIONS]', 'Loads/updates base (essential) class and relation metadata definitions. Intended for OneOps admins to seed (or update) OneOps with most fundamental and shared across all circuits classes and relations.'
  method_option :cookbook_path, :default => '-o base:shared/cookbooks', :desc => 'relative path to definitions (could be ":" separated list)'
  def init
    cd_to         = File.expand_path('', File.dirname(__FILE__))
    cookbook_path = options[:cookbook_path]
    cookbook_path = "#{'-o' unless cookbook_path.include?('-o')} #{cookbook_path}" if cookbook_path
    %w(classes relations).each do |type|
      run_knife("#{"cd #{cd_to};" if cd_to} knife model sync -a --#{type} #{cookbook_path}")
      wait_for_cache_to_clear
    end
  end

  desc 'install', 'Loads/updates circuit content. Equivalent to running: "circuit model -a; circuit register; circuit pack -a; circuit cloud -a". Intended for content providers (pack developers) and OneOps admins. Should be run from circuit root directory.'
  def install
    %w(classes relations).each {|type| model('-a', "--#{type}")}

    register

    pack('-a')
    clouds('-a')

    say "Circuit installed!\n", :green
  end

  desc 'model [OPTIONS] [COOKBOOKS]', 'Load/updates class and relation metadata (i.e. model definition)'
  def model(*args)
    run_knife("knife model sync #{args.join(' ')}")
    wait_for_cache_to_clear
    say("Models synced!\n\n", :green)
  end

  desc 'register [OPTIONS]', 'Registers circuit namespace (i.e. circuit source)'
  def register(*args)
    run_knife("knife register #{args.join(' ')}")
    say("Source registered!\n\n", :green)
  end

  desc 'pack [OPTIONS] [PACKS]', 'Loads/updates pack definitions.'
  def pack(*args)
    run_knife("knife pack sync #{args.join(' ')}")
    say("Packs synced!\n\n", :green)
  end

  desc 'cloud [OPTIONS] [CLOUDS]', 'Loads/updates cloud templates.'
  def clouds(*args)
    run_knife("knife cloud sync #{args.join(' ')}")
    say("Clouds synced!\n\n", :green)
  end

  desc 'test [OPTIONS]', 'Pack test script - internal, intended for oneops core developers but some modes can be utilized by pack developers.', :hide => true
  def test(*args)
    cmd = "ruby #{File.expand_path('../test/packs.rb', File.dirname(__FILE__))} #{args.join(' ')}"
    say_status('running', cmd)
    system(cmd)
  end

  no_commands do
    private

    def run_knife(cmd)
      say_status("running", cmd)
      system(cmd)
      exit_code = $?.exitstatus
      return true if exit_code == 0

      say("Failed: #{cmd}\nExit code: #{exit_code}", :red)
      exit(exit_code)
    end

    def wait_for_cache_to_clear
      print "\nWaiting #{MD_CACHE_VAR_TTL} seconds for CMS metadata cache to clear... "
      sleep(MD_CACHE_VAR_TTL)
      puts 'done'
    end
  end
end

Circuit.start
