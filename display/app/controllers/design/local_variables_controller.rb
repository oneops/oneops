class Design::LocalVariablesController < Base::VariablesController
  swagger_controller :platform_variables, 'Design Platform Variable Management'

  def new
    @variable = Cms::DjCi.build({:ciClassName  => 'catalog.Localvar',
                                :nsPath       => design_platform_ns_path(@assembly, @platform)},
                                {:owner => {}})
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable) }
    end
  end

  swagger_api :create do
    summary 'Add platform variable'
    param_path_parent_ids :assembly, :platform
    param :body, :body, :json, :required, 'Variable CI structure (include only required and non-default value attributes).'
    notes <<-NOTE
JSON body payload example<br>
Unencrypted variable:
<pre>
{
  "cms_dj_ci": {
    "ciName": "DT_INSTALL",
    "ciAttributes": {
      "value": "whatever"
    }
  }
}
</pre>
<br>
Encrypted variable:
<pre>
{
  "cms_dj_ci": {
    "ciName": "DB_PASSWORD",
    "ciAttributes": {
      "secure": "true",
      "encrypted_value": "secret123"
    }
  }
}
</pre>
NOTE
    end
  def create
    ns_path    = design_platform_ns_path(@assembly, @platform)
    attrs      = params[:cms_dj_ci].merge(:ciClassName => 'catalog.Localvar', :nsPath => ns_path)
    attr_props = attrs.delete(:ciAttrProps)
    @variable  = Cms::DjCi.build(attrs, attr_props)
    relation   = Cms::DjRelation.build(:nsPath       => ns_path,
                                       :relationName => 'catalog.ValueFor',
                                       :toCiId       => @platform.ciId,
                                       :fromCi       => @variable)

    ok = execute_nested(@variable, relation, :save)
    @variable = relation.fromCi if ok

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @variable) }
    end
  end

  # By "popular demand" disabling recently introduced validation/protection to disallow deleting platform variables
  # defined in the pack.  This validation is right but apparently it breaks some of the API clients, namely 'boo'.
  # def destroy
  #   begin
  #     pack_var = Cms::Ci.locate(@variable.ciName, platform_pack_design_ns_path(@platform), 'mgmt.catalog.Localvar')
  #   rescue Cms::Ci::NotFoundException => e
  #     pack_var = nil
  #   end
  #   ok = pack_var.blank? || pack_var.ciState == 'pending_deletion'
  #   if pack_var && pack_var.ciState != 'pending_deletion'
  #     message = 'This variable is defined in the pack. It can not be deleted.'
  #     @variable.errors.add(:base, message)
  #     respond_to do |format|
  #       format.js do
  #         flash[:error] = message
  #         render :js => ''
  #       end
  #       format.json { render_json_ci_response(ok, @variable) }
  #     end
  #     return
  #   end
  #
  #   super
  # end


  protected

  def find_parents
    @assembly = locate_assembly(params[:assembly_id])
    @platform = locate_design_platform(params[:platform_id], @assembly)
  end

  def find_variables
    pack_ns_path = platform_pack_ns_path(@platform)
    @variables   = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                   :direction         => 'to',
                                                   :relationShortName => 'ValueFor',
                                                   :targetClassName   => 'catalog.Localvar',
                                                   :attrProps         => 'owner'}).map do |r|
      variable = r.fromCi
      variable.add_policy_locations(pack_ns_path)
      variable
    end
  end

  def find_variable
    @variable = locate_ci_in_platform_ns(params[:id], @platform, 'catalog.Localvar', :attrProps => 'owner')
  end
end
