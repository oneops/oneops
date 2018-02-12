#!/usr/bin/env ruby
require 'optparse'
require 'yaml'
Dir[File.join(File.expand_path(File.dirname(__FILE__)), 'exec-order','*.rb')].each {|f| require f }

puts File.dirname(__FILE__)
# set cwd to shared/cookbooks directory
Dir.chdir File.expand_path('cookbooks',File.dirname(__FILE__))
Dir.glob('*.gemfile*').each do |f|
  File.delete f
end


log_level = "debug"
gem_sources  = get_gem_sources
config_files = [get_file_from_parent_dir('exec-gems.yaml'), get_file_from_parent_dir('exec-gems-az.yaml')]

config_files.each do |config_file|

  puts "parsing config_file #{config_file}"

  gem_config = YAML::load(File.read(config_file))
  gem_config.keys.each do |impl|
    dsl, version = impl.split('-')
    puts "dsl=#{dsl}, version=#{version}"
    case dsl
      when "chef"
        if (version != "12.11.18")
          gem_list = get_gem_list(dsl, version, config_file.split('/').last)
          create_gemfile(gem_sources, gem_list)

          puts 'Gemfile content is:'
          File.open('Gemfile').each do |line|
            puts line
          end

          start_time = Time.now.to_i
          cmd = "#{get_bin_dir}bundle package --no-install --no-prune"
          ec = system cmd
          if !ec || ec.nil?
            puts "#{cmd} failed with, #{$?}"
            exit 1
          end
          puts "#{cmd} took: #{Time.now.to_i - start_time} sec"
        elsif version == '12.11.18'
          gem_list = get_gem_list(dsl, version, config_file.split('/').last)
          create_gemfile(gem_sources, gem_list)
          puts 'Gemfile content is:'
          File.open('Gemfile').each do |line|
            puts line
          end
        else
          puts "skipping packaging gems for chef #{version}"
        end

        config_file_base_name = File.basename("#{config_file.split('/').last}", '.yaml')
        if File.exists?('Gemfile')
          File.rename('Gemfile', "#{config_file_base_name}-#{dsl}-#{version}.gemfile")
        end
        if File.exists?('Gemfile.lock')
          File.rename('Gemfile.lock',"#{config_file_base_name}-#{dsl}-#{version}.gemfile.lock")
        end
    end
  end
end



