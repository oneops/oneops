#!/usr/bin/env ruby
require 'json'
require 'optparse'
require 'ostruct'
require 'base64'
require 'net/http'
require 'io/console'
require 'readline'

SESSION_FILE_NAME  = '.tekton'
HISTORY_FILE_NAME  = '.tekton_hist'

@use_session = true   #  Set to fails  and swtich to FQDN to disable session auth.

TEKTON_HOSTS = {:local => 'http://localhost:9000',
                :dev   => 'http://tekton.dev.prod.walmart.com',
                :stg   => 'http://tekton-service.stg.tekton.oneops.cdcstg2.prod.walmart.com:9000',
                :prod  => 'http://10.120.185.120:9000'}
                # :prod  => 'http://10.227.217.183:9000'}
                # :prod  => 'http://10.227.209.74:9000'}
                # :prod  => 'http://tekton.prod0718.walmart.com'}
TEKTON_HOSTS[:default] = TEKTON_HOSTS[:prod]

ONEOPS_HOSTS = {:local => 'http://localhost:8080/',
                :dev   => 'http://cmsapi.prod-1312.core.oneops.prod.walmart.com:8080/',
                :stg   => 'http://adapter.stg.core-1612.oneops.prod.walmart.com:8080/',
                :prod  => 'http://cmsapi.prod-1312.core.oneops.prod.walmart.com:8080/'}
ONEOPS_HOSTS[:default] = ONEOPS_HOSTS[:prod]

class NilClass
  def [] (_)
    nil
  end

  def empty?
    true
  end
end

class String
  @@with_color = true
  def self.with_color=(val)
    @@with_color = val
  end

  [[:bold, "\e[1m"], [:red, "\e[31m"], [:green, "\e[32m"], [:yellow, "\e[33m"], [:blue, "\e[34m"]].each do |(name, code)|
    define_method(name) {|background = false|
      @@with_color ? "\e[0m#{"\e[7m" if background}#{code}#{self}\e[0m" : self
    }
  end

  def start_with?(pattern)
    pattern ? (self =~ /^#{pattern}/) == 0 : false
  end

  def end_with(string)
    self[-string.size..-1] == string ? self : (self + string)
  end
end

class Hash
  def method_missing(method, *args, &block)
    if include?(method)
      self[method]
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
  result, response = request(uri, req, msg)
  return result
end

def tt_request(cmd, msg, body = nil, method = 'POST')
  uri = URI("#{@params.tekton_host}#{cmd}")
  if body
    method.upcase!
    if method == 'PUT'
      req = Net::HTTP::Put.new(uri)
    elsif method == 'DELETE'
      req = Net::HTTP::Delete.new(uri)
    else
      req = Net::HTTP::Post.new(uri)
    end
    req.body = body.to_json
  else
    req = Net::HTTP::Get.new(uri)
  end
  if @tt_cookie.empty?
    req['Authorization'] = @params.tekton_auth
  else
    req['Cookie'] = @tt_cookie
  end
  result, response = request(uri, req, msg)
  if response
    cookie = response['set-cookie']
    @tt_cookie = cookie if @use_session && cookie && cookie.include?('JSESSIONID')
  end
  return result
end

def request(uri, req, msg)
  ts = Time.now
  result = nil
  response = nil
  if @params.verbose > 1
    say "#{msg}... "
    say "#{req.class.name.split('::').last.upcase.bold} #{uri.to_s.green}"
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
      say 'Invalid credentials, please login'.red
      @tt_cookie = nil
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
  return result, response
end

def set_tekton_auth(username, password = nil)
  @tt_cookie = nil
  @params.tekton_auth = password.empty? ? Base64.strict_encode64(username) : "#{'Basic ' unless password.empty?}#{Base64.strict_encode64("#{username}:#{password}")}"
end

def required_arg(name, value)
  if value.empty?
    say "Specify #{name.upcase}".red
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
  @actions.values.inject("  Actions:\n".bold) {|s, a| s << "    #{a[0]}\n      #{a[1]}\n"}
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

def sub_quota(sub, limits)
  say "#{sub.name.bold} =>\n  #{sub.description}"
  say '  Resource                      | Limit     '
  say '  ------------------------------|-----------'
  unless limits.empty?
    limits.keys.sort.each do |n|
      say "  #{n.ljust(30)}|#{limits[n].to_i.to_s.rjust(10).blue}"
    end
  end
  say
end

def full_quota(title, limits, usage, available, available_threshold = nil)
  if available_threshold
    quotas = limits.select do |resource, limit|
      r_limit = limit.to_f
      r_avail = available[resource].to_i
      r_limit <= 0 || (r_avail / r_limit) <= available_threshold
    end
  else
    quotas = limits
  end
  return if quotas.empty?

  say "#{title} =>\n"
  say '  Resource                      | Used      | Reserved  | Available | Limit     '
  say '  ------------------------------|-----------|-----------|-----------|-----------'
  quotas.keys.sort.each do |resource|
    r_limit = limits[resource].to_i
    r_avail = available[resource].to_i
    r_usage = usage[resource].to_i
    say "  #{resource.ljust(30)}|#{r_usage.to_s.rjust(10)} |#{(r_limit - r_usage - r_avail).to_s.rjust(10)} |#{r_avail.to_s.rjust(10).green} |#{r_limit.to_s.rjust(10).blue}"
  end
  say
end

def usage(title, oo_usage, tt_usage)
  has_diff = false
  say "#{title} =>\n"
  say '  Resource                      | OneOps    | Tekton    | Diff      '
  say '  ------------------------------|-----------|-----------|-----------'
  (oo_usage.keys + tt_usage.keys).uniq.sort.each do |r|
    u1   = oo_usage[r].to_i
    u2   = tt_usage[r]
    diff = u2 ? (u1 - u2) : nil
    line = "#{r.ljust(30)}|#{u1.to_s.rjust(10)} |#{u2.to_s.rjust(10)} |#{diff.to_s.rjust(10)}"

    has_resource_diff = diff && diff != 0
    say "  #{has_resource_diff ? line.red(true) : line}"
    has_diff ||= has_resource_diff
  end
  has_diff
end

def fetch_provider_mappings
  return @mappings if @mappings
  @mappings = oo_request('CLOUD_PROVIDER_MAPPINGS/vars', "Getting mappings")
  @mappings = JSON.parse(@mappings.value)
  if @mappings.empty?
    say 'No provider mappings found'.red
    exit(1)
  end
  say "Resource mappings:\n#{JSON.pretty_unparse(@mappings)}" if @params.verbose > 1
  @mappings
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
    result.keys.sort.each do |u|
      result[u] = result[u].keys.join(', ')
      say "#{u.bold} =>\n  #{result[u]}"
    end

  elsif action == 'oo:resources:transfer'
    resources = execute!('oo:resources')
    resources.each_pair {|name, desc| execute('resources:add', name, desc)}

  elsif action == 'oo:subs'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)
    provides_rels = %w(Azure Openstack).inject([]) do |a, clazz|
      a += oo_request("relations?nsPath=/#{org_name}/_clouds&recursive=true&relationShortName=Provides&targetClassName=#{clazz}&includeFromCi=true&includeToCi=true", 'Getting clouds')
    end

    result = provides_rels.inject({}) do |h, rel|
      sub_or_tenant = rel.toCi.ciAttributes['subscription'] || rel.toCi.ciAttributes['tenant']
      (h["#{rel.fromCi.ciAttributes.location.split('/').last}:#{sub_or_tenant}"] ||= []) << rel unless sub_or_tenant.empty?
      h
    end

    cloud_regex = /#{cloud_name}/i
    result.select! {|sub, rels| rels.any? {|rel| rel.fromCi.ciName =~ cloud_regex}}
    result.keys.sort.each {|s| say "#{s.bold} =>\n  #{result[s].map {|rel| rel.fromCi.ciName}.join(', ')}"}

  elsif action == 'oo:subs:transfer'
    subs = execute!('oo:subs', *args)
    subs.each_pair {|name, _| execute('subs:add', name, name.split(':').first)}
    execute('resources')

  elsif action == 'oo:sub:usage'
    sub_or_tenant, _ = args
    required_arg('subscription', sub_or_tenant)
    sub_or_tenant = sub_or_tenant.split(':').last
    cloud_services = {subscription: 'Azure', tenant: 'Openstack'}.inject([]) do |a, (attr_name, clazz)|
      a += oo_request("cis?nsPath=/&recursive=true&ciClassName=#{clazz}&attr=#{attr_name}:eq:#{sub_or_tenant}", 'Getting clouds')
    end
    result = {:oneops => {}, :tekton => {}}
    cloud_services.sort_by(&:nsPath).each do |cs|
      _, org, _, cloud, _ = cs.nsPath.split('/')
      next if org == 'public'
      org_usage = execute('oo:usage', org, cloud)
      result[:oneops][org] = org_usage[:oneops].values.first || {}
      result[:tekton][org] = org_usage[:tekton].values.first || {}
    end

    totals = result.keys.inject({}) do |h, source|
      h[source] = result[source].values.inject({}) {|hh, usage| hh.update(usage) {|__, value1, value2| value1 + value2}}
      h
    end
    say '=========================================================='
    usage("#{sub_or_tenant.bold} => #{'TOTAL'.bold}", totals[:oneops], totals[:tekton])

  elsif action == 'oo:usage'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)

    mappings = fetch_provider_mappings
    result = {:oneops => {}, :tekton => {}}
    subs = execute!('oo:subs', org_name, cloud_name)
    subs.keys.sort.each do |sub|
      provides_rels = subs[sub]
      say "Processing clouds for subscription '#{sub.bold}' (#{provides_rels.map {|u| u.fromCi.ciName}.join(', )')})" if @params.verbose > 0
      provider = provides_rels.first.toCi.ciClassName.split('.').last.downcase
      provider_mappings = mappings[provider]
      if provider_mappings.empty?
        say "  No mappings found for provider #{provider}".yellow
        next
      end

      oo_usage = {}
      cloud = nil
      provider_mappings.each_pair do |ci_class_name, class_mappings|
        provides_rels.each do |rel|
          cloud = rel.fromCi
          say "  Processing #{ci_class_name} for cloud '#{cloud.ciName}'"  if @params.verbose > 0
          deployed_tos = oo_request("relations?ciId=#{cloud.ciId}&direction=to&relationShortName=DeployedTo&targetClassName=#{ci_class_name.capitalize}&includeFromCi=true", "    Getting #{ci_class_name} for cloud '#{cloud.ciName}'")
          say "    Found #{deployed_tos.size} #{ci_class_name} instances."  if @params.verbose > 1
          deployed_tos.each do |u|
            ci = u.fromCi
            class_mappings.each_pair do |attr_name, attr_mappings|
              resources = nil
              attr_value = ci.ciAttributes[attr_name]
              resources = attr_mappings[attr_value] unless attr_value.empty?
              resources = attr_mappings['*'] if resources.empty?
              next if resources.empty?
              resources.each_pair do |resource, value|
                oo_usage[resource] = (oo_usage[resource] || 0) + value
              end
            end
          end
        end
      end

      result[:oneops][sub] = oo_usage
      tt_usage = tt_request("quota/usage/#{sub}/#{org_name}", 'Getting usage')
      result[:tekton][sub] = tt_usage
      has_diff = usage("#{sub.bold} => #{org_name.bold}", oo_usage, tt_usage)
      if has_diff
        say "  Fix this with:\n    #{"oo:usage:transfer -f #{org_name} #{cloud.ciName}".yellow}"
      end
      say
    end

  elsif action == 'oo:usage:transfer'
    org_name, cloud_name, _ = args
    required_arg('org', org_name)

    usage = execute!('oo:usage', org_name, cloud_name)[:oneops]
    usage.each_pair do |sub, sub_usage|
      next if sub_usage.empty?
      limits = tt_request("quota/#{sub}/#{org_name}", 'Getting quota')
      if limits.empty? || @params.force
        if limits.empty?
          if @params.transfer_buffer < 0
            say 'Invalid quota buffer %'.red
            action_help(@action)
          elsif @params.transfer_buffer == 0
            say 'Buffer % is not set, will set total quota to the same value as usage.'.yellow
          end
          execute!('quota:set', sub, org_name, *sub_usage.to_a.map {|u, v| "#{u}=#{(v + v * @params.transfer_buffer).to_i}"}) if @params.transfer_buffer > 0
        else
          say "Quota for #{sub.bold} in #{org_name.bold} already exists, will set usage only, use #{"'quota:set'".blue} action to set total quota values.".yellow
          sub_usage.delete_if {|u| limits[u]} if @params.only_missing
        end
        execute('quota:usage:set', sub, org_name, *sub_usage.to_a.map {|u, v| "#{u}=#{v}"})
      else
        say "Quota for #{sub.bold} in #{org_name.bold} already exists, use '-f' option to force update.".yellow
      end
      say
    end

  elsif action == 'resources'
    resources = tt_request('resource', 'Fetching resources')
    resources.sort_by(&:name).each do |u|
      say "#{u.name.bold} =>\n  #{u.description}"
    end

  elsif action == 'resources:add'
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
      quota = tt_request("quota/#{s.name}/%2f", 'Fetching hard quota')
      sub_quota(s, quota)
    end

  elsif action == 'subs:add'
    sub_name, desc, _ = args
    sub = tt_request("subscription/#{sub_name}", "Fetching subscription '#{sub_name}'")
    if sub
      if @params.force
        tt_request('subscription', "Subscription #{sub_name.bold} already exists, updating...", {:name => sub_name, :description => desc || sub_name})
      else
        say "Subscription #{sub_name.bold} already exists, use '-f' option to force update.".yellow
      end
    else
      tt_request('subscription', "Subscription #{sub_name.bold} is missing, creating...", {:name => sub_name, :description => desc || sub_name})
    end

  elsif action == 'subs:set'
    sub_name, *resources = args
    required_arg('subscription', sub_name)
    usage = resources && resources.inject({}) do |h, u|
      name, value = u.split(/[=:]/, 2)
      value = value.to_i
      h[name] = value if !name.empty? && value > 0
      h
    end
    required_arg('resources', usage)

    subs = execute('sub:prompt', sub_name)
    subs.each do |s|
      sub = tt_request("subscription/#{s}", "Fetching subscription '#{s}'")
      if sub
        tt_request("quota/hard/subscription/#{s}", 'Updating hard quota', usage)
        quota = tt_request("quota/#{s}/%2f", 'Fetching hard quota')
        sub_quota(sub, quota)
      else
        say "Subscription #{sub.bold} does not exist".red
      end
    end

  elsif action == 'orgs'
    org_regex = args[0]
    result = tt_request('org', 'Fetching orgs')
    unless org_regex.empty?
      org_regex = /#{org_regex}/
      result = result.select {|u| u.name =~ org_regex}
    end
    result.sort_by(&:name).each {|u| say u.name}

  elsif action == 'orgs:add'
    org_name = args[0]
    org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
    tt_request('org', "Org #{org_name} is missing, creating", {:name => org_name}) unless org

  elsif action == 'teams'
    org_name, team_regex, _ = args
    required_arg('org', org_name)

    result = tt_request("org/#{org_name}/team", 'Fetching teams')
    unless team_regex.empty?
      team_regex = /#{team_regex}/
      result = result.select {|u| u.name =~ team_regex}
    end
    result.sort_by(&:name).each {|u| say "#{u.name.bold} =>\n  #{u.description}"}

  elsif action == 'teams:add'
    org_name, name, desc, _ = args
    required_arg('org', org_name)
    required_arg('team_name', name)
    org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
    if org
      team = tt_request("org/#{org_name}/team/#{name}", 'Fetching team')
      if team.empty?
        team = {:orgName => org.name, :name => name, :description => desc}
        tt_request("org/#{org_name}/team", "Team #{name} is missing, creating", team)
      else
        team['name'] = name
        team['description'] = desc unless desc.empty?
        tt_request("org/#{org_name}/team", "Updating team #{name}", team)
      end
      say "#{team.name.bold} =>\n  #{team.description}"
    else
      say "Org #{org_name} not found".red
    end

  elsif action == 'teams:remove'
    org_name, team_name, desc, _ = args
    required_arg('org', org_name)
    required_arg('team_name', team_name)
    ok = tt_request("org/#{org_name}/team/#{team_name}", "Deleting #{team_name} fro org #{org_name}", {}, 'DELETE')
    say ok['ok'] ? 'Removed'.green : 'Failed'.red

  elsif action == 'users'
    org_name, team_name, _ = args
    required_arg('org', org_name)
    required_arg('team', team_name)

    result = tt_request("org/#{org_name}/team/#{team_name}/users", 'Fetching users')
    result.sort_by(&:username).each {|u| say "#{u.username.ljust(25).bold} |#{' team admin '.blue(true) if u.role == 'MAINTAINER'}"} if result

  elsif action == 'users:add'
    org_name, team_name, *usernames = args
    required_arg('username', usernames)
    users = usernames.map {|u| {:username => u}}
    execute('users:add:internal', org_name, team_name, users)

  elsif action == 'admins:add'
    org_name, team_name, *usernames = args
    required_arg('username', usernames)
    admins = usernames.map {|u| {:username => u, :role => 'MAINTAINER'}}
    execute('users:add:internal', org_name, team_name, admins)

  elsif action == 'users:add:internal'
    org_name, team_name, users = args
    required_arg('org', org_name)
    required_arg('team', team_name)
    ok = tt_request("org/#{org_name}/team/#{team_name}/users", "Adding #{usernames} to team #{team}", users, 'PUT')
    say ok['ok'] ? 'Added'.green : 'Failed'.red

  elsif action == 'users:remove'
    org_name, team_name, *usernames = args
    required_arg('org', org_name)
    required_arg('team', team_name)
    required_arg('username', usernames)
    ok = tt_request("org/#{org_name}/team/#{team_name}/users", "Deleting #{usernames} fro team #{team}", usernames, 'DELETE')
    say ok['ok'] ? 'Removed'.green : 'Failed'.red

  elsif action == 'sub:quotas'
    sub_name = args[0]
    required_arg('subscription', sub_name)
    limits = tt_request("quota/subscription/#{sub_name}", 'Getting quotas')
    usage = tt_request("quota/usage/subscription/#{sub_name}", 'Getting usages')
    available = tt_request("quota/available/subscription/#{sub_name}", 'Getting usages')
    limits.keys.sort.each {|org| full_quota(org, limits[org], usage[org], available[org])} unless limits.empty?

  elsif action == 'org:quotas'
    org = args[0]
    required_arg('org', org)
    orgs = execute('org:prompt', nil, org)
    orgs.each do |org_name|
      limits = tt_request("quota/entity/#{org_name}", 'Getting quotas')
      usage = tt_request("quota/usage/entity/#{org_name}", 'Getting usages')
      available = tt_request("quota/available/entity/#{org_name}", 'Getting usages')
      limits.keys.sort.each {|sub| full_quota("#{org_name.bold} => #{sub.bold}", limits[sub], usage[sub], available[sub], @params.depleted_threshold)} unless limits.empty?
    end

  elsif action == 'quota'
    sub_name, org_name, _ = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)

    cursor_control = ''
    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      orgs = execute('org:prompt', s, org_name)
      orgs.each do |u|
        while (true)
          limits = tt_request("quota/#{s}/#{u}", 'Getting quota')
          unless limits.empty?
            usage = tt_request("quota/usage/#{s}/#{u}", 'Getting usage')
            available = tt_request("quota/available/#{s}/#{u}", 'Getting available')
            full_quota("#{cursor_control}#{s.bold} => #{u.bold}", limits, usage, available)
          end
          break if @params.refresh == 0 || subs.size > 1 || orgs.size > 1
          begin
            sleep(@params.refresh)
          rescue Interrupt
            exit
          end
          cursor_control = "\e[#{limits.size + 4}A\r"
        end
      end
    end

  elsif action == 'quota:set' || action == 'quota:usage:set' || action == 'usage:set'
    sub_name, org_name, *resources = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)
    values = resources && resources.inject({}) do |h, u|
      name, _ = u.split(/[+-]?[=]/, 2)
      expr = u[name.size..-1]
      h[name] = expr unless name.empty? || expr.empty?
      h
    end
    required_arg('resources', values)

    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      orgs = execute('org:prompt', s, org_name)
      orgs.each do |o|

        sub = tt_request("subscription/#{s}", "Fetching subscription '#{s}'")
        execute!('subs:add', s) unless sub

        org = tt_request("org/#{o}", "Fetching org '#{o}'")
        execute!('orgs:add', o) unless org

        current = tt_request("quota/#{"usage/" if action.include?('usage:set')}#{s}/#{o}", 'Getting quota')
        resolved_values = values.inject({}) do |h, (resource, expr)|
          if expr[0] == '='
            value = expr[1..-1].to_i
            h[resource] = value if value > 0
          elsif expr[0..1] == '+=' || expr[0..1] == '-='
            current_value = current[resource].to_i
            if expr[-1] == '%'
              next h unless current_value > 0
              value = expr[2..-2].to_i * current_value / 100
            else
              value = expr[2..-1].to_i
            end
            next h unless value > 0
            if expr[0] == '-'
              h[resource] = [current[resource] - value, 0].max
            else
              h[resource] = current[resource] + value
            end
          end
          h
        end
        tt_request("quota/#{"usage/" if action.include?('usage:set')}#{s}/#{o}", 'Updating quota', resolved_values) unless resolved_values.empty?
        execute('quota', s, o)
      end
    end

  elsif action == 'sub:prompt'
    sub_name, org_name, _ = args
    if sub_name[0] == '?'
      if org_name.empty? || org_name[0] == '?'
        subs = tt_request('subscription', 'Fetching subscriptions').map(&:name)
      else
        quotas = tt_request("quota/entity/#{org_name}", 'Getting quotas')
        if quotas.empty?
          say "No existing quotas set up in org '#{org_name}'".red
          exit(1)
        end
        subs = quotas.keys
      end

      sub_pattern = sub_name[1..-1]
      unless sub_pattern.empty?
        sub_regex = /#{sub_pattern}/i
        subs = subs.select {|s| s =~ sub_regex}
        if subs.empty?
          if org_name[0] == '?'
            say "No existing subscriptions matching '#{sub_pattern}'".yellow
          else
            say "No existing quotas in org '#{org_name}' have subscriptions matching '#{sub_pattern}'".yellow
          end
          exit(1)
        end
      end

      if subs.size == 1
        result = subs
      else
        subs.sort!
        say "Choose one or more subscriptions:"
        subs.each_with_index {|sub, i| say "  #{"#{i + 1}.".to_s.ljust(3)} #{sub}"}
        blurt "Which subscription (n|n,m...|*): ".green
        p = ask
        say
        if p == '*'
          result = subs
        else
          result = []
          p.split(',').each do |u|
            index = u.to_i
            result << subs[index - 1] if index >= 1 && index <= subs.size
          end
        end
        result = result.uniq
        exit if result.empty?
      end
    else
      result = sub_name.split(',').uniq
    end

  elsif action == 'org:prompt'
    sub_name, org_name, _ = args
    if org_name[0] == '?'
      if sub_name.empty?
        orgs = tt_request('org', 'Fetching orgs').map(&:name)
      else
        quotas = tt_request("quota/subscription/#{sub_name}", 'Getting quotas')
        if quotas.empty?
          say "No existing quotas set up for subscription '#{sub_name}'".red
          exit(1)
        end
        orgs = quotas.keys
      end

      org_pattern = org_name[1..-1]
      unless org_pattern.empty?
        org_regex = /#{org_pattern}/i
        orgs = orgs.select {|s| s =~ org_regex}
        if orgs.empty?
          say "No existing quotas for subscription '#{sub_name}' have orgs matching '#{org_pattern}'".yellow
          exit(1)
        end
      end

      if orgs.size == 1
        result = orgs
      else
        orgs = orgs.sort!
        say "Choose one or more orgs:"
        orgs.each_with_index {|org, i| say "  #{"#{i + 1}.".to_s.ljust(3)} #{org}"}
        blurt "Which org (n|n,m...|*): ".green
        p = ask
        say
        if p == '*'
          result = orgs
        else
          result = []
          p.split(',').each do |u|
            index = u.to_i
            result << orgs[index - 1] if index >= 1 && index <= orgs.size
          end
        end
        result = result.uniq
        exit if result.empty?
      end
    elsif org_name[0] == '*'
      result = tt_request('org', 'Fetching orgs').map(&:name)
      org_pattern = org_name[1..-1]
      unless org_pattern.empty?
        org_regex = /#{org_pattern}/i
        result = result.select {|s| s =~ org_regex}
      end
    else
      result = org_name.split(',').uniq
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
      say
    end

    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

    set_tekton_auth(username, password)
    result = !tt_request('org', 'Checking credentials').empty?
    if result
      cfg = %w(tekton_host oneops_host tekton_auth).inject({}) {|h, key| h[key] = @params[key]; h}
      File.write(SESSION_FILE_NAME, JSON.pretty_unparse(cfg)) ##unless @repl
      say "Signed in - do not forget to logout when done!".green
    end

  elsif action == 'logout'
    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

  elsif action == 'help'
    action = match_action(args[0])
    action.empty? ? ('actions'.start_with?(args[0]) ? say(actions_help) : general_help) : action_help(action)

  else
    say "Unknown action: #{action}".red
    exit(1)
  end

  return result
end

def match_action(action)
  return nil if action.empty?
  regex = /#{action.gsub(/\W+/, '\w*:')}\w*$/
  @actions.keys.find {|k| (regex =~ k) == 0}
end

def run(args)
  @action, *@args = @opt_parser.parse(args)

  if @action.empty?
    general_help if @show_help

    say 'Specify ACTION!'.red
    say "#{actions_help}\nUse '-h' or 'help' action to see full help."
    exit(1)
  end

  @action = @action.downcase.gsub('oneops', 'oo')
  unless @actions.include?(@action)
    action = match_action(@action)
    @action = action if action
  end
  action_help(@action) if @show_help

  if @action != 'login' && File.exist?(SESSION_FILE_NAME) && %w(tekton_host oneops_host tekton_auth).none? {|key| @params[key]}
    cfg = JSON.parse(File.read(SESSION_FILE_NAME))
    %w(tekton_host oneops_host tekton_auth).each {|key| @params[key.to_sym] = cfg[key] unless cfg[key].empty?}
  end

  if @params.tekton_auth.empty? && @action != 'login' && @action != 'logout' && @action != 'help'
    say "Specify tekton auth with '--ta' option or use 'login' command.".red
    action_help('login')
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
end

@actions = {
  'help'                  => ['help [actions|ACTION]', 'display help: full or actions or specific acdtion'],
  'login'                 => ['login [-e ENV] [USERNAME [PASSWORD]]', 'log in for running Tekton commands, defaults to \'prod\' environment if not specified'],
  'logout'                => ['logout [USERNAME [PASSWORD]]', 'log out after running Tekton commands'],

  'orgs'                  => ['orgs [ORG_REGEX]', 'list orgs'],
  'orgs:add'              => ['orgs:add ORG', 'add org'],
  'org:quotas'            => ['org:quotas ORG [--depleted [THRESHOLD_%]]', 'list all quotas for org'],

  'teams'                 => ['teams ORG [TEAM_REGEX]', 'list teams'],
  'teams:add'             => ['teams:add ORG TEAM_NAME [TEAM_DESCRIPTION]', 'add team'],
#  'teams:remove'           => ['teams:add ORG TEAM_NAME', 'remove team'],

  'users'                 => ['users ORG TEAM [TEAM_REGEX]', 'list team users'],
  'users:add'             => ['users:add ORG TEAM USERNAME...', 'add users to team'],
  'admins:add'            => ['users:add ORG TEAM USERNAME...', 'add team admins to team'],
  'users:remove'          => ['users:remove ORG TEAM USERNAME...', 'remove users from team'],

  'resources'             => ['resources', 'list resource types'],
  'resources:add'         => ['resources:add  [-f]', 'add resource type'],

  'subs'                  => ['subs', 'list subsctiptions (including hard quota)'],
  'subs:add'              => ['subs:add [-f] SUBSCRIPTION', 'add subscription'],
  'subs:set'              => ['subs:set SUBSCRIPTION RESOURCE=VALUE...', 'set subscription limits (hard quota)'],
  'sub:quotas'            => ['sub:quotas SUBSCRIPTION', 'list all quotas for subscription'],

  'quota'                 => ['quota SUBSCRIPTION ORG', 'show quota'],
  'quota:set'             => ['quota:set SUBSCRIPTION ORG RESOURCE[+|-]=VALUE[%]...', "update quota limits: directly set with '=' or increment with '+=' or decrement with '-='; specify absolute value or percentage of current value with '%'"],
  'quota:usage:set'       => ['quota:usage:set SUBSCRIPTION ORG RESOURCE=VALUE...', 'update quota limits'],

  'oo:resources'          => ['oo:resources', 'list resource types in OneOps'],
  'oo:resources:transfer' => ['oo:resources:transfer [-f]', 'transfer resources types in OneOps to Tekton'],
  'oo:subs'               => ['oo:subs ORG [CLOUD_REGEX]', 'list subsctiptions in OneOps'],
  'oo:subs:transfer'      => ['oo:subs:transfer  [-f] ORG [CLOUD_REGEX]', 'transfer subsctiptions in OneOps to Tekton'],
  'oo:sub:usage'          => ['oo:sub:usage SUBSCRIPTION', 'list usage for a given subscription for all orgs in OneOps'],
  'oo:usage'              => ['oo:usage ORG [CLOUD_REGEX]', 'list usage in OneOps and compares with usage in Tekton'],
  'oo:usage:transfer'     => ['oo:usage:transfer [-f [--omr]] [-b BUFFER_%] ORG [CLOUD_REGEX]', 'convert current usage in OneOps into quota in Tekton or update usage for existing Tekton quota with the current ussage in OneOps']
}

@usage = <<-USAGE
#{'Usage:'.bold}
    #{__FILE__} [OPTIONS] ACTION ARGUMENTS
    
#{actions_help}
USAGE

@footer = <<-FOOTER
  #{'Modes:'.bold}
    You can use this tool in either of these 3 modes:
    1. Interactive mode: run this tool with no parameters to start a REPL console to execute a serious of action
       commands. The console has support for command history, auto-completion, help.  Advantages: less typing,
       better performance, more intuitive.
    2. Command line mode without auth session: run this tool for individual actions as a single bash command.
       Must use '--ta' in each command to specify the authentication credentials (except for some actions, e.g. 'help').
       Advantages: automic, good for scripting.
    3. Command line mode with auth session: run this tool with 'login' action to authenticate and start a session
       for the subsequent commands (unitl next 'logout' action command), then issue individual actions command as needed
       under the umbrella of current auth session.  For security reasons, remember to run 'loguot' action command when done.

  #{'Typical flow:'.bold}
    1. Login to run a series of commands (alternatively, you will need to provide auth on each subsequent
       command with '--ta' option):
         #{__FILE__} login my_username
       
    2. Import resources to Tekton from OneOps based on existing provider mappings
       (normally, done during initial seeding only or when new resource types are added):
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
__COLOR = true
@params = OpenStruct.new(:verbose => 0, :force => false, :only_missing => false, :transfer_buffer => 0, :depleted_threshold => nil, :refresh => 0)

@show_help = false
@opt_parser = OptionParser.new do |opts|
  opts.banner = <<-HELP
  Tool to query and to configure subscription, org and soft quota data in Tekton directly or based on current usage in OneOps.
  
  #{@usage}

  #{'Options:'.bold}
  HELP

  opts.on('-e',   '--environment ENV', [:prod, :stg, :dev, :local], 'Environment to use: prod|stg|dev|local') {|host| @params.tekton_host = host; @params.oneops_host = host}
  opts.on('--th', '--tekton-host HOST', "Tekton host, defaults to '#{TEKTON_HOSTS[:default]}' if no host or environment options are specified") {|host| @params.tekton_host = host}
  opts.on('--ta', '--tekton-auth CREDENTIALS', "Tekton auth if not using 'login' command: <username<:<password>  or <token>") {|creds| set_tekton_auth(*creds.split(':'))}
  opts.on('--oh', '--oneops-host HOST', "OneOps CMS host, defaults to '#{ONEOPS_HOSTS[:default]}' if no host or environment options are specified") {|host| @params.oneops_host = host}

  opts.on('-b', '--buffer BUFFER_%', 'Buffer (as a percentage of usage) to add on top of usage number when setting quota limit value for new quotas. Default value is 0 (no buffer) - usage and limit are the same') {|pct| @params.transfer_buffer = pct.to_f / 100}
  opts.on('--depleted [THRESHOLD_%]', 'Show depleted quotas only (zero available or less than threshold %)') {|pct| @params.depleted_threshold = pct.to_f / 100}

  opts.on('-r', '--refresh [SECONDS]', 'Refresh interval for quota commands') {|i| @params.refresh = i.to_i; @params.refresh = 10 if @params.refresh == 0}

  opts.on('-f', '--force', 'Force update if already exists') {@params.force = true}
  opts.on('--omr', '--only-missing-resources', "Transfer usage only for resources missing quota (when there is already quota set up for at least one other resource (for a given subscription and org); use with '-f' option") {@params.only_missing = true}

  opts.on('--no-color', 'No output coloring or font formatting.') {String.with_color = false}
  opts.on('-v', '--verbose', 'Verbose') {@params.verbose = 1}
  opts.on('--vv', '--very-verbose', 'Very verbose') {@params.verbose = 2}

  opts.on('-h', '--help', 'Show this message') {@show_help = true}
end

@repl = ARGV.size == 0
if @repl
  actions = @actions.keys
  Readline.completer_word_break_characters = "\n"
  Readline.completion_proc = lambda do |s|
    split = s.split(/\s+/, 2)
    if split.size == 1 && s[-1] != ' '
      actions.grep(/^#{Regexp.escape(s)}/)
    else
      action = @actions[split.first]
      action ? ["Usage: #{action[0]}", ' ' * 100] : []
    end
  end

  if File.exist?(HISTORY_FILE_NAME)
    File.read(HISTORY_FILE_NAME).split("\n").each {|c| Readline::HISTORY << c}
  end

  while input = Readline.readline('>>> '.bold, true).strip
    Readline::HISTORY.pop if input.empty?
    if input.start_with?('hist')
      say Readline::HISTORY.to_a
      next
    end
    input.split(';').each do |command|
      if command == 'exit' || command == 'quit'
        File.write(HISTORY_FILE_NAME, Readline::HISTORY.to_a[[Readline::HISTORY.size - 1000, 0].max..-1].join("\n"))
        exit
      end

      begin
        @show_help = false
        @params.verbose = 0
        @params.force = false
        @params.only_missing = false
        @params.transfer_buffer = 0
        @params.refresh = 0
        run(command.strip.split(/\s+/))
      rescue SystemExit
      end
    end
  end
else
  run(ARGV)
end
