require 'chef/config'
require 'chef/mixin/params_validate'
require 'chef/mixin/from_file'
#require 'chef/couchdb'
#require 'chef/index_queue'
require 'chef/mash'
require 'chef/json_compat'
require 'chef/search/query'



class Chef
  class Cloud

    include Chef::Mixin::FromFile
    include Chef::Mixin::ParamsValidate
    #include Chef::IndexQueue::Indexable
    # Create a new Chef::Service object.
    def initialize(couchdb=nil)
      @name = ''
      @description = ''
      @auth = ''
      @is_location = 'false'
      @services = Mash.new
      @ignore = false
    end

    def name(arg=nil)
      set_or_return(
        :name,
        arg,
        :regex => /^[\-[:alnum:]_]+$/
      )
    end

    def description(arg=nil)
      set_or_return(
        :description,
        arg,
        :kind_of => String
      )
    end

    def auth(arg=nil)
      set_or_return(
        :auth,
        arg,
        :kind_of => String
      )
    end

    def is_location(arg=nil)
      set_or_return(
        :is_location,
        arg,
        :kind_of => String
      )
    end

    def service(name, options)
      validate(
          options,
          {
              :cookbook => { :kind_of => String },
              :attributes => { :kind_of => Hash },
              :provides => { :kind_of => Hash }
          }
        )
      @services[name] = options
      @services[name]
    end

    def ignore(arg=nil)
      set_or_return(
        :ignore,
        arg,
        :kind_of => [ TrueClass, FalseClass ], :default => false
      )
    end

    def to_hash
      result = {
        "name" => @name,
        "description" => @description,
        "auth" => @auth,
        "services" => @services,
        "ignore" => @ignore
      }
      result
    end

    # Serialize this object as a hash
    def to_json(*a)
      to_hash.to_json(*a)
    end

    def update_from!(o)
      description(o.description)
      auth(o.auth)
      services(o.services) if defined?(o.services)
      ignore(o.ignore)
      self
    end

    # Create a Chef::Cloud from JSON
    def self.json_create(o)
      cloud = new
      cloud.name(o["name"])
      cloud.description(o["description"])
      cloud.auth(o["auth"])
      cloud.services(o["services"])
      cloud.ignore(o["ignore"])
      cloud
    end

    # Sync this service via the REST API
    def sync( options = {} )

      nspath = "#{Chef::Config[:nspath]}/#{options[:register]}/clouds"

      unless ensure_path_exists(nspath)
        return false
      end

      Chef::Log.info("Starting sync for cloud #{name} in namespace #{nspath}")

      if (options[:reload])
        Chef::Log.info("Deleting cloud #{self.name} because reload is specified")
        cms_ci_delete( :nsPath => nspath, :ciClassName => 'mgmt.Cloud', :ciName => self.name )
      end

      # check if cloud is ignored, if so, simply return to skip syncing it
      # waiting until after reload so it can be removed before ignored.
      if self.ignore
        puts("Cloud, #{self.name} is set to ignore, skipping...")
        return true
      end

      cloud = Cms::Ci.build( :nsPath => nspath, :ciClassName => 'mgmt.Cloud', :ciName => self.name )
      cloud.ciAttributes.attributes = { "description" => description, "auth" => auth, "is_location" => is_location }
      cloud = cms_ci_sync(cloud)

      # process each service
      self.to_hash['services'].each do |service,options|
        ciClassName = options[:cookbook].capitalize
        if options[:source]
          ciClassName = [ 'mgmt.cloud.service', options[:source], ciClassName ].join('.')
        else
          ciClassName = [ 'mgmt.cloud.service', ciClassName ].join('.')
        end
        relationName = 'mgmt.Provides'
        relation = Cms::Relation.all( :params => {  :ciId => cloud.id,
          :nsPath => nspath,
          :direction => 'from',
          :relationName => relationName,
          :targetClassName => ciClassName,
          :includeToCi => true
        }).select { |r| r.toCi.ciName == service }.first

        if relation.nil?
          Chef::Log.info( "Creating service #{service}")
          relation = build('Cms::Relation',   :relationName => relationName,
                                              :nsPath => nspath,
                                              :fromCiId => cloud.id
                                 )
          ci = Cms::Ci.first( :params => { :nsPath => "#{nspath}/#{cloud.ciName}", :ciClassName => ciClassName, :ciName => service })

          if ci.nil?
            relation.toCiId = 0
            relation.toCi = build('Cms::Ci',  :nsPath => "#{nspath}/#{cloud.ciName}",
                                              :ciClassName => ciClassName,
                                              :ciName => service
                                           )
          else
            relation.toCiId = ci.id
            relation.toCi = ci
            Log.debug(relation.inspect)
            # if relation is missing, but ci is present, save the relation only first
            if cms_relation_sync(relation)
              Chef::Log.info("Successfuly saved service #{service}")
              relation = Cms::Relation.find(relation.id, :params => {  :nsPath => nspath, :includeToCi => true } )
            else
              Chef::Log.error("Could not save service #{service}, skipping it")
            end
          end
          unless relation
            Chef::Log.error("Could not build service #{service}, skipping it")
            next;
          end

        else
          Chef::Log.info("Updating service #{service}")
        end

        relation.comments = "#{ENV['USER']}:#{$0}"
        relation.toCi.comments = "#{ENV['USER']}:#{$0}"

        # provides relation attributes
        relation.relationAttributes.attributes.each do |name,value|
          if options[:provides][name]
            relation.relationAttributes.send(name+'=',options[:provides][name])
          end
        end

        # service attributes
        relation.toCi.ciAttributes.attributes.each do |name,value|
          if options[:attributes] && options[:attributes].has_key?(name)
            relation.toCi.ciAttributes.send(name+'=',options[:attributes][name])
          end
        end

        Log.debug(relation.inspect)
        if cms_relation_sync(relation)
          Chef::Log.info("Successfuly saved service #{service}")
        else
          Chef::Log.error("Could not save service #{service}, skipping it")
        end

        if options[:offerings]
          cms_offering_sync(options[:offerings],relation.toCi)
        end
      end

      Chef::Log.info("Completed sync for cloud #{name}!")
      self
    end

    # Load a service from disk - prefers to load the JSON, but will happily load
    # the raw rb files as well.
    def self.from_disk(name, force=nil)
      js_file = File.join(Chef::Config[:cloud_path], "#{name}.json")
      rb_file = File.join(Chef::Config[:cloud_path], "#{name}.rb")

      if File.exists?(js_file) || force == "json"
        Chef::JSONCompat.from_json(IO.read(js_file))
      elsif File.exists?(rb_file) || force == "ruby"
        cloud = Chef::Cloud.new
      cloud.name(name)
      cloud.from_file(rb_file)
      cloud
      else
        raise Chef::Exceptions::RoleNotFound, "Cloud '#{name}' could not be loaded from disk"
      end
    end

    # Sync all the services with cms from disk
    def self.sync_all( options = {} )
      Dir[File.join(Chef::Config[:cloud_path], "*.rb")].each do |cloud_file|
        short_name = File.basename(cloud_file, ".rb")
        r = Chef::Cloud.from_disk(short_name, "rb")
        r.sync(options)
      end
    end

    private

    def ensure_path_exists(nspath)
      ns = Cms::Namespace.all( :params => { :nsPath => nspath } ).first
      if ns.nil?
        Chef::Log.info( "Creating namespace #{nspath}")
        ns = Cms::Namespace.new( :nsPath => nspath )
        begin
          ok = ns.save
        rescue Exception => e
          Log.debug(e.response.read_body)
        end
        if ok
          Chef::Log.info("Successfuly saved namespace source #{nspath}")
          return ns
        else
          Chef::Log.error("Could not save namespace #{nspath}")
          return false
        end
      end
      return ns
    end

    def cms_ci_sync(ci)
      begin
        o = Cms::Ci.first( :params => ci.attributes )
        o.ciAttributes = ci.ciAttributes
        Chef::Log.info("Updating #{o.ciClassName} #{o.ciName}")
      rescue Exception => e
      #raise e unless e.response.code == "404"
        o = ci
        Chef::Log.info("Creating #{o.ciClassName} #{o.ciName}")
      end

      Chef::Log.debug(o.inspect)
      begin
        o.save
        Chef::Log.info("Successfully loaded #{o.ciClassName} #{o.ciName}")
      rescue Exception => e
        Chef::Log.debug(e.response.read_body)
        Chef::Log.error("Failed loading #{o.ciClassName} #{o.ciName}")
      false
      end
      o
    end

    def cms_ci_delete(attrs)
      begin
        cis = Cms::Ci.all( :params => { :nsPath => attrs[:nsPath], :ciClassName => attrs[:ciClassName] } )
        # TODO need to get rid of the select with better rest call
        ci = cis.select {|i| i.ciName == attrs[:ciName] }.first
        unless ci.nil?
          Chef::Log.debug(ci.inspect)
          ci.destroy
          Chef::Log.info("Successfully deleted #{ci.ciClassName} #{ci.ciName}")
        end
      rescue Exception => e
      #raise e unless e.response.code == "404"
        Chef::Log.info("Failed deleting #{ci.ciClassName} #{ci.ciName}")
        false
      end
    end

    def cms_relation_sync(r)
      name = "#{r.relationName} #{r.toCi.ciClassName} #{r.toCi.ciName}"
      begin
        list = Cms::Relation.all( :params => { :ciId => r.fromCiId, :direction => 'from', :relationName => r.relationName, :targetClassName => r.toCi.ciClassName } )
        # TODO need to get rid of the select with better rest call
        o = list.select {|i| i.toCi.ciName == r.toCi.ciName && i.toCi.nsPath == r.toCi.nsPath }.first
        raise "Not Found" unless o
        o.relationAttributes = r.relationAttributes
        o.toCi.ciAttributes = r.toCi.ciAttributes
        Chef::Log.info("Updating #{name}")
      rescue Exception => e
      #raise e unless e.response.code == "404"
        o = r
        Chef::Log.info("Creating #{name}")
      end

      Chef::Log.debug(o.inspect)
      begin
        o.save
        Chef::Log.info("Successfully loaded #{name}")
      rescue Exception => e
        Chef::Log.debug(e.response.read_body)
        Chef::Log.error("Failed loading #{name}")
      false
      end
      o
    end

    def cms_light_relation_sync(r)
      name = "#{r.inspect}"
      begin
        list = Cms::Relation.all( :params => { :ciId => r.fromCiId, :direction => 'from', :relationName => r.relationName } )
        # TODO need to get rid of the select with better rest call
        o = list.select {|i| i.toCiId == r.toCiId }.first
        raise "Not Found" unless o
        o.relationAttributes = r.relationAttributes
        Chef::Log.info("Updating #{name}")
      rescue Exception => e
      #raise e unless e.response.code == "404"
        o = r
        Chef::Log.info("Creating #{name}")
      end

      Chef::Log.debug(o.inspect)
      begin
        o.save
        Chef::Log.info("Successfully loaded #{name}")
      rescue Exception => e
        Chef::Log.debug(e.response.read_body)
        Chef::Log.error("Failed loading #{name}")
      false
      end
      o
    end

    def cms_offering_sync(o,service)
    ciClassName = "mgmt.cloud.Offering"
    relationName = "base.Offers"

    o.each do |name,offering|
      relation = Cms::Relation.all( :params => {  :ciId => service.id,
        :nsPath => '#{service.nsPath}/#{service.ciClassName}/#{service.ciName}',
        :direction => 'from',
        :relationName => relationName,
        :targetClassName => ciClassName,
        :includeToCi => true
      }).select { |r| r.toCi.ciName == name }.first

      if relation.nil?
        relation = build('Cms::Relation',   :relationName => relationName,
                                            :nsPath => "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}",
                                            :fromCiId => service.id
                               )

        ci = Cms::Ci.first( :params => { :nsPath => "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}", :ciClassName => ciClassName, :ciName => name })
        if ci.nil?
          relation.toCiId = 0
          relation.toCi = build('Cms::Ci',  :nsPath => "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}",
                                            :ciClassName => ciClassName,
                                            :ciName => name
                                         )
        else
          relation.toCiId = ci.id
          relation.toCi = ci
          Log.debug(relation.inspect)
          # if relation is missing, but ci is present, save the relation only first
          if cms_relation_sync(relation)
            Chef::Log.info("Successfuly saved offering #{name}")
            relation = Cms::Relation.find(relation.id, :params => {  :nsPath => ci.nsPath, :includeToCi => true } )
          else
            Chef::Log.error("Could not save offering #{name}, skipping it")
          end
        end
      end

      relation.comments = "#{ENV['USER']}:#{$0}"
      relation.toCi.comments = "#{ENV['USER']}:#{$0}"

      # offering attributes
      relation.toCi.ciAttributes.attributes.each do |attr,value|
        if offering && offering.has_key?(attr)
          relation.toCi.ciAttributes.send(attr+'=',offering[attr])
        end
      end

      if cms_relation_sync(relation)
        Chef::Log.info("Successfuly saved offering #{name}")
      else
        Chef::Log.error("Could not save offering #{name}, skipping it")
      end

    end
   end

    def build(klass, options)
      begin
        object = klass.constantize.build(options)
      rescue Exception => e
        Log.debug(e.response.read_body)
      end
      object ? object : false
    end

  end
end
