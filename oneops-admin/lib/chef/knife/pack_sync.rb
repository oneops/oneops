require 'chef/knife/base_sync'
require 'chef/knife/core/object_loader'

class Chef
  class Knife
    class PackSync < Chef::Knife
      include ::BaseSync

      VISIBILITY_ALT_NS_TAG = 'enableForOrg'

      banner "Loads packs into OneOps.\nUsage:\n   circuit pack [OPTIONS] [PACKS...]"

      option :all,
             :short       => "-a",
             :long        => "--all",
             :description => "Sync all packs"

      option :register,
             :short       => "-r REGISTER",
             :long        => "--register REGISTER",
             :description => "Specify the source register name to use during sync"

      option :version,
             :short       => "-v VERSION",
             :long        => "--version VERSION",
             :description => "Specify the source register version to use during sync"

      option :pack_path,
             :short       => "-o PATH:PATH",
             :long        => "--pack-path PATH:PATH",
             :description => "A colon-separated path to look for packs in",
             :proc        => lambda {|o| o.split(":")}

      option :reload,
             :long        => "--reload",
             :description => "Force pack sync even if digest signatue has not changed (not applicable for packs with semantic versioning)"

      option :clean,
             :long        => "--clean",
             :description => "Remove the current pack (and corresponding namespace) and then sync - 'fresh start' (not applicable for packs with semantic versioning)"

      option :semver,
             :long        => "--semver",
             :description => "Creates new patch version for each change"


      def run
        t1 = Time.now
        ENV['CMS_TRACE'] = 'true' if config[:cms_trace]

        config[:pack_path] ||= Chef::Config[:pack_path]
        config[:register]  ||= Chef::Config[:register]
        config[:version]   ||= Chef::Config[:version]
        config[:semver]    ||= ENV['SEMVER'].present?

        Chef::Pack.config = config
        @packs_loader ||= Knife::Core::ObjectLoader.new(Chef::Pack, ui)

        validate_packs   # safety measure: make sure no packs conflict in scope

        circuit_ns_path = get_packs_ns
        unless Cms::Namespace.first(:params => {:nsPath => circuit_ns_path})
          ui.error("Can't find namespace #{circuit_ns_path}. Please register your source first with the register command.")
          exit 1
        end

        if config[:all]
          files = config[:pack_path].inject([]) {|a, dir| a + Dir.glob("#{dir}/*.rb").sort}
        else
          files = @name_args.inject([]) {|a, pack| a << "#{pack}.rb"}
        end

        if files.blank?
          ui.error 'You must specify pack name(s) or use the --all option to sync all.'
          exit(1)
        end

        comments = "#{ENV['USER']}:#{$0} #{config[:msg]}"
        loaded_files = files.inject([]) {|a, f| a << f if sync_pack(f, comments); a}

        t2 = Time.now
        ui.info("\nProcessed #{files.size} files, loaded #{loaded_files.size} packs.\nDone at #{t2} in #{(t2 - t1).round(1)}sec")
      end

      def validate_packs
        pack_map = {}
        config[:pack_path].each do |dir|
          Dir.glob("#{dir}/*.rb").each do |file|
            pack = @packs_loader.load_from(config[:pack_path], file)
            key  = "#{get_source}**#{pack.name.downcase}**#{pack.version.presence || config[:version].split('.').first}"
            if pack_map.has_key?(key)
              ui.error("Conflict of pack source-name-version: #{key} is defined in #{file} and #{pack_map[key]}")
              exit 1
            else
              pack_map[key] = file
            end
          end
        end
      end


      private

      def get_source
        config[:register]
      end

      def get_packs_ns
        "#{Chef::Config[:nspath]}/#{get_source}/packs"
      end

      def get_pack_ns(pack)
        "#{get_packs_ns}/#{pack.name}/#{pack.version}"
      end

      def sync_pack(file, comments)
        @existing_pack_ci_map ||= Cms::Ci.all(:params => {:nsPath      => get_packs_ns,
                                                          :ciClassName => 'mgmt.Pack'}).
          inject({}) {|h, p| h[p.ciName.downcase] = p; h}

        pack = @packs_loader.load_from(config[:pack_path], file)
        pack_ci = @existing_pack_ci_map[pack.name.downcase]
        pack.name(pack_ci ? pack_ci.ciName : pack.name.downcase)   # This kludge is deal with legacy problem of some existing packs loaded but not converted to down case.

        if pack.ignore
          ui.info("Ignoring pack #{pack.name} version #{pack.version.presence || config[:version]}")
          return false
        elsif config[:semver] || pack.semver?
          signature = sync_pack_semver(pack, comments)
        else
          signature = sync_pack_no_semver(pack, comments)
        end

        sync_docs(pack)
        ui.info("Successfully synched pack #{pack.name} version #{pack.version} #{"[signature: #{signature}]" if signature}".green)

        return signature
      end

      def sync_pack_semver(pack, comments)
        ui.info("\n--------------------------------------------------")
        ui.info(" #{pack.name} #{pack.version} ".blue(true))
        ui.info('--------------------------------------------------')
        if config[:reload]
          ui.warn('Reload option is not available in semver mode, all pack versions are '\
                  'immutable. If you need to force a new patch version, make a change in '\
                  'the pack (i.e. pack description) or specify patch version explicitly.')
        end

        signature = check_pack_version_ver_update(pack)
        return false unless signature   # If pack signature matches nothing to do.

        Log.debug(pack.to_yaml) if Log.debug?

        version_ci = setup_pack_version(pack, comments, signature)

        begin
          ns = get_pack_ns(pack)

          # Upload design template
          sync_env(ns, 'mgmt.catalog', pack, '_default', pack.design_resources, comments)

          # Upload manifest templates
          pack.environments.each do |env, _|
            setup_mode(pack, env, comments)
            sync_env("#{ns}/#{env}", 'mgmt.manifest', pack, env, pack.environment_resources(env), comments)
          end
        rescue Exception => e
          ui.error(e.message)
          ui.info('Attempting to clean up...')
          begin
            version_ci.destroy
          rescue Exception
            ui.warn("Failed to clean up pack #{pack.name} version #{pack.version}!")
          end
          raise e
        end

        return signature
      end

      def sync_pack_no_semver(pack, comments)
        signature = Digest::MD5.hexdigest(pack.signature)

        pack.version((pack.version.presence || config[:version]).split('.').first)   # default to the global knife version if not specified

        ui.info("\n--------------------------------------------------")
        ui.info(" #{pack.name} ver.#{pack.version} ".blue(true))
        ui.info('--------------------------------------------------')

        pack_ci = @existing_pack_ci_map[pack.name.downcase]
        if pack_ci && config[:clean]
          @existing_pack_ci_map.delete(pack.name.downcase)
          pack_ci.destroy
        end

        # If pack signature matches but reload option is not set - bail
        return false if !config[:reload] && check_pack_version_no_ver_update(pack, signature)

        Log.debug(pack.to_yaml) if Log.debug?

        # First, check to see if anything from CMS need to flip to pending_deletion
        fix_delta_cms(pack)

        version_ci = setup_pack_version(pack, comments, '')

        ns = get_pack_ns(pack)

        # Upload design template
        sync_env(ns, 'mgmt.catalog', pack, '_default', pack.design_resources, comments)

        # Upload manifest templates
        pack.environments.each do |env, _|
          setup_mode(pack, env, comments)
          sync_env("#{ns}/#{env}", 'mgmt.manifest', pack, env, pack.environment_resources(env), comments)
        end

        version_ci.ciAttributes.commit = signature
        unless save(version_ci)
          ui.warn("Failed to update signature for pack #{pack.name} version #{pack.version}")
        end

        return signature
      end

      def fix_delta_cms(pack)
        nsPath  = get_pack_ns(pack)
        cmsEnvs = ['_default'] + Cms::Ci.all(:params => {:nsPath => nsPath, :ciClassName => 'mgmt.Mode'}).map(&:ciName)
        cmsEnvs.each do |env|
          relations = fix_rels_from_cms(pack, env)
          fix_ci_from_cms(pack, env, relations, cmsEnvs)
        end
      end

      def fix_rels_from_cms(pack, env = '_default')
        pack_rels   = pack.relations
        target_rels = []
        scope       = (env == '_default') ? '' : "/#{env}"
        Cms::Relation.all(:params => {:nsPath        => "#{get_pack_ns(pack)}#{scope}",
                                      :includeToCi   => true,
                                      :includeFromCi => true}).each do |r|
          new_state      = nil
          fromCiName     = r.fromCi.ciName
          toCiName       = r.toCi.ciName
          relationShort  = r.relationName.split('.').last
          key            = "#{fromCiName}::#{relationShort.scan(/[A-Z][a-z]+/).join('_').downcase}::#{toCiName}"
          exists_in_pack = pack_rels.include?(key)
          # Search through resource to determine if relation exists or not
          unless exists_in_pack
            case relationShort
              when 'Payload'
                exists_in_pack = pack.resources[fromCiName] && pack.resources[fromCiName].include?('payloads') &&
                  pack.resources[fromCiName]['payloads'].include?(toCiName)
              when 'WatchedBy'
                exists_in_pack = pack.resources[fromCiName] && pack.resources[fromCiName].include?('monitors') &&
                  pack.resources[fromCiName]['monitors'].include?(toCiName)
              when 'Requires'
                exists_in_pack = pack.resources[fromCiName] && pack.resources[toCiName]
              when 'Entrypoint'
                exists_in_pack = pack.entrypoints.include?(toCiName)
            end
          end

          target_rels.push(toCiName) if exists_in_pack && !target_rels.include?(toCiName)

          if exists_in_pack && r.relationState == 'pending_deletion'
            new_state = 'default'
          elsif !exists_in_pack && r.relationState != 'pending_deletion'
            new_state = 'pending_deletion'
          end

          if new_state
            r.relationState = new_state
            if save(r)
              ui.debug("Successfuly updated ciRelationState to #{new_state} #{r.relationName} #{r.fromCi.ciName} <-> #{r.toCi.ciName} for #{env}")
            else
              ui.error("Failed to update ciRelationState to #{new_state} #{r.relationName} #{r.fromCi.ciName} <-> #{r.toCi.ciName} for #{env}")
            end
          end
        end
        target_rels
      end

      def fix_ci_from_cms(pack, env, relations, environments)
        scope          = (env == '_default') ? '' : "/#{env}"
        pack_resources = pack.resources
        Cms::Ci.all(:params => {:nsPath => "#{get_pack_ns(pack)}#{scope}"}).each do |resource|
          new_state      = nil
          exists_in_pack = pack_resources.include?(resource.ciName) || relations.include?(resource.ciName) || environments.include?(resource.ciName)
          if exists_in_pack && resource.ciState == 'pending_deletion'
            new_state = 'default'
          elsif !exists_in_pack && resource.ciState != 'pending_deletion'
            new_state = 'pending_deletion'
          end
          if new_state
            resource.ciState = new_state
            if save(resource)
              ui.debug("Successfuly updated ciState to #{new_state} for #{resource.ciName} for #{env}")
            else
              ui.error("Failed to update ciState to #{new_state} for #{resource.ciName} for #{env}")
            end
          end
        end
      end

      def check_pack_version_ver_update(pack)
        all_versions = Cms::Ci.all(:params => {:nsPath       => "#{get_packs_ns}/#{pack.name}",
                                               :ciClassName  => 'mgmt.Version',
                                               :includeAltNs => VISIBILITY_ALT_NS_TAG})
        major, minor, patch = (pack.version.blank? ? config[:version] : pack.version).split('.')
        minor               = '0' if minor.blank?

        # Need to filter version for the same major and find latest patch version for the same minor.
        latest_patch        = nil
        latest_patch_number = -1
        versions            = all_versions.select do |ci_v|
          split = ci_v.ciName.split('.')
          if major == split[0] && minor == split[1] && split[2].to_i > latest_patch_number
            latest_patch        = ci_v
            latest_patch_number = split[2].to_i
          end
          major == split[0]
        end

        if versions.size > 0
          version_ci = latest_patch || versions.sort_by(&:ciName).last
          # Carry over 'enable' and 'visibility' from the latest patch or latest version overall.
          pack.enabled(version_ci.ciAttributes.attributes['enabled'] != 'false')
          pack.visibility(version_ci.altNs.attributes[VISIBILITY_ALT_NS_TAG])
        end

        if patch.present?
          # Check to make sure version does not already exist.
          version = "#{major}.#{minor}.#{patch}"
          if versions.find {|ci_v| ci_v.ciName == version}
            ui.warn("Pack #{pack.name} version #{pack.version} explicitly specified but it already exists, ignore it - will SKIP pack loading, but will try to update docs.")
            return nil
          else
            pack.version(version)
            ui.info("Pack #{pack.name} version #{pack.version} explicitly specified and it does not exist yet, will load.")
            return pack.signature
          end
        else
          ui.info("Pack #{pack.name} version #{pack.version} - patch version is not explicitly specified, continue with checking for latest patch version for it.")
        end

        if latest_patch
          pack.version(latest_patch.ciName)
          signature = pack.signature
          if latest_patch.ciAttributes.attributes['commit'] == signature
            ui.info("Pack #{pack.name} latest patch version #{latest_patch.ciName} matches signature (#{signature}), will skip pack loading, but will try to update docs.")
            return nil
          else
            ui.info("Pack #{pack.name} latest patch version #{latest_patch.ciName} signature is different from new pack signature #{signature}, will increment patch version and load.")
            pack.version("#{major}.#{minor}.#{latest_patch.ciName.split('.')[2].to_i + 1}")
            return pack.signature
          end
        else
          ui.info("No patches found for #{pack.name} version #{major}.#{minor}, start at patch 0 and load.")
          pack.version("#{major}.#{minor}.0")
          return pack.signature
        end
      end

      def check_pack_version_no_ver_update(pack, signature)
        pack_version = Cms::Ci.first(:params => {:nsPath => "#{get_packs_ns}/#{pack.name}", :ciClassName => 'mgmt.Version', :ciName => pack.version})
        if pack_version.nil?
          ui.info("Pack #{pack.name} version #{pack.version} not found")
          return false
        else
          if pack_version.ciAttributes.attributes.key?('commit') && pack_version.ciAttributes.commit == signature
            ui.info("Pack #{pack.name} version #{pack.version} matches signature #{signature}, use --reload to force load.")
            return true
          else
            ui.warn("Pack #{pack.name} version #{pack.version} signature is different from file signature #{signature}")
            return false
          end
        end
      end

      def setup_pack_version(pack, comments, signature)
        pack_ci = @existing_pack_ci_map[pack.name.downcase]
        packs_ns = get_packs_ns
        if pack_ci
          ui.debug("Updating pack #{pack.name}")
        else
          ui.info("Creating pack CI #{pack.name}")
          pack_ci = build('Cms::Ci',
                          :nsPath      => packs_ns,
                          :ciClassName => 'mgmt.Pack',
                          :ciName      => pack.name)
        end

        pack_ci.comments                 = comments
        pack_ci.ciAttributes.pack_type   = pack.type
        pack_ci.ciAttributes.description = pack.description
        pack_ci.ciAttributes.category    = pack.category
        pack_ci.ciAttributes.owner       = pack.owner

        if save(pack_ci)
          ui.debug("Successfuly saved pack CI #{pack.name}")
          @existing_pack_ci_map[pack.name.downcase] = pack_ci
          pack_version = Cms::Ci.first(:params => {:nsPath      => "#{packs_ns}/#{pack.name}",
                                                   :ciClassName => 'mgmt.Version',
                                                   :ciName      => pack.version})
          if pack_version
            ui.debug("Updating pack CI #{pack.name} version #{pack.version}")
          else
            ui.info("Creating pack CI #{pack.name} version #{pack.version}")
            pack_version = build('Cms::Ci',
                                 :nsPath       => "#{packs_ns}/#{pack.name}",
                                 :ciClassName  => 'mgmt.Version',
                                 :ciName       => pack.version,
                                 :ciAttributes => {:enabled => pack.enabled},
                                 :altNs        => {VISIBILITY_ALT_NS_TAG => pack.visibility})
          end

          pack_version.comments                 = comments
          pack_version.ciAttributes.description = pack.description
          pack_version.ciAttributes.commit      = signature

          if save(pack_version)
            ui.debug("Successfuly saved pack version CI for: #{pack.name} #{pack.version}")
            return pack_version
          else
            ui.error("Could not save pack version CI for: #{pack.name} #{pack.version}")
          end
        else
          ui.error("Could not save pack CI #{pack.name}")
        end
        message = "Unable to setup namespace for pack #{pack.name} version #{pack.version}"

        raise Exception.new(message)
      end

      def setup_mode(pack, env, comments)
        ns   = get_pack_ns(pack)
        mode = Cms::Ci.first(:params => {:nsPath => ns, :ciClassName => 'mgmt.Mode', :ciName => env})
        if mode
          ui.debug("Updating pack #{pack.name} version #{pack.version} environment mode #{env}")
        else
          ui.info("Creating pack #{pack.name} version #{pack.version} environment mode #{env}")
          mode = build('Cms::Ci',
                       :nsPath      => ns,
                       :ciClassName => 'mgmt.Mode',
                       :ciName      => env)
        end

        mode.comments                 = comments
        mode.ciAttributes.description = pack.description

        if save(mode)
          ui.debug("Successfuly saved pack mode CI #{env}")
          return mode
        else
          message = "Unable to setup environment namespace for pack #{pack.name} version #{pack.version} environment mode #{env}"
          ui.error(message)
          raise Exception.new(message)
        end
      end

      def sync_env(ns_path, package, pack, env, resources, comments)
        ui.info("======> #{env == '_default' ? 'design' : env}")
        Log.debug([pack.name, pack.version, package, ns_path, resources, comments].to_yaml) if Log.debug?

        platform = sync_platform(ns_path, package, pack, comments)
        if platform
          components = sync_components(package, ns_path, platform, resources, comments)
          %w(DependsOn ManagedVia SecuredBy).each do |relation_name|
            sync_relations(relation_name, package, ns_path, pack.env_relations(env, relation_name), components)
          end
          upload_template_entrypoint(ns_path, pack, resources, components, platform, env)
          upload_template_procedures(ns_path, pack, platform, env)
          upload_template_variables(ns_path, pack, package, platform, env)
          upload_template_policies(ns_path, pack, package, env)
          sync_monitors(package, ns_path, resources, components)
          sync_payloads(ns_path, resources, components) if package == 'mgmt.manifest'
        end
      end

      def sync_platform(nspath, package, pack, comments)
        ci_class_name = "#{package}.#{pack.type.capitalize}"
        platform      = Cms::Ci.first(:params => {:nsPath      => nspath,
                                                  :ciClassName => ci_class_name,
                                                  :ciName      => pack.name})
        if platform
          ui.debug("Updating #{ci_class_name}")
        else
          ui.info("Creating #{ci_class_name}")
          platform = build('Cms::Ci',
                           :nsPath      => nspath,
                           :ciClassName => ci_class_name,
                           :ciName      => pack.name)
        end

        plat_attrs = pack.platform && pack.platform[:attributes]
        if plat_attrs
          attrs = platform.ciAttributes.attributes
          attrs.each {|name, _| attrs[name] = plat_attrs[name] if plat_attrs.has_key?(name)}
        end

        platform.comments                 = comments
        platform.ciAttributes.description = pack.description
        platform.ciAttributes.source      = get_source
        platform.ciAttributes.pack        = pack.name
        platform.ciAttributes.version     = pack.version

        if save(platform)
          ui.debug("Successfuly saved #{ci_class_name}")
          return platform
        else
          ui.error("Could not save #{ci_class_name}, skipping pack")
          return false
        end
      end

      def sync_components(package, ns_path, platform, resources, comments)
        relations = []
        existing = Cms::Relation.all(:params => {:ciId              => platform.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'Requires',
                                                 :includeToCi       => true})

        resources.each do |resource_name, resource|
          class_name_parts     = resource[:cookbook].split('.')
          class_name_parts[-1] = class_name_parts[-1].capitalize
          class_name_parts     = class_name_parts.unshift(resource[:source]) if resource[:source]
          class_name_parts     = class_name_parts.unshift(package)
          ci_class_name        = class_name_parts.join('.')

          relation = existing.find {|r| r.toCi.ciName == resource_name && r.toCi.ciClassName == ci_class_name}

          if relation
            ui.debug("Updating resource #{resource_name}")
          else
            ui.info("Creating resource #{resource_name}")
            relation = build('Cms::Relation',
                             :relationName => 'mgmt.Requires',
                             :nsPath       => ns_path,
                             :fromCiId     => platform.ciId,
                             :toCiId       => 0,
                             :toCi         => build('Cms::Ci',
                                                    :nsPath      => ns_path,
                                                    :ciClassName => ci_class_name,
                                                    :ciName      => resource_name))
          end

          relation.comments                    = comments
          relation.toCi.comments               = comments
          relation.relationAttributes.template = resource_name # default value for template attribute is the resource name
          requires_attrs                       = resource[:requires]
          if requires_attrs
            attrs = relation.relationAttributes.attributes
            attrs.each {|name, _| attrs[name] = requires_attrs[name] if requires_attrs[name]}
          end

          component_attrs = resource[:attributes]
          if component_attrs
            attrs = relation.toCi.ciAttributes.attributes
            attrs.each {|name, _| attrs[name] = component_attrs[name] if component_attrs.has_key?(name)}
          end

          relations << relation
        end

        relations, error = Cms::Relation.bulk(relations)
        unless relations
          ui.error("Could not save components: #{error}")
          raise(error)
        end
        ui.info("synced #{relations.size} components")
        return relations.inject({}) {|h, r| h[r.toCi.ciName] = r.toCiId; h}
      end

      def sync_relations(short_name, package, ns_path, pack_rels, components)
        relation_name = "#{package}.#{short_name}"
        existing_rels = Cms::Relation.all(:params => {:nsPath       => ns_path,
                                                      :relationName => relation_name})
        relations = pack_rels.inject([]) do |rels_to_save, pack_rel|
          from     = pack_rel[:from_resource]
          to       = pack_rel[:to_resource]
          from_id  = components[from]
          to_id    = components[to]
          problems = []
          problems << "component #{from} not found" unless from_id
          problems << "component #{to} not found" unless to_id
          if problems.present?
            ui.warn("Can't process #{short_name} from #{from} to #{to}: #{problems.join('; ')}")
            next rels_to_save
          end

          relation = rels_to_save.find {|d| d.fromCiId == from_id && d.toCiId == to_id}
          if relation
            ui.debug("Updating again #{short_name} from #{from} to #{to}")
          else
            relation = existing_rels.find {|d| d.fromCiId == from_id && d.toCiId == to_id}
            if relation
              ui.debug("Updating #{short_name} from #{from} to #{to}")
            else
              ui.info("Creating #{short_name} between #{from} to #{to}")
              relation = build('Cms::Relation',
                               :relationName => relation_name,
                               :nsPath       => ns_path,
                               :fromCiId     => from_id,
                               :toCiId       => to_id)
            end
            rels_to_save << relation
          end
          relation.merge_attributes(pack_rel[:attributes])
          rels_to_save
        end

        if relations.present?
          relations, error = Cms::Relation.bulk(relations)
          unless relations
            ui.error("Could not save #{short_name} relations: #{error}")
            raise(error)
          end
          ui.info("synched #{relations.size} #{short_name} relations")
        end
      end

      def upload_template_entrypoint(nspath, pack, resources, components, platform, env)
        relation_name = 'mgmt.Entrypoint'
        relations     = Cms::Relation.all(:params => {:ciId         => platform.ciId,
                                                      :nsPath       => nspath,
                                                      :direction    => 'from',
                                                      :relationName => relation_name})
        resources.each do |resource_name, _|
          next unless pack.environment_entrypoints(env)[resource_name]
          entrypoint = relations.find {|r| r.toCi.ciId == components[resource_name]}
          if entrypoint
            ui.debug("Updating entrypoint between platform and #{resource_name}")
          else
            ui.info("Creating entrypoint between platform and #{resource_name}")
            entrypoint = build('Cms::Relation',
                               :relationName => relation_name,
                               :nsPath       => nspath,
                               :fromCiId     => platform.ciId,
                               :toCiId       => components[resource_name])
          end

          entrypoint_attrs = pack.entrypoints[resource_name]['attributes']
          attrs            = entrypoint.relationAttributes.attributes
          attrs.each {|name, __| attrs[name] = entrypoint_attrs[name] if entrypoint_attrs[name]}

          if save(entrypoint)
            ui.debug("Successfuly saved entrypoint between platform and #{resource_name}")
          else
            ui.error("Could not save entrypoint between platform and #{resource_name}, skipping it")
          end
        end
      end

      def sync_monitors(package, ns_path, resources, components)
        relation_name = "#{package}.WatchedBy"
        ci_class_name = "#{package}.Monitor"
        relations     = Cms::Relation.all(:params => {:nsPath       => ns_path,
                                                      :relationName => relation_name,
                                                      :includeToCi  => true}).to_a

        resources.each do |resource_name, resource|
          next unless resource[:monitors]
          resource[:monitors].each do |monitor_name, monitor|
            relation = relations.find {|r| r.fromCiId == components[resource_name] && r.toCi.ciName == monitor_name}

            if relation
              ui.debug("Updating monitor #{monitor_name} for #{resource_name} in #{package}")
            else
              ui.info("Creating monitor #{monitor_name} for #{resource_name}")
              relation = build('Cms::Relation',
                               :relationName => relation_name,
                               :nsPath       => ns_path,
                               :fromCiId     => components[resource_name])
              # For legacy reasons, we might have monitors with same name, so several components
              # link (via relation) to the same CI in the pack template. Therefore,
              # monitor CI may already exists.
              duplicate_ci_name_rel = relations.find {|r| r.toCi.ciName == monitor_name}
              if duplicate_ci_name_rel
                ui.warn("Monitor #{monitor_name} for component #{resource_name} is not uniquely named, will re-use existing monitor CI with the same name")
                relation.toCiId = duplicate_ci_name_rel.toCiId
                if save(relation)
                  relation.toCi = duplicate_ci_name_rel.toCi
                else
                  ui.error("Could not create WatchedBy relation #{monitor_name} for #{resource_name}, skipping it")
                  next
                end
              else
                relation.toCiId = 0
                relation.toCi = build('Cms::Ci',
                                      :nsPath      => ns_path,
                                      :ciClassName => ci_class_name,
                                      :ciName      => monitor_name)
              end
              relations << relation
            end

            attrs = relation.toCi.ciAttributes.attributes
            attrs.each do |name, _|
              if monitor[name]
                monitor[name] = monitor[name].to_json if monitor[name].is_a?(Hash)
                attrs[name]   = monitor[name]
              end
            end

            if save(relation)
              ui.debug("Successfuly saved monitor #{monitor_name} for #{resource_name} in #{package}")
            else
              ui.error("Could not save monitor #{monitor_name} for #{resource_name}, skipping it")
            end
          end
        end
      end

      def sync_payloads(ns_path, resources, components)
        relation_name = 'mgmt.manifest.Payload'
        ci_class_name = 'mgmt.manifest.Qpath'
        relations     = Cms::Relation.all(:params => {:nsPath          => ns_path,
                                                      :relationName    => relation_name,
                                                      :targetClassName => ci_class_name,
                                                      :includeToCi     => true})
        existing_rels = relations.inject({}) {|h, r| h[r.toCi.ciName.downcase] = r; h}

        resources.each do |resource_name, resource|
          next unless resource[:payloads]
          resource[:payloads].each do |payload_name, payload|
            relation = relations.find {|r| r.toCi.ciName == payload_name && r.fromCiId == components[resource_name]}

            # For legacy reasons, we might have payloads with same name, so several components
            # link (via relation) to the same pyaload CI in the pack template. Therefore,
            # payload CI may already exists.
            duplicate_ci_name_rel = existing_rels[payload_name.downcase]
            if duplicate_ci_name_rel && (!relation || relation.fromCiId != duplicate_ci_name_rel.fromCiId)
              ui.warn("Payload #{payload_name} for component #{resource_name} is not uniquely named, will re-use existing payload CI with the same name")
            end

            if relation
              ui.debug("Updating payload #{payload_name} for #{resource_name}")
            else
              ui.info("Creating payload #{payload_name} for #{resource_name}")
              relation = build('Cms::Relation',
                               :relationName => relation_name,
                               :nsPath       => ns_path,
                               :fromCiId     => components[resource_name])
              if duplicate_ci_name_rel
                relation.toCiId = duplicate_ci_name_rel.toCiId
                unless save(relation)
                  ui.error("Could not create Payload relation #{payload_name} for #{resource_name}, skipping it")
                  next
                end
                relation.toCi = duplicate_ci_name_rel.toCi
              else
                relation.toCiId = 0
                relation.toCi = build('Cms::Ci',
                                      :nsPath      => ns_path,
                                      :ciClassName => ci_class_name,
                                      :ciName      => payload_name)
              end
            end

            attrs = relation.toCi.ciAttributes.attributes
            attrs.each {|name, _| attrs[name] = payload[name] if payload[name]}

            if save(relation)
              existing_rels[payload_name.downcase] = relation unless duplicate_ci_name_rel
              ui.debug("Successfuly saved payload #{payload_name} for #{resource_name}")
            else
              ui.error("Could not save payload #{payload_name} for #{resource_name}, skipping it")
            end
          end
        end
      end

      def upload_template_procedures(nspath, pack, platform, env)
        relation_name = 'mgmt.manifest.ControlledBy'
        ci_class_name = 'mgmt.manifest.Procedure'
        relations     = Cms::Relation.all(:params => {:ciId            => platform.ciId,
                                                      :nsPath          => nspath,
                                                      :direction       => 'from',
                                                      :relationName    => relation_name,
                                                      :targetClassName => ci_class_name,
                                                      :includeToCi     => true})
        pack.environment_procedures(env).each do |procedure_name, procedure_attributes|
          relation = relations.find {|r| r.toCi.ciName == procedure_name}
          if relation
            ui.debug("Updating procedure #{procedure_name} for environment #{env}")
          else
            ui.info("Creating procedure #{procedure_name} for environment #{env}")
            relation = build('Cms::Relation',
                             :relationName => relation_name,
                             :nsPath       => nspath,
                             :fromCiId     => platform.ciId,
                             :toCiId       => 0,
                             :toCi         => build('Cms::Ci',
                                                    :nsPath      => nspath,
                                                    :ciClassName => ci_class_name,
                                                    :ciName      => procedure_name))
          end

          attrs = relation.toCi.ciAttributes.attributes
          attrs.each do |name, _|
            if procedure_attributes[name]
              if name == 'arguments' && procedure_attributes[name].is_a?(Hash)
                procedure_attributes[name] = procedure_attributes[name].to_json
              end
              attrs[name] = procedure_attributes[name]
            end
          end

          if save(relation)
            ui.debug("Successfuly saved procedure #{procedure_name} for environment #{env}")
          else
            ui.error("Could not save procedure #{procedure_name} for environment #{env}, skipping it")
          end
        end
      end

      def upload_template_variables(nspath, pack, package, platform, env)
        relation_name = "#{package}.ValueFor"
        ci_class_name = "#{package}.Localvar"
        relations     = Cms::Relation.all(:params => {:ciId            => platform.ciId,
                                                      :direction       => 'to',
                                                      :relationName    => relation_name,
                                                      :targetClassName => ci_class_name,
                                                      :includeFromCi   => true})
        pack.environment_variables(env).each do |variable_name, var_attrs|
          relation = relations.find {|r| r.fromCi.ciName == variable_name}
          if relation
            ui.debug("Updating variable #{variable_name} for environment #{env}")
          else
            ui.info("Creating variable #{variable_name} for environment #{env}")
            relation = build('Cms::Relation',
                             :relationName => relation_name,
                             :nsPath       => nspath,
                             :toCiId       => platform.ciId,
                             :fromCiId     => 0,
                             :fromCi       => build('Cms::Ci',
                                                    :nsPath      => nspath,
                                                    :ciClassName => ci_class_name,
                                                    :ciName      => variable_name))
          end

          attrs = relation.fromCi.ciAttributes.attributes
          attrs.each {|name, _| attrs[name] = var_attrs[name] if var_attrs[name]}

          if save(relation)
            ui.debug("Successfuly saved variable #{variable_name} for environment #{env}")
          else
            ui.error("Could not save variable #{variable_name} for environment #{env}, skipping it")
          end
        end
      end

      def upload_template_policies(nspath, pack, package, env)
        ci_class_name = "#{package}.Policy"
        policies      = Cms::Ci.all(:params => {:nsPath      => nspath,
                                                :ciClassName => ci_class_name})
        pack.environment_policies(env).each do |policy_name, policy_attrs|
          policy = policies.find {|p| p.ciName == policy_name}
          unless policy
            policy = build('Cms::Ci',
                           :nsPath      => nspath,
                           :ciClassName => ci_class_name,
                           :ciName      => policy_name)
          end

          attrs = policy.ciAttributes.attributes
          attrs.each {|name, _| attrs[name] = policy_attrs[name] if policy_attrs[name]}

          if save(policy)
            ui.debug("Successfuly saved policy #{policy_name} attributes for environment #{env} and #{pack}")
          else
            ui.error("Could not save policy #{policy_name} attributes for environment #{env} and #{pack}, skipping it")
          end
        end
      end

      def sync_docs(pack)
        return unless sync_docs?

        doc_dir = File.expand_path('doc', File.dirname(pack.filename))
        files = Dir.glob("#{doc_dir}/#{pack.name}.*")
        if files.present?
          ui.info('docs and images:')
          files.each {|file| sync_doc_file(file, file.gsub(doc_dir, "#{get_source}/packs/#{pack.name}/#{pack.version}"))}
        end
      end
    end
  end
end
