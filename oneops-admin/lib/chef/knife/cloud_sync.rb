require 'cms'

class Chef
  class Knife
    class CloudSync < Chef::Knife

      banner "knife cloud sync CLOUD (options)"

      option :all,
             :short       => "-a",
             :long        => "--all",
             :description => "Sync all clouds"

      option :register,
             :short       => "-r REGISTER",
             :long        => "--register REGISTER",
             :description => "Specify the source register name to use during uploads"

      option :version,
             :short       => "-v VERSION",
             :long        => "--version VERSION",
             :description => "Specify the source register version to use during uploads"

      option :cloud_path,
             :short       => "-o PATH:PATH",
             :long        => "--cloud-path PATH:PATH",
             :description => "A colon-separated path to look for clouds in",
             :proc        => lambda {|o| o.split(":")}

      option :reload,
             :long        => "--reload",
             :description => "Remove the current cloud before syncing"

      option :msg,
             :short       => '-m MSG',
             :long        => '--msg MSG',
             :description => "Append a message to the comments"

      option :cms_trace,
             :short       => "-t",
             :long        => "--trace",
             :description => "Raw HTTP debug trace for CMS calls"

      def clouds_loader
        @clouds_loader ||= Knife::Core::ObjectLoader.new(Chef::Cloud, ui)
      end

      def run
        ENV['CMS_TRACE'] = 'true' if config[:cms_trace]

        config[:register]   ||= Chef::Config[:register]
        config[:version]    ||= Chef::Config[:version]
        config[:cloud_path] ||= Chef::Config[:cloud_path]

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
