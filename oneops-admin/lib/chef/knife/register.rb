class Chef
  class Knife
    class Register < Chef::Knife

      banner "knife register (options)"

      option :description,
        :short => '-d DESCRIPTION',
        :long => '--description DESCRIPTION',
        :description => "Description of the source"

      option :register,
        :short => "-r REGISTER",
        :long => "--register REGISTER",
        :description => "Specify the source register name to use during uploads"

      option :version,
        :short => "-v VERSION",
        :long => "--version VERSION",
        :description => "Specify the source register version to use during uploads"

      option :msg,
        :short => '-m MSG',
        :long => '--msg MSG',
        :description => "Append a message to the comments"

      option :nspath,
        :long => "--nsPath NSPATH",
        :description => "Namespace path to register the source",
        :default => '/public'

      def run
        name = config[:register] ||= Chef::Config[:register]
        nspath = config[:nspath] ||= Chef::Config[:nspath]

        unless name
          ui.error( "Missing 'register' entry in the config file" )
          return false
        end

        comments = "#{ENV['USER']}:#{$0}"
        comments += " #{config[:msg]}" if config[:msg]

        ui.info( "Ensuring namespace #{nspath} exists" )

        unless ensure_path_exists(nspath)
          return false
        end

        ui.info( "Registering source #{name} in namespace #{nspath}" )

        source = Cms::Ci.first( :params => { :nsPath => nspath, :ciClassName => 'mgmt.Source', :ciName => name })
        if source.nil?
          ui.info( "Registering source #{name}")
          unless source = build('Cms::Ci', :nsPath => nspath, :ciClassName => 'mgmt.Source', :ciName => name )
            ui.error("Could not register source #{name}")
            return false
          end
        else
          ui.info("Updating source #{name}")
        end

        source.comments = comments
        source.ciAttributes.description = config[:description]

        Chef::Log.debug(source.to_json)
        if save(source)
          ui.info("Successfuly registered source #{name}")
        else
          ui.error("Could not register source #{name}")
          return false
        end

        if ensure_path_exists("#{source.nsPath}/#{source.ciName}/packs")
          ui.info("Successfuly created packs namespace for source #{name}")
          return source
        else
          return false
        end

      end

      def ensure_path_exists(nspath)
        ns = Cms::Namespace.all( :params => { :nsPath => nspath } ).first
        if ns.nil?
          ui.info( "Creating namespace #{nspath}")
          ns = Cms::Namespace.new( :nsPath => nspath )
          if save(ns)
            ui.info("Successfuly saved namespace source #{nspath}")
            return ns
          else
            ui.error("Could not save namespace #{nspath}")
            return false
          end
        end
        return ns
      end

      def save(object)
        begin
          ok = object.save
        rescue Exception => e
          Log.debug(e.response.read_body)
        end
        ok ? object : false
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
end
