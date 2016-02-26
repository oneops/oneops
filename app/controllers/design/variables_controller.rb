class Design::VariablesController < Base::VariablesController
  def new
    @variable = Cms::DjCi.build({:ciClassName => 'catalog.Globalvar',
                                 :nsPath      => assembly_ns_path(@assembly)},
                                {:owner => {}})
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable) }
    end
  end

  def create
    ns_path    = assembly_ns_path(@assembly)
    attrs      = params[:cms_dj_ci].merge(:ciClassName => 'catalog.Globalvar', :nsPath => ns_path)
    attr_props = attrs.delete(:ciAttrProps)
    @variable  = Cms::DjCi.build(attrs, attr_props)
    relation   = Cms::DjRelation.build(:nsPath       => ns_path,
                                       :relationName => 'base.ValueFor',
                                       :toCiId       => @assembly.ciId,
                                       :fromCi       => @variable)

    ok = execute_nested(@variable, relation, :save)
    @variable = relation.fromCi if ok

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @variable) }
    end
  end


  protected

  def find_parents
    @assembly = locate_assembly(params[:assembly_id])
  end

  def find_variables
    @variables = Cms::DjRelation.all(:params => {:ciId              => @assembly.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'catalog.Globalvar',
                                                 :attrProps         => 'owner'}).map(&:fromCi)
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], assembly_ns_path(@assembly), 'catalog.Globalvar', :attrProps => 'owner')
  end
end
