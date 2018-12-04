class Design::ComponentsController < Base::ComponentsController
  swagger_controller :components, 'Design Components Management'

  swagger_api :new do
    summary 'Build a sample component CI json with default attribute values'
    param_path_parent_ids :assembly, :platform
    param :form, 'template_name', :string, :required, 'Type (name of component in the pack) of optional component to build.'
  end
  def new
    find_template
    if @template
      @component = Cms::DjCi.build({:nsPath      => design_platform_ns_path(@assembly, @platform),
                                    :ciClassName => component_class_name,
                                    :ciName      => @template_name},
                                   {:owner => {}})

      ci_attributes = @component.ciAttributes.attributes
      ci_attributes.keys.each do |attribute|
        default = @template.ciAttributes.attributes[attribute]
        ci_attributes[attribute] = default if default
        ci_attributes[attribute] = '' if default == '--ENCRYPTED--'
      end
    end

    respond_to do |format|
      format.html do
        build_linkable_component_sibling_map(@component)
        render :action => :edit
      end

      format.json { render_json_ci_response(@component, @component, ["Unknown component tamplate: #{params[:template_name]}"]) }
    end
  end

  swagger_api :create do
    summary 'Add optional component to platform'
    param_path_parent_ids :assembly, :platform
    param :form, 'template_name', :string, :required, 'Type (name of component in the pack) of optional component to be added.'
    param :body, :body, :json, :required, 'Component CI structure (include only required and non-default value attributes).'
    notes <<-NOTE
Only optional components can be added to platform.  For some components (e.g. file, download, artifact) many instances can
be added, others can only be added once (e.g. java, ruby).<br>
JSON body payload example - add download component:
<pre style="white-space:pre-wrap">
{
  "template_name": "download",
  "cms_dj_ci": {
    "ciName": "deploy-oneagent-dynatrace",
    "ciAttributes": {
      "path": "/tmp/apm-deploy.jar",
      "basic_auth_password": "...",
      "source": "http://gec-maven-nexus.walmart.com/content/groups/public/com/walmart/platform/systel/apm/apm-deploy/0.0.4/apm-deploy-0.0.4.jar",
      "post_download_exec_cmd": "(yum -y install ansible; cd /tmp; rm -rf apm-deploy; mkdir apm-deploy; cd apm-deploy; jar -xf ../apm-deploy.jar; ansible-playbook apm-deploy-playbook.yml -i inventory.ini -l local -u app --extra-vars \"ORG=$OO_LOCAL{DT_ORG} $OO_LOCAL{EXTRA_DT_HOST_TAGS} uninstall_agent=$OO_LOCAL{DT_UNINSTALL}\")"
    }
  }
}
</pre>
NOTE
  end
  def create
    find_template
    ns_path    = design_platform_ns_path(@assembly, @platform)
    attrs      = params[:cms_dj_ci].merge(:nsPath => ns_path, :ciClassName => component_class_name)
    attr_props = attrs.delete(:ciAttrProps)
    @component = Cms::DjCi.build(attrs, attr_props)

    if @template.blank?
      @component.errors.add(:base, "Unknown component type '#{@template_name}'.")
    elsif @template.ciState == 'pending_deletion'
      @component.errors.add(:base, "Component type '#{@template_name}' is obsolete.")
    else
      build_linkable_component_sibling_map(@component)
      if @component_siblings.size >= @cardinality.max
        @component.errors.add(:base, "Not allowed to add more '#{@template_name}' components.")
      end
    end

    ok = @component.errors.blank?
    if ok
      @requires = Cms::DjRelation.build(:relationName => 'base.Requires',
                                       :nsPath       => ns_path,
                                       :fromCiId     => @platform.ciId,
                                       :toCi         => @component)
      @requires.relationAttributes = @template.requires.relationAttributes.attributes.
        merge(:template => @template_name).
        slice(*@requires.meta.attributes[:mdAttributes].map(&:attributeName)).
        reject {|k, v| v.blank?}

      # ok = execute_nested(@component, @requires, :save)
      @requires = Transistor.create_component(@platform.ciId, @requires)
      @component = @requires.toCi
      ok = @requires.errors.blank?
      if ok
        unless save_sibling_depends_on_relations
          @component.errors.add(:base, 'Created component but failed to save some peer dependencies.')
        end
      end
    end


    respond_to do |format|
      format.html do
        if ok && @component.errors.blank?
          redirect_to_show_platform
        else
          render :action => :edit
        end
      end

      format.json { render_json_ci_response(ok, @component) }
    end
  end

  swagger_api :update, :responses => [:not_found] do
    summary 'Update existing component.'
    param_path_parent_ids :assembly, :platform
    param_path_ci_id :component
    param :body, :body, :json, :required, 'Component CI structure (include only what is to be updated).'
    notes <<-NOTE
JSON body payload examples:<br>
Update'authorized_keys' and 'sudoer' attributes for user component
<pre>
{
  "cms_dj_ci": {
    "ciAttributes": {
      "authorized_keys": "[\"key1\",\"key2\",\"key3\"]",
      "sudoer": "true"
    }
  }
}
</pre>
<br>
Same as above with locking of 'sudoer' attribute
<pre>
{
  "cms_dj_ci": {
    "ciAttributes": {
      "authorized_keys": "[\"key1\",\"key2\",\"key3\"]",
      "sudoer": "true"
    },
    "ciAttrProps": {
      "owner": {
        "sudoer": "design"
      }
    }
  }
}
</pre>
NOTE
  end

  swagger_api :destroy, :responses => [:not_found] do
    summary 'Delete an optional componnent from platform.'
    param_path_parent_ids :assembly, :platform
    param_path_ci_id :component
  end
  def destroy
    ok = false
    find_template
    if find_component_siblings.size > requires_relation.relationAttributes.constraint.split('..').first.to_i
      ok = execute(@component, :destroy)
      flash[:error] = "Failed to delete #{@component.ciName}." unless ok
    else
      @component.errors.add(:base, 'Cannot delete required component.')
      flash[:error] = "Cannot delete required component #{@component.ciName}."
    end

    respond_to do |format|
      format.html { redirect_to_show_platform }
      format.json { render_json_ci_response(ok, @component) }
    end
  end


  protected

  def find_platform
    @assembly = locate_assembly(params[:assembly_id])
    @platform = locate_design_platform(params[:platform_id], @assembly)
  end

  def find_component
    @component = locate_ci_in_platform_ns(params[:id], @platform, params[:class_name], :attrProps => 'owner')
  end

  def redirect_to_show_platform
    redirect_to assembly_design_platform_url(@assembly, @platform)
  end
end
