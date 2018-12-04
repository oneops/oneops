class DesignController < ApplicationController
  swagger_controller :design, 'Design Management'

  include ::Search

  before_filter :find_assembly
  before_filter :find_latest_release, :only => [:show, :extract, :load]
  before_filter :check_open_release, :only => [:load]

  PLATFORM_EXPORT_ATTRIBUTES = %w(description major_version pack source version)

  def show
    respond_to do |format|
      format.html do
        platforms = Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                    :direction       => 'from',
                                                    :targetClassName => 'catalog.Platform',
                                                    :relationName    => 'base.ComposedOf'})
        @platforms = platforms.map(&:toCi)
        @diagram = prepare_platforms_diagram(platforms) if @platforms.present?

        render :action => :show
      end
    end
  end

  def extract
    collapse = params[:collapse]
    collapse = false if collapse == 'false'
    respond_to do |format|
      format.json do
        render :json => export_design(collapse, params[:platform_id])
      end

      format.yaml do
        render :text => export_design(collapse, params[:platform_id]).to_yaml, :content_type => 'text/data_string'
      end
    end
  end

  def load
    if request.put?
      loaded = false

      @preview = params[:preview] == 'true'
      data_file = params[:data_file]
      @data_string = (data_file && data_file.read).presence || params[:data]

      begin
        assembled_data, @data_string, @errors = assemble_load_data(@data_string)
      rescue Exception => e
        @errors = ["Failed to assemble configuration file - unexpected import data. #{e}"]
      end

      if assembled_data
        begin
          load_data, @errors = convert_load_data(assembled_data)
        rescue Exception => e
          @errors = ["Failed to parse configuration file - unexpected data structure. #{e}"]
        end

        if load_data && !@preview
          loaded, message = Transistor.import_design(@assembly, load_data)
          @errors = [message] unless loaded
        end
      end

      respond_to do |format|
        format.html do
          if loaded
            flash[:notice] = 'Successfully loaded design.'
            redirect_to assembly_design_url(@assembly)
          end
        end

        format.json do
            if @errors.blank?
              render(:json => assembled_data, :status => :ok)
            else
              render(:json   => (assembled_data || {}).merge(:errors => @errors),
                     :status => @preview ? :ok : :unprocessable_entity)
            end
        end

        if @preview
          format.yaml do
            render :text => @errors.blank? ? assembled_data : (assembled_data || {}).merge(:errors => @errors).to_yaml,
                   :content_type => 'text/data_string'
          end
        end
      end
    end
  end

  def diagram
    send_data(prepare_platforms_diagram, :type => 'image/svg+xml', :disposition => 'inline')
  end


  protected

  def search_ns_path
    design_ns_path(@assembly)
  end


  private

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
  end

  def find_latest_release
    @release = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))
  end

  def check_open_release
    if @release && @release.releaseState == 'open'
      message = 'Design load is not allowed when there is an open release. Please commit or discard current release before proceeding with design loading.'
      respond_to do |format|
        format.html do
          flash.now[:error] = message
          show
        end

        format.json {render :json => {:errors => [message]}, :status => :unprocessable_entity}
      end
    end
  end

  def prepare_platforms_diagram(platforms = nil)
    platforms ||= Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                  :direction       => 'from',
                                                  :targetClassName => 'catalog.Platform',
                                                  :relationName    => 'base.ComposedOf',
                                                  :includeToCi     => true})
    links_to = Cms::DjRelation.all(:params => {:nsPath            => [@assembly.nsPath, @assembly.ciName].join('/'),
                                               :relationShortName => 'LinksTo'})
    begin
      return graphvis_sub_pack_remote_images(platforms_diagram(platforms, links_to, assembly_design_path(@assembly), params[:size]).output(:svg => String))
    rescue
      return nil
    end
  end

  def export_design(collapse, platform_id = nil)
    design = Transistor.export_design(@assembly, platform_id && [platform_id])

    result = {}

    transfer_if_present('variables', design, result)

    platforms = design['platforms']
    return result if platforms.blank?

    result['platforms'] = platforms.sort_by {|p| p['name']}.inject({}) do |plats, p|
      attrs = p['attributes'].slice(*PLATFORM_EXPORT_ATTRIBUTES)
      plat = {'pack' => "#{attrs.delete('source')}/#{attrs.delete('pack')}:#{attrs.delete('version')}"}
      attrs.delete('description') if attrs['description'].blank?
      plat.merge!(attrs)

      transfer_if_present('links', p, plat)
      transfer_if_present('variables', p, plat)

      components = p.delete('components')
      if components.present?
        plat['components'] = components.group_by {|c| "#{c['template']}/#{c['type'].sub(/^catalog\./, '')}"}.inject({}) do |templates_hash, (template_name, template_components)|
          templates_hash[template_name] = template_components.sort_by {|c| c['name']}.to_map_with_value do |c|
            comp = c['attributes'].presence || {}
            comp = convert_json_attrs_from_string(comp, c['type']) unless collapse

            transfer_if_present('depends', c, comp)

            attachments = c['attachments']
            if attachments.present?
              comp['attachments'] = attachments.sort_by {|a| a['name']}.to_map_with_value do |a|
                attrs = a['attributes']
                attrs = convert_json_attrs_from_string(attrs, 'catalog.Attachment') unless collapse
                [a['name'], attrs]
              end
            end

            monitors = c['monitors']
            watched_bys = c['watchedBy']
            if watched_bys.present?
              monitor_map = monitors.blank? ? {} : monitors.to_map {|w| w['name']}
              monitor_map = watched_bys.inject(monitor_map) do |mm, w|
                monitor_name = w['name']
                monitor = mm[monitor_name]
                if monitor
                  monitor['attributes'].merge!(w['attributes'])
                else
                  mm[monitor_name] = w
                end
                mm
              end
              monitors = monitor_map.values
            end
            if monitors.present?
              comp['monitors'] = monitors.sort_by {|a| a['name']}.to_map_with_value do |a|
                attrs = a['attributes']
                attrs = convert_json_attrs_from_string(attrs, 'catalog.Monitor') unless collapse
                [a['name'], attrs]
              end
            end

            [c['name'], comp]
          end
          templates_hash
        end
      end
      plats[p['name']] = plat
      plats
    end
    result
  end

  def assemble_load_data(data_string)
    return nil, data_string, ['Failed to parse configuration file - no data detected.'] if data_string.blank?

    data, format, errors = parse_load_data(data_string)
    return nil, data_string, errors if errors.present?

    imports = data.delete('import')
    return data, data_string if imports.blank?

    imports_data = {}
    errors = {}
    imports.each do |import|
      begin
        uri = URI(import)
      rescue Exception => e
        errors[import] = "Invalid import URI: #{e}"
        next
      end

      begin
        response = Net::HTTP.get_response(uri)
        if response.is_a?(Net::HTTPOK)
          import_string = response.body
        else
          raise Exception.new(response.body)
        end
      rescue Exception => e
        errors[import] = "Failed to load: #{e}"
        next
      end

      import_data, import_format, error = parse_load_data(import_string)
      if import_data
        imports_data = imports_data.deep_merge(import_data) if errors.blank?
      else
        errors[import] = "Failed to parse: #{error}"
      end
    end

    if errors.blank?
      data = imports_data.deep_merge(data)
      return data, format == :yaml ? data.to_yaml : data.to_json
    else
      return false, data_string, {'import' => errors}
    end
  end

  def convert_load_data(data)
    result = {}
    errors = {}

    assembly_ns_path = assembly_ns_path(@assembly)

    vars = data['variables']
    if vars.present?
      result['variables'] = {}
      errors['variables'] = {}
      vars.each do |var_name, value|
        var_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Globalvar',
                                  :nsPath       => assembly_ns_path,
                                  :ciName       => var_name,
                                  :ciAttributes => {:value => value}})
        errors['variables'][var_name] = {'errors' => var_ci.errors.full_messages} unless var_ci.valid?
        result['variables'][var_name] = value
      end
    end

    plats = data['platforms']
    if plats.present?
      attachment_md_attrs = Cms::CiMd.look_up('catalog.Attachment').mdAttributes.map(&:attributeName)
      monitor_md_attrs    = Cms::CiMd.look_up('catalog.Monitor').mdAttributes.map(&:attributeName)
      watched_by_md_attrs = Cms::RelationMd.look_up('catalog.WatchedBy').mdAttributes.map(&:attributeName)

      result['platforms'] = []
      errors['platforms'] = {}
      plats.each_pair do |plat_name, plat|
        errors['platforms'][plat_name] = {}

        pack_path = plat['pack']
        if pack_path =~ /^\w+\/[\w\-]+:\d+(\.\d+\.\d+)?$/
          source, pack, version = pack_path.split(/[\/:]/)
          ci_attrs = plat.slice(*PLATFORM_EXPORT_ATTRIBUTES)
          ci_attrs = ci_attrs.merge(:source => source, :pack => pack, :version => version)

          platform_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Platform',
                                         :nsPath       => assembly_ns_path,
                                         :ciName       => plat_name,
                                         :ciAttributes => ci_attrs})

          attrs = platform_ci.ciAttributes
          pack_ver = Cms::Ci.first(:params => {:nsPath      => "/public/#{attrs.source}/packs/#{attrs.pack}",
                                               :ciClassName => 'mgmt.Version',
                                               :ciName      => attrs.version})

          if pack_ver.blank?
            errors['platforms'][plat_name]['errors'] = ["Unknown platform pack [#{pack_path}]"]
          elsif pack_ver.ciAttributes.enabled == 'false'
            errors['platforms'][plat_name]['errors'] = ['Pack is disabled.']
          else
            platform_pack_ns_path = platform_pack_design_ns_path(platform_ci)
            pack_template = Cms::Ci.first(:params => {:nsPath      => platform_pack_ns_path,
                                                      :ciClassName => 'mgmt.catalog.Platform'})
            if pack_template
              unless platform_ci.valid?
                errors['platforms'][plat_name]['errors'] = platform_ci.errors.full_messages
              end

              result['platforms'] << ci_to_load(platform_ci)

              transfer_if_present('links', plat, result['platforms'].last)

              platform_ns_path = design_platform_ns_path(@assembly, platform_ci)
              pack_ns_path = platform_pack_ns_path(platform_ci)

              vars = plat['variables']
              if vars.present?
                result['platforms'].last['variables'] = {}
                errors['platforms'][plat_name]['variables'] = {}
                vars.each_pair do |var_name, value|
                  var_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Localvar',
                                            :nsPath       => platform_ns_path,
                                            :ciName       => var_name,
                                            :ciAttributes => {:value => value}})
                  var_ci.add_policy_locations(pack_ns_path)
                  errors['platforms'][plat_name]['variables'][var_name] = {'errors' => var_ci.errors.full_messages} unless var_ci.valid?
                  result['platforms'].last['variables'][var_name] = value
                end
              end

              comps_by_template = plat['components']
              if comps_by_template.present?
                result['platforms'].last['components'] = []
                errors['platforms'][plat_name]['components'] = {}

                comps_by_template.each_pair do |template_and_class, comps|
                  errors['platforms'][plat_name]['components'][template_and_class] = {'errors' => []}
                  if template_and_class =~ /^[\w\.\-]+\/[\w\.-]+$/
                    template, component_class = template_and_class.split('/')
                    component_class    = "catalog.#{component_class}" unless component_class.start_with?('catalog.')
                    component_class_md = Cms::CiMd.look_up!(component_class)
                    if component_class_md && component_class_md.is_a?(Cms::CiMd)
                      component_md_attrs = component_class_md.mdAttributes.map(&:attributeName)

                      component_template = Cms::Ci.first(:params => {:nsPath      => platform_pack_ns_path,
                                                                     :ciClassName => "mgmt.#{component_class}",
                                                                     :ciName      => template})
                      if component_template
                        comps.each_pair do |comp_name, comp|
                          errors_component = {}
                          errors['platforms'][plat_name]['components'][template_and_class][comp_name] = errors_component

                          component_attrs = comp.slice(*component_md_attrs)
                          component_attrs = convert_json_attrs_to_string(component_attrs)
                          component_ci    = Cms::DjCi.build({:ciClassName  => component_class,
                                                             :nsPath       => platform_ns_path,
                                                             :ciName       => comp_name,
                                                             :ciAttributes => component_template.ciAttributes.attributes.merge(component_attrs)})
                          component_ci.add_policy_locations(pack_ns_path)

                          errors_component['errors'] = component_ci.errors.full_messages unless component_ci.valid?
                          result['platforms'].last['components'] << ci_to_load(component_ci, :template => template, :attributes => component_attrs)

                          result_component = result['platforms'].last['components'].last
                          transfer_if_present('depends', comp, result_component)

                          attachments = comp['attachments']
                          if attachments.present?
                            result_component['attachments'] = []
                            errors_component['attachments'] = {}
                            attachments.each do |attachment_name, attachment|
                              errors_component['attachments'][attachment_name] = {}
                              attachment_attrs = attachment.slice(*attachment_md_attrs)
                              attachment_attrs = convert_json_attrs_to_string(attachment_attrs)
                              attachment_ci    = Cms::DjCi.build({:ciClassName  => 'catalog.Attachment',
                                                                  :nsPath       => platform_ns_path,
                                                                  :ciName       => attachment_name,
                                                                  :ciAttributes => attachment_attrs})
                              attachment_ci.add_policy_locations(pack_ns_path)

                              errors_component['attachments'][attachment_name]['errors'] = attachment_ci.errors.full_messages unless attachment_ci.valid?
                              result_component['attachments'] << ci_to_load(attachment_ci, :attributes => attachment_attrs)
                            end
                          end

                          monitors = comp['monitors']
                          if monitors.present?
                            result_component['monitors'] = []
                            result_component['watchedBy'] = []
                            errors_component['monitors'] = {}
                            monitors.each do |monitor_name, monitor|
                              errors_component['monitors'][monitor_name] = {}

                              monitor_attrs    = convert_json_attrs_to_string(monitor.slice(*monitor_md_attrs))
                              watched_by_attrs = convert_json_attrs_to_string(monitor.slice(*watched_by_md_attrs))

                              monitor_template = monitor_name.start_with?("#{plat_name}-#{comp_name}-") &&
                                Cms::Ci.first(:params => {:nsPath      => platform_pack_ns_path,
                                                          :ciClassName => 'mgmt.catalog.Monitor',
                                                          :ciName      => monitor_name.split('-').last})
                              if monitor_template
                                monitor_attrs.delete(:custom)
                              else
                                monitor_attrs.merge!(:custom => 'true')
                                watched_by_attrs.merge!(:soure => 'design')
                              end
                              monitor_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Monitor',
                                                            :nsPath       => platform_ns_path,
                                                            :ciName       => monitor_name,
                                                            :ciAttributes => monitor_template ? monitor_template.ciAttributes.attributes.merge(monitor_attrs) : monitor_attrs})
                              monitor_ci.add_policy_locations(pack_ns_path)

                              errors_component['monitors'][monitor_name]['errors'] = monitor_ci.errors.full_messages unless monitor_ci.valid?
                              result_component['monitors'] << ci_to_load(monitor_ci, :attributes => monitor_attrs)
                              result_component['watchedBy'] << {:name => monitor_name, :type => 'catalog.WatchedBy', :attributes => watched_by_attrs} if watched_by_attrs.present?
                            end
                          end
                        end
                      else
                        errors['platforms'][plat_name]['components'][template_and_class] = "Unknown component template [#{template}]"
                      end
                    else
                      errors['platforms'][plat_name]['components'][template_and_class] = "Unknown component class [#{component_class}]"
                    end
                  else
                    errors['platforms'][plat_name]['components'][template_and_class] = 'Invalid component template/type specification. Expected format: <template>/<class>'
                  end
                end
              end
            else
              errors['platforms'][plat_name][:errors] = ["Unknown platform pack [#{pack_path}]"]
            end
          end
        else
          errors['platforms'][plat_name][:errors] = ['Invalid platform pack specification. Expected format: <source>/<pack>:<version>']
        end
      end
    end

    errors = errors.delete_blank
    errors = ['No configuration data detected.'] if result.blank? && errors.blank?
    return errors.blank? && result, errors
  end

  def parse_load_data(data_string)
    format = :yaml
    if data_string =~ /\A\s*\{/
      format = :json
      begin
        data = JSON.parse(data_string)
      rescue Exception => e
        return nil, format, ["Failed to parse configuration file - invalid JSON: #{e.message}"]
      end
    else
      begin
        data = YAML.load(data_string)
      rescue Exception => e
        more_info = e.is_a?(Psych::SyntaxError) ? "%s %s at line %d column %d" % [e.problem, e.context, e.line, e.column] : e.message
        return nil, format, ["Failed to parse configuration file - invalid YAML: #{more_info}"]
      end
    end
    return data, format
  end

  def convert_json_attrs_to_string(attrs)
    attrs.each_pair {|k, v| attrs[k] = v.to_json if v && !v.is_a?(String)}
  end

  def ci_to_load(ci, extra = {})
    result = {:name => ci.ciName, :type => ci.ciClassName}
    extra[:attributes] ||= ci.ciAttributes.attributes.to_hash
    result.merge!(extra)
    result
  end

  def transfer_if_present(key, source, target)
    value = source[key]
    target[key] = value if value.present?
  end
end
