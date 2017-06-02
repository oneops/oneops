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
    class ModelUpload < Chef::Knife::CookbookUpload
  
      banner "knife model upload [COOKBOOKS...] (options)"
  
      private
      def upload(cookbook, justify_width)
        ui.info("Pushing #{cookbook.name.to_s.ljust(justify_width + 10)} [#{cookbook.version}]")
        check_for_broken_links(cookbook)
        cms_classes = generate_from_metadata(cookbook.metadata)
        
        Log.debug(cookbook.metadata.to_yaml)
        
        puts cms_classes.to_xml(:skip_types => true, :dasherize => false, :root => "cms_model")
                                  
      end
      
      def generate_class_from_metadata(md)
        cms_classes = Hash.new
        cms_classes['cms_classes'] = Array.new
        md.groupings.each do |package_name,package_properties|
          cms_class = Hash.new
          cms_class['class_attributes'] = Array.new
          cms_class['class_name'] = [package_name, md.name].join('.')
          cms_class['class_desc'] = package_properties[:description]
          cms_class['is_namespace'] = package_properties[:namespace] ? 1 : 0
          cms_class['access_level'] = package_properties[:access]
  
          md.attributes.each do |name,properties|
            attribute = Hash.new
            attribute['attr_name'] = name
            attribute['attr_desc'] = properties['description']
            attribute['is_mandatory'] = properties['required'] == 'required' ? 1 : 0
            attribute['is_inheritable'] = properties['inherit'] == 'no' ? 0 : 1
            attribute['default_value'] = properties['default']
            attribute['value_format'] = properties['format']
            case properties['type']
            when 'array'
              attribute['data_type'] = 'enum'
            when 'hash'
              attribute['data_type'] = 'object'
            else
              attribute['data_type'] = 'string'
            end
            cms_class['class_attributes'].push(attribute) 
          end
          cms_classes['cms_classes'].push(cms_class)
        end              
        return cms_classes
      end    
    
    end
  end
end
