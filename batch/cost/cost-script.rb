#!/usr/bin/env ruby
require 'json'
require 'time_diff'

BATCH_SIZE = 20000

class NilClass
  def [] (_)
    nil
  end

  def empty?
    true
  end
end

@tags_cache = {}

def existing_ci_cost (date)
  ts = Time.now
  end_of_day = (Time.parse(date) + 24.hours).utc.beginning_of_day

  req = {
    "_source" => [
      "ciId",
      "ciClassName",
      "nsPath",
      "created",
      "workorder.cloud.ciName",
      "ops.cloudName",
      "workorder.rfcCi.rfcAction",
      "workorder.box.ciAttributes.pack",
      "workorder.box.ciAttributes.major_version",
      "workorder.box.ciAttributes.source",
      "workorder.payLoad.Organization.ciAttributes.tags",
      "workorder.payLoad.Organization.ciAttributes.owner",
      "workorder.payLoad.Assembly.ciAttributes.tags",
      "workorder.payLoad.Assembly.ciAttributes.owner",
      "workorder.payLoad.offerings",
      "workorder.payLoad.RealizedAs.ciId",
      "workorder.payLoad.Environment.ciAttributes.profile",
      "workorder.searchTags.responseDequeTS"
    ],
    "size"    => BATCH_SIZE,
    "query"   => {
      "bool" => {
        "must" => [
          {
            "nested" => {
              "path"   => "workorder.payLoad.offerings",
              "filter" => {
                "exists" => {
                  "field" => "workorder.payLoad.offerings"
                }
              }
            }
          },
          {
            "range" => {
              "created" => {
                "lt" => end_of_day.iso8601.to_s
              }
            }
          }
        ]
      }
    },
    "sort"    => {"ciId" => "asc"}
  }

  from           = 0
  total          = 1
  ci_counter     = 0
  record_counter = 0
  while from < total
    records = []
    req[:from] = from

    print 'Loading CIs... '
    cmd     = %(curl -s -XGET 'http://#{@host}:9200/cms-all/ci/_search' -d '#{req.to_json}')
    results = JSON.parse(`#{cmd}`)['hits']
    cis     = results['hits']
    total   = results['total']
    puts "#{cis.size} loaded."

    cis.each do |ci|
      begin
        fields = ci['_source']
        wo     = fields['workorder']

        if wo
          payLoad   = wo['payLoad']
          offerings = payLoad['offerings']

          if offerings
            ciId           = fields['ciId']
            ciClassName    = fields['ciClassName']
            nsPath         = fields['nsPath']
            cloud          = wo['cloud']['ciName'] || fields['ops']['cloudName'] || 'openstack'
            org_attrs      = payLoad['Organization'][0]['ciAttributes']
            assembly_attrs = payLoad['Assembly'][0]['ciAttributes']
            envProfile     = payLoad['Environment'][0]['ciAttributes']['profile']
            manifestId     = payLoad['RealizedAs'][0]['ciId']
            box            = wo['box']['ciAttributes']
            packName       = box['pack']
            packVersion    = box['major_version']
            packSource     = box['source']
            created_ts     = fields['created']
            created_ts     = (wo['rfcCi']['rfcAction'] == 'add' ? (wo['searchTags']['responseDequeTS'] || created_ts) : created_ts).in_time_zone('UTC')
            hours          = (end_of_day - created_ts).to_f / 3600
            hours          = 24 if hours > 24
            _, organization, assembly, env = nsPath.split('/', 5)

            if hours > 0
              offerings.each do |o|
                o_attrs   = o['ciAttributes']
                cost_rate = o_attrs['cost_rate'].to_f
                records << {:ts            => Time.now.utc.iso8601,
                            :date          => date,
                            :ciId          => ciId,
                            :ciClassName   => ciClassName,
                            :nsPath        => nsPath,
                            :organization  => organization,
                            :assembly      => assembly,
                            :environment   => env,
                            :envProfile    => envProfile,
                            :packSource    => packSource,
                            :packName      => packName,
                            :packVersion   => packVersion,
                            :manifestId    => manifestId,
                            :cloud         => cloud,
                            :serviceType   => o_attrs['service_type'],
                            :servicensPath => o['nsPath'],
                            :tags          => tags(organization, assembly, org_attrs, assembly_attrs),
                            :cost_rate     => cost_rate,
                            :cost          => (cost_rate * hours).round(3),
                            :unit          => o_attrs['cost_unit'],
                            :origin        => 'ci'}.to_json
              end
            end
          end
        end
      rescue Exception => e
        puts "ERROR! Failed to process CI: #{ci.to_json}"
        puts e
      end
      ci_counter += 1
      print "Processing Cis: #{ci_counter}\r" if ci_counter % 100 == 0 || ci_counter == cis.size
    end

    write_records(records)
    record_counter += records.size

    from += BATCH_SIZE
    break if from > total
  end
  puts "Processed #{ci_counter} CIs, created #{record_counter} cost records in #{(Time.now - ts).to_i}sec."
  @day_record_count += record_counter
end

def deleted_ci_cost(date)
  ts = Time.now
  start_of_day = Time.parse(date).utc.beginning_of_day
  end_of_day   = start_of_day + 24.hours

  req = {
    "_source" => [
      "rfcCi.ciId",
      "rfcCi.nsPath",
      "rfcCi.ciClassName",
      "rfcCi.created",
      "cloud.ciName",
      "cloudName",
      "payLoad.Organization.ciAttributes.tags",
      "payLoad.Organization.ciAttributes.owner",
      "payLoad.Assembly.ciAttributes.tags",
      "payLoad.Assembly.ciAttributes.owner",
      "payLoad.RealizedAs.ciAttributes.ciId",
      "payLoad.Environment.ciAttributes.profile",
      "payLoad.offerings",
      "box.ciAttributes.pack",
      "box.ciAttributes.major_version",
      "box.ciAttributes.source",
      "searchTags.responseDequeTS"
    ],
    "size"    => BATCH_SIZE,
    "query"   => {
      "bool" => {
        "must" => [
          {
            "nested" => {
              "path"   => "payLoad.offerings",
              "filter" => {
                "exists" => {
                  "field" => "payLoad.offerings"
                }
              }
            }
          },
          {
            "terms" => {"searchTags.rfcAction" => %w(delete replace)}
          },
          {
            "term" => {"dpmtRecordState" => 'complete'}
          },
          {
            "range" => {
              "searchTags.responseDequeTS" => {
                "gt" => start_of_day.iso8601.to_s
              }
            }
          }
        ]
      }
    },
    "sort"    => {"searchTags.responseDequeTS" => "asc"}
  }

  from           = 0
  total          = 1
  wo_counter     = 0
  record_counter = 0
  while from < total
    records = []
    req[:from] = from

    print 'Loading delete WOs... '
    cmd     = %(curl -s -XGET 'http://#{@host}:9200/cms-weekly/workorder/_search' -d '#{req.to_json}')
    results = JSON.parse(`#{cmd}`)['hits']
    wos     = results['hits']
    total   = results['total']

    puts "#{wos.size} loaded."

    time_parser = ActiveSupport::TimeZone.new('UTC')
    wos.each do |wo|
      wo_counter += 1
      print "Processing delete WOs #{wo_counter}\r" if wo_counter % 100 == 0 || wo_counter == wos.size
      begin
        fields  = wo['_source']
        rfc_ci  = fields['rfcCi']
        ciId    = rfc_ci['ciId']
        payload = fields['payLoad']

        offerings = payload['offerings']
        begin
          created_ts = time_parser.parse(rfc_ci['created'])
        rescue
          created_ts = nil
        end

        next unless offerings && created_ts && created_ts < end_of_day

        ciClassName    = rfc_ci['ciClassName']
        nsPath         = rfc_ci['nsPath']
        cloud          = fields['cloud']['ciName'] || fields['cloudName'] || 'openstack'
        org_attrs      = payload['Organization'][0]['ciAttributes']
        assembly_attrs = payload['Assembly'][0]['ciAttributes']
        envProfile     = payload['Environment'][0]['ciAttributes']['profile']
        manifestId     = payload['RealizedAs'][0]['ciId']
        box            = fields['box']['ciAttributes']
        packName       = box['pack']
        packVersion    = box['major_version']
        packSource     = box['source']
        wo_ts          = wo['_source']['searchTags']['responseDequeTS'].in_time_zone('UTC')
        hours          = (wo_ts - (start_of_day > created_ts ? start_of_day : created_ts)).to_f / 3600
        hours          = 24 if hours > 24
        _, organization, assembly, env = nsPath.split('/', 5)

        if hours > 0
          offerings.each do |o|
            cost_rate = o['ciAttributes']['cost_rate'].to_f
            records << {:ts            => Time.now.utc.iso8601,
                        :date          => date,
                        :ciId          => ciId,
                        :ciClassName   => ciClassName,
                        :nsPath        => nsPath,
                        :organization  => organization,
                        :assembly      => assembly,
                        :environment   => env,
                        :envProfile    => envProfile,
                        :packSource    => packSource,
                        :packName      => packName,
                        :packVersion   => packVersion,
                        :manifestId    => manifestId,
                        :cloud         => cloud,
                        :serviceType   => o['ciAttributes']['service_type'],
                        :servicensPath => o['nsPath'],
                        :tags          => tags(organization, assembly, org_attrs, assembly_attrs),
                        :cost_rate     => cost_rate,
                        :cost          => (cost_rate * hours).round(3),
                        :unit          => o['ciAttributes']['cost_unit'],
                        :origin        => 'wo'}.to_json
          end
        end
      rescue Exception => e
        puts "ERROR! Failed to process WO: #{wo.to_json}"
        puts e
      end
    end

    write_records(records)
    record_counter += records.size

    from += BATCH_SIZE
    break if from > total
  end
  puts "Processed #{wo_counter} delete WOs, created #{record_counter} cost records in #{(Time.now - ts).to_i}sec."
  @day_record_count += record_counter
end

def write_records(records)
  open(@result_file_name, 'a') do |c|
    c << "\n"
    c << records.join("\n")
  end
end

def load_tags
  print 'Loading tags info... '
  ts = Time.now

  req = {
    :_source => %w(ciName ciAttributes.tags ciAttributes.owner),
    :filter  => {:term => {'ciClassName.keyword' => 'account.Organization'}},
    :size    => 9999
  }
  org_cis = JSON.parse(`curl -s -XGET 'http://#{@host}:9200/cms-all/ci/_search' -d '#{req.to_json}'`)['hits']['hits']
  org_cis.each do |o|
    organization = o['_source']
    key = "/#{organization['ciName']}"
    @tags_cache[key] = parse_tags_and_owner(organization['ciAttributes'])
  end

  req = {
    :_source => %w(ciName nsPath ciAttributes.tags ciAttributes.owner),
    :filter  => {:term => {'ciClassName.keyword' => 'account.Assembly'}},
    :size    => 999999
  }
  assembly_ci = JSON.parse(`curl -s -XGET 'http://#{@host}:9200/cms-all/ci/_search' -d '#{req.to_json}'`)['hits']['hits']
  assembly_ci.each do |a|
    assembly = a['_source']
    ns       = assembly['nsPath']
    key      = "#{ns}/#{assembly['ciName']}"
    @tags_cache[key] = (@tags_cache[ns] || {}).merge(parse_tags_and_owner(assembly['ciAttributes']))
  end
  puts "loaded tags in #{(Time.now - ts).to_i}sec."
end

def tags(org, assembly, wo_org_attrs, wo_assembly_attrs)
  load_tags if @tags_cache.empty?

  results = @tags_cache["/#{org}/#{assembly}"]
  return results if results

  if wo_assembly_attrs
    # If no tags for assembly have been loaded (most likely due to assembly and/org have been deleted), try to use
    # in-lined org/assembly tags directly from Wo - but do NOT cache them (they could have changed over time).
    results = (@tags_cache["/#{org}"] || parse_tags_and_owner(wo_org_attrs)).merge(parse_tags_and_owner(wo_assembly_attrs))
  else
    results = @tags_cache["/#{org}"] || parse_tags_and_owner(wo_org_attrs)
  end

  return results
end

def parse_tags_and_owner(attrs, fallback = {})
  result = fallback
  if attrs
    begin
      result = JSON.parse(attrs['tags'])
    rescue
    end
    owner = attrs['owner']
    result['owner'] = owner unless owner.empty?
  end

  result
end

def do_curl(cmd, msg, retries = 3)
  ts = Time.now
  r = nil
  print msg
  retries.times do
    r = `curl -s -i #{cmd}`
    break if r.include?('200 OK') || r.include?('404 Not Found')
    print "failed, retrying... "
  end
  System.exit(1) unless r
  puts "done in #{(Time.now - ts).to_f.round(1)}sec."
  return r
end

def show_help
  puts <<-HELP
Rebuilds or displays daily cost indices for a given time period by integrating over cost rate for
existing and deleted bom CIs with offerings.
Usage:
  <this_script> [OPTIONS] ES_HOST START_DAY END_DAY [INDEX_NAME_PREFIX]

  OPTIONS:
    -f | --force   - force day cost reindexing when daily index already exists
                     (deletes daily index and rebuilds it), otherwise day is skipped
    -h | --help    - display help info
    -l | --list    - list daily index info

Example:
  ./cost-batch.rb es.prod-1312.core.oneops.prod.walmart.com 2017-07-14 2017-07-14
HELP
end

#------------------------------------------------------------------------------------------------
# Start here.
#------------------------------------------------------------------------------------------------
puts Time.now

if ARGV.size < 3
  show_help
  exit(1)
end

force = false
list_only = false
no_write = false
ARGV.delete_if do |a|
  if a == '-f' || a == '--force'
    force = true
  elsif a == '-h' || a == '--help'
    show_help
    exit
  elsif a == '-l' || a == '--list'
    list_only = true
  elsif a == '-d' || a == '--debug'
    @debug = true
  elsif a == '--no-write'
    puts '***** READ-ONLY MODE *****'
    no_write = true
  elsif a.start_with?('-')
    puts "Unknown option ''#{a}'', use '-h' option to see help!"
    exit(1)
  else
    false
  end
end

@host, start_day, end_day, index_name_prefix = ARGV
index_name_prefix = 'cost' if !index_name_prefix || index_name_prefix.empty?
load_tags unless list_only

(DateTime.parse(start_day).to_date..DateTime.parse(end_day).to_date).map(&:to_s).each do |d|
  @index_name = "#{index_name_prefix}-#{Time.parse("#{d}").strftime("%Y%m%d")}"

  if list_only
    puts `curl -s "http://#{@host}:9200/_cat/indices/#{@index_name}"`
    next
  end

  puts "--------------- Indexing cost for #{d} ---------------"
  ts = Time.now

  @result_file_name = "#{@index_name}.json"
  File.delete(@result_file_name) if File.exist?(@result_file_name)

  unless no_write
    response = do_curl(%(-XHEAD "http://#{@host}:9200/#{@index_name}"), "Checking if #{@index_name} already exist...")
    if response.include?('200 OK')
      unless force
        puts "Index #{@index_name} already exists - skipping, use '-f' option to force reindexing\n\n"
        next
      end
      do_curl(%(-XDELETE 'http://#{@host}:9200/#{@index_name}'), "Index #{@index_name} already exists - deleting... ")
    end
    do_curl(%(-XPOST 'http://#{@host}:9200/#{@index_name}'), "Creating index #{@index_name}... ")
  end

  @day_record_count = 0
  existing_ci_cost(d)
  deleted_ci_cost(d)

  unless no_write
    ts1 = Time.now
    cmd = %(cat #{@result_file_name} | ./stream2es stdin --target "http://#{@host}:9200/#{@index_name}/ci")
    puts cmd
    `#{cmd}`
    puts "Done streaming in #{(Time.now - ts1).to_i}sec."

    File.delete(@result_file_name)
  end

  puts "\nCost indexer done for '#{d}', generated #{@day_record_count} in #{(Time.now - ts).to_i}sec.\n\n"
end
