class Base::PlatformsController < ApplicationController
  def show
    respond_to do |format|
      format.html do
        group_map = get_platform_requires_relation_temlates(@platform, @environment).inject({}) do |map, r|
          template_name    = r.toCi.ciName.split('::').last
          short_class_name = r.toCi.ciClassName.split('.')[2..-1].join('.')
          cardinality      = r.relationAttributes.constraint.gsub('*', '999')
          group_id = "#{template_name}_#{@platform.ciId}"
          map.update(group_id => {:id            => group_id,
                                  :template_name => template_name,
                                  :class_name    => "#{scope}.#{short_class_name}",
                                  :cardinality   => Range.new(*cardinality.split('..').map(&:to_i)),
                                  :items         => []})
        end

        components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                     :direction         => 'from',
                                                     :relationShortName => 'Requires',
                                                     :includeToCi       => true,
                                                     :attrProps         => 'owner'})
        components.each do |c|
          group_id = "#{c.relationAttributes.template}_#{@platform.ciId}"
          group_map[group_id][:items] << c.toCi
        end

        group_map.reject! { |k, v| v[:items].blank? } if @environment

        @component_groups = group_map.values.sort {|g1, g2| g1[:template_name] <=> g2[:template_name]}

        @policy_compliance = Cms::Ci.violates_policies(components.map(&:toCi), false, true) if Settings.check_policy_compliance

        render(:action => :show)
      end

      format.json do
        @platform.links_to = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                             :direction         => 'from',
                                                             :relationShortName => 'LinksTo',
                                                             :includeToCi       => true}).map { |r| r.toCi.ciName } if @platform
        render_json_ci_response(true, @platform)
      end
    end
  end

  def diagram
    begin
      graph = GraphViz::new('G')
      graph_options = {
            :truecolor  => true,
            :rankdir    => 'TB',
            :center     => true,
            :ratio      => 'fill',
            :size       => params[:size] || '9,6',
            :bgcolor    => 'transparent'}
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


      ns_path = if @environment
                  transition_platform_ns_path(@environment, @platform)
                elsif @catalog
                  catalog_platform_ns_path(@catalog, @platform)
                else
                  design_platform_ns_path(@assembly, @platform)
                end
      depends_rels = Cms::DjRelation.all(:params => {:nsPath => ns_path, :relationShortName => 'DependsOn'})

      components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                   :relationShortName => 'Requires',
                                                   :direction         => 'from',
                                                   :includeToCi       => true})
      components.each do |node|
        ci = node.toCi
        url = nil
        if @assembly
          if @environment
            url = edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, ci.id)
          else
            url = edit_assembly_design_platform_component_path(@assembly, @platform, ci.id)
          end
        elsif @catalog
          url = edit_catalog_platform_component_path(@catalog, @platform, ci.id)
        end
        img = "<img scale='both' src='#{GRAPHVIZ_IMG_STUB}'/>"
        label = "<<table border='0' cellspacing='2' fixedsize='true' width='180' height='48'>"
        label << "<tr><td fixedsize='true' rowspan='2' cellpadding='4' width='40' height='40' align='center'>#{img}</td>"
        label << "<td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='12'>#{ci.ciName.size > 20 ? "#{ci.ciName[0..18]}..." : ci.ciName}</font></td></tr>"
        label << "<tr><td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='10'>#{ci.updated_timestamp.to_s(:short_us)}</font></td></tr></table>>"
        graph.add_node(node.toCiId.to_s,
                       :id => node.toCiId.to_s,
                       :target => "_parent",
                       :tooltip => ci.ciClassName,
                       :URL    => url,
                       :label  => label,
                       :color  => to_color(ci.rfcAction))

        depends_rels.select {|rel| rel.fromCiId == node.toCiId}.each do |edge|
          if edge.relationAttributes.flex == 'true'
            edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Scale</font></td></tr>"
            edgelabel << "<tr><td align='left'>Minimum</td><td>#{edge.relationAttributes.min}</td></tr>"
            edgelabel << "<tr><td align='left' bgcolor='#D9EDF7'>Current</td><td bgcolor='#D9EDF7'>#{edge.relationAttributes.current}</td></tr>"
            edgelabel << "<tr><td align='left'>Maximum</td><td>#{edge.relationAttributes.max}</td></tr>"
            edgelabel << "</table>>"
            graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s,
              :labeltarget => "_parent",
              :labelURL => "#{url}",
              :minlen => 1,
              :penwidth => 1,
              :color => 'black',
              :labeldistance => 3.0,
              :arrowtail => 'odiamond',
              :dir => 'back',
              :label => edgelabel
            )
          elsif edge.relationAttributes.converge == 'true'
            edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Converge</font></td></tr></table>>"
            graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s,
              :labeltarget => "_parent",
              :labelURL => url,
              :minlen => 1,
              :penwidth => 1,
              :color => 'black',
              :labeldistance => 3.0,
              :arrowhead => 'odiamond',
              :label => edgelabel
            )
          else
            graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s)
          end
        end
      end
      @diagram = graphvis_sub_ci_remote_images(graph.output(:svg => String))
    rescue Exception => e
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

  def get_platform_requires_relation_temlates(platform, environment = nil)
    ns_path = environment ? platform_pack_transition_ns_path(platform) : platform_pack_design_ns_path(platform)
    template_ci = Cms::Ci.first(:params => {:nsPath      => ns_path,
                                            :ciClassName => "mgmt.#{scope}.Platform"})
    Cms::Relation.all(:params => {:ciId              => template_ci.ciId,
                                  :relationShortName => 'Requires',
                                  :direction         => 'from',
                                  :includeToCi       => true,
                                  :getEncrypted      => true})
  end
end
