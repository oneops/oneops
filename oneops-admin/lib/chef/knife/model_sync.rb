require 'cms'
require 'chef/cookbook_loader'
require 'fog'
require 'kramdown'

class Chef
  class Knife
    class ModelSync < Chef::Knife::CookbookMetadata

      banner "Loads class and relation metadata into CMS\nUsage: \n   knife model sync [COOKBOOKS...] (options)"

      @object_store_connection = nil
      @remote_dir              = nil

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

      option :relations,
             :short       => "-r",
             :long        => "--relations",
             :description => "Sync metadata for all relation cookbooks, rather than just a single cookbook"

      option :cookbook_path,
             :short       => "-o PATH:PATH",
             :long        => "--cookbook-path PATH:PATH",
             :description => "A colon-separated path to look for cookbooks in",
             :proc        => lambda {|o| o.split(":")}

      option :cms_trace,
             :short       => "-t",
             :long        => "--trace",
             :description => "Raw HTTP debug trace for CMS calls"


      def run
        ENV['CMS_TRACE'] = 'true' if config[:cms_trace]

        config[:cookbook_path] ||= Chef::Config[:cookbook_path]
        config[:register]      ||= Chef::Config[:register]
        config[:version]       ||= (Chef::Config[:version] || '1.0.0')

        if config[:all]
          cookbooks = Chef::CookbookLoader.new(config[:cookbook_path]).load_cookbooks.keys
        else
          cookbooks = @name_args
        end

        if cookbooks.present?
          models = cookbooks.inject([]) do |a, cookbook|
            Array(config[:cookbook_path]).reverse.inject(a) do |aa, path|
              file = File.expand_path(File.join(path, cookbook, 'metadata.rb'))
              File.exists?(file) ? (aa + sync_cookbook_metadata(cookbook, file)) : aa
            end
          end
          ui.warn('Nothing to do - no matching metadata definitions found.') unless models.present?
        else
          ui.error 'You must specify cookbook name(s) or use the --all option to sync all.'
          exit(1)
        end
      end

      def gen_doc(md, f)
        if !Chef::Config.has_key?("object_store_provider") ||
          Chef::Config[:object_store_provider].nil? || Chef::Config[:object_store_provider].empty?
          puts "skipping doc - no object_store_provider"
          return
        end
        class_name  = build_md_name(md.name)
        class_parts = class_name.split('.')
        # handle pre-versioned classes
        if class_parts.size < 2
          class_name = class_parts.last
        end
        get_remote_dir
        doc_dir = f.gsub("metadata.rb", "doc")

        image_groupings = []
        md.groupings.each do |group_name, group_properties|
          group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
            puts "package_name: #{package_name}"
            if package_name =~ /service|notification|relay/
              image_groupings.push(package_name.gsub("account.", ""))
            end

          end
        end

        initial_dir = Dir.pwd
        if File.directory? doc_dir
          Dir.chdir doc_dir
          Dir.glob("**/*").each do |file|
            remote_file = class_name + '/' + file
            local_file  = doc_dir + '/' + file
            if file =~ /\.md$/
              content = Kramdown::Document.new(File.read(local_file)).to_html
              remote_file.gsub!(".md", ".html")
            else
              content = File.open(local_file)
            end

            puts "doc: #{local_file} remote: #{remote_file}"
            obj = {:key => remote_file, :body => content}
            if remote_file =~ /\.html/
              obj['content_type'] = 'text/html'
            end

            @remote_dir.files.create(obj)

            # components can be services, sinks, relays too
            if image_groupings.size > 0
              orig_remote = remote_file
              image_groupings.each do |g|
                if g =~ /cloud.service/
                  remote_file = "service." + orig_remote
                else
                  remote_file = g + "." + orig_remote
                end
                puts "doc: #{local_file} remote: #{remote_file}"
                @remote_dir.files.create(:key => remote_file, :body => content)
              end
            end

          end
        end
        Dir.chdir initial_dir
      end

      def sync_cookbook_metadata(cookbook, file)
        md = Chef::Cookbook::Metadata.new
        md.name(cookbook.capitalize)
        md.from_file(file)

        return [] if md.groupings.blank? # Nothing to do - just a placeholder metadata file.

        is_md_relation = md.groupings['default'][:relation]
        load_relations = config[:relations]
        if is_md_relation && load_relations
          models = build_relation_models(md)
        elsif !is_md_relation && !load_relations
          models = build_class_models(md)
          gen_doc(md, file)
        else
          return []
        end
        Log.debug(models.to_yaml) if Log.debug?
        if models.present?
          ok, error = (is_md_relation ? Cms::RelationMd : Cms::CiMd).bulk(models)
          if ok
            ui.info("\e[7m\e[32mSuccessfully synched models\e[0m")
          else
            ui.error("Failed to save models: #{error}")
            exit 1
          end
        else
          ui.info('Nothing to do - no model definitions found.')
        end

        return models
      rescue Exceptions::ObsoleteDependencySyntax, Exceptions::InvalidVersionConstraint => e
        ui.error "ERROR: The cookbook '#{cookbook}' contains invalid or obsolete metadata syntax in #{file}: #{e.message}"
        exit 1
      rescue Exception => e
        ui.error "Failed to process cookbook #{cookbook}: #{e}"
        exit 1
      end

      def build_class_models(md)
        ui.info("\n============> Building classes for \e[7m\e[34m #{md.name} \e[0m")
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

        ui.info("   #{cms_class.className}")

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

      def build_relation_models(md)
        ui.info("\n============> Building relations for \e[7m\e[34m #{md.name} \e[0m")
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

        ui.info("   #{cms_relation.relationName}")

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

      def get_remote_dir
        return @remote_dir if @remote_dir

        conn       = get_connection
        env_bucket = Chef::Config[:environment_name]

        @remote_dir = conn.directories.get(env_bucket)
        if @remote_dir.nil?
          @remote_dir = conn.directories.create(:key => env_bucket)
          puts "created #{env_bucket}"
        end
        puts "remote_dir:\n #{@remote_dir.inspect}"
      end

      def get_connection
        unless @object_store_connection
          object_store_provider = Chef::Config[:object_store_provider]
          case object_store_provider
            when 'OpenStack'
              @object_store_connection = Fog::Storage.new({:provider           => object_store_provider,
                                                           :openstack_username => Chef::Config[:object_store_user],
                                                           :openstack_api_key  => Chef::Config[:object_store_pass],
                                                           :openstack_auth_url => Chef::Config[:object_store_endpoint]})
            when 'Local'
              @object_store_connection = Fog::Storage.new({:provider   => object_store_provider,
                                                           :local_root => Chef::Config[:object_store_local_root]})
            else
              ui.error "Unsupported object_store_provider: #{object_store_provider}"
              exit 1
          end
        end
        return @object_store_connection
      end

      def build_md_name(name, package = nil)
        suffix = Chef::Config[:admin] ? '' : "#{config[:register]}.#{"#{config[:version].split('.').first}." if Chef::Config[:useversion]}"
        "#{"#{package}." if package}#{suffix}#{name[0].upcase}#{name[1..-1]}"
      end
    end
  end
end
