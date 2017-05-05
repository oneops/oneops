require 'chef/knife'
require 'chef/exceptions'
require 'fog'
require 'rubygems'
require 'kramdown'
require 'bundler'
require 'cms'

ENV['BUNDLE_GEMFILE'] ||= File.dirname(__FILE__) + '/../../../Gemfile'
require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])

Bundler.setup(:default)

class Chef
  class Knife
    class ModelSync < Chef::Knife::CookbookMetadata
      
      banner "knife model sync [COOKBOOKS...] (options)"

      @object_store_connection = nil
      @remote_dir = nil
      
      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Sync metadata for all class cookbooks, rather than just a single cookbook"

      option :register,
        :short => "-r REGISTER",
        :long => "--register REGISTER",
        :description => "Specify the source register name to use during uploads"
        
      option :version,
        :short => "-v VERSION",
        :long => "--version VERSION",
        :description => "Specify the source register version to use during uploads"
              
      option :relations,
        :short => "-r",
        :long => "--relations",
        :description => "Sync metadata for all relation cookbooks, rather than just a single cookbook"

      option :cookbook_path,
         :short => "-o PATH:PATH",
         :long => "--cookbook-path PATH:PATH",
         :description => "A colon-separated path to look for cookbooks in",
         :proc => lambda { |o| o.split(":") }

      def gen_doc(md,f)
         if !Chef::Config.has_key?("object_store_provider") ||
             Chef::Config[:object_store_provider].nil? || Chef::Config[:object_store_provider].empty?
           puts "skipping doc - no object_store_provider"
           return
         end
         class_name =  generatename(md.name)
         class_parts = class_name.split(".")
         # handle pre-versioned classes
         if class_parts.size < 2
           class_name = class_parts.last
         end         
         remote_dir = get_remote_dir
         doc_dir = f.gsub("metadata.rb","doc")
                    
        image_groupings = []
        md.groupings.each do |group_name,group_properties|
         group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
            puts "package_name: #{package_name}"
            if package_name =~ /service|notification|relay/
               image_groupings.push(package_name.gsub("account.",""))
            end

          end
        end
         
         initial_dir = Dir.pwd
         if File.directory? doc_dir 
           Dir.chdir doc_dir
           Dir.glob("**/*").each do |file| 
             remote_file =  class_name  + '/' + file
             local_file = doc_dir + '/' + file
             if file =~ /\.md$/
                content = Kramdown::Document.new(File.read(local_file)).to_html
                remote_file.gsub!(".md",".html")
             else
                content = File.open(local_file)
             end

             puts "doc: #{local_file} remote: #{remote_file}"
             obj = { :key => remote_file, :body => content }
             if remote_file =~ /\.html/
               obj['content_type'] = 'text/html'
             end
             
             file = @remote_dir.files.create obj
             
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
                 file = @remote_dir.files.create :key => remote_file, :body => content
               end
             end

           end
         end
         Dir.chdir initial_dir
      end 

      def generate_metadata_from_file(cookbook, file)
        
        config[:register] ||= Chef::Config[:register]
        config[:version] ||= Chef::Config[:version]
        config[:version] ||='1.0.0'
        ui.info("Processing metadata for #{cookbook} from #{file}")
        md = Chef::Cookbook::Metadata.new
        md.name(cookbook.capitalize)
        md.from_file(file)
        Chef::Log.debug(md.to_yaml)
        
        # sync_class and sync_relation return boolean
        success = false
        if md.groupings['default'][:relation]
          if config[:relations]
            success = sync_relation_from_md(md)
          else
            ui.info("Skipping metadata for relations #{cookbook} since --relations option is not specified")
            success = true
          end
        else
          if config[:relations]
            ui.info("Skipping metadata for class #{cookbook} since --relations option is specified")
            success = true
          else
            success = sync_class_from_md(md)
          end
        end
        if !success
          ui.error("exiting")
          exit 1
        end

        gen_doc(md, file)
       
  
      rescue Exceptions::ObsoleteDependencySyntax, Exceptions::InvalidVersionConstraint => e
        STDERR.puts "ERROR: The cookbook '#{cookbook}' contains invalid or obsolete metadata syntax."
        STDERR.puts "in #{file}:"
        STDERR.puts
        STDERR.puts e.message
        exit 1
      end

      def sync_class_from_md(md)
        # must sync the base class first
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].select {|v| v == 'base'}.each do |package_name|
           find_class = findname(package_name, md.name)
           existing_class = find('Cms::CiMd',find_class)
            if existing_class.nil?
              ui.info("Creating class #{find_class}")
            else
              ui.info("Updating class #{find_class}")
            end
            cms_class = generate_class(existing_class,md,package_name,group_name)
            if save(cms_class)
              ui.info("Successfuly saved class #{cms_class.className}")
            else
              ui.error("Could not save class #{cms_class.className}")
              return false
            end
          end
        end
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
          find_class = findname(package_name, md.name)
            existing_class = find('Cms::CiMd',find_class)
            if existing_class.nil?
              ui.info("Creating class #{find_class}")
            else
              ui.info("Updating class #{find_class}")
            end
            cms_class = generate_class(existing_class,md,package_name,group_name)
            if save(cms_class)
              ui.info("Successfuly saved class #{cms_class.className}")
            else
              ui.error("Could not save class #{cms_class.className}")
              return false
            end
          end
        end
        return true
      end

      def sync_relation_from_md(md)
        # must sync the base relation first
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].select {|v| v == 'base'}.each do |package_name|
            find_relation = findname(package_name, md.name)
            #find_relation = Chef::Config[:admin] ? [package_name, md.name].join('.') : [package_name, config[:register],config[:version].split(".").first, md.name].join('.')
            existing_relation = find('Cms::RelationMd',find_relation)
            if existing_relation.nil?
              ui.info("Creating relation #{find_relation}")
            else
              ui.info("Updating relation #{find_relation}")
            end
            cms_relation = generate_relation(existing_relation,md,package_name,group_name)
            if save(cms_relation)
              ui.info("Successfuly saved relation #{cms_relation.relationName}")
            else
              ui.error("Could not save class #{cms_relation.relationName}")
              return false
            end
          end
        end
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].reject {|v| v == 'base'}.each do |package_name|
            #find_relation = Chef::Config[:admin] ? [package_name, md.name].join('.') : [package_name, config[:register],config[:version].split(".").first, md.name].join('.')
            find_relation = findname(package_name, md.name)
            existing_relation = find('Cms::RelationMd',find_relation)
            if existing_relation.nil?
              ui.info("Creating relation #{find_relation}")
            else
              ui.info("Updating relation #{find_relation}")
            end
            cms_relation = generate_relation(existing_relation,md,package_name,group_name)
            if save(cms_relation)
              ui.info("Successfuly saved relation #{cms_relation.relationName}")
            else
              ui.error("Could not save class #{cms_relation.relationName}")
              return false
            end
          end
        end
        return true
      end
      

      def generate_class(existing_class,md,package,group)
        props = md.groupings[group]
        cms_class = existing_class || Cms::CiMd.new()
        full_name = generatename(md.name)

        #full_name = Chef::Config[:admin] ? md.name : [ config[:register],config[:version].split(".").first, md.name ].join('.')
        cms_class.impl = props[:impl] || Chef::Config[:default_impl]  
        
        cms_class.className = [package, full_name].join('.')
        cms_class.description = props[:description] ? props[:description] : md.description
        cms_class.superClassName = ['base', full_name].join('.') unless package == 'base'
        if !props[:namespace].nil? && !(props[:namespace].class.to_s == "TrueClass" || props[:namespace].class.to_s == "FalseClass")
          ui.error "You must specify boolean value type for namespace attribute."
          exit 1
        end
        cms_class.isNamespace = props[:namespace] ? true : false

        cloud_classes = %w(mgmt.cloud.service cloud.service service)
        cms_class.useClassNameNS = true if cloud_classes.index { |s| cms_class.className.start_with?(s) } != nil

        cms_class.accessLevel = props[:access] ? props[:access] : 'global'
        cms_class.mdAttributes = Array.new
        cms_class.fromRelations = Array.new
        cms_class.toRelations = Array.new
        cms_class.actions = Array.new
        
        md.attributes.each do |name,properties|
          if package == 'base'
            if !properties[:grouping]
              attribute = generate_attribute(name,properties)
              cms_class.mdAttributes.push(attribute)
            end
          else
            if properties[:grouping] && properties[:grouping] == group
              attribute = generate_attribute(name,properties)
              cms_class.mdAttributes.push(attribute)
            end
          end
        end

        md.recipes.each do |recipe,properties|
          if properties['args'].nil?
            cms_class.actions.push({ :actionName => recipe, :description => properties})
          else
            properties['args'] = properties['args'].to_json if (properties['args'].is_a?(Hash))
            JSON.parse(properties['args'])
            cms_class.actions.push({ :actionName => recipe, :description => properties['description'], :arguments => properties['args']})
          end
        end
	
        return cms_class
      end

      def generate_relation(existing_relation,md,package,group)      
        cms_relation = existing_relation || Cms::RelationMd.new()
        full_name = generatename(md.name)
        #full_name = Chef::Config[:admin] ? md.name : [ config[:register],config[:version].split(".").first, md.name ].join('.')
        props = md.groupings[group]
        
        cms_relation.relationName = [package, full_name].join('.')
        cms_relation.description = props[:description] ? props[:description] : md.description
        cms_relation.mdAttributes = Array.new
        cms_relation.targets = Array.new
        md.attributes.each do |name,properties|
          if properties[:relation_target]
            if properties[:package] && properties[:package] == package
              cms_relation.targets.push( generate_target(name,properties) )
            end
          else
            if !properties[:grouping] or properties[:grouping] == group
              attribute = generate_attribute(name,properties)
              attribute.attributes.delete(:isInheritable)
              attribute.attributes.delete(:isEncrypted)
              attribute.attributes.delete(:isImmutable)
              cms_relation.mdAttributes.push(attribute)
            else
              # if properties[:grouping] == group
                # attribute = generate_attribute(name,properties)
                # cms_relation.mdAttributes.push(attribute)
              # end
            end
          end
        end
        return cms_relation
      end

      def generate_attribute(name,properties)
        attribute = Cms::AttrMd.new()
        attribute.attributeName = name
        attribute.description = properties['description']
        attribute.isMandatory = properties['required'] == 'required' ? true : false
        attribute.isInheritable = properties['inherit'] == 'no' ? false : true
        attribute.isEncrypted = properties['encrypted'] || false
        attribute.isImmutable = properties['immutable'] || false
        attribute.defaultValue = properties['default'] || ''
        attribute.valueFormat = properties['format'].is_a?(Hash) ? properties['format'].to_json : properties['format']
        case properties['type']
        when 'array'
          attribute.dataType = 'enum'
        when 'hash'
          attribute.dataType = 'object'
        else
          attribute.dataType = 'string'
        end
        attribute.dataType = properties['data_type'] if properties['data_type']
        return attribute
      end

      def generate_target(name,properties)
        target = Cms::TargetMd.new()
        target.fromClassName = generatename(properties[:from_class])
        target.toClassName = generatename(properties[:to_class])
          
        #target.fromClassName = Chef::Config[:admin] ? properties[:from_class] : [ config[:register], config[:version].split(".").first,properties[:from_class] ].join('.')
        #target.toClassName = Chef::Config[:admin] ? properties[:to_class] : [ config[:register], config[:version].split(".").first,properties[:to_class] ].join('.')
        target.linkType = properties[:link_type]
        target.isStrong = properties['required'] == 'required' ? true : false
        target.description = name
        return target
      end

      def get_remote_dir
          if !@remote_dir.nil?
             return @remote_dir
          end
          
          conn = get_connection
          env_bucket = Chef::Config[:environment_name]

          @remote_dir = conn.directories.get env_bucket
          if @remote_dir.nil?
            @remote_dir = conn.directories.create :key => env_bucket
            puts "created #{env_bucket}"
          end
          puts "remote_dir:\n #{@remote_dir.inspect}"

      end
      
      def get_connection
        
        if !@object_store_connection.nil?
          return @object_store_connection
        end
        object_store_provider = Chef::Config[:object_store_provider]

        case object_store_provider
        when "OpenStack"   
          conn = Fog::Storage.new({
            :provider            => object_store_provider,
            :openstack_username  => Chef::Config[:object_store_user],
            :openstack_api_key   => Chef::Config[:object_store_pass],
            :openstack_auth_url  => Chef::Config[:object_store_endpoint]
          })
        when "Local"
          conn = Fog::Storage.new({
            :provider    => object_store_provider,
            :local_root  => Chef::Config[:object_store_local_root]
          })
        end
        
        if conn.nil?
          puts "unsupported provider: #{object_store_provider}"
          exit 1
        end		    
        @object_store_connection = conn
        
        return conn        
      end
      
      def run
        config[:cookbook_path] ||= Chef::Config[:cookbook_path]
        if config[:all]
          cl = Chef::CookbookLoader.new(config[:cookbook_path])
          cl.load_cookbooks
          cl.each do |cname, cookbook|
            generate_metadata(cname.to_s)
          end
        
         begin
            res = Cms::MdCache.cache_refresh 
            unless res.nil?
              ui.info("Metadata cache status update http response code : #{res.code}")
            end
         rescue Exception => e
          STDERR.puts(e.inspect)
         end   
        
        else
          cookbook_name = @name_args[0]
          if cookbook_name.nil? || cookbook_name.empty?
            ui.error "You must specify the cookbook to generate metadata for, or use the --all option."
            exit 1
          end
          generate_metadata(cookbook_name)
        end
      end
      
      def find(klass, options)
        begin
          object = klass.constantize.find(options)
        rescue ActiveResource::BadRequest, ActiveResource::ResourceNotFound, ActiveResource::ResourceInvalid
          Log.debug("#{options.to_s} not found!")
        rescue Exception => e
          STDERR.puts(e.response.inspect)
          exit 1
        end
        object ? object : nil
      end
      
      def save(object)
        begin
          ok = object.save
        rescue Exception => e
          Log.debug(e.response.read_body)
        end
        ok ? object : false
      end

      def destroy(object)
        begin
          ok = object.destroy
        rescue Exception => e
          Log.debug(e.response.read_body)
        end
        ok ? object : false
      end

      def build(klass, options)
        begin
          object = klass.constantize.build(options)
        rescue Exception => e
          Log.debug(e.response.read_body)
        end
        object ? object : false
      end
      
      def findname(package_name, classname)
       if Chef::Config[:admin]
          [package_name, classname].join('.') 
       else
          if Chef::Config[:useversion] 
            [package_name, config[:register], config[:version].split(".").first, classname].join('.') 
          else 
           [package_name, config[:register], classname].join('.')
          end
       end
      end 
      
      def generatename(name)
       if Chef::Config[:admin]
          name
       else
          if Chef::Config[:useversion] 
            [config[:register], config[:version].split(".").first, name].join('.') 
          else 
           [config[:register], name].join('.')
          end
       end
      end           

            
    end
  end
end
