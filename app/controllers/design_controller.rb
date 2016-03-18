class DesignController < ApplicationController
  before_filter :find_assembly
  before_filter :find_latest_release, :only => [:show, :extract, :load]
  before_filter :check_open_release, :only => [:load]

  def show
    respond_to do |format|
      format.html do
        platforms = Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                    :direction       => 'from',
                                                    :targetClassName => 'catalog.Platform',
                                                    :relationName    => 'base.ComposedOf'})
        @platforms = platforms.map(&:toCi)
        @diagram = prepare_platforms_diagram(platforms)

        render :action => :show
      end
    end
  end

  def extract
    respond_to do |format|
      format.json do
        render :json => export_design
      end

      format.yaml do
        render :text => export_design.to_yaml, :content_type => 'text/data_string'
      end
    end
  end

  def load
    if request.put?
      data = nil
      data_file = params[:data_file]
      @data_string = (data_file && data_file.read).presence || params[:data]
      begin
        data = YAML.load(@data_string)
      rescue
        begin
          data = JSON.parse(@data_string)
        rescue
        end
      end

      if data.present?
        import_data, @errors = prepare_import_design_data(data)

        if import_data
          ok, message = Transistor.import_design(@assembly, import_data)
          @errors = [message] unless ok
        end
      else
        ok = false
        @errors = ['Please specify proper design coonfguration in YAML format.']
      end

      respond_to do |format|
        format.html do
          # render :text => (import_data || errors).to_yaml, :content_type => 'text/data_string'
          if ok
            flash[:notice] = 'Successfully loaded design.'
            redirect_to assembly_design_url(@assembly)
          end
        end

        format.json {ok ? show : render_json_ci_response(false, nil, @errors)}
      end
    end
  end

  def diagram
    send_data(prepare_platforms_diagram, :type => 'image/svg+xml', :disposition => 'inline')
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

        format.json {render_json_ci_response(false, nil, [message])}
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

  def export_design
    design = Transistor.export_design(@assembly)
    # return design

    result = {}

    transfer_if_present('variables', design, result)

    platforms = design['platforms']
    return result if platforms.blank?

    result['platforms'] = platforms.sort_by {|p| p['name']}.inject({}) do |plats, p|
      attrs = p['attributes']
      plat = {'pack' => "#{attrs.delete('source')}/#{attrs.delete('pack')}:#{attrs.delete('version')}"}
      transfer_if_present('description', p, attrs)
      attrs.delete('description')
      plat.merge!(attrs)

      transfer_if_present('links', p, plat)
      transfer_if_present('variables', p, plat)

      components = p.delete('components')
      if components.present?
        plat['components'] = components.group_by {|c| "#{c['template']}/#{c['type'].sub(/^catalog\./, '')}"}.inject({}) do |templates_hash, (template_name, template_components)|
          templates_hash[template_name] = template_components.sort_by {|c| c['name']}.to_map_with_value do |c|
            comp = c['attributes'].presence || {}

            transfer_if_present('depends', c, comp)

            attachments = c['attachments']
            comp['attachments'] = attachments.sort_by {|a| a['name']}.to_map_with_value {|a| [a['name'], a['attributes']]} if attachments.present?

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

  def prepare_import_design_data(data)
    result = {}
    errors = {}

    assembly_ns_path = assembly_ns_path(@assembly)

    vars = data['variables']
    if vars.present?
      result['variables'] = {}
      errors['variables'] = {}
      ns_path = assembly_ns_path
      vars.each do |var_name, value|
        var_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Globalvar',
                                  :nsPath       => ns_path,
                                  :ciName       => var_name,
                                  :ciAttributes => {:value => value}})
        errors['variables'][var_name] = {'errors' => var_ci.errors.full_messages} unless var_ci.valid?
        result['variables'][var_name] = value
      end
    end

    plats = data['platforms']
    if plats.present?
      platform_md_attrs   = Cms::CiMd.look_up('catalog.Platform').mdAttributes.map(&:attributeName)
      attachment_md_attrs = Cms::CiMd.look_up('catalog.Attachment').mdAttributes.map(&:attributeName)

      result['platforms'] = []
      errors['platforms'] = {}
      plats.each_pair do |plat_name, plat|
        errors['platforms'][plat_name] = {'errors' => []}

        pack_path = plat['pack']
        if pack_path =~ /^\w+\/[\w\-]+:\d+$/
          source, pack, version = pack_path.split(/\/|:/)
          ci_attrs = plat.slice(*platform_md_attrs)
          ci_attrs = ci_attrs.merge(:source => source, :pack => pack, :version => version)

          platform_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Platform',
                                         :nsPath       => assembly_ns_path,
                                         :ciName       => plat_name,
                                         :ciAttributes => ci_attrs})

          platform_pack_ns_path = platform_pack_design_ns_path(platform_ci)
          pack_template = Cms::Ci.first(:params => {:nsPath      => platform_pack_ns_path,
                                                    :ciClassName => 'mgmt.catalog.Platform'})
          if pack_template
            errors['platforms'][plat_name]['errors'] = platform_ci.errors.full_messages unless platform_ci.valid?

            result['platforms'] << ci_to_import(platform_ci)

            transfer_if_present('links', plat, result['platforms'].last)

            platform_ns_path = design_platform_ns_path(@assembly, platform_ci)

            vars = plat['variables']
            if vars.present?
              result['platforms'].last['variables'] = {}
              errors['platforms'][plat_name]['variables'] = {}
              vars.each_pair do |var_name, value|
                var_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Localvar',
                                          :nsPath       => platform_ns_path,
                                          :ciName       => var_name,
                                          :ciAttributes => {:value => value}})
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
                        errors['platforms'][plat_name]['components'][template_and_class][comp_name] = {}

                        component_ci = Cms::DjCi.build({:ciClassName  => component_class,
                                                        :nsPath       => platform_ns_path,
                                                        :ciName       => comp_name,
                                                        :ciAttributes => comp.slice(*component_md_attrs)})

                        errors['platforms'][plat_name]['components'][template_and_class][comp_name]['errors'] = component_ci.errors.full_messages unless component_ci.valid?
                        result['platforms'].last['components'] << ci_to_import(component_ci, :template => template)

                        transfer_if_present('depends', comp, result['platforms'].last['components'].last)

                        attachments = comp['attachments']
                        if attachments.present?
                          result['platforms'].last['components'].last['attachments'] = []
                          errors['platforms'][plat_name]['components'][template_and_class][comp_name]['attachments'] = {}
                          attachments.each do |attachment_name, attachment|
                            errors['platforms'][plat_name]['components'][template_and_class][comp_name]['attachments'][attachment_name] = {}
                            attachment_ci = Cms::DjCi.build({:ciClassName  => 'catalog.Attachment',
                                                             :nsPath       => platform_ns_path,
                                                             :ciName       => attachment_name,
                                                             :ciAttributes => attachment.slice(*attachment_md_attrs)})

                            errors['platforms'][plat_name]['components'][template_and_class][comp_name]['attachments'][attachment_name]['errors'] = attachment_ci.errors.full_messages unless attachment_ci.valid?
                            result['platforms'].last['components'].last['attachments'] << ci_to_import(attachment_ci)
                          end
                        end
                      end
                    else
                      errors['platforms'][plat_name]['components'][template_and_class]['errors'] << 'Unknown component type (template).'
                    end
                  else
                    errors['platforms'][plat_name]['components'][template_and_class]['errors'] << 'Unknown component type (class).'
                  end
                else
                  errors['platforms'][plat_name]['components'][template_and_class]['errors'] << 'Invalid component template/type specification. Expected format: <template>/<class> .'
                end
              end
            end
          else
            errors['platforms'][plat_name]['errors'] << 'Unknown platform pack.'
          end
        else
          errors['platforms'][plat_name]['errors'] << 'Invalid platform pack specification. Expected format: <source>/<pack>:<version> .'
        end
      end
    end

    errors = errors.delete_blank
    return errors.blank? && result, errors
  end

  def ci_to_import(ci, extra = nil)
    result = {:name => ci.ciName, :type => ci.ciClassName, :attributes => ci.ciAttributes.attributes.to_hash}
    result.merge!(extra) if extra.present?
    result
  end

  def transfer_if_present(key, source, target)
    value = source[key]
    target[key] = value if value.present?
  end
end
