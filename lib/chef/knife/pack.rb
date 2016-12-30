#
require 'chef/config'
require 'chef/mixin/params_validate'
require 'chef/mixin/from_file'
#require 'chef/couchdb'
require 'chef/run_list'
#require 'chef/index_queue'
require 'chef/mash'
require 'chef/json_compat'
require 'chef/search/query'

class Chef
  class Pack

    include Chef::Mixin::FromFile
    include Chef::Mixin::ParamsValidate
    #include Chef::IndexQueue::Indexable

    attr_reader   :platform,
    		  :environments,
                  :resources,
                  :relations,
                  :serviced_bys,
                  :entrypoints,
                  :procedures,
                  :variables

    # DESIGN_DOCUMENT = {
      # "version" => 6,
      # "language" => "javascript",
      # "views" => {
        # "all" => {
          # "map" => <<-EOJS
          # function(doc) {
            # if (doc.chef_type == "pack") {
              # emit(doc.name, doc);
            # }
          # }
          # EOJS
        # },
        # "all_id" => {
          # "map" => <<-EOJS
          # function(doc) {
            # if (doc.chef_type == "pack") {
              # emit(doc.name, doc.name);
            # }
          # }
          # EOJS
        # }
      # }
    # }

    #attr_accessor :couchdb_rev, :couchdb
    #attr_reader :couchdb_id

    # Create a new Chef::Pack object.
    def initialize(couchdb=nil)
      @name = ''
      @description = ''
      @category = ''
      @version = ''
      @group_id = ''      
      @ignore = false
      @enabled = true
      @type = ''
      @platform = Hash.new
      @services = ''
      @environments = Mash.new
      @resources = Mash.new
      @relations = Mash.new
      @default_attributes = Mash.new
      @override_attributes = Mash.new
      @depends_on = Mash.new
      @managed_via = Mash.new
      @serviced_bys = Mash.new
      @entrypoints = Mash.new
      @procedures = Mash.new
      @variables = Mash.new
      @env_run_lists = {"_default" => Chef::RunList.new}
      @owner = ''
      @policies = Mash.new
      #@couchdb_rev = nil
      #@couchdb_id = nil
      #@couchdb = couchdb || Chef::CouchDB.new
    end

    def couchdb_id=(value)
      @couchdb_id = value
      self.index_id = value
    end

    def chef_server_rest
      Chef::REST.new(Chef::Config[:chef_server_url])
    end

    def self.chef_server_rest
      Chef::REST.new(Chef::Config[:chef_server_url])
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

    def owner(arg=nil)
      set_or_return(
        :owner,
        arg,
        :kind_of => String
      )
    end

    def category(arg=nil)
      set_or_return(
        :category,
        arg,
        :kind_of => String
      )
    end

    def group_id(arg=nil)
      set_or_return(
        :group_id,
        arg,
        :kind_of => String
      )
    end    
    
    def version(arg=nil)
      set_or_return(
        :version,
        arg,
        :kind_of => String
      )
    end

    def ignore(arg=nil)
      set_or_return(
        :ignore,
        arg,
        :kind_of => [ TrueClass, FalseClass ], :default => false
      )
    end

    def enabled(arg=nil)
      set_or_return(
      :enabled,
      arg,
      :kind_of => [ TrueClass, FalseClass ], :default => true
      )
    end

    def type(arg=nil)
      set_or_return(
        :type,
        arg,
        :kind_of => String
      )
    end

    def services(arg=nil)
      set_or_return(
        :services,
        arg,
        :kind_of => Array
      )
    end

    def environments(arg=nil)
      set_or_return(
        :environments,
        arg,
        :kind_of => Hash
      )
    end

    def environment(name, options={})
      validate(
          options,
          {
          }
        )
      @environments[name] = options
      @environments[name]
    end

    def resources(arg=nil)
      set_or_return(
        :resources,
        arg,
        :kind_of => Hash
      )
    end

    def resource(name, options={})
      validate(
          options,
          {
              :except => { :kind_of => Array },
              :only => { :kind_of => Array },
              :cookbook => { :kind_of => String },
              :design => { :kind_of => [ TrueClass, FalseClass ], :default => true },
              :attributes => { :kind_of => Hash },
              :requires => { :kind_of => Hash },
              :monitors => { :kind_of => Hash }
          }
        )
      if @resources.has_key?(name)
        @resources[name].merge!(options)
      else
        @resources[name] = options
      end
      @resources[name]
    end

    def design_resources
      @resources.reject { |n,r| !r[:design] }
    end

    def platform(arg=nil)
      set_or_return(
        :platform,
        arg,
        :kind_of => Hash
      )

    end   
 
    def environment_resources(environment)
      @resources.reject do |n,r|
        if envs = r[:only]
          envs.include?(environment) ? false : true
        elsif envs = r[:except]
          envs.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def run_list(*args)
      if (args.length > 0)
        @env_run_lists["_default"].reset!(args)
      end
      @env_run_lists["_default"]
    end

    alias_method :recipes, :run_list

    # For run_list expansion
    def run_list_for(environment)
      if env_run_lists[environment].nil?
        env_run_lists["_default"]
      else
        env_run_lists[environment]
      end
    end

    def active_run_list_for(environment)
      @env_run_lists.has_key?(environment) ? environment : '_default'
    end

    # Per environment run lists
    def env_run_lists(env_run_lists=nil)
      if (!env_run_lists.nil?)
        unless env_run_lists.key?("_default")
          msg = "_default key is required in env_run_lists.\n"
          msg << "(env_run_lists: #{env_run_lists.inspect})"
          raise Chef::Exceptions::InvalidEnvironmentRunListSpecification, msg
        end
        @env_run_lists.clear
        env_run_lists.each { |k,v| @env_run_lists[k] = Chef::RunList.new(*Array(v))}
      end
      @env_run_lists
    end

    alias :env_run_list :env_run_lists

    def default_attributes(arg=nil)
      set_or_return(
        :default_attributes,
        arg,
        :kind_of => Hash
      )
    end

    def override_attributes(arg=nil)
      set_or_return(
        :override_attributes,
        arg,
        :kind_of => Hash
      )
    end

    def relations(arg=nil)
      set_or_return(
        :relations,
        arg,
        :kind_of => Hash
      )
    end

    def relation(name, options={})
      validate(
          options,
          {
              :except => { :kind_of => Array },
              :only => { :kind_of => Array },
              :relation_name => { :kind_of => String },
              :design => { :kind_of => [ TrueClass, FalseClass ], :default => true }
          }
        )
      @relations[name] = options
      @relations[name]
    end

    def environment_relations(environment)
      @relations.reject do |n,r|
        if envs = r[:only]
          envs.include?(environment) ? false : true
        elsif envs = r[:except]
          envs.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def depends_on(arg=nil)
      set_or_return(
        :depends_on,
        arg,
        :kind_of => Hash
      )
    end

    def managed_via(arg=nil)
      set_or_return(
        :managed_via,
        arg,
        :kind_of => Hash
      )
    end

    def serviced_bys(arg=nil)
      set_or_return(
        :serviced_bys,
        arg,
        :kind_of => Hash
      )
    end

    def serviced_by(name, options={})
      validate(
          options,
          {
            :except => { :kind_of => Array },
            :only => { :kind_of => Array },
            :pack => { :kind_of => String },
            :version => { :kind_of => String }
          }
        )
      @serviced_bys[name] = options
      @serviced_bys[name]
    end

    def environment_serviced_bys(environment)
      @serviced_bys.reject do |n,r|
        if sbs = r[:only]
          sbs.include?(environment) ? false : true
        elsif eps = r[:except]
          sbs.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def entrypoints(arg=nil)
      set_or_return(
        :entrypoints,
        arg,
        :kind_of => Hash
      )
    end

    def entrypoint(name, options={})
      validate(
          options,
          {
            :except => { :kind_of => Array },
            :only => { :kind_of => Array },
            :attributes => { :kind_of => Hash }
          }
        )
      @entrypoints[name] = options
      @entrypoints[name]
    end

    def environment_entrypoints(environment)
      @entrypoints.reject do |n,r|
        if eps = r[:only]
          eps.include?(environment) ? false : true
        elsif eps = r[:except]
          eps.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def procedures(arg=nil)
      set_or_return(
        :procedures,
        arg,
        :kind_of => Hash
      )
    end

    def procedure(name, options={})
      validate(
          options,
          {
            :except => { :kind_of => Array },
            :only => { :kind_of => Array },
            :description => { :kind_of => String },
            :owner => { :kind_of => String },
            :arguments => { :kind_of => Hash },	    
            :definition => { :kind_of => String }
          }
        )
      @procedures[name] = options
      @procedures[name]
    end

    def environment_procedures(environment)
      @procedures.reject do |n,r|
        if procs = r[:only]
          procs.include?(environment) ? false : true
        elsif eps = r[:except]
          procs.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def variables(arg=nil)
      set_or_return(
        :variables,
        arg,
        :kind_of => Hash
      )
    end

    def variable(name, options={})
      validate(
          options,
          {
            :except => { :kind_of => Array },
            :only => { :kind_of => Array },
            :description => { :kind_of => String },
            :owner => { :kind_of => String },
            :value => { :kind_of => String }
          }
        )
      @variables[name] = options
      @variables[name]
    end


    def policies(arg=nil)
      set_or_return(
          :policies,
          arg,
          :kind_of => Hash
      )
    end

    def policy(name, options={})
      validate(
          options,
          {
              :except => { :kind_of => Array },
              :only => { :kind_of => Array },
              :description => { :kind_of => String },
              :owner => { :kind_of => String },
              :value => { :kind_of => String }
          }
      )
      @policies[name] = options
      @policies[name]
    end

    def environment_variables(environment)
      @variables.reject do |n,r|
        if vars = r[:only]
          vars.include?(environment) ? false : true
        elsif eps = r[:except]
          vars.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def environment_policies(environment)
      @policies.reject do |n,r|
        if pols = r[:only]
          pols.include?(environment) ? false : true
        elsif eps = r[:except]
          pols.include?(environment) ? true : false
        else
          false
        end
      end
    end

    def to_hash
      env_run_lists_without_default = @env_run_lists.dup
      env_run_lists_without_default.delete("_default")
      result = {
        "name" => @name,
        "description" => @description,
        "category" => @category,
        "version" => @version,
        "ignore" => @ignore,
        "enabled" => @enabled,
        "type" => @type,
	"platform" => @platform,
	"services" => @services,
        "environments" => @environments,
        "resources" => @resources,
        'json_class' => self.class.name,
        "default_attributes" => @default_attributes,
        "override_attributes" => @override_attributes,
        "depends_on" => @depends_on,
        "managed_via" => @managed_via,
        "serviced_bys" => @serviced_bys,
        "entrypoints" => @entrypoints,
        "procedures" => @procedures,
        "variables" => @variables,
        "policies" => @policies,
        "chef_type" => "pack",
        "run_list" => run_list,
        "owner" => @owner,
        "relations" => @relations,
        "env_run_lists" => env_run_lists_without_default
      }
      result
    end

    # Serialize this object as a hash
    def to_json(*a)
      to_hash.to_json(*a)
    end

    def update_from!(o)
      description(o.description)
      owner(o.owner)
      category(o.category)
      version(o.version)
      ignore(o.ignore)
      enabled(o.enabled)
      type(o.type)
      platform(o.platform)
      services(o.services) unless o.services.empty?
      environments(o.environments)
      resources(o.resources)
      recipes(o.recipes) if defined?(o.recipes)
      default_attributes(o.default_attributes)
      override_attributes(o.override_attributes)
      depends_on(o.depends_on)
      managed_via(o.managed_via)
      serviced_bys(o.serviced_bys)
      entrypoints(o.entrypoints)
      procedures(o.procedures)
      variables(o.variables)
      env_run_lists(o.env_run_lists) unless o.env_run_lists.nil?
      self
    end

    # Create a Chef::Pack from JSON
    def self.json_create(o)
      pack = new
      pack.name(o["name"])
      pack.description(o["description"])
      pack.category(o["category"])
      pack.version(o["version"])
      pack.ignore(o["ignore"])
      pack.enabled(o["enabled"])
      pack.type(o["type"])
      pack.platform(o["platform"])
      pack.services(o["services"])
      pack.environments(o["environments"])
      pack.resources(o["resources"])
      pack.default_attributes(o["default_attributes"])
      pack.override_attributes(o["override_attributes"])
      pack.depends_on(o["depends_on"])
      pack.managed_via(o["managed_via"])
      pack.serviced_bys(o["serviced_bys"])
      pack.entrypoints(o["entrypoints"])
      pack.procedures(o["procedures"])
      pack.variables(o["variables"])
      pack.owner(o["owner"])

      # _default run_list is in 'run_list' for newer clients, and
      # 'recipes' for older clients.
      env_run_list_hash = {"_default" => (o.has_key?("run_list") ? o["run_list"] : o["recipes"])}

      # Clients before 0.10 do not include env_run_lists, so only
      # merge if it's there.
      if o["env_run_lists"]
        env_run_list_hash.merge!(o["env_run_lists"])
      end
      pack.env_run_lists(env_run_list_hash)

      pack.couchdb_rev = o["_rev"] if o.has_key?("_rev")
      pack.index_id = pack.couchdb_id
      pack.couchdb_id = o["_id"] if o.has_key?("_id")
      pack
    end

    # List all the Chef::Pack objects in the CouchDB.  If inflate is set to true, you will get
    # the full list of all packs, fully inflated.
    def self.cdb_list(inflate=false, couchdb=nil)
      rs = (couchdb || Chef::CouchDB.new).list("packs", inflate)
      lookup = (inflate ? "value" : "key")
      rs["rows"].collect { |r| r[lookup] }
    end

    # Get the list of all packs from the API.
    def self.list(inflate=false)
      if inflate
        response = Hash.new
        Chef::Search::Query.new.search(:pack) do |n|
          response[n.name] = n unless n.nil?
        end
        response
      else
        chef_server_rest.get_rest("packs")
      end
    end

    # Load a pack by name from CouchDB
    def self.cdb_load(name, couchdb=nil)
      (couchdb || Chef::CouchDB.new).load("pack", name)
    end

    # Load a pack by name from the API
    def self.load(name)
      chef_server_rest.get_rest("packs/#{name}")
    end

    def self.exists?(packname, couchdb)
      begin
        self.cdb_load(packname, couchdb)
      rescue Chef::Exceptions::CouchDBNotFound
        nil
      end
    end

    # Remove this pack from the CouchDB
    def cdb_destroy
      couchdb.delete("pack", @name, couchdb_rev)
    end

    # Remove this pack via the REST API
    def destroy
      chef_server_rest.delete_rest("packs/#{@name}")
    end

    # Save this pack to the CouchDB
    def cdb_save
      self.couchdb_rev = couchdb.store("pack", @name, self)["rev"]
    end

    # Save this pack via the REST API
    def save
      begin
        chef_server_rest.put_rest("packs/#{@name}", self)
      rescue Net::HTTPServerException => e
        raise e unless e.response.code == "404"
        chef_server_rest.post_rest("packs", self)
      end
      self
    end

    # Create the pack via the REST API
    def create
      chef_server_rest.post_rest("packs", self)
      self
    end

    # Set up our CouchDB design document
    def self.create_design_document(couchdb=nil)
      (couchdb || Chef::CouchDB.new).create_design_document("packs", DESIGN_DOCUMENT)
    end

    # As a string
    def to_s
      "pack[#{@name}]"
    end

    # Load a pack from disk
    def self.from_disk(file)
      if File.exists?(file)
        pack = Chef::Pack.new
        #pack.name(name)
        pack.from_file(file)
        pack
      else
        raise Chef::Exceptions::RoleNotFound, "pack '#{file}' could not be loaded from disk"
      end
    end

    # Sync all the json packs with couchdb from disk
    def self.sync_from_disk_to_couchdb
      Dir[File.join(Chef::Config[:pack_path], "*.json")].each do |pack_file|
        short_name = File.basename(pack_file, ".json")
        Chef::Log.warn("Loading #{short_name}")
        r = Chef::Pack.from_disk(short_name, "json")
        begin
          couch_pack = Chef::Pack.cdb_load(short_name)
          r.couchdb_rev = couch_pack.couchdb_rev
          Chef::Log.debug("Replacing pack #{short_name} with data from #{pack_file}")
        rescue Chef::Exceptions::CouchDBNotFound
          Chef::Log.debug("Creating pack #{short_name} with data from #{pack_file}")
        end
        r.cdb_save
      end
    end

    def include_pack(name, force=nil)
      file = File.join(Chef::Config[:pack_path], "#{name}.rb")
      Chef::Log.debug("Including pack #{name}")
      o = Chef::Pack.from_disk(file)
      if o
        services(o.services) unless o.services.empty?
        environments(o.environments)
        platform(o.platform)
	resources(o.resources)
        relations(o.relations)
        recipes(o.recipes) if defined?(o.recipes)
        serviced_bys(o.serviced_bys)
        entrypoints(o.entrypoints)
        procedures(o.procedures)
        variables(o.variables)
        env_run_lists(o.env_run_lists) unless o.env_run_lists.nil?
        self
      else
        return false
      end
    end

    def metric(options=nil)
      { :display => true }.merge(options)
    end

    def threshold(bucket,stat,metric,trigger,reset,state = 'notify')
      # need to add validations for values
      { :bucket => bucket, :stat => stat, :metric => metric, :trigger => trigger, :reset => reset, :state => state }
    end

    def trigger(operator,value,duration,numocc)
      { :operator => operator, :value => value, :duration => duration, :numocc => numocc }
    end

    alias :reset :trigger

    def signature
      require 'digest/md5'
      Digest::MD5.hexdigest( sigflat self.to_hash )
    end

    def sigflat(body)
      if body.class == Mash
        arr = []
        body.each do |key, value|
          arr << "#{sigflat key}=>#{sigflat value}"
        end
        body = arr
      end
      if body.class == Hash
        arr = []
        body.each do |key, value|
          arr << "#{sigflat key}=>#{sigflat value}"
        end
        body = arr
      end
      if body.class == Array
        str = ''
        body.map! do |value|
          sigflat value
        end.sort!.each do |value|
          str << value
        end
      end
      if body.class != String
        body = body.to_s << body.class.to_s
      end
      body
    end

  end
end
