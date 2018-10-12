class Base::PlatformsController < ApplicationController
  include ::RfcHistory

  def diagram
    begin
      ns_path = if @environment
                  transition_platform_ns_path(@environment, @platform)
                elsif @design
                  catalog_design_platform_ns_path(@design, @platform)
                elsif @assembly
                  design_platform_ns_path(@assembly, @platform)
                else
                  @platform.nsPath
                end

      templates     = get_platform_requires_relation_temlates(@platform)
      rels          = Cms::DjRelation.all(:params => {:nsPath => ns_path, :includeToCi => true})
      depends_rels  = []
      requires_rels = []
      cluster_map   = {}
      rels.each do |r|
        short_name = r.relationName.split('.').last
        if short_name == 'DependsOn'
          depends_rels << r
        elsif short_name == 'Requires'
          requires_rels << r
        elsif short_name == 'ManagedVia'
          cluster_map[r.fromCiId] = r.toCi
          cluster_map[r.toCiId] = r.toCi
        end
      end

      graph_options = {:truecolor => true,
                       :rankdir   => 'TB',
                       :center    => true,
                       :ratio     => 'fill',
                       :size      => '9,6',
                       :bgcolor   => 'transparent',
                       :compound  => true}

      graph = GraphViz::new('G')
      graph[graph_options.merge(params.slice(*graph_options.keys))]

      graph.node[:fontsize  => 8,
                 :fontname  => 'ArialMT',
                 :fontcolor => 'black',
                 :color     => 'black',
                 :fillcolor => 'whitesmoke',
                 :fixedsize => true,
                 :width     => '2.50',
                 :height    => '0.66',
                 :shape     => 'rect',
                 :style     => 'rounded']
      graph.edge[:fontsize  => 10,
                 :fontname  => 'ArialMT',
                 :fontcolor => 'black',
                 :color     => 'gray']

      clusters = cluster_map.values.uniq.to_map_with_value do |c|
        [c, graph.add_graph("cluster_#{c.ciId}",
                            :clusterrank => 'local',
                            :label => "managed via #{c.ciName}",
                            :fillcolor => 'whitesmoke',
                            :color => 'lightgray',
                            :style => 'solid,rounded,filled')]
      end
      clusters[nil] = graph

      urls = {}
      requires_rels.each do |requires|
        ci       = requires.toCi
        template = templates.find {|t| t.toCi.ciName == requires.relationAttributes.template}
        if @assembly
          if @environment
            url = edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, ci)
          else
            url = edit_assembly_design_platform_component_path(@assembly, @platform, ci)
          end
        elsif @design
          url = catalog_design_platform_component_path(@design, @platform, ci)
        else
          url = catalog_pack_platform_component_path(:platform_id => @platform, :id => ci)
        end
        urls[ci.ciId] = url

        img   = "<img scale='both' src='#{GRAPHVIZ_IMG_STUB}'/>"
        label = "<<table border='0' cellspacing='2' fixedsize='true' width='180' height='48'>"
        label << "<tr><td fixedsize='true' rowspan='2' cellpadding='4' width='40' height='40' align='center'>#{img}</td>"
        label << "<td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='12'>#{ci.ciName.size > 20 ? "#{ci.ciName[0..18]}..." : ci.ciName}</font></td></tr>"
        label << "<tr><td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='10'>#{ci.updated_timestamp.to_s(:short_us)}</font></td></tr></table>>"

        optional = requires.relationAttributes.constraint.start_with?('0..')
        obsolete = template && template.toCi.ciState == 'pending_deletion'
        g = clusters[cluster_map[requires.toCiId]]

        g.add_node(requires.toCiId.to_s,
                       :id        => requires.toCiId.to_s,
                       :target    => '_parent',
                       :tooltip   => ci.ciClassName,
                       :URL       => url,
                       :label     => label,
                       # :shape     => optional ? 'ellipse' : nil,
                       :style     => "bold,rounded#{',dashed' if optional}#{',filled' if obsolete}",
                       :fillcolor => obsolete ? '#FFDDDD' : 'transparent',
                       :color     => rfc_action_to_color(ci.rfcAction))
      end

      depends_rels.each do |rel|
        if rel.relationAttributes.flex == 'true'
          edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Scale</font></td></tr>"
          edgelabel << "<tr><td align='left'>Minimum</td><td>#{rel.relationAttributes.min}</td></tr>"
          edgelabel << "<tr><td align='left' bgcolor='#D9EDF7'>Current</td><td bgcolor='#D9EDF7'>#{rel.relationAttributes.current}</td></tr>"
          edgelabel << "<tr><td align='left'>Maximum</td><td>#{rel.relationAttributes.max}</td></tr>"
          edgelabel << "</table>>"
          graph.add_edge(rel.fromCiId.to_s, rel.toCiId.to_s,
                         :labeltarget   => '_parent',
                         :labelURL      => urls[rel.fromCiId],
                         :minlen        => 1,
                         :penwidth      => 1,
                         :color         => rfc_action_to_color(rel.rfcAction),
                         :labeldistance => 3.0,
                         :arrowhead     => 'crow',
                         :label         => edgelabel)
        elsif rel.relationAttributes.converge == 'true'
          edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Converge</font></td></tr></table>>"
          graph.add_edge(rel.fromCiId.to_s, rel.toCiId.to_s,
                         :labeltarget   => '_parent',
                         :minlen        => 1,
                         :penwidth      => 1,
                         :color         => rfc_action_to_color(rel.rfcAction),
                         :labeldistance => 3.0,
                         :arrowhead     => 'odiamond',
                         :label         => edgelabel)
        else
          graph.add_edge(rel.fromCiId.to_s, rel.toCiId.to_s,
                         :color => rfc_action_to_color(rel.rfcAction),
                         :style => rel.relationAttributes.attributes['source'] == 'user' ? 'dashed' : 'solid')
        end
      end
      @diagram = graphvis_sub_ci_remote_images(graph.output(:svg => String))
    rescue Exception  => e
      Rails.logger.warn "Failed to generate platform diagram: #{e}"
      @diagram = nil
    end

    respond_to do |format|
      format.html do
        if @diagram
          send_data(@diagram, :type => 'image/svg+xml', :disposition => 'inline')
        else
          send_data('Failed to generate', :type => 'text', :disposition => 'inline')
        end
      end

      format.js {render 'base/platforms/diagram'}
    end
  end


  protected

  def ci_resource
    @platform
  end

  def get_platform_requires_relation_temlates(platform)
    Cms::Relation.all(:params => {:nsPath      => platform_pack_ns_path(platform),
                                  :relationShortName => 'Requires',
                                  :includeToCi       => true,
                                  :getEncrypted      => true})
  end

  def build_component_groups
    group_map = get_platform_requires_relation_temlates(@platform).inject({}) do |map, r|
      template_ci      = r.toCi
      template_name    = template_ci.ciName.split('::').last
      short_class_name = template_ci.ciClassName.split('.')[2..-1].join('.')
      cardinality      = r.relationAttributes.constraint.gsub('*', '999')
      group_id         = "#{template_name}_#{@platform.ciId}"
      map.update(group_id => {:id            => group_id,
                              :template_name => template_name,
                              :class_name    => "catalog.#{short_class_name}",
                              :cardinality   => Range.new(*cardinality.split('..').map(&:to_i)),
                              :obsolete      => template_ci.ciState == 'pending_deletion',
                              :items         => []})
    end

    pack_ns_path     = platform_pack_ns_path(@platform)
    platform_ns_path = design_platform_ns_path(@assembly, @platform)

    monitors_map = Cms::DjRelation.all(:params => {:nsPath => platform_ns_path,
                                                   :relationShortName => 'WatchedBy'}).inject({}) do |h, r|
      h[r.fromCiId] ||= 0
      h[r.fromCiId] += 1
      h
    end

    attachments_map = Cms::DjRelation.all(:params => {:nsPath            => platform_ns_path,
                                                      :relationShortName => 'EscortedBy'}).inject({}) do |h, r|
      h[r.fromCiId] ||= 0
      h[r.fromCiId] += 1
      h
    end

    @components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'Requires',
                                                  :includeToCi       => true,
                                                  :attrProps         => 'owner'}).map do |r|
      group_id  = "#{r.relationAttributes.template}_#{@platform.ciId}"
      component = r.toCi
      component.monitors = monitors_map[component.ciId].to_i
      component.attachments = attachments_map[component.ciId].to_i
      component.add_policy_locations(pack_ns_path) if Settings.check_policy_compliance
      group = group_map[group_id]
      unless group
        # Must be due to deleted component template.
        group = {:id            => group_id,
                 :template_name => r.relationAttributes.template,
                 :class_name    => component.ciClassName,
                 :cardinality   => Range.new(*r.relationAttributes.constraint.gsub('*', '999').split('..').map(&:to_i)),
                 :obsolete      => true,
                 :items         => []}
        group_map[group_id] = group
      end
      group[:items] << component
      component
    end

    @component_groups = group_map.values
                          .reject {|g| g[:obsolete] && g[:items].blank?}
                          .sort { |g1, g2| g1[:template_name] <=> g2[:template_name] }
  end
end
