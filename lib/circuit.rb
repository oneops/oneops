require 'rubygems'
require 'thor'
require 'json'

# prevent ArgumentError: invalid byte sequence in US-ASCII from play2app under 1.9.3
ENV['LANG'] = "en_US.UTF-8"

class Circuit < Thor
  include Thor::Actions

  desc "create", "Setup Environment for cookbook/Pack creation"
  method_option :path, :default => File.expand_path('circuit', Dir.pwd)
  method_option :force, :default => true  
  def create
    directory File.expand_path('templates/circuit',File.dirname(__FILE__)), options[:path]
    empty_directory "#{options[:path]}/components/cookbooks"
    empty_directory "#{options[:path]}/packs"
    empty_directory "#{options[:path]}/clouds"
    empty_directory "#{options[:path]}/.chef"

    extra_config = ""
    if ENV.has_key?('DISPLAY_LOCAL_STORE')
      display_store = ENV['DISPLAY_LOCAL_STORE']
      puts "using display local object store: #{display_store}"
      extra_config = "object_store_provider 'Local'\n"
      extra_config += "environment_name 'cms'\n"
      extra_config += "object_store_local_root '#{display_store}'\n"
      `mkdir -p #{display_store}`
    end
    
    # unversioned base components and relationships    
    open(File.join("#{options[:path]}/.chef", "knife.rb-base"), "w") do |file|
      file.puts <<-EOH
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
default_impl 'oo::chef-11.4.0'
#{extra_config}
EOH
    end

    # allow versioned different chef knife.rb for shared components (Keypair, Artifact, etc) 
    # adapter changes are needed to use shortname of Keypair, etc ; keeping un-versioned for now 
    open(File.join("#{options[:path]}/.chef", "knife.rb-shared"), "w") do |file|
      file.puts <<-EOH
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
default_impl 'oo::chef-11.4.0'
#{extra_config}
EOH
    end
    
    puts "Next Step: cd circuit ; circuit init"
  end

  def self.source_root
    File.dirname(__FILE__)
  end

  desc "init", "Initialize the circuit"
  method_option :cookbook_path, :default => "-o base:shared/cookbooks"
  method_option :force, :default => true    
  def init
    base_path="#{File.expand_path(File.dirname(__FILE__))};"
    options[:cookbook_path].split(":").each do |path|
      if !path.include?("-o")
        path = "-o #{path}"
      end
      if path =~ /base/        
        run("cp .chef/knife.rb-base .chef/knife.rb")
      else
        run("cp .chef/knife.rb-shared .chef/knife.rb")                  
      end
      run("cp -r .chef #{base_path}")
      syncmodelclasses(base_path, path);
      #syncmodelrelations(base_path, path);      
    end
    options[:cookbook_path].split(":").each do |path|
      if !path.include?("-o")
        path = "-o #{path}"
      end
      if path =~ /base/        
        run("cp .chef/knife.rb-base .chef/knife.rb")
      else
        run("cp .chef/knife.rb-shared .chef/knife.rb")                  
      end
      run("cp -r .chef #{base_path}")
      #syncmodelclasses(base_path, path);
      syncmodelrelations(base_path, path);      
    end

  end

  desc "install", "Install the circuit"
  def install
    
    if ENV.has_key?('DISPLAY_LOCAL_STORE')
      display_store = ENV['DISPLAY_LOCAL_STORE']
      puts "using display local object store: #{display_store}"
      `ls .chef/knife.rb`
      if $?.to_i != 0
        puts "missing chef config: .chef/knife.rb - please run circuit install from root of the circuit dir"
        exit 1
      end

      # cleanup config
      `grep -v object_store .chef/knife.rb > .chef/tmp`
      `grep -v environment_name .chef/tmp > .chef/knife.rb`
      `rm -f .chef/tmp`
      # add config
      `echo "object_store_provider 'Local'" >> .chef/knife.rb`        
      `echo "environment_name 'cms'"  >> .chef/knife.rb`
      `echo "object_store_local_root '#{display_store}'" >> .chef/knife.rb`
      `mkdir -p #{display_store}`
    end

    model()
    sleep(md_cache_ttl)
    register()
    packs()
    clouds()
    puts 'Installed repository!'
  end

  desc "register", "Register source"
  def register
    cmd = "knife register"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "fail. #{cmd} returned: #{exit_code}", :red
      exit exit_code 
    end    
    puts "Source registered!"
  end

  desc "model", "Model Sync"

  def model
    syncmodelclasses()
    syncmodelrelations()
    
    puts "Model synced!"
  end

  desc "packs", "Pack Sync"

  def packs
    cmd = "knife pack sync -a"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "fail. #{cmd} returned: #{exit_code}", :red
      exit exit_code 
    end    
    puts "Pack synced!"
  end

  desc "clouds", "Clouds Sync"
  def clouds
    cmd = "knife cloud sync -a"
    say_status("running", cmd)
    system(cmd)
    exit_code = $?.exitstatus
    if exit_code != 0
      say "fail. #{cmd} returned: #{exit_code}", :red
      exit exit_code 
    end    
    puts "Cloud synced!"
  end

  
  no_commands do

    # Metadata cache TTL in seconds. Used to sleep before
    # doing pack sync to clear the adapter metadata cache.
    # Default value is 5 seconds. The default value can be
    # overridden by "export MD_CACHE_VAR_TTL=<TTL in sec>".
    #
    def md_cache_ttl
      (ENV['MD_CACHE_VAR_TTL'] || 5).to_i
    end

    def syncmodelclasses(path=nil, path_val=nil)
      pathVal = "cd #{path} " if !path.nil?
      cmd = "#{pathVal} knife model sync -a #{path_val}"
      say_status("running", cmd)
      system(cmd)
      exit_code = $?.exitstatus
      if exit_code != 0
        say "fail. #{cmd} returned: #{exit_code}", :red
        exit exit_code 
      end
    end

    def syncmodelrelations(path=nil, path_val=nil)
      pathVal = "cd #{path} " if !path.nil?
      cmd = "#{pathVal} knife model sync -a -r #{path_val}"
      say_status("running", cmd)
      system(cmd)
      exit_code = $?.exitstatus
      if exit_code != 0
        say "fail. #{cmd} returned: #{exit_code}", :red
        exit exit_code 
      end
      
      
    end
  end

end

Circuit.start
