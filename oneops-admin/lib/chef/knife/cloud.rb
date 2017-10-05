class Chef
  class Cloud
    include Chef::Mixin::FromFile
    include Chef::Mixin::ParamsValidate
    def initialize
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
    def sync( options = {}, comments )
      nspath = "#{Chef::Config[:nspath]}/#{options[:register]}/clouds"
      return false unless ensure_path_exists(nspath)

      Chef::Log.info("Starting sync for cloud #{name} in namespace #{nspath}")
      if (options[:reload])
        puts("Deleting cloud #{self.name} because reload is specified")
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
      puts('services:')
      self.to_hash['services'].each do |service,options|
        ciClassName = options[:cookbook].capitalize
        if options[:source]
          ciClassName = [ 'mgmt.cloud.service', options[:source], ciClassName ].join('.')
        else
          ciClassName = [ 'mgmt.cloud.service', ciClassName ].join('.')
        end
        puts(" - #{service} #{ciClassName}")
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
            next
          end

        else
          Chef::Log.info("Updating service #{service}")
        end

        relation.comments = comments
        relation.toCi.comments = comments

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

        cms_offering_sync(options[:offerings],relation.toCi)
      end

      Chef::Log.info("Completed sync for cloud #{name}!")
      self
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

      Chef::Log.debug(o.to_yaml) if Log.debug?
      begin
        o.save
        Chef::Log.info("Successfully loaded #{o.ciClassName} #{o.ciName}")
      rescue Exception => e
        Log.error("Failed loading #{o.ciClassName} #{o.ciName}: #{e.response.read_body}")
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
        Log.error("Failed deleting #{ci.ciClassName} #{ci.ciName}: #{e}")
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
        Log.error("Failed loading #{name}: #{e}")
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

      Log.debug(o.to_yaml) if Log.debug?
      begin
        o.save
        Log.info("Successfully loaded #{name}")
      rescue Exception => e
        Log.error("Failed loading #{name}: #{e}")
      false
      end
      o
    end

    def cms_offering_sync(offerings, service)
      return if offerings.blank?

      puts('   offerings:')
      relations      = []
      relations_name = 'base.Offers'
      ci_class_name  = 'mgmt.cloud.Offering'
      existing       = Cms::Relation.all(:params => {:ciId            => service.ciId,
                                                     :direction       => 'from',
                                                     :relationName    => relations_name,
                                                     :targetClassName => ci_class_name,
                                                     :includeToCi     => true})

      offerings.each do |name, offering|
        relation = existing.find {|r| r.toCi.ciName == name}
        unless relation
          relation = build('Cms::Relation',
                           :relationName => relations_name,
                           :nsPath       => "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}",
                           :fromCiId     => service.ciId,
                           :toCiId       => 0,
                           :toCi         => build('Cms::Ci',
                                                  :nsPath      => "#{service.nsPath}/#{service.ciClassName}/#{service.ciName}",
                                                  :ciClassName => ci_class_name,
                                                  :ciName      => name))

        end

        attrs = relation.toCi.ciAttributes.attributes
        attrs.each {|attr, _| attrs[attr] = offering[attr] if offering[attr]}

        puts("    - #{name}")
        relations << relation
      end
      relations, error = Cms::Relation.bulk(relations)
      unless relations
        Log.error("Could not save offerings: #{error}")
        raise(error)
      end
      Log.debug('Successfuly saved offerings')
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
