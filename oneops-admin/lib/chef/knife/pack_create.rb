class Chef
 class Knife
   class PackCreate < Chef::Knife

     banner "knife pack create PACK (options)"

     option :pack_path,
        :short => "-o PATH",
        :long => "--pack-path PATH",
        :description => "The directory where the pack will be created"
     option :pack_copyright,
        :short => "-C COPYRIGHT",
        :long => "--copyright COPYRIGHT",
        :description => "Name of Copyright holder"
     option :owner,
        :short => "-m Owner",
        :long => "--owner owner",
        :description => "Email address of pack owner"
     option :pack_subdir,
        :short => "-s subdirectory",
        :long => "--subdirectory SUBDIRECTORY",
        :description => "Subdirectory under packs '1/2/3'"
     option :pack_category,
        :short => "-c CATEGORY",
        :long => "--category CATEGORY",
        :description => "Category of the pack"
     option :type,
        :short => "-t TYPE",
        :long => "--type TYPE",
        :description => "Limit to the specified type "

     def run
        self.config = Chef::Config.merge!(config)
        if @name_args.length < 1
           show_usage
           ui.fatal("You must specify a pack name")
           exit 1
        end


        pack_path = File.expand_path(Array(config[:pack_path]).first)
        subdir_path = config[:pack_subdir]||""

        version = config[:version]||"1.0.0"

        full_path=File.join(pack_path,subdir_path)
        ui.info("Path : #{full_path}")

        pack_name = @name_args.first
        copyright = config[:pack_copyright] || "YOUR_COMPANY_NAME"
        category = config[:pack_category] || "OTHER"
        owner = config[:owner] || "YOUR_EMAIL"
        config[:type] ||='platform'


        create_pack(full_path, pack_name, copyright,owner,category,config[:type].capitalize,config[:version].split(".").first)
      end

    def create_pack(pack_path, pack_name, copyright,owner,category,type,version)
        FileUtils.mkdir_p(File.join(pack_path)) unless File.exists?(File.join(pack_path))
        unless File.exists?(File.join(pack_path, "#{pack_name}.rb"))
        open(File.join(pack_path, "#{pack_name}.rb"), "w") do |file|

file.puts <<-EOH

#
# Pack Name:: #{pack_name}
# Maintainer:: #{owner}
# Copyright #{Time.now.year}, #{copyright}
#
  
include_pack "base"

name "#{pack_name}"
description "#{pack_name}"
category "#{category}"
owner "#{owner}"
version "#{version}"
type "#{type}"

#environment "single", {}
#environment "redundant", {}

EOH
         end
       end

      end

    end
   end
 end
