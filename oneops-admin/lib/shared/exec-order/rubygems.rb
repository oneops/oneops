def update_gem_sources (primary_source, secondary_source, log_level = 'info')
  gem = "#{get_bin_dir}gem"
  expected_sources = [primary_source, secondary_source]
  puts "Expected gem sources: #{expected_sources}" if log_level == 'debug'
  actual_sources = `#{gem} source`.split("\n").select{|l| (l =~ /^http/)}
  puts "Actual gem sources: #{actual_sources}" if log_level == 'debug'
  
  if expected_sources != actual_sources
    puts 'Expected gem sources do not match the actual gem sources. Updating...'
    
    #1.Remove all existing sources
    actual_sources.each do |source|
      `#{gem} source --remove #{source}`
    end
    #2.Add expected sources
    expected_sources.each do |source|
      `#{gem} source --add #{source}`
    end
  else
    puts 'Expected gem sources match the actual gem sources.' if log_level == 'debug'
  end

  proxy_file = '/opt/oneops/rubygems_proxy'
  if File.exists?(proxy_file) && File.read(proxy_file).chomp != primary_source
    puts 'Rubygems_proxy config file is outdated. Updating...'
    File.open(proxy_file, 'w') {|f| f.puts primary_source }
  end
end


def get_gem_list (provisioner, version = nil)
  require 'yaml'

  config_file = get_file_from_parent_dir('exec-gems.yaml')
  gem_config = YAML::load(File.read(config_file))

  gem_list  = gem_config['common'] + [[provisioner,version]]
  gem_list += gem_config[provisioner] if gem_config[provisioner]
  gem_list += gem_config["#{provisioner}-#{version}"] if gem_config["#{provisioner}-#{version}"]

  gem_list.uniq
end


def create_gemfile(rubygems_proxy, gems)
  gemfile_content = "source '#{rubygems_proxy}'\n"

  gems.each do |gem_set|
    if gem_set.size > 1
      gemfile_content += "gem '#{gem_set[0]}', '#{gem_set[1]}'\n"
    else
      gemfile_content += "gem '#{gem_set[0]}'\n"
    end
  end

  File.open('Gemfile', 'w') {|f| f.write(gemfile_content) }
end


def is_gem_installed?(gem, version)
  out = `#{get_bin_dir}gem list ^#{gem}$ -i -v #{version}`.chomp
  if out == 'true'
    true
  else
    false
  end
end


def check_gem_update_needed (gems, log_level = 'info')
  update_needed = false

  gems.each do |g|
    if !is_gem_installed?(g[0],g[1])
      puts "Gem #{g[0]} version #{g[1]} is not installed." if log_level == 'debug'
      update_needed = true
      break
    end 
  end

  update_needed
end


def gen_gemfile_and_install (rubygems_proxy, gems, log_level)

    #Determine bundle method 
    #  - install if running for the first time
    #  - update if any gems from exec-gems.yaml have mismatching versions

    if !File.exists?('Gemfile.lock')
      puts 'Gemfile.lock is not found, will run bundle install.' if log_level == 'debug'
      method = 'install'
      create_gemfile(rubygems_proxy, gems)
    elsif check_gem_update_needed(gems, log_level)
      puts 'Gemfile.lock is found, and gem update is required.' if log_level == 'debug'
      File.delete('Gemfile') #re-create Gemfile in case the exec-gems.yaml has changed
      create_gemfile(rubygems_proxy, gems)
      method = 'update'
    else
      puts 'Gemfile.lock is found, and no gem update is required.' if log_level == 'debug'
      method = nil
    end

    if !method.nil?
      start_time = Time.now.to_i
      cmd = "#{get_bin_dir}bundle #{method} --full-index"
      ec = system cmd

      if !ec || ec.nil?
        puts "#{cmd} failed with, #{$?}"
        exit 1
      end
      puts "#{cmd} took: #{Time.now.to_i - start_time} sec"

      cmd_to_patch_azure_blob_sdk = "sed -i -e's/2012-02-12/2014-02-14/' \"$(dirname `gem which azure`)/azure/core/http/http_request.rb\""
      system cmd_to_patch_azure_blob_sdk
    end
end