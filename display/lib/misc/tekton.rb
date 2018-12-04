#!/usr/bin/env ruby
require 'json'
require 'optparse'
require 'ostruct'
require 'base64'
require 'net/http'
require 'io/console'
require 'readline'

SESSION_FILE_NAME  = "#{Dir.home}/.tekton"
HISTORY_FILE_NAME  = "#{Dir.home}/.tekton_hist"

@use_session = false   #  Set to false and swtich to FQDN to disable session auth.

TEKTON_HOSTS = {:local => 'http://localhost:9000',
                :dev   => 'http://tekton.dev.prod.walmart.com',
                :stg   => 'http://tekton.stg.prod.walmart.com:9000',
                # :prod  => 'http://10.120.220.37:9000'}   #  secondary
                :prod  => 'http://tekton.prod0718.walmart.com'}
TEKTON_HOSTS[:default] = TEKTON_HOSTS[:prod]

ONEOPS_HOSTS = {:local => 'http://localhost:8080/',
                :dev   => 'http://adapter.stg.core-1612.oneops.prod.walmart.com:8080',
                :stg   => 'http://adapter.stg.core-1612.oneops.prod.walmart.com:8080',
                :prod  => 'http://cmsapi.prod-1312.core.oneops.prod.walmart.com:8080'}
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

  [[:bold, "\e[1m"], [:invert, "\e[7m"], [:red, "\e[31m"], [:green, "\e[32m"], [:yellow, "\e[33m"], [:blue, "\e[34m"]].each do |(name, code)|
    define_method(name) {|background = false|
      @@with_color ? "\e[0m#{"\e[7m" if background}#{code}#{self}\e[0m" : self
    }
    define_method("#{name}!".to_sym) {|background = false|
      @@with_color ? "#{"\e[7m" if background}#{code}#{self}" : self
    }
  end

  def terminate_with(string)
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

def ask
  STDIN.gets[0...-1]
end

def oo_request(cmd, msg)
  host = @params.oneops_host
  host = host.gsub('cmsapi.', 'transistor.') if cmd.start_with?('transistor')
  uri = URI("#{host}#{cmd}")
  req = Net::HTTP::Get.new(uri)
  result, response = request(uri, req, msg)
  return result
end

def tt_request(cmd, msg, body = nil, method = 'POST')
  uri = URI(cmd.start_with?('http') ? cmd : "#{@params.tekton_host}#{cmd}")
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
    say "#{req.class.name.split('::').last.upcase.bold} #{uri.to_s.blue}"
  elsif @params.verbose > 0
    blurt msg, '... '
  end
  begin
    req['Content-Type'] = 'application/json'
    say "REQUEST: #{req.body}" if @params.verbose > 1 && !req.body.empty?
    response = Net::HTTP.start(uri.host, uri.port, :use_ssl => uri.scheme == 'https') {|http| http.request(req)}
    code = response.respond_to?(:code) ? response.code : response.class.name
    blurt "RESPONSE: #{code == '200' ? code.green : code.red} " if @params.verbose > 1
    if response.is_a?(Net::HTTPSuccess)
      body = response.body
      unless body.empty?
        say response.body if @params.verbose > 1
        result = JSON.parse(body)
      end
    elsif response.is_a?(Net::HTTPNotFound)
    elsif response.is_a?(Net::HTTPUnauthorized)
      say 'Invalid credentials, please login'.red
      @params.tekton_host = nil
      @params.oneops_host = nil
      @params.tekton_auth = nil
      @tt_cookie = nil
      exit(1)
    elsif response.is_a?(Net::HTTPClientError) || response.is_a?(Net::HTTPServerError)
      body = response.respond_to?(:body) ? response.body : ''
      say "Failed: #{body}".red
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

def v2_url
  @params.tekton_host.gsub(/api\/v\d+\//, 'api/v2/')
end

def set_tekton_auth(username, password = nil)
  @tt_cookie = nil
  @params.tekton_auth = if password.empty?
                          Base64.strict_encode64(username)
                        elsif username.empty?
                          Base64.strict_encode64(password)
                        else
                          "#{'Basic ' unless password.empty?}#{Base64.strict_encode64("#{username}:#{password}")}"
                        end
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

def value_prompt(name, values, pattern)
  if pattern.empty?
    prompt_values = values
  else
    pattern = '.*' if pattern == '*'
    regex = /#{pattern}/i
    prompt_values = values.select {|s| s =~ regex}
    if prompt_values.empty?
      say "No existing #{name} matching '#{pattern}'".yellow
      return []
    end
  end

  return prompt_values if prompt_values.size == 1
  prompt_values.sort!
  say "Choose one or more #{name}:"
  prompt_values.each_with_index {|sub, i| say "  #{"#{i + 1}.".to_s.ljust(3)} #{sub}"}
  blurt "Which #{name} (n|n,m...|*): ".green
  p = ask
  say
  if p == '*'
    result = prompt_values
  else
    result = []
    p.split(',').each do |u|
      index = u.to_i
      result << prompt_values[index - 1] if index >= 1 && index <= prompt_values.size
    end
  end
  result.uniq
end

def value_match(pattern, values)
  return values if pattern.empty?
  regex = /#{pattern}/i
  values.select {|s| s =~ regex}
end

def sub_quota(sub, limits)
  say "#{sub.name.bold} #{"(#{sub.description})" unless sub.name == sub.description}"
  return if limits.empty?

  say '  Resource                      | Limit     '
  say '  ------------------------------|-----------'
  unless limits.empty?
    limits.keys.sort.each do |n|
      say "  #{n.ljust(30)}|#{limits[n].to_i.to_s.rjust(10).blue}"
    end
  end
  say
end

def show_quota(title, quota, available_threshold = nil)
  if available_threshold
    quotas = quota.select do |resource, r_quota|
      r_limit = r_quota.limit.to_f
      r_avail = r_quota.available.to_f
      r_limit <= 0 || (r_avail / r_limit) <= available_threshold
    end
  else
    quotas = quota
  end
  return if quotas.empty?

  say "#{title}"
  say '  Resource                      | Used      | Reserved  | Available | Limit     '
  say '  ------------------------------|-----------|-----------|-----------|-----------'
  quotas.keys.sort.each do |resource|
    r_quota = quota[resource]
    say "  #{resource.ljust(30)}|#{r_quota.used.to_s.rjust(10)} |#{r_quota.reserved.to_s.rjust(10)} |#{r_quota.available.to_s.rjust(10).green} |#{r_quota.limit.to_s.rjust(10).blue}"
  end
  say
end

def show_quota_totals(title, quotas)
  return if quotas.empty?
  totals = quotas.values.inject({}) do |h, group_quotas|
    group_quotas.each_pair do |resource, resource_quota|
      h[resource] ||= {}
      h[resource].update(resource_quota) {|__, value1, value2| value1.to_i + value2.to_i}
    end
    h
  end
  say '=' * 80
  show_quota(title, totals)
end

def usage(title, oo_usage, tt_usage, diff_only = false)
  oo_usage ||= {}
  tt_usage ||= {}
  diffs = []
  output = "#{title}\n"
  output += "  Resource                      | OneOps    | Tekton    | Diff      \n"
  output += "  ------------------------------|-----------|-----------|-----------\n"
  (oo_usage.keys + tt_usage.keys).uniq.sort.each do |r|
    u1   = oo_usage[r].to_i
    u2   = tt_usage[r]
    diff = u2 ? (u1 - u2) : nil
    line = "#{r.ljust(30)}|#{u1.to_s.rjust(10)} |#{u2.to_s.rjust(10)} |#{diff.to_s.rjust(10)}"

    has_resource_diff = diff && diff != 0
    output += "  #{has_resource_diff ? line.red(true) : line}\n"
    diffs << r if has_resource_diff
  end
  say output unless diff_only && diffs.empty?
  return diffs.empty? ? nil : diffs
end

def fetch_provider_mappings
  return @mappings if @mappings
  @mappings = oo_request('adapter/rest/cm/simple/CLOUD_PROVIDER_MAPPINGS/vars', 'Getting mappings')
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
  if action == 'version'
    say 'CLI version:    1.2.4'
    info = tt_request('server/version', 'Getting tekton version')
    say "Tekton version: #{info.version} (#{info.timestamp})"
    say "Tekton host:    #{@params.tekton_host}"
    say "OneOps host:    #{@params.oneops_host}"

  elsif action == 'oo:resources'
    result = {}
    mappings = fetch_provider_mappings
    mappings.each_pair do |provider, provider_mappings|
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

  elsif action == 'oo:resource:mappings'
    say JSON.pretty_unparse(fetch_provider_mappings)

  elsif action == 'oo:resources:transfer'
    resources = execute!('oo:resources')
    resources.each_pair {|name, desc| execute('resources:add', name, desc)}

  elsif action == 'oo:subs'
    org, cloud_name, _ = args
    required_arg('org', org)
    provides_rels = %w(Azure Openstack).inject([]) do |a, clazz|
      a += oo_request("adapter/rest/cm/simple/relations?nsPath=/#{org}/_clouds&recursive=true&relationShortName=Provides&targetClassName=#{clazz}&includeFromCi=true&includeToCi=true", 'Getting clouds')
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
    execute('subs')

  elsif action == 'oo:sub:usage'
    sub_name, orgs, _ = args
    required_arg('subscription', sub_name)
    fix = @params.fix_mismatch
    mismatch_only = @params.mismatch_only
    result = {}
    subs = execute('sub:prompt', sub_name)
    org_names = orgs.empty? ? nil : orgs.split(',').inject({}) {|h, o| h[o] = o; h}
    subs.each do |sub|
      blurt("#{sub}\r") if mismatch_only
      tt_usage = tt_request("quota/usage/subscription/#{sub}", 'Getting usages in Tekton')
      sub_or_tenant = sub.split(':').last
      cloud_services = {subscription: 'Azure', tenant: 'Openstack'}.inject([]) do |a, (attr_name, clazz)|
        a += oo_request("adapter/rest/cm/simple/cis?nsPath=/&recursive=true&ciClassName=#{clazz}&attr=#{attr_name}:eq:#{sub_or_tenant}", 'Getting clouds')
      end
      result[sub] = {:oneops => {}, :tekton => {}}
      clouds_by_org = cloud_services.inject({}) do |h, cs|
        _, org, _, cloud, _ = cs.nsPath.split('/')
        next h if org == 'public' || (org_names && !org_names[org])
        cloud = oo_request("adapter/rest/cm/simple/cis?nsPath=/#{org}/_clouds&ciName=#{cloud}", "Loading cloud CI for #{cloud}").first
        (h[org] ||=[]) << cloud if sub == "#{cloud.ciAttributes.location.split('/').last}:#{sub_or_tenant}"
        h
      end
      clouds_by_org.keys.sort.each do |org|
        blurt("#{"#{sub} => #{org}"}\r") if mismatch_only
        clouds = clouds_by_org[org]
        oo_org_usage = execute('oo:cloud:usage:internal', clouds)
        tt_org_usage = tt_usage[org] || {}
        diff = usage("#{sub.bold} => #{org.bold}", oo_org_usage, tt_org_usage, mismatch_only)
        if mismatch_only && !diff
          blurt("#{' ' * (sub.size + org.size + 5)} \r")
          next
        end
        result[sub][:oneops][org] = oo_org_usage
        result[sub][:tekton][org] = tt_org_usage
        if diff && fix
          resources = diff.map {|r| "#{r}=#{oo_org_usage[r]}"}
          say "  Fixing usage mismatch with:\n    #{"usage:set #{sub} #{org} #{resources.join(' ')}".yellow}\n"
          execute('usage:set', sub, org, *resources)
        end
        say
      end

      unless mismatch_only
        totals = result[sub].inject({}) do |h, (source, usage)|
          h[source] = usage.values.inject({}) {|hh, org_usage| hh.update(org_usage) {|_, value1, value2| value1 + value2}}
          h
        end
        say '=' * 68
        usage("#{sub.bold} => #{'TOTAL'.bold}", totals[:oneops], totals[:tekton])
      end
    end

  elsif action == 'oo:org:usage' || action == 'oo:usage'
    org, cloud_name, _ = args
    required_arg('org', org)

    tt_org_usage = tt_request("quota/usage/entity/#{org}", 'Getting usage in Tekton')
    result = {:oneops => {}, :tekton => {}}
    subs = execute!('oo:subs', org, cloud_name)
    subs.keys.sort.each do |sub|
      provides_rels = subs[sub]
      say "Processing clouds for subscription '#{sub.bold}' (#{provides_rels.map {|u| u.fromCi.ciName}.join(', )')})" if @params.verbose > 0

      clouds = provides_rels.map(&:fromCi)
      result[:oneops][sub] = execute('oo:cloud:usage:internal', clouds)
      result[:tekton][sub] = tt_org_usage[sub]
      has_diff = usage("#{sub.bold} => #{org.bold}", result[:oneops][sub], result[:tekton][sub])
      say "  Fix this with:\n    #{"oo:org:usage:transfer -f #{org} #{clouds.first.ciName}".yellow}" if has_diff
      say
    end

  elsif action == 'oo:cloud:usage:internal'
    result = {}
    clouds = args[0]
    clouds.each do |cloud|
      capacity = oo_request("transistor/rest/clouds/#{cloud.ciId}/capacity", "    Getting capacity for cloud '#{cloud.ciName}'")
      if result.empty?
        result = capacity
      else
        capacity.each_pair do |resource, value|
          result[resource] = (result[resource] || 0) + value
        end
      end
    end

  elsif action == 'oo:org:usage:transfer' || action == 'oo:usage:transfer'
    org, cloud_name, _ = args
    required_arg('org', org)

    usage = execute!('oo:org:usage', org, cloud_name)[:oneops]
    usage.each_pair do |sub, sub_usage|
      next if sub_usage.empty?
      limits = tt_request("quota/#{sub}/#{org}", 'Getting quota')
      if limits.empty? || @params.force
        if limits.empty?
          if @params.transfer_buffer < 0
            say 'Invalid quota buffer %'.red
            action_help(@action)
          elsif @params.transfer_buffer == 0
            say 'Buffer % is not set, will set total quota to the same value as usage.'.yellow
          end
          force = @params.force
          @params.force = true
          execute!('quota:set', sub, org, *sub_usage.to_a.map {|u, v| "#{u}=#{(v + v * @params.transfer_buffer).to_i}"}) if @params.transfer_buffer > 0
          @params.force = force
        else
          say "Quota for #{sub.bold} in #{org.bold} already exists, will set usage only, use #{"'quota:set'".blue} action to set total quota values.".yellow
          sub_usage.delete_if {|u| limits[u]} if @params.only_missing
        end
        execute('usage:set', sub, org, *sub_usage.to_a.map {|u, v| "#{u}=#{v}"})
      else
        say "Quota for #{sub.bold} in #{org.bold} already exists, use '-f' option to force update.".yellow
      end
      say
    end

  elsif action == 'resources'
    result = tt_request('resource', 'Fetching resources')
    result.sort_by(&:name).each {|r| say "#{r.name.bold} =>\n  #{r.description}"}

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
    sub_name = args[0]
    result = tt_request('subscription', 'Fetching subscriptions')
    unless sub_name.empty?
      subs = execute('sub:prompt', *args).inject({}) {|h, s| h[s] = s; h}
      result = result.select {|sub| subs.include?(sub.name)}
    end
    result.sort_by(&:name).each do |s|
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

  elsif action == 'sub:delete'
    sub_name, desc, _ = args
    limits = tt_request("quota/subscription/#{sub_name}", 'Getting quotas')
    if limits.empty?
      tt_request("subscription/#{sub_name}", 'Deleting subscription', {}, 'DELETE')
      say "#{sub_name.bold} - #{'deleted'.green}"
    else
      say "Can not delete subscription with quotas (#{limits.size} quotas found). Delete quotas first.".red
    end

  elsif action == 'sub:set'
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

  elsif action == 'users'
    *usernames = args
    required_arg('username', usernames)
    usernames.each do |u|
      info = tt_request("user/#{u}", 'Getting user info')
      blurt "#{u.ljust(15).bold} | "
      if info.empty?
        say  'not found'.red
      else
        blurt "#{info.email.to_s.ljust(25)}|"
        say (info.admin ? ' global admin '.blue(true) : info.orgs.map(&:name).join(', '))
      end
    end

  elsif action == 'users:delete'
    *usernames = args
    required_arg('username', usernames)
    usernames.each do |u|
      info = tt_request("user/#{u}", 'Getting user info')
      if info.empty?
        say "#{u.ljust(25).bold} - #{'not found'.red}"
      else
        ok = tt_request("user/#{info.id}", "Deleting user '#{usernames}'", {}, 'DELETE')
        say "#{u.ljust(25).bold} - #{'deleted'.green}"
      end
    end

  elsif action == 'admins'
    result = tt_request('user/admins', 'Getting admins')
    result.sort_by(&:name).each {|a| say "#{a.name.ljust(15).bold} | #{a.email.to_s.ljust(25)}"}

  elsif action == 'admins:add' || action == 'admins:remove'
    *usernames = args
    required_arg('username', usernames)
    usernames.each do |u|
      tt_request('user', "Managing admin status for user ''#{u}''", {:username => u, :admin => action[-4..-1] == ':add'})
      execute('users', u)
    end

  elsif action == 'orgs'
    org = args[0]
    result = tt_request('org', 'Fetching orgs')
    unless org.empty?
      orgs = execute('org:prompt', nil, org).inject({}) {|h, o| h[o] = o; h}
      result = result.select {|org| orgs.include?(org.name)}
    end
    result.sort_by(&:name).each {|o| say o.name}

  elsif action == 'orgs:add'
    org_name = args[0]
    org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
    tt_request('org', "Org #{org_name} is missing, creating", {:name => org_name}) unless org

  elsif action == 'teams'
    org, team_regex, _ = args
    required_arg('org', org)

    result = tt_request("org/#{org}/team", 'Fetching teams')
    unless team_regex.empty?
      team_regex = /#{team_regex}/
      result = result.select {|t| t.name =~ team_regex}
    end
    result.sort_by(&:name).each {|t| say "#{t.name.bold} #{"(#{t.description})" unless t.description.empty?}"}

  elsif action == 'teams:add'
    org_name, team_name, desc, _ = args
    required_arg('org', org_name)
    required_arg('team_name', team_name)
    org = tt_request("org/#{org_name}", "Fetching org '#{org_name}'")
    if org
      team = tt_request("org/#{org_name}/team/#{team_name}", 'Fetching team')
      if team.empty?
        team = {:orgName => org.name, :name => team_name, :description => desc}
        tt_request("org/#{org_name}/team", "Team #{team_name} is missing, creating", team)
      else
        team['name'] = team_name
        team['description'] = desc unless desc.empty?
        tt_request("org/#{org_name}/team", "Updating team #{team_name}", team)
      end
      say "#{team.name.bold} =>\n  #{team.description}"
    else
      say "Org #{org_name} not found".red
    end

  elsif action == 'teams:remove'
    org, team_name, desc, _ = args
    required_arg('org', org)
    required_arg('team_name', team_name)
    ok = tt_request("org/#{org}/team/#{team_name}", "Deleting #{team_name} from org #{org}", {}, 'DELETE')
    say ok['ok'] ? 'Removed'.green : 'Failed'.red

  elsif action == 'team:users'
    org, team_name, _ = args
    required_arg('org', org)
    required_arg('team', team_name)

    result = tt_request("org/#{org}/team/#{team_name}/users", 'Fetching users')
    if result
      result.sort_by(&:username).each {|u| say "#{u.username.ljust(25).bold} | #{u.email.to_s.ljust(25)} | #{' team admin '.blue(true) if u.role == 'MAINTAINER'}"}
    end

  elsif action == 'team:users:add'
    org, team_name, *usernames = args
    required_arg('username', usernames)
    users = usernames.map {|u| {:username => u}}
    execute('team:users:add:internal', org, team_name, users)

  elsif action == 'team:admins:add'
    org, team_name, *usernames = args
    required_arg('username', usernames)
    admins = usernames.map {|u| {:username => u, :role => 'MAINTAINER'}}
    execute('team:users:add:internal', org, team_name, admins)

  elsif action == 'team:users:add:internal'
    org, team_name, users = args
    required_arg('org', org)
    required_arg('team', team_name)
    ok = tt_request("org/#{org}/team/#{team_name}/users", "Adding #{usernames} to team #{team}", users, 'PUT')
    say ok['ok'] ? 'Added'.green : 'Failed'.red

  elsif action == 'team:users:remove'
    org, team_name, *usernames = args
    required_arg('org', org)
    required_arg('team', team_name)
    required_arg('username', usernames)
    ok = tt_request("org/#{org}/team/#{team_name}/users", "Deleting #{usernames} from team #{team}", usernames, 'DELETE')
    say ok['ok'] ? 'Removed'.green : 'Failed'.red

  elsif action == 'sub:quotas'
    sub = args[0]
    required_arg('subscription', sub)
    subs = execute('sub:prompt', sub)
    subs.each do |sub_name|
      required_arg('subscription', sub_name)
      quotas = tt_request("#{v2_url}subscription/#{sub_name}/quotas", 'Getting quotas')
      quotas.keys.sort.each {|org| show_quota("#{sub_name.bold} => #{org.bold}", quotas[org], @params.depleted_threshold)}
      show_quota_totals("#{sub_name} => TOTAL".bold, quotas) if quotas.size > 1 && !@params.depleted_threshold
    end

  elsif action == 'org:quotas'
    org = args[0]
    required_arg('org', org)
    orgs = execute('org:prompt', nil, org)
    orgs.each do |org_name|
      quotas = tt_request("#{v2_url}entity/#{org_name}/quotas", 'Getting quotas')
      quotas.keys.sort.each {|sub| show_quota("#{org_name.bold} => #{sub.bold}", quotas[sub], @params.depleted_threshold)}
      show_quota_totals("#{org_name} => TOTAL".bold, quotas) if quotas.size > 1 && !@params.depleted_threshold
    end

  elsif action == 'quota'
    sub_name, org, _ = args
    required_arg('subscription', sub_name)
    required_arg('org', org)

    result = {}
    cursor_control = ''
    subs = execute('sub:prompt', sub_name, org)
    subs.each do |s|
      orgs = execute('org:prompt', s, org)
      orgs.each do |o|
        while (true)
          quota = tt_request("#{v2_url}subscription/#{s}/entity/#{o}/quota", 'Getting quota')
          result[s] ||= {}
          result[s][o] = quota
          show_quota("#{cursor_control}#{s.bold} => #{o.bold}", quota) unless quota.empty?
          break if @params.refresh == 0 || subs.size > 1 || orgs.size > 1
          begin
            sleep(@params.refresh)
          rescue Interrupt
            exit
          end
          cursor_control = "\e[#{quota.size + 4}A\r"
        end
      end
    end

  elsif action == 'quota:set' || action == 'usage:set'
    sub_name, org_name, *resources = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)
    known_resources = tt_request('resource', 'Fetching resources').inject({}) {|h, r| h[r.name] = r.name; h}
    values = resources && resources.inject({}) do |h, u|
      name, _ = u.split(/[+-]?[=]/, 2)
      if known_resources.include?(name)
        expr = u[name.size..-1]
        h[name] = expr unless name.empty? || expr.empty?
      end
      h
    end
    required_arg('resources', values)

    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      orgs = execute('org:prompt', s, org_name)
      orgs.each do |o|

        sub = tt_request("subscription/#{s}", "Fetching subscription '#{s}'")
        unless sub
          # execute!('subs:add', s)
          say "Subscription '#{s}' does not exist.  Add subscription first with:".red
          say " \tsubs:add #{s}".blue
          exit(1)
        end

        org = tt_request("org/#{o}", "Fetching org '#{o}'")
        execute!('orgs:add', o) unless org

        setting_usage = action.include?('usage:set')
        unless setting_usage || @params.force
          usage = tt_request("quota/usage/#{s}/#{o}", 'Getting quota')
          no_usage_res = values.keys - usage.keys
          if no_usage_res.size > 0
            say "Some resources [#{no_usage_res.join(',')}] have no usage set.\nWill NOT set limits for these resources. Use '-f' to force limit update for all specified resources.".yellow
            say 'Do you need to transfer usage first?'.bold
            values.delete_if {|r| no_usage_res.include?(r)}
          end
        end

        current = tt_request("quota/#{"usage/" if setting_usage}#{s}/#{o}", 'Getting quota')
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
              h[resource] = [current_value - value, 0].max
            else
              h[resource] = current_value + value
            end
          end
          h
        end
        tt_request("quota/#{"usage/" if setting_usage}#{s}/#{o}", 'Updating quota', resolved_values) unless resolved_values.empty?
        execute('quota', s, o)
      end
    end

  elsif action == 'quota:delete'
    sub_name, org_name, *resources = args
    required_arg('subscription', sub_name)
    required_arg('org', org_name)
    subs = execute('sub:prompt', sub_name, org_name)
    subs.each do |s|
      orgs = execute('org:prompt', s, org_name)
      orgs.each do |o|
        quota = execute('quota', s, o)[s][o]
        next if quota.empty?
        confirm = quota.entries.find {|e| e.last.limit > 0} || quota.entries.last
        blurt "Confirm delete by entering limit for #{confirm.first}: "
        if ask.to_i == confirm.last.limit
          tt_request("#{v2_url}subscription/#{s}/entity/#{o}", 'Deleting quota', {}, 'DELETE')
          say "#{s.bold} => #{o} - #{'deleted'.green}"
        else
          say "Not confirmed, skipping.".yellow
        end
      end
    end

  elsif action == 'sub:prompt'
    sub_name, org, _ = args
    if sub_name[0] == '?'
      if org.empty? || org[0] == '?'
        subs = tt_request('subscription', 'Fetching subscriptions').map(&:name)
      else
        quotas = tt_request("quota/entity/#{org}", 'Getting quotas')
        if quotas.empty?
          say "No existing quotas set up in org '#{org}'".red
          exit(1)
        end
        subs = quotas.keys
      end

      result = value_prompt('subscriptions', subs, sub_name[1..-1])
      exit if result.empty?
    elsif sub_name[0] == '*'
      subs = tt_request('subscription', 'Fetching subscriptions').map(&:name)
      result = value_match(sub_name[1..-1], subs)
    else
      result = sub_name.split(',').uniq
    end

  elsif action == 'org:prompt'
    sub_name, org, _ = args
    if org[0] == '?'
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

      result = value_prompt('subscriptions', orgs, org[1..-1])
      exit if result.empty?
    elsif org[0] == '*'
      orgs = tt_request('org', 'Fetching orgs').map(&:name)
      result = value_match(org[1..-1], orgs)
    else
      result = org.split(',').uniq
    end

  elsif action == 'login'
    username, password, _ = args
    if username.empty?
      password = nil
      blurt 'username: '
      username = ask
    end
    if password.empty?
      blurt 'password: '
      STDIN.noecho {password = STDIN.gets[0...-1]}
      say
    end

    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

    set_tekton_auth(username, password)
    if username.empty? || password.empty?
      result = !tt_request('org', 'Checking credentials').empty?
    else
      api_key = tt_request('apikey', 'Getting api token', {:username => username, :name => 'CLI'})['key']
      unless api_key.empty?
        @params.tekton_auth = api_key
        result = true
      end
    end

    if result
      cfg = %w(tekton_host oneops_host tekton_auth).inject({}) {|h, key| h[key] = @params[key]; h}
      File.write(SESSION_FILE_NAME, JSON.pretty_unparse(cfg)) ##unless @repl
      say 'Signed in - do not forget to logout when done!'.green
    end

  elsif action == 'logout'
    File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)

  elsif action == 'help'
    action = match_action(args[0])
    action.empty? ? (args[0] =~ /^(actions)|(commands)/i ? say(actions_help) : general_help) : action_help(action)

  else
    say "Unknown action: #{action}".red
    exit(1)
  end

  return result
end

def action_regex(action)
  /#{action.gsub(/\W+/, '\w*:')}\w*$/
end

def match_action(action)
  return nil if action.empty?
  regex = action_regex(action)
  @actions.keys.find {|k| (regex =~ k) == 0}
end

def run(args)
  t = Time.now

  @show_help = false
  @params.verbose         = 0
  @params.force           = false
  @params.only_missing    = false
  @params.transfer_buffer = 0
  @params.refresh         = 0
  @params.mismatch_only   = false
  @params.fix_mismatch    = false
  begin
    @action, *@args = @opt_parser.parse(args)
  rescue OptionParser::ParseError => e
      say e.message.red
      exit(1)
  end

  if @action.empty?
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

  if %w(tekton_host oneops_host tekton_auth).none? {|key| @params[key]} && @action != 'login' && File.exist?(SESSION_FILE_NAME)
    cfg = JSON.parse(File.read(SESSION_FILE_NAME))
    %w(tekton_host oneops_host tekton_auth).each {|key| @params[key.to_sym] = cfg[key] unless cfg[key].empty?}
  end

  if @params.tekton_auth.empty? && @action != 'login' && @action != 'logout' && @action != 'help'
    say "Specify tekton auth with '--ta' option or use 'login' command to start a session.".red
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
  @params.tekton_host = @params.tekton_host.terminate_with('/').terminate_with('api/v1/')

  if @params.oneops_host.empty?
    @params.oneops_host = ONEOPS_HOSTS[:default]
    say "OneOps host not specified, defaulting to #{@params.oneops_host.blue}".yellow if @params.verbose > 0
  else
    host = @params.oneops_host.downcase.to_sym
    @params.oneops_host = ONEOPS_HOSTS[host] if ONEOPS_HOSTS.include?(host)
  end
  @params.oneops_host = "https://#{@params.oneops_host}" unless @params.oneops_host.start_with?('http')
  @params.oneops_host = @params.oneops_host.terminate_with('/')

  @args = @args.select {|a| a =~ /\w/}
  execute(@action, *@args)
  say "Done #{@action.bold} in #{(Time.now - t).round(1)} sec" if @params.verbose > 0
end

#------------------------------------------------------------------------------------------------
# Start here.
#------------------------------------------------------------------------------------------------
@actions = {
  'help'                  => ['help [actions|ACTION]', 'display help: full descriptiin or list of avaialble actions or specific action'],
  'version'               => ['version', 'display CLI and tekton server versions'],

  'login'                 => ['login [-e prod|stg|dev|local] [USERNAME [PASSWORD]]', 'log in for running Tekton commands, defaults to \'prod\' environment if not specified'],
  'logout'                => ['logout', "log out after running Tekton commands\n"],

  'admins'                => ['admins ', 'list global admins'],
  'admins:add'            => ['admins:add USERNAME...', 'add users global admins'],
  'admins:remove'         => ['admins:remove USERNAME...', 'remove users as global admins'],
  'users'                 => ['users USERNAME... ', 'list user info'],
  'users:delete'          => ['users:delete USERNAME... ', 'completely removes users (from all orgs)'],

  'orgs'                  => ['orgs ORG,...|?[ORG_REGEX]|*[ORG_REGEX]', 'list orgs'],
  'orgs:add'              => ['orgs:add ORG', 'add org'],

  'teams'                 => ['teams ORG [TEAM_REGEX]', 'list teams'],
  'teams:add'             => ['teams:add ORG TEAM_NAME [TEAM_DESCRIPTION]', 'add team'],
  'teams:remove'          => ['teams:remove ORG TEAM_NAME', 'remove team'],

  'team:users'            => ['team:users ORG TEAM [TEAM_REGEX]', 'list team users (both regular and admin usres)'],
  'team:users:add'        => ['team:users:add ORG TEAM USERNAME...', 'add users (regular, non-admin) to team (resets team admin status if user is team admin)'],
  'team:users:remove'     => ['team:users:remove ORG TEAM USERNAME...', 'remove users from team'],
  'team:admins:add'       => ['team:admins:add ORG TEAM USERNAME...', "add team admin user to team (makes user a team admin if user is alrady on the team)\n"],

  'resources'             => ['resources', 'list resource types'],
  'resources:add'         => ['resources:add  [-f]', "add resource type\n"],

  'subs'                  => ['subs SUB,...|?[SUB_REGEX]|*[SUB_REGEX]', 'list subsctiptions (including hard quota)'],
  'subs:add'              => ['subs:add [-f] SUB', 'add subscription'],
  'sub:delete'            => ['sub:delete SUB', 'delete subscription (only if there are no quotas configured for it)'],
  'sub:set'               => ['sub:set SUB,...|?[SUB_REGEX]|*[SUB_REGEX] RESOURCE=VALUE...', "set subscription limits (hard quota)\n"],

  'sub:quotas'            => ['sub:quotas SUB,...|?[SUB_REGEX]|*[SUB_REGEX] [--depleted [THRESHOLD_%]]', 'list all quotas for subscription'],
  'org:quotas'            => ['org:quotas ORG,...|?[ORG_REGEX]|*[ORG_REGEX] [--depleted [THRESHOLD_%]]', 'list all quotas for org'],

  'quota'                 => ['quota SUB,...|?[SUB_REGEX]|*[SUB_REGEX] ORG,...|?[ORG_REGEX]|*[ORG_REGEX]', 'show quota'],
  # 'usage:set'             => ['usage:set SUB,...|?[SUB_REGEX]|*[SUB_REGEX] ORG,...|?[ORG_REGEX]|*[ORG_REGEX] RESOURCE=VALUE...', 'update quota usage'],
  'quota:set'             => ['quota:set SUB,...|?[SUB_REGEX]|*[SUB_REGEX] ORG,...|?[ORG_REGEX]|*[ORG_REGEX] RESOURCE[+|-]=VALUE[%]...', "update quota limits: directly set with \'=\' or increment with \'+=\' or decrement with \'-=\'; specify absolute value or percentage of current value with \'%\'"],
  'quota:delete'          => ['quota:delete SUB,...|?[SUB_REGEX]|*[SUB_REGEX] ORG,...|?[ORG_REGEX]|*[ORG_REGEX]', "delete existing quota (with confirmation)\n"],

  'oo:resources'          => ['oo:resources', 'list resource types in OneOps'],
  'oo:resources:transfer' => ['oo:resources:transfer [-f]', 'transfer resource definitions in OneOps to Tekton (idempotent!)'],
  'oo:subs'               => ['oo:subs ORG [CLOUD_REGEX]', 'list subsctiptions in OneOps'],
  'oo:subs:transfer'      => ['oo:subs:transfer  [-f] ORG [CLOUD_REGEX]', 'transfer subsctiptions in OneOps to Tekton  (idempotent!)'],
  'oo:sub:usage'          => ['oo:sub:usage [--mismatch [--fix]] SUB,...|?[SUB_REGEX]|*[SUB_REGEX] [ORG,...]', 'For a given subscription list usage in OneOps and compare with usage in Tekton'],
  'oo:org:usage'          => ['oo:org:usage ORG [CLOUD_REGEX]', 'For a given org list usage in OneOps and compare with usage in Tekton'],
  'oo:org:usage:transfer' => ['oo:or./t(:' '):usage:transfer [-f [--omr]] [-b BUFFER_%] ORG [CLOUD_REGEX]', "convert current usage in OneOps into quota in Tekton or update usage for existing Tekton quota with the current ussage in OneOps (idempotent!)\n"]
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
         #{__FILE__} usage:set azure-southcentralus-wm:102e961b-18a2-4ff0-a03e-c58794d04d55 some-org vm=37 Dv2_vCPU=128
    
    4. List all exising quotas for a given org in Tekton:
         #{__FILE__} org:quotas some-org
    
    5. Logout:
         #{__FILE__} logout
FOOTER

@params = OpenStruct.new
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

  opts.on('-f', '--force', 'Force operation to overwrite warnings (used in the context of certain actions)') {@params.force = true}
  opts.on('--omr', '--only-missing-resources', "Transfer usage only for resources missing quota (when there is already quota set up for at least one other resource (for a given subscription and org); use with '-f' option") {@params.only_missing = true}
  opts.on('--mismatch', 'Show usage mismatch only') {@params.mismatch_only = true}
  opts.on('--fix', 'Fix usage mismatch by transfering usage number from OneOps to the corresponding soft quotas in Tekton') {@params.fix_mismatch = true}

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
      actions.grep(action_regex(s))
    else
      action = @actions[split.first]
      action ? ["Usage: #{action[0]}", ' ' * 100] : []
    end
  end

  if File.exist?(HISTORY_FILE_NAME)
    File.read(HISTORY_FILE_NAME).split("\n").each {|c| Readline::HISTORY << c}
  end

  prompt = "#{'>>>'.invert} "
  while input = Readline.readline(prompt, true).strip
    if input.empty?
      Readline::HISTORY.pop
    elsif input.start_with?('hist')
      Readline::HISTORY.pop
      say Readline::HISTORY.to_a
    else
      input.split(';').each do |command|
        action = command.split(/\s/,2).first
        if action == 'exit' || action == 'quit'
          File.write(HISTORY_FILE_NAME, Readline::HISTORY.to_a[[Readline::HISTORY.size - 1000, 0].max..-1].join("\n"))
          exit
        elsif action == 'login'
          @params.tekton_host = nil
          @params.oneops_host = nil
          @params.tekton_auth = nil
          File.delete(SESSION_FILE_NAME) if File.exist?(SESSION_FILE_NAME)
        end

        begin
          t = Time.now
          cmd = command.gsub(/^time\s+/i, '')
          begin
            run(cmd.strip.split(/\s+/))
          rescue SystemExit => e
            exit(e.status)
          rescue Exception => e
            say e.message.red
            say "   #{e.backtrace.join("\n   ")}" if @params.verbose > 0
          end
          say "Done in #{(Time.now - t).round(1).to_s.bold} sec" unless cmd == command
        rescue SystemExit
        end
      end
    end
  end
else
  run(ARGV)
end
