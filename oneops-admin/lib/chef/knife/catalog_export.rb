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
    class CatalogExport < Chef::Knife

      banner "knife catalog export CATALOG (options)"

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Export all catalogs"

      option :nspath,
        :long => "--nsPath NSPATH",
        :description => "Namespace path for the assembly catalog",
        :default => '/oneops/_catalogs'

      option :ciname,
        :long => "--ciName CI_NAME",
        :description => "Assembly ciName to use for the export"

      option :catalog_path,
        :short => "-p PATH:PATH",
        :long => "--catalog-path PATH:PATH",
        :description => "A colon-separated path to look for catalogs in",
        :proc => lambda { |o| o.split(":") }
      def catalog_loader
        @catalog_loader ||= Knife::Core::ObjectLoader.new(Chef::Catalog, ui)
      end

      def run
        config[:catalog_path] ||= Chef::Config[:catalog_path]
        nspath = config[:nspath]

        comments = "#{ENV['USER']}:#{$0}"
        comments += " #{config[:msg]}" if config[:msg]

        if config[:all]
          ui.info("Starting export of all catalogs")
          Chef::Catalog.list(nspath).each do |ciname|
            export(nspath,ciname,ciname)
          end
          ui.info("Completed export of all catalogs!")
        else
          if @name_args.empty?
            ui.error "You must specify the catalog name to export or use the --all option."
            exit 1
          end
          @name_args.each do |name|
            ciname = config[:ciname] || name
            export(nspath,name,ciname)
          end
        end

      end

      private

      def export(nspath,name,ciname)
        ui.info("Starting export for catalog #{name} from #{nspath}/#{ciname}")
        catalog = Chef::Catalog.export(name,nspath,ciname)
        Chef::Log.debug(catalog.inspect)
        catalog.to_disk('yaml')
        ui.info("Completed export of catalog #{name}!")
      end

    end
  end

end