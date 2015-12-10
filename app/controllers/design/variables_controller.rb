class Design::VariablesController < Base::VariablesController
  before_filter :find_assembly
  before_filter :find_variable, :only => [:show, :edit, :update, :destroy]

  def index
    @variables = Cms::DjRelation.all(:params => {:ciId              => @assembly.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'catalog.Globalvar'}).map(&:fromCi).sort_by { |r| r.ciName }
    respond_to do |format|
      format.js   { render :action => :index }
      format.json { render :json => @variables }
    end
  end

  def new
    @variable = Cms::DjCi.build(:ciClassName  => 'catalog.Globalvar',
                                :ciName       => 'New_Variable_Name',
                                :nsPath       => assembly_ns_path(@assembly),
                                :ciAttributes => {:value => ''})
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable) }
    end
  end

  def create
    @variable = Cms::DjCi.build(params[:cms_dj_ci].merge(:ciClassName => 'catalog.Globalvar', :nsPath => assembly_ns_path(@assembly)))
    relation  = Cms::DjRelation.build(:nsPath       => assembly_ns_path(@assembly),
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

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], assembly_ns_path(@assembly), 'catalog.Globalvar')
  end
end
