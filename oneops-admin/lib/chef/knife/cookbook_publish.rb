require 'chef/knife'
require 'chef/exceptions'
require 'chef/cookbook_loader'
require 'chef/cookbook_uploader'

require 'bundler'
ENV['BUNDLE_GEMFILE'] ||= File.dirname(__FILE__) + '/../../../Gemfile'
require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])

Bundler.setup(:default)

require 'cms'

class Chef
  class Knife
    class CookbookPublish < Chef::Knife::CookbookMetadata
  
      banner "knife cookbook publish (options)"

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Publish all cookbooks"

      option :cookbook_path,
        :short => "-o PATH:PATH",
        :long => "--cookbook-path PATH:PATH",
        :description => "A colon-separated path to look for cookbooks in",
        :proc => lambda { |o| o.split(":") }

      option :publish_path,
        :short => "-p PATH",
        :long => "--publish-path PATH",
        :description => "Destination path to publish cookbooks"
                     
      def run
        config[:cookbook_path] ||= Chef::Config[:cookbook_path]
        config[:publish_path] ||= Chef::Config[:publish_path]
        config[:version] ||= Chef::Config[:version]

        if config[:all]
          version_name = "#{Chef::Config[:register]}-#{config[:version]}"
          # packaged
          FileUtils.mkdir_p(config[:publish_path])
          tarball_name = "cookbooks_#{version_name}.tar.gz"

          # cache
          cache_dir = File.join(config[:publish_path], 'cache')
          version_dir = File.join(cache_dir, version_name)
          ui.info "Purging cookbooks from #{version_dir} directory cache"
          FileUtils.remove_dir(version_dir, true) if File.directory?(version_dir)
          ui.info "Publishing cookbooks to #{version_dir} directory"
          FileUtils.mkdir_p("#{version_dir}/cookbooks")
         
          cl = Chef::CookbookLoader.new(config[:cookbook_path])
          cl.each do |cname, cookbook|
            ui.info "Publishing cookbook #{cname}"
            version_cookbook_dir = File.join("#{version_dir}/cookbooks", cname) 
            FileUtils.mkdir_p(version_cookbook_dir)
            child_folders = [ "cookbooks/#{cname}", "site-cookbooks/#{cname}" ]
            child_folders.each do |folder|
              file_path = File.join(folder, ".")
              FileUtils.cp_r(file_path, version_cookbook_dir) if File.directory?(file_path)
            end
          end
          
          ui.info "Packaging cookbooks from cache to #{config[:publish_path]}/#{tarball_name}"                    
          system("tar", "-C", version_dir, "-czf", File.join(config[:publish_path], tarball_name), "cookbooks")
            
        else
          ui.error "You must specify -a | --all option to publish all cookbooks at once."
          exit 1
        end
      end
         
    end
  end
end
