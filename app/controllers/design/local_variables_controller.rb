class Design::LocalVariablesController < Base::LocalVariablesController
  before_filter :find_assembly_and_platform
  before_filter :find_variable, :only => [:show, :edit, :update, :destroy]

  def index
    @variables = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'catalog.Localvar'}).map(&:fromCi).sort_by { |r| r.ciName }
    respond_to do |format|
      format.js   { render :action => :index }
      format.json { render :json => @variables }
    end
  end

  def new
    @variable = Cms::DjCi.build(:ciClassName  => 'catalog.Localvar',
                                :ciName       => '',
                                :nsPath       => design_platform_ns_path(@assembly, @platform),
                                :ciAttributes => {:value => ''})
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable) }
    end
  end

  def create
    design_platform_ns_path = design_platform_ns_path(@assembly, @platform)
    @variable = Cms::DjCi.build(params[:cms_dj_ci].merge(:ciClassName => 'catalog.Localvar', :nsPath => design_platform_ns_path))
    relation  = Cms::DjRelation.build(:nsPath       => design_platform_ns_path,
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


  def destroy
    ok = execute(@variable, :destroy)

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @variable) }
    end
  end


  protected

  def find_assembly_and_platform
    @assembly = locate_assembly(params[:assembly_id])
    @platform = Cms::DjCi.locate(params[:platform_id], assembly_ns_path(@assembly), 'catalog.Platform')
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], design_platform_ns_path(@assembly, @platform), 'catalog.Localvar')
  end
end
