class Cloud::VariablesController < Base::VariablesController
  before_filter :find_cloud_and_variable
  before_filter :authorize_write, :only => [:new, :create, :update, :destroy]

  def index
    @variables = Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                               :direction         => 'to',
                                               :relationShortName => 'ValueFor',
                                               :targetClassName   => 'account.Cloudvar'}).map(&:fromCi).sort_by { |r| r.ciName }
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @variables }
    end
  end

  def new
    @variable = Cms::Ci.build(:ciClassName  => 'account.Cloudvar',
                              :ciName       => '',
                              :nsPath       => cloud_ns_path(@cloud),
                              :ciAttributes => {:value => ''})
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable) }
    end
  end

  def create
    @variable = Cms::Ci.build(params[:cms_ci].merge(:ciClassName => 'account.Cloudvar', :nsPath => cloud_ns_path(@cloud)))
    relation  = Cms::Relation.build(:nsPath       => cloud_ns_path(@cloud),
                                    :relationName => 'account.ValueFor',
                                    :toCiId       => @cloud.ciId,
                                    :fromCiId     => 0,
                                    :fromCi       => @variable)
    ok        = execute_nested(@variable, relation, :save)
    @variable = relation.fromCi if ok

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @variable) }
    end
  end

  def update
    ok = execute(@variable, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @variable) }
    end
  end

  protected

  def authorize_write
    unauthorized unless @cloud && has_cloud_services?(@cloud.ciId)
  end

  def find_cloud_and_variable
    @cloud      = locate_cloud(params[:cloud_id])
    variable_id = params[:id]
    @variable   = Cms::Ci.locate(variable_id, cloud_ns_path(@cloud)) if variable_id.present?
  end
end
