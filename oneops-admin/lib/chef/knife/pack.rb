class Chef
  class Pack
    include Chef::Mixin::ParamsValidate

    cattr_accessor :config

    attr_reader :platform,
                :environments,
                :resources,
                :relations,
                :serviced_bys,
                :entrypoints,
                :procedures,
                :variables,
                :filename

    def initialize
      @name                = ''
      @description         = ''
      @category            = ''
      @version             = ''
      @visibility          = []
      @ignore              = false
      @enabled             = true
      @type                = ''
      @platform            = Hash.new
      @services            = ''
      @environments        = Mash.new
      @resources           = Mash.new
      @relations           = Mash.new
      @default_attributes  = Mash.new
      @override_attributes = Mash.new
      @serviced_bys        = Mash.new
      @entrypoints         = Mash.new
      @procedures          = Mash.new
      @variables           = Mash.new
      @env_run_lists       = {'_default' => Chef::RunList.new}
      @owner               = ''
      @policies            = Mash.new
    end

    def from_file(filename)
      if File.exists?(filename) && File.readable?(filename)
        self.instance_eval(IO.read(filename), filename, 1)
        @filename = filename
      else
        raise IOError, "Cannot open or read #{filename}!"
      end
    end

    def include_pack(name, force=nil)
      file = File.join(config[:pack_path], "#{name}.rb")
      Chef::Log.debug("Including pack #{name}")
      o = Chef::Pack.new
      o.from_file(file)
      # LK: Technically such "coarse" assignments below are 'dangerous'. This relies on
      # include_pack declarations in pack file being at the very top (before any 'resource'
      # or 'relations' declarations).  Better approach could be to iterate though member collections
      # below and assign individually given that each single assingment is implemented properly
      # to merge data (e.g., 'resource' method should match on relation_name-from_resource-to_resource
      # key and merge attributes when one already exists).
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
    end

    def name(arg=nil)
      set_or_return(:name, arg, :regex => /^[\-[:alnum:]_]+$/)
    end

    def description(arg=nil)
      set_or_return(:description, arg, :kind_of => String)
    end

    def owner(arg=nil)
      set_or_return(:owner, arg, :kind_of => String)
    end

    def category(arg=nil)
      set_or_return(:category, arg, :kind_of => String)
    end

    def visibility(arg = nil)
      set_or_return(:visibility, arg, :kind_of => Array, :default => [])
    end

    def version(arg=nil)
      set_or_return(:version, arg, :kind_of => String)
    end

    def semver?
      @version.present? && @version.include?('.')
    end

    def ignore(arg=nil)
      set_or_return(:ignore, arg, :kind_of => [TrueClass, FalseClass], :default => false)
    end

    def enabled(arg=nil)
      set_or_return(:enabled, arg, :kind_of => [TrueClass, FalseClass], :default => false)
    end

    def type(arg=nil)
      set_or_return(:type, arg, :kind_of => String)
    end

    def services(arg=nil)
      set_or_return(:services, arg, :kind_of => Array)
    end

    def environments(arg=nil)
      set_or_return(:environments, arg, :kind_of => Hash)
    end

    def environment(name, options={})
      validate(options, {})
      @environments[name] = options
      @environments[name]
    end

    def resources(arg=nil)
      set_or_return(:resources, arg, :kind_of => Hash)
    end

    def resource(name, options={})
      validate(
        options,
        {
          :except     => {:kind_of => Array},
          :only       => {:kind_of => Array},
          :cookbook   => {:kind_of => String},
          :design     => {:kind_of => [TrueClass, FalseClass], :default => true},
          :attributes => {:kind_of => Hash},
          :requires   => {:kind_of => Hash},
          :monitors   => {:kind_of => Hash}
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
      @resources.reject {|n, r| !r[:design]}
    end

    def platform(arg=nil)
      set_or_return(:platform, arg, :kind_of => Hash)
    end

    def environment_resources(env)
      filter_by_env(@resources, env)
    end

    def run_list(*args)
      @env_run_lists['_default'].reset!(args) if args.length > 0
      @env_run_lists['_default']
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
        env_run_lists.each {|k, v| @env_run_lists[k] = Chef::RunList.new(*Array(v))}
      end
      @env_run_lists
    end

    alias :env_run_list :env_run_lists

    def default_attributes(arg=nil)
      set_or_return(:default_attributes, arg, :kind_of => Hash)
    end

    def override_attributes(arg=nil)
      set_or_return(:override_attributes, arg, :kind_of => Hash)
    end

    def relations(arg=nil)
      set_or_return(:relations, arg, :kind_of => Hash)
    end

    def relation(name, options = {})
      validate(options, {:except        => {:kind_of => Array},
                         :only          => {:kind_of => Array},
                         :relation_name => {:kind_of => String},
                         :design        => {:kind_of => [TrueClass, FalseClass], :default => true}})
      # LK: The proper way would be the code below (merging of attributes when they are overwritten, based on relation name and from and to resources.)
      # But will keep the "old" (incorrect) way for "backward-compatibility"
      # name = "#{options[:relation_name]}**#{options[:from_resource]}**#{options[:to_resource]}"
      # if @relations[name]
      #   @relations[name][:attributes] = (@relations[name][:attributes] || {}).merge(options[:attributes] || {})
      # else
      #   @relations[name] = options
      # end
      @relations[name] = options
      @relations[name]
    end

    def env_relations(env, relation_name)
      filter_by_env(@relations, env).values.select {|r| r[:relation_name] == relation_name}
    end

    def serviced_bys(arg=nil)
      set_or_return(:serviced_bys, arg, :kind_of => Hash)
    end

    def serviced_by(name, options={})
      validate(
        options,
        {
          :except  => {:kind_of => Array},
          :only    => {:kind_of => Array},
          :pack    => {:kind_of => String},
          :version => {:kind_of => String}
        }
      )
      @serviced_bys[name] = options
      @serviced_bys[name]
    end

    def entrypoints(arg=nil)
      set_or_return(:entrypoints, arg, :kind_of => Hash)
    end

    def entrypoint(name, options={})
      validate(
        options,
        {
          :except     => {:kind_of => Array},
          :only       => {:kind_of => Array},
          :attributes => {:kind_of => Hash}
        }
      )
      @entrypoints[name] = options
      @entrypoints[name]
    end

    def environment_entrypoints(env)
      filter_by_env(@entrypoints, env)
    end

    def procedures(arg=nil)
      set_or_return(:procedures, arg, :kind_of => Hash)
    end

    def procedure(name, options={})
      validate(
        options,
        {
          :except      => {:kind_of => Array},
          :only        => {:kind_of => Array},
          :description => {:kind_of => String},
          :owner       => {:kind_of => String},
          :arguments   => {:kind_of => Hash},
          :definition  => {:kind_of => String}
        }
      )
      @procedures[name] = options
      @procedures[name]
    end

    def environment_procedures(env)
      filter_by_env(@procedures, env)
    end

    def variables(arg=nil)
      set_or_return(:variables, arg, :kind_of => Hash)
    end

    def variable(name, options={})
      validate(
        options,
        {
          :except      => {:kind_of => Array},
          :only        => {:kind_of => Array},
          :description => {:kind_of => String},
          :owner       => {:kind_of => String},
          :value       => {:kind_of => String}
        }
      )
      @variables[name] = options
      @variables[name]
    end


    def policies(arg=nil)
      set_or_return(:policies, arg, :kind_of => Hash)
    end

    def policy(name, options={})
      validate(
        options,
        {
          :except      => {:kind_of => Array},
          :only        => {:kind_of => Array},
          :description => {:kind_of => String},
          :owner       => {:kind_of => String},
          :value       => {:kind_of => String}
        }
      )
      @policies[name] = options
      @policies[name]
    end

    def environment_variables(env)
      filter_by_env(@variables, env)
    end

    def environment_policies(env)
      filter_by_env(@policies, env)
    end

    def to_hash
      env_run_lists_without_default = @env_run_lists.dup
      env_run_lists_without_default.delete('_default')
      {
        "name"                => @name,
        "description"         => @description,
        "category"            => @category,
        "version"             => @version,
        "enabled"             => @enabled,
        "type"                => @type,
        "platform"            => @platform,
        "services"            => @services,
        "environments"        => @environments,
        "resources"           => @resources,
        'json_class'          => self.class.name,
        "default_attributes"  => @default_attributes,
        "override_attributes" => @override_attributes,
        "serviced_bys"        => @serviced_bys,
        "entrypoints"         => @entrypoints,
        "procedures"          => @procedures,
        "variables"           => @variables,
        "policies"            => @policies,
        "chef_type"           => 'pack',
        "run_list"            => run_list,
        "owner"               => @owner,
        "relations"           => @relations,
        "env_run_lists"       => env_run_lists_without_default
      }
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
      serviced_bys(o.serviced_bys)
      entrypoints(o.entrypoints)
      procedures(o.procedures)
      variables(o.variables)
      env_run_lists(o.env_run_lists) unless o.env_run_lists.nil?
      self
    end

    def to_s
      "pack[#{@name}]"
    end

    def metric(options=nil)
      {:display => true}.merge(options)
    end

    def threshold(bucket, stat, metric, trigger, reset, state = 'notify')
      # need to add validations for values
      {:bucket => bucket, :stat => stat, :metric => metric, :trigger => trigger, :reset => reset, :state => state}
    end

    def trigger(operator, value, duration, numocc)
      {:operator => operator, :value => value, :duration => duration, :numocc => numocc}
    end

    alias :reset :trigger

    def signature
      Digest::MD5.hexdigest(self.class.flatten(to_hash))
    end

    def self.flatten(o, seed = '')
      return o.sort_by {|e| e.first.to_s}.inject(seed) {|s, e| flatten(e, s)} if o.is_a?(Hash)
      return o.inject(seed) {|s, e| flatten(e, s)} if o.is_a?(Array)
      "#{seed}|#{o.to_s}"
    end


    private

    def filter_by_env(hash, env)
      hash.select do |_, v|
        envs = v[:only]
        if envs
          envs.include?(env)
        else
          envs = v[:except]
          envs ? !envs.include?(env) : true
        end
      end
    end
  end
end
