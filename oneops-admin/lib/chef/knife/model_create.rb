class Chef
 class Knife
   class ModelCreate < Chef::Knife::CookbookCreate

     option :cookbook_path,
        :short => "-o PATH",
        :long => "--cookbook-path PATH",
        :description => "The directory where the cookbook will be created"
     option :readme_format,
        :short => "-r FORMAT",
        :long => "--readme-format FORMAT",
        :description => "Format of the README file, supported formats are 'md' (markdown) and 'rdoc' (rdoc)"
     option :cookbook_license,
        :short => "-I LICENSE",
        :long => "--license LICENSE",
        :description => "License for cookbook, apachev2, gplv2, gplv3, mit or none"
     option :cookbook_copyright,
        :short => "-C COPYRIGHT",
        :long => "--copyright COPYRIGHT",
        :description => "Name of Copyright holder"
     option :cookbook_email,
        :short => "-m EMAIL",
        :long => "--email EMAIL",
        :description => "Email address of cookbook maintainer"

    banner "knife model create COOKBOOK (options)"


    def run
     self.config = Chef::Config.merge!(config)
     if @name_args.length < 1
       show_usage
       ui.fatal("You must specify a cookbook name")
       exit 1
     end

    if default_cookbook_path_empty? && parameter_empty?(config[:cookbook_path])
     raise ArgumentError, "Default cookbook_path is not specified in the knife.rb config file, and a value to -o is not provided. Nowhere to write the new cookbook to."
    end
  	version = config[:version] || "1.0.0"
  	cookbook_path = File.expand_path(Array(config[:cookbook_path]).first)

    parse("#{version}")

  	cookbook_name = @name_args.first
  	copyright = config[:cookbook_copyright] || "YOUR_COMPANY_NAME"
  	email = config[:cookbook_email] || "YOUR_EMAIL"
  	license = ((config[:cookbook_license] != "false") && config[:cookbook_license]) || "none"
  	readme_format = ((config[:readme_format] != "false") && config[:readme_format]) || "md"

  	create_cookbook(cookbook_path, cookbook_name, copyright, license)
  	create_readme(cookbook_path, cookbook_name, readme_format)
  	create_changelog(cookbook_path, cookbook_name)
  	create_metadata(cookbook_path, cookbook_name, copyright, email, license, readme_format,version)
  end


    def create_cookbook(dir, cookbook_name, copyright, license)
      super
       puts "#{dir} : #{cookbook_name} "
        #Remove default
       FileUtils.rm(File.join(dir, cookbook_name, "recipes", "default.rb"))
       files =["add.rb", "update.rb", "remove.rb", "replace.rb", "status.rb"]
       files.each do |filename|

        unless File.exists?(File.join(dir, cookbook_name, "recipes", filename))
          open(File.join(dir, cookbook_name, "recipes", filename), "w") do |file|
file.puts <<-EOH

  #
  # Cookbook Name:: #{cookbook_name}
  # Recipe:: #{filename}
  #
  # Copyright #{Time.now.year}, #{copyright}
  #
EOH
         end
       end

      end
     end

      def create_metadata(dir, cookbook_name, copyright, email, license, readme_format,version)
          cookbook_caps = cookbook_name.capitalize
		  open(File.join(dir, cookbook_name, "metadata.rb"), "w") do |file|
             if File.exists?(File.join(dir, cookbook_name, "README.#{readme_format}"))
                long_description = "long_description IO.read(File.join(File.dirname(__FILE__), 'README.#{readme_format}'))"
             end

file.puts <<-EOH
name '#{cookbook_caps}'
maintainer '#{copyright}'
maintainer_email '#{email}'
license '#{license}'
description 'Installs/Configures #{cookbook_name}'
#{long_description}
version '#{version}'

grouping 'default',
  :access => 'global',
  :packages => ['base', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest']


attribute 'name',
  :description => 'Enter the name',
  :default => '0',
  :format => {
    :help => 'Name',
    :category => '1.',
    :order => 1
  }

recipe "status", "#{cookbook_name} Status"
recipe "update", "#{cookbook_name} Update"
recipe "remove", "#{cookbook_name} Remove"
recipe "replace", "#{cookbook_name} Replace"
EOH
         end
       end

 def parse(str="")
	@major, @minor, @patch =
	case str.to_s
	when /^(\d+)\.(\d+)\.(\d+)$/
	[ $1.to_i, $2.to_i, $3.to_i ]
	when /^(\d+)\.(\d+)$/
	[ $1.to_i, $2.to_i, 0 ]
	else
	msg = "'#{str.to_s}' does not match 'x.y.z' or 'x.y'"
	raise Chef::Exceptions::InvalidCookbookVersion.new( msg )
	end
 end

    end
   end
 end
