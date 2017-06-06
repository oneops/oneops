require 'chef/knife'
require 'chef/exceptions'
require 'chef/knife/core/object_loader'
require 'chef/json_compat'

require 'bundler'
ENV['BUNDLE_GEMFILE'] ||= File.dirname(__FILE__) + '/../../../Gemfile'
require 'bundler/setup' if File.exists?(ENV['BUNDLE_GEMFILE'])

Bundler.setup(:default)

require 'cms'

$:.unshift File.dirname(__FILE__)
require 'cloud'

# class ActiveResource::Connection
# # Creates new Net::HTTP instance for communication with
# # remote service and resources.
# def http
# http = Net::HTTP.new(@site.host, @site.port)
# http.use_ssl = @site.is_a?(URI::HTTPS)
# http.verify_mode = OpenSSL::SSL::VERIFY_NONE if http.use_ssl
# http.read_timeout = @timeout if @timeout
# #Here's the addition that allows you to see the output
# http.set_debug_output $stderr
# return http
# end
# end

class Chef
  class Knife
    class CloudSync < Chef::Knife

      banner "knife cloud sync CLOUD (options)"

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Sync all clouds"

      option :register,
        :short => "-r REGISTER",
        :long => "--register REGISTER",
        :description => "Specify the source register name to use during uploads"
        
      option :version,
        :short => "-v VERSION",
        :long => "--version VERSION",
        :description => "Specify the source register version to use during uploads"
        
      option :cloud_path,
        :short => "-o PATH:PATH",
        :long => "--cloud-path PATH:PATH",
        :description => "A colon-separated path to look for clouds in",
        :proc => lambda { |o| o.split(":") }

      option :reload,
        :long => "--reload",
        :description => "Remove the current cloud before syncing"
        
      option :msg,
        :short => '-m MSG',
        :long => '--msg MSG',
        :description => "Append a message to the comments"
        
      def clouds_loader
        @clouds_loader ||= Knife::Core::ObjectLoader.new(Chef::Cloud, ui)
      end

      def run
        config[:register] ||= Chef::Config[:register]
        config[:version] ||= Chef::Config[:version]
        config[:cloud_path] ||= Chef::Config[:cloud_path]

        #if not Chef::Config[:admin]
        #  if Chef::Config[:useversion]
        #      config[:source] = [config[:register], config[:version].split(".").first].join('.')
        #  else
        #      config[:source] = config[:register]
        #  end
        #end

        comments = "#{ENV['USER']}:#{$0}"
        comments += " #{config[:msg]}" if config[:msg]

        if config[:all]
          ui.info("Starting sync for all clouds")
          Chef::Cloud.sync_all(config)
          ui.info("Completed sync for all clouds")
        else
          if @name_args.empty?
            ui.error "You must specify the cloud to sync or use the --all option."
            exit 1
          end
          @name_args.each do |name|
            ui.info("Starting sync for cloud #{name}")
            cloud = Chef::Cloud.from_disk(name)
            Chef::Log.debug(cloud.inspect)
            cloud.sync(config)
            ui.info("Completed sync for cloud #{name}!")
          end
        end

      end

    end
  end
end
