require 'chef/knife/base_sync'
require 'chef/cookbook_loader'

class Chef
  class Knife
    class ModelSync < Chef::Knife::CookbookMetadata
      include ::BaseSync

      banner "Loads class and relation metadata into OneOps\nUsage: \n   circuit model [OPTIONS] [COOKBOOKS...]"

      option :all,
             :short       => "-a",
             :long        => "--all",
             :description => "Sync metadata for all class cookbooks, rather than just a single cookbook"

      option :register,
             :short       => "-r REGISTER",
             :long        => "--register REGISTER",
             :description => "Specify the source register name to use during uploads"

      option :version,
             :short       => "-v VERSION",
             :long        => "--version VERSION",
             :description => "Specify the source register version to use during uploads"

      option :classes,
             :long        => "--classes",
             :description => "Sync metadata for classes only (by default is ON if not specified and --relations not specified)"

      option :relations,
             :short       => "-r",
             :long        => "--relations",
             :description => "Sync metadata for relations only (by default is OFF if not specified)"

      option :cookbook_path,
             :short       => "-o PATH:PATH",
             :long        => "--cookbook-path PATH:PATH",
             :description => "A colon-separated path to look for cookbooks in",
             :proc        => lambda {|o| o.split(":")}


      def run
        t1 = Time.now
        ENV['CMS_TRACE'] = 'true' if config[:cms_trace]

        config[:cookbook_path] ||= Chef::Config[:cookbook_path]
        config[:register]      ||= Chef::Config[:register]
        config[:version]       ||= (Chef::Config[:version] || '1.0.0')

        if config[:all]
          cookbooks = config[:cookbook_path].inject([]) do |a, path|
            a + Chef::CookbookLoader.new(path).load_cookbooks.keys.sort
          end
        else
          cookbooks = @name_args
        end

        if cookbooks.blank?
          ui.error 'You must specify cookbook name(s) or use the --all option to sync all.'
          exit(1)
        end

        sync_relations = config[:relations]
        sync_classes   = config[:classes] || sync_relations.nil?

        models = []
        models += sync_cookbooks(cookbooks, true, false) if sync_classes
        models += sync_cookbooks(cookbooks, false, true) if sync_relations
        if models.present?
          ok, error = Cms::MdCache.reset
          ui.warn("Failed to tigger metadata cache reset: #{error}") unless ok
        else
          ui.warn('Nothing to do - no matching metadata definitions found.')
        end
        t2 = Time.now
        ui.info("\nProcessed #{cookbooks.size} cookbooks, resulting in #{models.size} models.\nDone at #{t2} in #{(t2 - t1).round(1)}sec")
      end


      private

      def sync_cookbooks(cookbooks, sync_classes, sync_relations)
        cookbooks.inject([]) do |a, cookbook|
          config[:cookbook_path].inject(a) do |aa, path|
            file = File.expand_path(File.join(path, cookbook, 'metadata.rb'))
            File.exists?(file) ? (aa + sync_cookbook_metadata(cookbook, file, sync_classes, sync_relations)) : aa
          end
        end
      end

      def sync_cookbook_metadata(cookbook, file, sync_classes, sync_relations)
        md = Chef::Cookbook::Metadata.new
        md.name(cookbook.capitalize)
        md.from_file(file)

        return [] if md.groupings.blank? # Nothing to do - just a placeholder metadata file.

        ui.info("\n--------------------------------------------------")
        ui.info("#{" #{md.name} ".blue(true)} #{sync_classes ? 'classes' : 'relations'}")
        ui.info('--------------------------------------------------')

        models = []
        if md.groupings['default'][:relation]
          models = build_model_relations(md) if sync_relations
        else
          if sync_classes
            models = build_model_classes(md)
            sync_docs(md, file)
          end
        end

        Log.debug(models.to_yaml) if Log.debug?
        if models.present?
          ok, error = (sync_classes ? Cms::CiMd : Cms::RelationMd).bulk(models)
          if ok
            ui.info('Successfully synched models'.green)
          else
            ui.error("Failed to save models: #{error}")
            exit 1
          end
        else
          ui.info("Nothing to do - no #{sync_classes ? 'class' : 'relation'} definitions found.")
        end

        return models
      rescue Exceptions::ObsoleteDependencySyntax, Exceptions::InvalidVersionConstraint => e
        ui.error "ERROR: The cookbook '#{cookbook}' contains invalid or obsolete metadata syntax in #{file}: #{e.message}"
        exit 1
      rescue Exception => e
        ui.error "Failed to process cookbook #{cookbook}: #{e}"
        exit 1
      end

      def build_model_classes(md)
        ui.info('models:')
        classes = []
        # must sync the base class first
        md.groupings.each do |group_name, group_properties|
          group_properties[:packages].select {|v| v == 'base'}.each do |package_name|
            classes << build_class(md, package_name, group_name, group_properties)
          end
        end
        md.groupings.each do |group_name, group_properties|
          group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
            classes << build_class(md, package_name, group_name, group_properties)
          end
        end
        return classes
      end

      def build_class(md, package, group, group_props)
        short_name = build_md_name(md.name)
        cms_class  = Cms::CiMd.new

        cms_class.className      = "#{package}.#{short_name}"
        cms_class.superClassName = "base.#{short_name}" unless package == 'base'

        ui.info(" - #{cms_class.className}")

        cms_class.impl        = group_props[:impl] || Chef::Config[:default_impl]
        cms_class.description = group_props[:description] || md.description

        namespace = group_props[:namespace]
        if namespace && !namespace.is_a?(TrueClass) && !namespace.is_a?(FalseClass)
          ui.error 'You must specify boolean value type for namespace attribute.'
          exit 1
        end
        cms_class.isNamespace = !!namespace

        # Who and why hacked this in?
        cloud_classes            = %w(mgmt.cloud.service cloud.service service)
        cms_class.useClassNameNS = true if cloud_classes.index {|s| cms_class.className.start_with?(s)} != nil

        cms_class.accessLevel   = group_props[:access] || 'global'
        cms_class.fromRelations = []
        cms_class.toRelations   = []
        cms_class.actions       = []

        cms_class.mdAttributes = md.attributes.inject([]) do |a, (name, properties)|
          if properties[:grouping] == group || (package == 'base' && !properties[:grouping])
            a << generate_class_attribute(name, properties)
          end
          a
        end

        cms_class.actions = md.recipes.inject([]) do |a, (recipe, properties)|
          action = {:actionName => recipe, :description => properties['description']}
          args   = properties['args']
          if args
            args = args.to_json if args.is_a?(Hash)
            JSON.parse(args) # check for valid json.
            action[:arguments] = args
          end
          a << action
        end

        return cms_class
      end

      def build_model_relations(md)
        ui.info('models:')
        relations = []
        # must sync the base relation first
        md.groupings.each do |group_name, group_properties|
          group_properties[:packages].select {|v| v == 'base'}.each do |package_name|
            relations << build_relation(md, package_name, group_name, group_properties)
          end
        end
        md.groupings.each do |group_name, group_properties|
          group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
            relations << build_relation(md, package_name, group_name, group_properties)
          end
        end
        return relations
      end

      def build_relation(md, package, group, group_props)
        short_name                = build_md_name(md.name)
        cms_relation              = Cms::RelationMd.new
        cms_relation.relationName = "#{package}.#{short_name}"
        cms_relation.description  = group_props[:description] || md.description
        cms_relation.mdAttributes = Array.new
        cms_relation.targets      = Array.new

        ui.info("#{cms_relation.relationName}")

        md.attributes.each do |name, properties|
          if properties[:relation_target]
            if properties[:package] && properties[:package] == package
              cms_relation.targets.push(generate_target(name, properties))
            end
          else
            if !properties[:grouping] || properties[:grouping] == group
              attribute = generate_relation_attribute(name, properties)
              cms_relation.mdAttributes.push(attribute)
            end
          end
        end
        return cms_relation
      end

      def generate_class_attribute(name, properties)
        attribute               = generate_attribute(name, properties)
        attribute.isInheritable = properties['inherit'] == 'no' ? false : true
        attribute.isEncrypted   = properties['encrypted'] || false
        attribute.isImmutable   = properties['immutable'] || false
        attribute
      end

      def generate_relation_attribute(name, properties)
        generate_attribute(name, properties)
      end

      def generate_attribute(name, properties)
        attribute               = Cms::AttrMd.new
        attribute.attributeName = name
        attribute.description   = properties['description']
        attribute.isMandatory   = properties['required'] == 'required'
        attribute.defaultValue  = properties['default'] || ''
        attribute.valueFormat   = properties['format'].is_a?(Hash) ? properties['format'].to_json : properties['format']
        attribute.dataType      = properties['data_type']
        unless properties['data_type']
          case properties['type']
            when 'array'
              attribute.dataType = 'enum'
            when 'hash'
              attribute.dataType = 'object'
            else
              attribute.dataType = 'string'
          end
        end

        return attribute
      end

      def generate_target(name, properties)
        target               = Cms::TargetMd.new
        target.fromClassName = properties[:from_class]
        target.toClassName   = properties[:to_class]
        target.linkType      = properties[:link_type]
        target.isStrong      = (properties['required'] == 'required')
        target.description   = name
        return target
      end

      def sync_docs(md, md_file)
        return unless sync_docs?

        doc_dir = md_file.gsub(/metadata\.rb$/, 'doc')
        files   = Dir.glob("#{doc_dir}/**/*")
        if files.present?
          ui.info('docs and images:')
          files.each {|file| sync_doc_file(file, file.gsub(doc_dir, build_md_name(md.name)))}
        end
      end

      def build_md_name(name)
        suffix = Chef::Config[:admin] ? '' : "#{config[:register]}.#{"#{config[:version].split('.').first}." if Chef::Config[:useversion]}"
        "#{suffix}#{name[0].upcase}#{name[1..-1]}"
      end
    end
  end
end
