#!/usr/bin/env ruby
require 'json'
require 'optparse'
require 'ostruct'
require 'base64'
require 'net/http'
require 'io/console'

class NilClass
  def [] (_)
    nil
  end

  def empty?
    true
  end
end

class String
  [[:red, "\e[31m"], [:green, "\e[32m"], [:yellow, "\e[33m"], [:blue, "\e[34m"]].each do |(name, code)|
    define_method(name) {|background = false|
      "\e[0m#{"\e[7m" if background}#{code}#{self}\e[0m"
    }
  end

  def start_with?(pattern)
    (self =~ /^#{pattern}/) == 0
  end

  def end_with(string)
    self[-string.size..-1] == string ? self : (self + string)
  end
end

class Hash
  def method_missing(method, *args, &block)
    if include?(method)
      self[method_s]
    else
      method_s = method.to_s
      include?(method_s) ? self[method_s] : (respond_to?(method) ? super : nil)
    end
  end
end

def say(*args)
  puts *args unless @silent
end

def blurt(*args)
  print *args unless @silent
end

def ask()
  STDIN.gets[0...-1]
end

def oo_request(cmd, msg)
  uri = URI("#{@params.oneops_host}#{cmd}")
  req = Net::HTTP::Get.new(uri)
  request(uri, req, msg)
end

def tt_request(cmd, msg, body = nil)
  uri = URI("#{@params.tekton_host}#{cmd}")
  if body
    req = Net::HTTP::Post.new(uri)
    req.body = body.to_json
  else
    req = Net::HTTP::Get.new(uri)
  end
  req['Authorization'] = "#{'Basic ' if @params.tekton_auth.include?(':')}#{Base64.strict_encode64(@params.tekton_auth)}"
  request(uri, req, msg)
end

def request(uri, req, msg)
  ts = Time.now
  result = nil
  response = nil
  if @params.verbose > 1
    say uri.to_s.green
    say "#{msg}... "
  elsif @params.verbose > 0
    blurt msg, '... '
  end
  begin
    req['Content-Type'] = 'application/json'
    say "REQUEST: #{req.body}" if @params.verbose > 1 && !req.body.empty?
    response = Net::HTTP.start(uri.host, uri.port, :use_ssl => uri.scheme == 'https') {|http| http.request(req)}
    if response.is_a?(Net::HTTPSuccess)
      body = response.body
      unless body.empty?
        say "RESPONSE #{response.code.green}: #{response.body}" if @params.verbose > 1
        result = JSON.parse(body)
      end
    elsif response.is_a?(Net::HTTPNotFound)
    elsif response.is_a?(Net::HTTPUnauthorized)
      say 'Invalid credentials'.red
      exit(1)
    else
      raise Exception.new('Bad response.')
    end
  rescue SystemExit => e
    exit(1)
  rescue Exception => e
    say "\nFailed to perform '#{msg}': #{uri.to_s.blue}".red
    say "RESPONSE #{response.code.red}: #{response.body}" if response
    say @params.verbose > 0 ? e : e.message
    exit(1)
  end

  say "done in #{(Time.now - ts).to_f.round(1)}sec." if @params.verbose > 0
  return result
end

def required_arg(name, value)
  if value.empty?
    say "Specify #{name}".red
    action_help(@action)
  else
    say "#{name}: #{value.to_s.blue}" if @params.verbose > 0
  end
end

def general_help
  say "#{@opt_parser}\n#{@footer}"
  exit
end

def actions_help
  @actions.values.inject("  Actions:\n") {|s, a| s << "    #{a[0]}\n      #{a[1]}\n"}
end

def action_help(action = nil)
  if action.empty?
    say @usage
  else
    usage = @actions[action]
    say "Usage:\n  #{usage[0]}" if usage
  end
  exit(1)
end

def full_quota(title, total, usage, available)
  unless total.empty? && usage.empty?
    say "#{title.blue} =>\n"
    say "  Resource                      | Used      | Reserved  | Available | Total     "
    say "  ------------------------------|-----------|-----------|-----------|-----------"
    total.keys.sort.each do |n|
      r_usage = usage[n].to_i
      r_total = total[n].to_i
      r_avail = available[n].to_i
      say "  #{n.ljust(30)}|#{r_usage.to_s.rjust(10)} |#{(r_total - r_usage - r_avail).to_s.rjust(10)} |#{r_avail.to_s.rjust(10).green} |#{r_total.to_s.rjust(10).blue}"
    end
    say
  end
end


def execute!(action, *args)
  silent = @silent
  @silent = @params.verbose == 0   # No output for nested commands unless verbose is on
  begin
    execute(action, *args)
  ensure
    @silent = silent
  end
end

def execute(action, *args)
  result = nil
  if action == 'oo:resources'
    result = {}
    mappings = oo_request('CLOUD_PROVIDER_MAPPINGS/vars', 'Getting mappings')
    JSON.parse(mappings.value).each_pair do |provider, provider_mappings|
      provider_mappings.each_pair do |component, component_mappings|
        component_mappings.each_pair do |attr, attr_mappings|
          attr_mappings.values.each do |resource_mappings|
            resource_mappings.keys.each do |resource|
              (result[resource] ||= {})["#{provider}:#{component}:#{attr}"] = resource
            end
          end
        end
      end
    end
    result.keys.sort.each do |r|
      result[r] = result[r].keys.join(', ')
      say "#{r.blue} =>\n  #{result[r]}"
    end

  elsif action == 'oo:resources:transfer'
    resources = execute!('oo:resources')
    resources.each_pair {|name, desc| execute('resource:add', name, desc)}

  elsif action == 'oo:subs'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)
    provides_rels = %w(Azure Openstack).inject([]) do |a, clazz|
      a += oo_request("relations?nsPath=/#{org_name}/_clouds&recursive=true&relationShortName=Provides&targetClassName=#{clazz}&includeFromCi=true&includeToCi=true", 'Getting clouds')
    end
    result = {}
    provides_rels.sort_by {|p| p.fromCi.ciName}.each do |p|
      cloud = p.fromCi
      next unless cloud.ciName =~ /#{cloud_name}/i
      subsciption = p.toCi.ciAttributes['subscription'] || p.toCi.ciAttributes['tenant']
      (result["#{cloud.ciAttributes.location.split('/').last}:#{subsciption}"] ||= []) << p unless subsciption.empty?
    end
    result.keys.sort.each do |s|
      say "#{s.blue} =>\n  #{result[s].map {|p| p.fromCi.ciName}.join(', ')}"
    end

  elsif action == 'oo:subs:transfer'
    subs = execute!('oo:subs', *args)
    subs.each_pair {|name, provides_rel| execute('sub:add', name, name.split(':').first)}
    execute('resources')

  elsif action == 'oo:usage'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)

    mappings = oo_request('CLOUD_PROVIDER_MAPPINGS/vars', "Getting mappings")
    mappings = JSON.parse(mappings.value)
    if mappings.empty?
      say 'No provider mappings found'.red
      exit(1)
    end
    say "Resource mappings:\n#{JSON.pretty_unparse(mappings)}" if @params.verbose > 1

    result = {}
    subs = execute!('oo:subs', org_name, cloud_name)
    subs.each_pair do |sub, provides_rels|
      say "Processing clouds for subscription '#{sub.blue}' (#{provides_rels.map {|r| r.fromCi.ciName}.join(', )')})" if @params.verbose > 0
      provider = provides_rels.first.toCi.ciClassName.split('.').last.downcase
      provider_mappings = mappings[provider]
      if provider_mappings.empty?
        say "  No mappings found for provider #{provider}".yellow
        next
      end

      sub_usage = {}
      provider_mappings.each_pair do |ci_class_name, class_mappings|
        provides_rels.each do |rel|
          cloud = rel.fromCi
          say "  Processing #{ci_class_name} for cloud '#{cloud.ciName}'"  if @params.verbose > 0
          deployed_tos = oo_request("relations?ciId=#{cloud.ciId}&direction=to&relationShortName=DeployedTo&targetClassName=#{ci_class_name.capitalize}&includeFromCi=true", "    Getting #{ci_class_name} for cloud '#{cloud.ciName}'")
          say "    Found #{deployed_tos.size} #{ci_class_name} instances."  if @params.verbose > 1
          deployed_tos.each do |r|
            ci = r.fromCi
            class_mappings.each_pair do |attr_name, attr_mappings|
              resources = nil
              attr_value = ci.ciAttributes[attr_name]
              resources = attr_mappings[attr_value] unless attr_value.empty?
              resources = attr_mappings['*'] if resources.empty?
              next if resources.empty?
              resources.each_pair do |resource, value|
                sub_usage[resource] = (sub_usage[resource] || 0) + value
              end
            end
          end
        end
      end

      unless sub_usage.empty?
        result[sub] = sub_usage
        say "  #{sub.blue}/#{org_name} =>"
        sub_usage.keys.sort.each {|n| say "    #{n.ljust(25)} | #{sub_usage[n].to_s.rjust(10)}"}
        say
      end
    end

  elsif action == 'oo:usage:transfer'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)

    usage = execute!('oo:usage', org_name, cloud_name)
    usage.each_pair do |sub, sub_usage|
      total = tt_request("quota/#{sub}/#{org_name}", 'Getting quota')
      if total.empty? || @params.force
        if total.empty?
          if @params.transfer_buffer < 0
            say 'Invalid quota buffer %'.red
            action_help(@action)
            exit(1)
          elsif @params.transfer_buffer == 0
            say 'Buffer % is not set, will set total quota to the same value as usage.'.yellow
          end
          execute!('quota:set', sub, org_name, *sub_usage.to_a.map {|r, v| "#{r}=#{(v + v * @params.transfer_buffer).to_i}"}) if @params.transfer_buffer > 0
        else
          say "Quota for #{sub}/#{org_name} already exists, will set usage only, use #{"'quota:set'".blue} action to set total quota values.".yellow
          sub_usage.delete_if {|r| total[r]} if @params.only_missing
        end
        execute('quota:usage:set', sub, org_name, *sub_usage.to_a.map {|r, v| "#{r}=#{v}"})
      else
        say "Quota for #{sub}/#{org_name} already exists, use '-f' option to force update.".yellow
      end
      say
    end

  elsif action == 'resources'
    resources = tt_request('resource', 'Fetching resources')
    resources.sort_by(&:name).each do |r|
      say "#{r.name.blue} =>\n  #{r.description}"
    end

  elsif action == 'resource:add'
    resource_name, desc, _ = args
    resource = tt_request("resource/#{resource_name}", "Fetching resource '#{resource_name}'")
    if resource
      if @params.force
        tt_request('resource', "Resource #{resource_name} already exists, updating...", {:name => resource_name, :description => desc || resource_name})
      else
        say "Resource #{resource_name} already exists, use '-f' option to force update.".yellow
      end
    else
      tt_request('resource', "Resource #{resource_name} is missing, creating...", {:name => resource_name, :description => desc || resource_name})
    end

  elsif action == 'subs'
    subs = tt_request('subscription', 'Fetching subscriptions')
    subs.sort_by(&:name).each do |s|
      say "#{s.name.blue} =>\n  #{s.description}"
    end

  elsif action == 'sub:add'
    sub_name, desc, _ = args
    sub = tt_request("subscription/#{sub_name}", "Fetching subscription '#{sub_name}'")
    if sub
      if @params.force
        tt_request('subscription', "Subscription #{sub_name} already exists, updating...", {:name => sub_name, :description => desc || sub_name})
      else
        say "Subscription #{sub_name} already exists, use '-f' option to force update.".yellow
      end
    else
      tt_request('subscription', "Subscription #{sub_name} is missing, creating...", {:name => sub_name, :description => desc || sub_name})
    end

  elsif action == 'orgs'
    result = tt_request('org', 'Fetching orgs')
    result.sort_by(&:name).each {|o| say o.name}

  elsif action == 'org:add'
    org_name = args[0]
    org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
    tt_request('org', "Org #{org_name} is missing, creating", {:name => org_name}) unless org

  elsif action == 'sub:quotas'
    sub_name = args[0]
    required_arg('subscription', sub_name)
    total = tt_request("quota/subscription/#{sub_name}", 'Getting quotas')
    usage = tt_request("quota/usage/subscription/#{sub_name}", 'Getting usages')
    available = tt_request("quota/available/subscription/#{sub_name}", 'Getting usages')
    total.keys.sort.each {|org| full_quota(org, total[org], usage[org], available[org])} unless total.empty?

  elsif action == 'org:quotas'
    org_name = args[0]
    required_arg('org', org_name)
    total = tt_request("quota/entity/#{org_name}", 'Getting quotas')
    usage = tt_request("quota/usage/entity/#{org_name}", 'Getting usages')
    available = tt_request("quota/available/entity/#{org_name}", 'Getting usages')
    total.keys.sort.each {|sub| full_quota(sub, total[sub], usage[sub], available[sub])} unless total.empty?

  elsif action == 'quota'
    sub_name, org_name, _ = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)

    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      total = tt_request("quota/#{s}/#{org_name}", 'Getting quota')
      unless total.empty?
        usage = tt_request("quota/usage/#{s}/#{org_name}", 'Getting usage')
        available = tt_request("quota/available/#{s}/#{org_name}", 'Getting available')
        full_quota("#{s}/#{org_name}", total, usage, available)
      end
    end

  elsif action == 'quota:set' || action == 'quota:usage:set' || action == 'usage:set'
    sub_name, org_name, *resources = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)
    required_arg('resources', resources)

    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      usage = resources.inject({}) do |h, r|
        name, value = r.split(/[=:]/, 2)
        value = value.to_i
        h[name] = value if !name.empty? && value > 0
        h
      end
      required_arg('resources', usage)

      sub = tt_request("subscription/#{s}", "Fetching subscription '#{s}'")
      execute!('sub:add', s) unless sub

      org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
      execute!('org:add', org_name) unless org

      tt_request("quota/#{"usage/" if action.include?('usage:set')}#{s}/#{org_name}", "Updating quota", usage)
      execute('quota', s, org_name)
    end

  elsif action == 'sub:prompt'
    sub_name, org_name, _ = args
    if sub_name[0] == '?'
      total = tt_request("quota/entity/#{org_name}", 'Getting quotas')
      if total.empty?
        say "No existing quotas set up in org '#{org_name}'".red
        exit(1)
      else
        subs = total.keys
        sub_pattern = sub_name[1..-1]
        unless sub_pattern.empty?
          sub_regex = /#{sub_pattern}/i
          subs = subs.select {|s| s =~ sub_regex}
          if subs.empty?
            say "No existing quotas in org '#{org_name}' have subscriptions matching '#{sub_pattern}'".yellow
            exit(1)
          end
        end

        if subs.size == 1
          result = subs
        else
          say "Choose subscription from existing quotas in this org:"
          subs.each_with_index {|sub, i| say "  #{i + 1}. #{sub}"}
          blurt "Which subscription (n|n,m...|*): ".green
          p = ask
          if p == '*'
            result = subs
          else
            result = []
            p.split(',').each do |t|
              index = t.to_i
              result << subs[index - 1] if index >= 1 && index <= subs.size
            end
          end
          result = result.uniq
          exit if result.empty?
        end
      end
    else
      result = sub_name.split(',').uniq
    end

  elsif action == 'login'
    username, password, _ = args
    if username.empty?
      password = nil
      print 'username: '
      username = ask
    end
    if password.empty?
      print 'password: '
      STDIN.noecho {password = STDIN.gets[0...-1]}
      puts
    end

    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

    @params.tekton_auth = "#{username}#{":#{password}" unless password.empty?}"
    result = !tt_request('org', 'Checking credentials').empty?
    if result
      cfg = %w(tekton_host oneops_host tekton_auth).inject({}) {|h, key| h[key] = @params[key]; h}
      File.write(SESSION_FILE_NAME, JSON.pretty_unparse(cfg))
      say "Signed in - do not forget to logout when done!".green
    end

  elsif action == 'logout'
    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

  elsif action == 'help'
    general_help

  else
    say "Unknown action: #{action}".red
    exit(1)
  end

  return result
end

@actions = {
  'help'                  => ['help', 'display help'],
  'login'                 => ['login [-e ENV] [USERNAME [PASSWORD]]', 'log in for running Tekton commands, defaults to \'prod\' environment if not specified'],
  'logout'                => ['logout [USERNAME [PASSWORD]]', 'log out after running Tekton commands'],
  'orgs'                  => ['orgs', 'list orgs'],
  'org:add'               => ['org:add ORG', 'add org'],
  'resources'             => ['resources', 'list resources'],
  'resource:add'          => ['resource:add  [-f]', 'add resource'],
  'subs'                  => ['subs', 'list subsctiption'],
  'sub:add'               => ['sub:add [-f] SUBSCRIPTION', 'add subscription'],
  'sub:quotas'            => ['sub:quotas SUBSCRIPTION', 'list all quotas for subscription'],
  'org:quotas'            => ['org:quotas ORG', 'list all quotas for org'],
  'quota'                 => ['quota SUBSCRIPTION ORG', 'show quota'],
  'quota:set'             => ['quota:set SUBSCRIPTION ORG RESOURCE=VALUE [RESOURCE=VALUE ...]', 'update quota totals'],
  'quota:usage:set'       => ['quota:usage:set SUBSCRIPTION ORG RESOURCE=VALUE [RESOURCE=VALUE ...]', 'update quota totals'],
  'oo:resources'          => ['oo:resources', 'list resource types in OneOps'],
  'oo:resources:transfer' => ['oo:resources:transfer [-f]', 'transfer resources types in OneOps to Tekton'],
  'oo:subs'               => ['oo:subs ORG [CLOUD_REGEX]', 'list subsctiptions in OneOps'],
  'oo:subs:transfer'      => ['oo:subs:transfer  [-f] ORG [CLOUD_REGEX]', 'transfer subsctiptions in OneOps to Tekton'],
  'oo:usage'              => ['oo:usage ORG [CLOUD_REGEX]', 'list usage in OneOps'],
  'oo:usage:transfer'     => ['oo:usage:transfer [-f [--omr]] [-b BUFFER_%] ORG [CLOUD_REGEX]', 'convert current usage in OneOps into quota in Tekton or update usage for existing Tekton quota to the current ussage in OneOps']
}

@usage = <<-USAGE
  Usage:
    #{__FILE__} [OPTIONS] ACTION ARGUMENTS
    
#{actions_help}
USAGE

@footer = <<-FOOTER
Typical flow:
1. Login first (alternatively, you will need to provide auth on each subsequent command
   with '--ta' option):
     #{__FILE__} login my_username
   
2. Import resources to Tekton from OneOps based on existing provider mappings
   (done during initial seeding or when new resource types are added):
     #{__FILE__} oo:resources:transfer

3. For a given org import existing subscription to Tekton from OneOps:
     #{__FILE__} oo:subs:transfer some-org
   Or skip this step and go directly to creating quotas (orgs and subscription will be added
   on the fly) by transfering usage from OneOps with some buffer for max allowed quota
     #{__FILE__} oo:usage:transfer some-org some-cloud -b 50
   This will bootstrap a quota in Tekton with usage set to current usage numbers in OneOps
   and max quota set to 50% above current usage.  If quota for that org and subscription already
   exists it will only update the usage numbers without changing max allowed values.

   Alternatively, add/set quota manually:
     #{__FILE__} quota:set azure-southcentralus-wm:102e961b-18a2-4ff0-a03e-c58794d04d55 some-org vm=100 Dv2_vCPU=250
     #{__FILE__} quota:usage:set azure-southcentralus-wm:102e961b-18a2-4ff0-a03e-c58794d04d55 some-org vm=37 Dv2_vCPU=128

4. List all exising quotas for a given org in Tekton:
     #{__FILE__} org:quotas some-org

5. Logout:
     #{__FILE__} logout

FOOTER


#------------------------------------------------------------------------------------------------
# Start here.
#------------------------------------------------------------------------------------------------
SESSION_FILE_NAME      = '.tekton'
TEKTON_HOSTS           = {:local => 'http://localhost:9000',
                :stg   => 'http://tekton-service.stg.tekton.oneops.cdcstg2.prod.walmart.com:9000',
                :prod  => 'http://tekton.prod.walmart.com:9000'}
TEKTON_HOSTS[:default] = TEKTON_HOSTS[:prod]

ONEOPS_HOSTS = {:local => 'http://localhost:8080/',
                :stg   => 'http://adapter.stg.core-1612.oneops.prod.walmart.com:8080/',
                :prod  => 'http://cmsapi.prod-1312.core.oneops.prod.walmart.com:8080/'}
ONEOPS_HOSTS[:default] = ONEOPS_HOSTS[:prod]

@params = OpenStruct.new(:verbose => 0, :force => false, :only_missing => false, :transfer_buffer => 0)

@show_help = false
@opt_parser = OptionParser.new do |opts|
  opts.banner = <<-HELP
  Tool to query and to configure subscription, org and soft quota data in Tekton directly or based on current usage in OneOps.
  
  #{@usage}

  Options:
  HELP

  opts.on('-e',   '--environment ENV', [:prod, :stg, :local], 'Environment to use: prod|stg|local') {|host| @params.tekton_host = host}
  opts.on('--th', '--tekton-host HOST', "Tekton host, defaults to '#{TEKTON_HOSTS[:default]}' if no host or environment options are specified") {|host| @params.tekton_host = host}
  opts.on('--ta', '--tekton-auth CREDENTIALS', "Tekton auth if not using 'login' command: <username<:<password>  or <token>") {|creds| @params.tekton_auth = creds}
  opts.on('--oh', '--oneops-host HOST', "OneOps CMS host, defaults to '#{ONEOPS_HOSTS[:default]}' if no host or environment options are specified") {|host| @params.oneops_host = host}

  opts.on('-b', '--buffer BUFFER_%', 'Buffer (as a percentage of usage) to add on top of usage number when setting total quota value for new quotas. Default value is 0 (no buffer) - usage and total are the same') {|buffer_pct| @params.transfer_buffer = buffer_pct.to_f / 100}

  opts.on('-f', '--force', 'Force update if already exists') {@params.force = true}
  opts.on('--omr', '--only-missing-resources', "Transfer usage only for resources missing quota (when there is already quota set up for at least one other resource (for a given subscription and org); use with '-f' option") {@params.only_missing = true}

  opts.on('-v', '--verbose', 'Verbose') {@params.verbose = 1}
  opts.on('--vv', '--very-verbose', 'Very verbose') {@params.verbose = 2}

  opts.on('-h', '--help', 'Show this message') {@show_help = true}
end

@action, *@args = @opt_parser.parse(ARGV)

if @action.empty?
  general_help if @show_help

  say "Specify ACTION!".red
  say "#{actions_help}\nUse '-h' or 'help' action to see full help."
  exit(1)
end

@action = @action.downcase.gsub('oneops', 'oo')
unless @actions.include?(@action)
  regex = /#{@action.gsub(/\W+/, '.*:')}\w*/
  action = @actions.keys.find {|k| (regex =~ k) == 0}
  if action.empty?
    say "Unknown action: #{@action}".red
    action_help
  else
    @action = action
  end
end
action_help(@action) if @show_help

if File.exist?(SESSION_FILE_NAME) && %w(tekton_host oneops_host tekton_auth).none? {|key| @params[key]}
  cfg = JSON.parse(File.read(SESSION_FILE_NAME))
  %w(tekton_host oneops_host tekton_auth).each {|key| @params[key.to_sym] = cfg[key] unless cfg[key].empty?}
end

if @params.tekton_auth.empty? && @action != 'login' && @action != 'help'
  say "Specify tekton auth with '--ta' option or use 'login' command.".red
  action_help('login')
  exit(1)
end

@params.tekton_host = @params.oneops_host if @params.tekton_host.empty? && !@params.oneops_host.empty? && ONEOPS_HOSTS.include?(@params.oneops_host.downcase.to_sym)
@params.oneops_host = @params.tekton_host if @params.oneops_host.empty? && !@params.tekton_host.empty? && TEKTON_HOSTS.include?(@params.tekton_host.downcase.to_sym)

if @params.tekton_host.empty?
  @params.tekton_host = TEKTON_HOSTS[:default]
  say "Tekton host not specified, defaulting to #{@params.tekton_host.blue}".yellow if @params.verbose > 0
else
  host = @params.tekton_host.downcase.to_sym
  @params.tekton_host = TEKTON_HOSTS[host] if TEKTON_HOSTS.include?(host)
end
@params.tekton_host = "https://#{@params.tekton_host}" unless @params.tekton_host.start_with?('http')
@params.tekton_host = "#{@params.tekton_host}/" unless @params.tekton_host.end_with?('/')
@params.tekton_host = @params.tekton_host.end_with('api/v1/')

if @params.oneops_host.empty?
  @params.oneops_host = ONEOPS_HOSTS[:default]
  say "OneOps host not specified, defaulting to #{@params.oneops_host.blue}".yellow if @params.verbose > 0
else
  host = @params.oneops_host.downcase.to_sym
  @params.oneops_host = ONEOPS_HOSTS[host] if ONEOPS_HOSTS.include?(host)
end
@params.oneops_host = "https://#{@params.oneops_host}" unless @params.oneops_host.start_with?('http')
@params.oneops_host = "#{@params.oneops_host}/" unless @params.oneops_host.end_with?('/')
@params.oneops_host = @params.oneops_host.end_with('adapter/rest/cm/simple/')

execute(@action, *@args)
