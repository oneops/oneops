#!/usr/bin/env ruby
require 'optparse'
require 'yaml'
require 'fileutils'
Dir[File.join(File.expand_path(File.dirname(__FILE__)), 'exec-order','*.rb')].each {|f| require f }

# set cwd to shared/cookbooks directory
Dir.chdir File.expand_path('cookbooks',File.dirname(__FILE__))

if File.directory? 'vendor'
  puts 'removing vendor directory'
  FileUtils.rm_r 'vendor'
end

Dir.glob('*.gemfile').each do |f|

  if !f.include? '12.11.18'
    puts "packaging gemfile #{f}"

    start_time = Time.now.to_i
    cmd = "bundle package --no-install --no-prune --gemfile #{f}"
    ec = system cmd
    if !ec || ec.nil?
      puts "#{cmd} failed with, #{$?}"
      exit 1
    end

    puts "#{cmd} took: #{Time.now.to_i - start_time} sec"
  end
end




