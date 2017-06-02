require 'chef/knife'
require 'chef/exceptions'
require 'chef/knife/core/object_loader'
require 'chef/json_compat'

$:.unshift File.dirname(__FILE__)
require 'bundler'

ENV['BUNDLE_GEMFILE'] ||= File.dirname(__FILE__) + '/../../../Gemfile'
require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])


Bundler.setup(:default)

require 'cms'

class Chef
  class Knife
    class CatalogUpload < Chef::Knife

      banner "knife catalog upload CATALOG (options)"

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Import all catalogs"

      option :register,
        :short => "-r REGISTER",
        :long => "--register REGISTER",
        :description => "Specify the source register name to use during uploads"
        
      option :version,
        :short => "-v VERSION",
        :long => "--version VERSION",
        :description => "Specify the source register version to use during uploads"
        
      option :catalog_path,
        :short => "-p PATH:PATH",
        :long => "--catalog-path PATH:PATH",
        :description => "A colon-separated path to look for catalogs",
        :proc => lambda { |o| o.split(":") }

      option :nspath,
        :long => "--nsPath NSPATH",
        :description => "Namespace path for the design catalog"

      option :reload,
        :long => "--reload",
        :description => "Remove the current design before uploading"

      option :desc,
        :long => "--desc DESCRIPTION",
        :description => "Description of a new catalog"
                
      def catalog_loader
        @catalog_loader ||= Knife::Core::ObjectLoader.new(Chef::Catalog, ui)
      end

      def run
        config[:register] ||= Chef::Config[:register]
        config[:version] ||= Chef::Config[:version]
               
        Cms::Ci.headers['X-Cms-User']         = config[:register]
        Cms::Ci.headers['X-Cms-Client']       = config[:register]
        Cms::Relation.headers['X-Cms-User']   = config[:register]
        Cms::Relation.headers['X-Cms-Client'] = config[:register]
        
        config[:catalog_path] ||= Chef::Config[:catalog_path]
        nspath = config[:nspath] || "#{Chef::Config[:nspath]}/#{config[:register]}/catalogs"
        
        comments = "#{ENV['USER']}:#{$0}"
        comments += " #{config[:msg]}" if config[:msg]

        if config[:all]
          ui.info("Starting upload of all catalogs")
          Chef::Catalog.upload_all
          ui.info("Completed upload of all catalogs!")
        else
          if @name_args.empty?
            ui.error "You must specify the catalog name to upload or use the --all option."
            exit 1
          end
          @name_args.each do |name|
            ui.info("Starting upload of catalog #{name}")
            catalog = Chef::Catalog.from_disk(name)
            catalog.description(config[:desc])
            Chef::Log.debug(catalog.inspect)
            if catalog.public != "true"
              ui.info("Catalog #{name} not marked for public upload. Please add 'public: true' to enable uploads to public namespace!")
              exit 1
            end
            
            begin
              catalog.upload(nspath,name,config[:reload])
            rescue Exception => e
              Chef::Log.error(e.to_s)
              exit 1
            end
            ui.info("Completed upload of catalog #{name}!")
          end
        end

      end

    end
  end
end