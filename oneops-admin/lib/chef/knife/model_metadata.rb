class Chef
  class Knife
    class ModelMetadata < Chef::Knife::CookbookMetadata

      banner 'knife model metadata [COOKBOOKS...] (options)'

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Generate metadata for all cookbooks, rather than just a single cookbook"
      def generate_metadata_from_file(cookbook, file)
        ui.info("Generating metadata for #{cookbook} from #{file}")
        md = Chef::Cookbook::Metadata.new
        md.name(cookbook)
        md.from_file(file)
        Chef::Log.debug(md.to_yaml)
        xml_file = File.join(File.dirname(file), 'metadata.xml')
        if md.groupings['default'][:relation]
          content = generate_relation_xml_from_md(md)
        else
          content = generate_class_xml_from_md(md)
        end
        File.open(xml_file, "w") do |f|
          f.write(content.to_xml(:skip_types => true, :dasherize => false, :root => "cms_model"))
        end
        generated = true
        ui.info("Generated #{xml_file}")
      rescue Exceptions::ObsoleteDependencySyntax, Exceptions::InvalidVersionConstraint => e
        STDERR.puts "ERROR: The cookbook '#{cookbook}' contains invalid or obsolete metadata syntax."
        STDERR.puts "in #{file}:"
        STDERR.puts
        STDERR.puts e.message
        exit 1
        end

      def generate_class_xml_from_md(md)
        cms_classes = Hash.new
        cms_classes['cms_classes'] = Array.new
        # super class first
        # cms_classes['cms_classes'].push( generate_class(md,'base','default') )
        # subclasses
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].each do |package_name|
            cms_classes['cms_classes'].push( generate_class(md,package_name,group_name) )
          end
        end
        return cms_classes
      end

      def generate_relation_xml_from_md(md)
        cms_relations = Hash.new
        cms_relations['cms_relations'] = Array.new
        # super class first
        # cms_relations['cms_relations'].push( generate_relation(md,'base','default') )
        # subclasses
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].each do |package_name|
            cms_relations['cms_relations'].push( generate_relation(md,package_name,group_name) )
          end
        end
        return cms_relations
      end

      def generate_class(md,package,group)
        cms_class = Hash.new
        properties = md.groupings[group]
        cms_class['class_name'] = [package, md.name].join('.')
        cms_class['class_desc'] = properties[:description] ? properties[:description] : md.description
        cms_class['super_class_name'] = ['base',md.name].join('.') unless package == 'base'
        cms_class['is_namespace'] = properties[:namespace] ? 1 : 0
        cms_class['access_level'] = properties[:access] ? properties[:access] : 'global'
        cms_class['class_attributes'] = Array.new
        md.attributes.each do |name,properties|
          if package == 'base'
            if !properties[:grouping]
              attribute = generate_attribute(name,properties)
              cms_class['class_attributes'].push(attribute)
            end
          else
            if properties[:grouping] && properties[:grouping] == group
              attribute = generate_attribute(name,properties)
              cms_class['class_attributes'].push(attribute)
            end
          end
        end
        return cms_class
      end

      def generate_relation(md,package,group)
        cms_relation = Hash.new
        properties = md.groupings[group]
        cms_relation['relation_name'] = [package, md.name].join('.')
        cms_relation['relation_desc'] = properties[:description] ? properties[:description] : md.description
        cms_relation['relation_attributes'] = Array.new
        cms_relation['relation_targets'] = Array.new
        md.attributes.each do |name,properties|
          if properties[:relation_target]
            if properties[:package] && properties[:package] == package
              cms_relation['relation_targets'].push( generate_target(name,properties) )
            end
          else
            if !properties[:grouping]
              attribute = generate_attribute(name,properties)
              cms_relation['relation_attributes'].push(attribute)
            else
              if properties[:grouping] == group
                attribute = generate_attribute(name,properties)
                cms_relation['relation_attributes'].push(attribute)
              end
            end
          end
        end
        return cms_relation
      end

      def generate_attribute(name,properties)
        attribute = Hash.new
        attribute['attr_name'] = name
        attribute['attr_desc'] = properties['description']
        attribute['is_mandatory'] = properties['required'] == 'required' ? 1 : 0
        attribute['is_inheritable'] = properties['inherit'] == 'no' ? 0 : 1
        attribute['is_encrypted'] = properties['encrypted'] == true ? 1 : 0
        attribute['is_immutable'] = properties['immutable'] == true ? 1 : 0
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
        attribute['data_type'] = properties['data_type'] if properties['data_type']
        return attribute
      end

      def generate_target(name,properties)
        target = Hash.new
        target['from_class'] = properties[:from_class]
        target['to_class'] = properties[:to_class]
        target['link_type'] = properties[:link_type]
        target['is_strong'] = properties['required'] == 'required' ? 1 : 0
        target['comments'] = name
        return target
      end
    end
  end
end
