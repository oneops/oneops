require 'chef/knife'
require 'chef/exceptions'
require 'cms'

class Chef
  class Knife
    class ModelGraph < Chef::Knife::CookbookMetadata

      banner "knife model graph [COOKBOOKS...] (options)"

      option :all,
        :short => "-a",
        :long => "--all",
        :description => "Generate graph for all cookbooks"
      
      option :include,
        :short => "-i PACKAGE",
        :long => "--include PACKAGE",
        :description => "Include package PACKAGE"

      option :exclude,
        :short => "-e PACKAGE",
        :long => "--exclude PACKAGE",
        :description => "Exclude package PACKAGE"

      option :format,
        :short => "-f FORMAT",
        :long => "--format FORMAT",
        :description => "Output format FORMAT"
        
      option :output,
        :short => "-o FILE",
        :long => "--output FILE",
        :description => "Output file FILE"
        
      def run
      
        require 'graphviz'
        
        config[:cookbook_path] ||= Chef::Config[:cookbook_path]

        if config[:all]
          graph = GraphViz::new( "G" )
          graph.node['width'] = "3"
          cl = Chef::CookbookLoader.new(config[:cookbook_path])
          cl.each do |cname, cookbook|
            graph = generate_graph(graph, cname.to_s)
          end
          if (config[:format])
            graph.output( config[:format].to_sym => config[:output] )
          else
            graph.output( :dot => nil )
          end
        else
          ui.error "You must specify -a or -all for graphing the model"
          exit 1
        end
      end

      def generate_graph(g, cookbook)
        Array(config[:cookbook_path]).reverse.each do |path|
          file = File.expand_path(File.join(path, cookbook, 'metadata.rb'))
          if File.exists?(file)
            g = generate_metadata_from_file(g, cookbook, file)
          else
            validate_metadata_json(path, cookbook)
          end
          return g
        end
      end       
        
        
      def generate_metadata_from_file(g, cookbook, file)
        ui.info("Generating graph data for #{cookbook} from #{file}")
        md = Chef::Cookbook::Metadata.new
        md.name(cookbook)
        md.from_file(file)
        Chef::Log.debug(md.to_yaml)
        if md.groupings['default'][:relation]
          g = generate_relation_graph_from_md(g, md)
        else
          g = generate_class_graph_from_md(g, md)
        end
        generated = true
        ui.info("Generated graph data for #{cookbook} from #{file}")
        return g
      end

      def generate_class_graph_from_md(g, md)
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].each do |package_name|
            if (config[:include])
              next unless package_name == config[:include]
            end
            if (config[:exclude])
              next if package_name == config[:exclude]
            end
            c = g.add_graph( "cluster-#{package_name}", :label => package_name)
            cms_class = generate_class(md,package_name,group_name)
            n = c.add_node( cms_class['class_name'], :shape => "rectangle", :label => "#{cms_class['class_name']}\n#{cms_class['class_desc']}" )
            #g.add_edge( cms_class['class_name'], cms_class['super_class_name'] ) unless cms_class['super_class_name'].nil?
          end
        end
        return g
      end

      def generate_relation_graph_from_md(g, md)
        md.groupings.each do |group_name,group_properties|
          group_properties[:packages].each do |package_name|
            if (config[:include])
              next unless package_name == config[:include]
            end
            if (config[:exclude])
              next if package_name == config[:exclude]
            end
            c = g.add_graph( "cluster-#{package_name}", :label => package_name)
            cms_relation = generate_relation(md,package_name,group_name)
            #n = c.add_node( cms_relation['relation_name'], :shape => "rectangle", :label => cms_relation['relation_name'] )
            cms_relation['relation_targets'].each do |target|
              g.add_edge(target['from_class'], target['to_class'], :label => cms_relation['relation_name'])
            end
          end
        end
        return g
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
