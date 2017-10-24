require 'chef/knife/base_sync'

class Chef
  class Knife
    class CloudSync < Chef::Knife
      include ::BaseSync

      banner "Loads cloud templates into OneOps\nUsage: \n   circuit cloud [OPTIONS] [CLOUDS...]"

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


      def clouds_loader
        @clouds_loader ||= Knife::Core::ObjectLoader.new(Chef::Cloud, ui)
      end

      def run
        t1 = Time.now
        ENV['CMS_TRACE'] = 'true' if config[:cms_trace]

        config[:register]   ||= Chef::Config[:register]
        config[:version]    ||= Chef::Config[:version]
        config[:cloud_path] ||= Chef::Config[:cloud_path]

        @packs_loader ||= Knife::Core::ObjectLoader.new(Chef::Cloud, ui)

        if config[:all]
          files = (config[:cloud_path] || []).inject([]) {|a, dir| a + Dir.glob("#{dir}/*.rb").sort}
        else
          files = @name_args.inject([]) {|a, cloud| a << "#{cloud}.rb"}
        end

        if files.blank? && config[:all].blank?
          ui.error 'You must specify cloud name(s) or use the --all option to sync all.'
          exit(1)
        end

        comments = "#{ENV['USER']}:#{$0} #{config[:msg]}"
        files.each {|f| exit(1) unless sync_cloud(f, comments)}

        t2 = Time.now
        ui.info("\nProcessed #{files.size} clouds.\nDone at #{t2} in #{(t2 - t1).round(1)}sec")
      end

      def sync_cloud(file, comments)
        cloud = @packs_loader.load_from(config[:cloud_path], file)
        ui.info("\n--------------------------------------------------")
        ui.info(" #{cloud.name} ".blue(true))
        ui.info('--------------------------------------------------')
        cloud.sync(config, comments)
        ui.info("\e[7m\e[32mSuccessfully synched\e[0m cloud #{cloud.name}")
        return cloud
      end
    end
  end
end
