#! ruby

BASE_DIR = File.expand_path(File.dirname(__FILE__))
require File.expand_path('../lib/cms', BASE_DIR)
require 'yaml'

def ns_suffix(ns)
  mode = ns.split('/')[6]
  "...#{mode if mode}"
end

def ci_key(ci)
  "#{ns_suffix(ci.nsPath)} - #{ci.ciClassName} - #{ci.ciName}"
end

def relation_key(relation, ci_map)
  "#{ns_suffix(relation.nsPath)} - #{relation.relationName} - #{ci_map[relation.fromCiId].ciName} => #{ci_map[relation.toCiId].ciName}"
end

def compare(key, errors, attrs, master_attrs)
  if master_attrs
    mismatch = attrs.diff(master_attrs, attrs.keys)
    if mismatch.blank?
      status('OK', key) if @verbose
    else
      errors[:MISMATCH] << [key, mismatch]
    end
  else
    errors[:EXTRA] << key
  end
end

def status(status, message, data = nil)
  puts "#{status.ljust(8, ' ').send(status == 'OK' ? :green : :red)} #{message} #{data.to_yaml if data.present?}"
end

def error(message)
  puts "\n#{'ERROR'.red}: #{message}"
  exit(1)
end

def print_errors(errors)
  error_count = 0
  errors.each_pair do |type, errs|
    errs.sort.each {|k| status(type.to_s, *k)}
    error_count += errs.size
  end
  error_count
end

def match_pack_against_mater_yaml(pack_full_name, pack_location)
  source, name = pack_full_name.split('/')
  file_name = File.expand_path("fixtures/#{name}_validation.yaml", BASE_DIR)

  print "Loading master pack from #{file_name}... "
  begin
    master = YAML.load_file(file_name)
  rescue Exception => e
    error("could not load valiation yaml #{file_name}: #{e}")
  end

  match_pack(source.presence || 'test', name, nil, pack_location, master['cis'], master['relations'])
end

def match_pack_against_server(pack_full_name, pack_location, master_site, master_pack_full_name)
  source, name, version = pack_full_name.split('/')
  master_source, master_name, master_version = master_pack_full_name.split('/')
  if source.blank? || name.blank? || version.blank?
    error("invalid pack indentifier: #{pack_full_name}. Must be in a form of 'source/pack/version'.")
  elsif master_source.blank? || master_name.blank? || master_version.blank?
    error("invalid master pack indentifier: #{pack_full_name}. Must be in a form of 'source/pack/version'.")
  end
  master_pack_ns = "/public/#{master_source}/packs/#{master_name}/#{master_version}"

  print "Fetching master pack CIs from #{master_pack_ns}... "

  site = ActiveResource::Base.site
  begin
    puts "====== #{master_site}"
    ActiveResource::Base.site = master_site
    cis = Cms::Ci.all(:params => {:nsPath => master_pack_ns, :recursive => true}).
      reject {|ci| ci.ciState == 'pending_deletion'}
    error("master pack not found in #{master_pack_ns} at #{Cms::Ci.site}") if cis.size == 0

    rels = Cms::Relation.all(:params => {:nsPath => master_pack_ns, :recursive => true}).
      reject {|ci| ci.relationState == 'pending_deletion' }
  ensure
    ActiveResource::Base.site = site
  end

  match_pack(source, name, version, pack_location, cis, rels)
end

def match_pack(pack_source, pack_name, pack_version, pack_location, master_cis, master_rels)
  puts 'done:'
  puts "\t- #{master_cis.size} CIs"
  puts "\t- #{master_rels.size} relations"

  master_ci_map    = master_cis.inject({}) {|h, ci| h[ci_key(ci)] = ci; h}
  master_ci_id_map = master_cis.inject({}) {|h, ci| h[ci.ciId] = ci; h}
  master_rel_map   = master_rels.inject({}) {|h, r| h[relation_key(r, master_ci_id_map)] = r; h}

  source_ns = "/public/#{pack_source}"
  packs_ns  = "#{source_ns}/packs"
  pack_ns   = "#{packs_ns}/#{pack_name}#{"/#{pack_version}" if pack_version}"

  unless @skip_sync
    unless @keep_ns
      ns = Cms::Namespace.first(:params => {:nsPath => source_ns})
      if ns
        print "Namespace #{source_ns} found, dropping... "
        ns.destroy
        puts 'done'
      end
      print "Creating namespace #{source_ns}... "
      if Cms::Namespace.new(:nsPath => source_ns).save
        puts 'done'
      else
        error('failed')
      end
      print "Creating namespace #{packs_ns}... "
      if Cms::Namespace.new(:nsPath => packs_ns).save
        puts 'done'
      else
        error('failed')
      end
    end

    cmd = %(cd "#{pack_location}"; knife pack sync #{pack_name} -o "#{pack_location}" -r #{pack_source} #{'--reload' if @keep_ns} #{'>/dev/null' unless @verbose})
    puts "Loading sample pack via: \n#{cmd.blue}"
    exit($?.exitstatus) unless system(cmd)
  end

  print "Fetching target pack from #{pack_ns}..."
  cis = Cms::Ci.all(:params => {:nsPath => pack_ns, :recursive => true}).
    reject {|ci| ci.ciState == 'pending_deletion'}
  error("target pack not found in #{pack_ns} at #{Cms::Ci.site}") if cis.size == 0
  ci_id_map = cis.inject({}) {|h, ci| h[ci.ciId] = ci; h}
  rels = Cms::Relation.all(:params => {:nsPath => pack_ns, :recursive => true}).
    reject {|ci| ci.relationState == 'pending_deletion' }
  puts 'done:'
  puts "\t- #{cis.size} CIs"
  puts "\t- #{rels.size} relations"

  puts '--------------------------------------------------------------------------------'
  puts ' CIs'
  puts '--------------------------------------------------------------------------------'
  errors = {:EXTRA => [], :MISSING => [], :MISMATCH => []}
  cis.sort_by {|ci| ci_key(ci)}.each do |ci|
    key = ci_key(ci)
    master_ci = master_ci_map.delete(key)
    compare(key, errors, ci.ciAttributes.attributes, master_ci && master_ci.ciAttributes.attributes)
  end
  errors[:MISSING] = master_ci_map.keys

  ci_error_count = print_errors(errors)
  puts "#{'Ok'.green}: matched #{cis.size} CIs" if ci_error_count == 0

  puts
  puts
  puts '--------------------------------------------------------------------------------'
  puts ' Relations'
  puts '--------------------------------------------------------------------------------'
  errors = {:EXTRA => [], :MISSING => [], :MISMATCH => []}
  rels.sort_by {|rel| relation_key(rel, ci_id_map)}.each do |rel|
    key = relation_key(rel, ci_id_map)
    master_rel = master_rel_map.delete(key)
    compare(key, errors, rel.relationAttributes.attributes, master_rel && master_rel.relationAttributes.attributes)
  end
  errors[:MISSING] = master_rel_map.keys

  rel_error_count = print_errors(errors)
  puts "#{'Ok'.green}: matched #{rels.size} relations" if rel_error_count == 0

  puts
  error_count = ci_error_count + rel_error_count
  if error_count > 0
    puts "#{'FAILED:'.red(true)} #{error_count} #{'problem'.pluralize(error_count)}"
  else
    puts "#{'SUCCESS'.green(true)}: matched #{cis.size} CIs and #{rels.size} relations"
  end
end


#------- ----------------------------------------------------------------------------
site                  = ENV['CMSAPI']
master_site           = nil
master_pack_full_name = nil
pack_location         = "#{BASE_DIR}/fixtures"

i = 0
while i < ARGV.size
  break unless ARGV[i]

  unless ARGV[i][0] == '-'
    i += 1
    next
  end
  a = ARGV.delete_at(i)

  if a == '--skip-sync'
    @skip_sync = true
  elsif a == '--keep-ns'
    @keep_ns = true
  elsif a == '-s' || a == '--site'
    error("'-s|--site' option is supported only with '--skip-sync', specify '--skip-sync' first") unless @skip_sync
    site = ARGV.delete_at(i)
  elsif a == '-m' || a == '--master-site'
    master_site = ARGV[i]
    master_site = (master_site.blank? || master_site[0] == '-') ? '' : ARGV.delete_at(i)
  elsif a == '-p' || a == '--master-pack'
    master_pack_full_name = ARGV.delete_at(i)
  elsif a == '-o' || a == '--pack-location'
    pack_location = ARGV.delete_at(i)
  elsif a == '-V' || a == '--verbose'
    @verbose = true
  elsif a == '-h' || a == '--help'
    puts <<-HELP
This script can be used to test pack sync changes for regression.  It can load and compare a pack
against some "master". Dependinging on a "master" it could be run in 2 modes.

1. Compares a loaded (i.e. "target") pack against a validation yaml fixture. In this mode it syncs 
   pack to some test namespace (typically on local CMS server), takes its CIs-Relations roster snap-shot 
   and compares it against the roster in the corresponding validation yaml.
   Usage:
     ruby test/packs.rb [--skip-sync] [--keep-ns] [-V] [-o|--pack-location PATH] PACK_SOURCE/PACK_NAME 

   Examples.
   Load 'test-pack' pack (from default location: 'fixtures' dir) into '/public/testns' namespace
   (CMS site is specified by CMSAPI env variable - same as for circuit commands) and compare 
   against test pack validation yaml (in the same default location: *_valiadation.yaml), use '-V'
   for verbose mode:
     ruby test/packs.rb -V testns/test-pack
   
   Same but point to different "fixtures" location for pack file and its validation:
     ruby test/packs.rb -o /path_to_fixtures testns/test-pack
   
   Same but do not sync pack (assuming pack was already loaded into '/public/testns' before):
     ruby test/packs.rb --skip-sync testns/test-pack

   Same as first one but do not drop the existing namespace (will force '--reload'
   for pack sync):
     ruby test/packs.rb --keep-ns testns/test-pack

2. Compares two same loaded (i.e. "target") packs in 2 different locations.  Basically, instead
   of using valiation yaml for "master", use an existing loaded pack in some trusted (to be right)
   location (e.g. staging or even prod server).  In this mode it will optionally syncs pack to
   some test namespace (typically on local CMS server), takes its CIs-Relations roster snap-shot 
   and compares it against the roster in another ("master") location.  Technically, this mode can
   be used to generally compare two loaded packs (i.e. 2 versions of the same pack or same pack in
   two different sites, e.g. pack on stg vs pack in prod).

   Usage:
     ruby test/packs.rb [--skip-sync] [--keep-ns] [-V] -m|--master-site MASTER_SITE
                        [-o|--pack-location PATH] [-p|--master--pack]                        
                        PACK_SOURCE/PACK_NAME/PACK_VERSION 

   Examples.
   Load 'mssql' pack (from default location: 'fixtures' dir) into '/public/testns' namespace
   (CMS site is specified by CMSAPI env variable - same as for circuit commands) and compare
   with the same pack loaded on the same server in its circuit namespace:
     ruby test/packs.rb  -o /path_to_circuit/packs -m http://localhost:8080/ -p oneops/mssql/1 testns/mssql/1

   Compare 'mssql' pack on local cMS (i.e. based on CMSAPI environment variable) to the same
   pack on stg:
     ruby test/packs.rb --skip-sync -m http://STG_CMS_ADAPTER_IP:8080/ oneops/mssql/1

   Compare version '1.1.5' of 'tomcat' pack on stg with version '1.1.6' of 'tomcat' pack locally: 
     ruby test/packs.rb --skip-sync -m http://STG_CMS_ADAPTER_IP:8080/ -p oneops/tomcat/1.1.5 oneops/tomcat/1.1.6


All examples here assumes oneops-admin root as pwd.
  HELP
    exit(0)
  elsif a.start_with?('-')
    error("Unknown option '#{a}'")
  end
end

pack_full_name = ARGV[0]
unless pack_full_name
  error('specify pack name')
end

if master_site.nil?
  match_pack_against_mater_yaml(pack_full_name, pack_location)
else
  ActiveResource::Base.site = site
  master_site = site if master_site.blank?
  match_pack_against_server(pack_full_name, pack_location, master_site, master_pack_full_name.presence || pack_full_name)
end
