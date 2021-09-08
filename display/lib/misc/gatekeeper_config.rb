#!/usr/bin/env ruby
require 'json'
require 'optparse'
require 'ostruct'

class NilClass
  def [] (_)
    nil
  end

  def empty?
    true
  end
end

class String
  def red(background = false)
    colorize("\e[31m", background)
  end

  def green(background = false)
    colorize("\e[32m", background)
  end

  def yellow(background = false)
    colorize("\e[33m", background)
  end

  def blue(background = false)
    colorize("\e[34m", background)
  end

  def colorize(color_code, background = false)
    "\e[0m#{"\e[7m" if background}#{color_code}#{self}\e[0m"
  end

  def start_with?(pattern)
    (self =~ /^#{pattern}/) == 0
  end
end

class Hash
  def method_missing(method, *args, &block)
    method_s = method.to_s
    include?(method_s) ? self[method_s] : (include?(method) ? self[method] : super)
  end
end

def curl(cmd, msg)
  puts cmd if @params.verbose
  ts = Time.now
  result = nil
  print msg, '... '
  cmd = "curl -s -u #{@params.token}: #{@params.host}#{cmd}"
  begin
    response = `#{cmd}`
    result = JSON.parse(response)
  rescue Exception => e
    puts "Failed to perform: #{cmd.blue}".red
    puts "RESPONSE: #{response}"
    puts @params.verbose ? e : e.message
    System.exit(1)
  end

  puts "done in #{(Time.now - ts).to_f.round(1)}sec."
  return result
end


#------------------------------------------------------------------------------------------------
# Start here.
#------------------------------------------------------------------------------------------------
usage = <<-USAGE
  Usage:
    <this_script> -t TOKEN [OPTIONS] ONEOPS_HOST ORG [ORG ...]
  
  Example:
    ./gatekeeper_config.rb https://oneops org1 org2 org3
USAGE

params = OpenStruct.new
opt_parser = OptionParser.new do |opts|
  opts.banner = <<-HELP
  Configures organization(s) for GateKeeper integration by inserting deployment notification
  sink CI and cloud support CIs for production clouds.
  
  #{usage}

  Options:
  HELP

  opts.on('-h', '--help', 'Show this message') do
    puts opts
    exit
  end

  opts.on('-t', '--token TOKEN', 'OneOps auth token') {|token| params.token = token}
  opts.on('-v', '--verbose [LEVEL]', 'Verbose: default level 1') {|level| params.verbose = (level || 1)}
end

params.host, *@orgs = opt_parser.parse(ARGV)
if params.host.empty?
  puts 'Specify HOST argument.'.red
  puts usage
  exit(1)
elsif @orgs.empty?
  puts 'Specify one or more'.red
  puts usage
  exit(1)
elsif params.token.empty?
  print 'Token: '
  params.token = gets
end

params.host = "https:// #{params.host}" unless params.host.start_with?('http')
params.host = "#{params.host}/" unless params.host.end_with?('/')
@params = params

@orgs.each do |org|
  puts "Org: #{org.blue}"
  clouds = curl("#{org}/clouds.json", 'Getting clouds').map(&:ciName).select {|c| c.start_with?(/prod/i)}
  puts clouds
end
