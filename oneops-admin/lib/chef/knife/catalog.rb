require 'chef/config'
require 'chef/mixin/params_validate'
require 'chef/mixin/from_file'
require 'chef/mash'
require 'chef/json_compat'

class Chef
  class Catalog

    include Chef::Mixin::FromFile
    include Chef::Mixin::ParamsValidate
      
    # Create a new Chef::Catalog object.
    def initialize
      @name = ''
      @public = ''
      @description = ''
      @nspath = ''
      @cis = Mash.new
      @relations = Mash.new
    end

    def name(arg=nil)
      set_or_return(
        :name,
        arg,
        :regex => /^[\-[:alnum:]_]+$/
      )
    end

    def public(arg=nil)
      set_or_return(
        :public,
        arg,
        :kind_of => [ TrueClass, FalseClass ], :default => false
      )
    end
    
    def description(arg=nil)
      set_or_return(
        :description,
        arg,
        :kind_of => String
      )
    end

    def nspath(arg=nil)
      set_or_return(
        :nspath,
        arg,
        :kind_of => String
      )
    end
    
    def ci(id, options)
      @cis[id] = options
      @cis[id]
    end
    
    def cis
      @cis
    end

    def relation(id, options)
      @relations[id] = options
      @relations[id]
    end
    
    def relations
      @relations
    end
    
    # As a string
    def to_s
      "catalog[#{@name}]"
    end

    def to_hash
      result = {
        "name" => @name,
        "public" => @public,
        "description" => @description,
        "nspath" => @nspath,
        'json_class' => self.class.name,
        "cis" => @cis,
        "relations" => @relations,
        "chef_type" => "catalog"
      }
      result
    end

    # Serialize this object as a hash
    def to_json(*a)
      to_hash.to_json(*a)
    end
=begin
    def update_from!(o)
      public(o.public)
      description(o.description)
      nspath(o.nspath)
      cis(o.cis) if defined?(o.cis)
      relations(o.relations) if defined?(o.relations)
      self
    end
=end

    # Create a Chef::Catalog from JSON
    def self.json_create(o)
      catalog = new
      catalog.name(o["name"])
      catalog.public(o["public"])
      catalog.description(o["description"])
      catalog.nspath(o["nspath"])
      o["cis"].each do |id,options|
        catalog.ci(id,options)
      end
      o["relations"].each do |id,options|
        catalog.relation(id,options)
      end
      catalog
    end

    # Get the list of all catalogs from the API.
    def self.list(nspath)
      list = Cms::Ci.all( :params => { :nsPath => nspath, :ciClassName => 'account.Design' })
      return list.collect { |a| a.ciName }
    end

    # Export a catalog by name from the API
    def self.export(name,nspath,ciname)
      catalogCI = Cms::Ci.first( :params => { :nsPath => nspath, :ciClassName => 'account.Design', :ciName => ciname })
      if catalogCI.nil?
        raise Chef::Exceptions::Application, "catalog #{ciname} not found in ns #{nspath}!"
      else
        catalog = new
        catalog.name(name)
        catalog.description(catalogCI.ciAttributes.description)
        Cms::Ci.all( :params => { :nsPath => "#{nspath}/#{ciname}", :getEncrypted => true, :recursive => true }).each do |e_ci|
          e_ci = _purge(e_ci)
          catalog.ci(e_ci.ciId,e_ci)
        end
        Cms::Relation.all( :params => { :nsPath => "#{nspath}/#{ciname}",  :recursive => true}).each do |e_relation|
          e_relation = _purge(e_relation)
          catalog.relation(e_relation.ciRelationId,e_relation)
        end
        return catalog
      end
    end

    def upload(nspath,ciname,reload = false)
      
      return false unless ensure_path_exists(nspath)
        
      design = Cms::Ci.first( :params => { :nsPath => "#{nspath}", :ciClassName => 'account.Design', :ciName => ciname })
      
      #Chef::Log.debug(design.inspect)
      unless design.nil?
        if reload
          unless _destroy(design)
            raise Chef::Exceptions::Application, "unable to remove existing design #{ciname} in #{nspath}"
          end
        else
          raise Chef::Exceptions::Application, "design #{ciname} already exists in #{nspath}"
        end
      end
      
      unless design = _build('Cms::Ci', :nsPath => "#{nspath}", :ciClassName => 'account.Design', :ciName => ciname )
        raise Chef::Exceptions::Application, "could not build design #{ciname} in #{nspath}"
      end
      design.ciAttributes.description = self.description

      if _save(design)
        load(design,nspath,ciname)
      else
        raise Chef::Exceptions::Application, "could not save design #{ciname} in #{nspath}"
      end
    end       
          
    # Load a catalog from disk - prefers to load the JSON, but will happily load
    # the raw rb files as well.
    def self.from_disk(name, force=nil)
      yaml_file = File.join(Chef::Config[:catalog_path], "#{name}.yaml")
      js_file = File.join(Chef::Config[:catalog_path], "#{name}.json")
      rb_file = File.join(Chef::Config[:catalog_path], "#{name}.rb")

      if File.exists?(yaml_file) || force == "yaml"
        YAML.load_file(yaml_file)
      elsif File.exists?(js_file) || force == "json"
        Chef::JSONCompat.from_json(IO.read(js_file))
      elsif File.exists?(rb_file) || force == "ruby"
        catalog = Chef::Catalog.new
        catalog.name(name)
        catalog.from_file(rb_file)
        catalog
      else
        raise Chef::Exceptions::RoleNotFound, "catalog #{name} could not be loaded from disk"
      end
    end

    def to_disk(force=nil)
      name = self.name
      case force
      when 'yaml'
        yaml_file = File.join(Chef::Config[:catalog_path], "#{name}.yaml")
        if File.open(yaml_file, 'w') { |f| f.write(self.to_yaml) }
          return true
        else
          raise Chef::Exceptions::RoleNotFound, "catalog '#{name}' could not be written to disk in file #{yaml_file}"
        end
      else
        js_file = File.join(Chef::Config[:catalog_path], "#{name}.json")
        if File.open(js_file, 'w') { |f| f.write(self.to_json) }
          return true
        else
          raise Chef::Exceptions::RoleNotFound, "catalog '#{name}' could not be written to disk in file #{js_file}"
        end
      end
    end

    def self.upload_all
      Dir[File.join(Chef::Config[:catalog_path], "*.yaml")].each do |catalog_file|
        short_name = File.basename(catalog_file, ".yaml")
        catalog = Chef::Catalog.from_disk(short_name, "yaml")
        if catalog.public
          catalog.upload
        end
      end
    end


    def load(parent,nspath,ciname)
    
      self.cis.select { |ci_id,ci| ci.ciClassName.split('.').first == 'catalog' }.each do |ci_id,ci|
        ci = _purge(ci)
        ci.attributes.delete("ciId")
        ci.nsPath = "#{nspath}/#{ciname}" + ci.nsPath     
        ensure_path_exists(ci.nsPath)
        if _save(ci)
          self.ci(ci_id,ci)
        else
          raise Chef::Exceptions::Application, "could not save ci #{ci.ciName} in #{nspath}/#{ciname}"
        end
      end
      
      self.relations.each do |relation_id,relation|
        relation = _purge(relation)
        relation.attributes.delete("ciRelationId")
        relation.nsPath = "#{nspath}/#{ciname}" + relation.nsPath
        case relation.relationName
        when 'base.ComposedOf' # platforms are linked to the new assembly
          relation.fromCiId = parent.ciId
          relation.toCiId = self.cis[relation.toCiId].ciId
        when 'base.ValueFor'  # variables are linked to the new assembly
          relation.fromCiId = self.cis[relation.fromCiId].ciId
          relation.toCiId = parent.ciId
        else
          if self.cis.has_key?(relation.fromCiId) && self.cis.has_key?(relation.toCiId)
            relation.fromCiId = self.cis[relation.fromCiId].ciId
            relation.toCiId = self.cis[relation.toCiId].ciId
          else
            next # relation to external object outside the nsPath
          end
        end
        if _save(relation)
          self.relation(relation_id,relation)
        else
          raise Chef::Exceptions::Application, "could not save relation #{relation.inspect}"
        end
      end
    end

    private
    
    def ensure_path_exists(nspath)
      ns = Cms::Namespace.all( :params => { :nsPath => nspath } ).first
      if ns.nil?
        ns = Cms::Namespace.new( :nsPath => nspath )
        if _save(ns)
          return ns
        else
          raise Chef::Exceptions::Application, "could not create namespace #{nspath}"
          return false
        end
      end
      return ns
    end

    def self._purge(object)
      object.attributes.delete("created")
      object.attributes.delete("updated")
      object.attributes.delete("createdBy")
      object.attributes.delete("updatedBy")
      object.attributes.delete("lastAppliedRfcId")
      object.nsPath = object.nsPath.gsub!(/.*_design/,"/_design") unless object.nsPath.nil? 
      object.attributes.delete("comments")
      object.attributes.delete("ciState")
      object.attributes.delete("ciGoid")
      object.attributes.delete("relationGoid")
      object.attributes.delete("relationState")
      return object
    end

    def _purge(object)
      object.attributes.delete("created")
      object.attributes.delete("updated")
      object.attributes.delete("createdBy")
      object.attributes.delete("updatedBy")
      object.attributes.delete("lastAppliedRfcId")
      if object.nsPath.nil?
        object.nsPath =''
      else
        object.nsPath = object.nsPath.gsub!(/.*_design/,"/_design")
      end  
      object.attributes.delete("comments")
      object.attributes.delete("ciState")
      object.attributes.delete("ciGoid")
      object.attributes.delete("relationGoid")
      object.attributes.delete("relationState")
      return object
    end
    def _save(object)
      Log.debug(object.inspect)
      begin
        ok = object.save
      rescue Exception => e
        Log.debug(e.to_s)
      end
      ok ? object : false
    end

    def _destroy(object)
      begin
        ok = object.destroy
      rescue Exception => e
        Log.debug(e.response.read_body)
      end
      ok ? object : false
    end
      
    def _build(klass, options)
      begin
        object = klass.constantize.build(options)
      rescue Exception => e
        Log.debug(e.to_s)
      end
      object ? object : false
    end

  end

end
