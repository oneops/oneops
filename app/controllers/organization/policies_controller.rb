class Organization::PoliciesController < ApplicationController
  before_filter :authorize_admin, :only => [:new, :create, :update, :destroy, :evaluate]
  before_filter :find_policy, :only => [:show, :edit, :update, :destroy, :evaluate]

  def index
    @policies = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                        :ciClassName => 'account.Policy'})
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @policies }
    end
  end

  def show
    render_json_ci_response(true, @policy)
  end

  def new
    attrs = params[:policy].presence || {}
    @policy = Cms::Ci.build(attrs.merge(:ciClassName => 'account.Policy', :nsPath => organization_ns_path))
    render :action => :edit
  end

  def create
    @policy = Cms::Ci.build(params[:cms_ci].merge(:ciClassName => 'account.Policy', :nsPath => organization_ns_path))

    ok = execute(@policy, :save)

    respond_to do |format|
      format.js { ok ? index : edit }
      format.json { render_json_ci_response(ok, @policy) }
    end
  end

  def edit
    render :action => :edit
  end

  def update
    ok = execute(@policy, :update_attributes, params[:cms_ci])
    respond_to do |format|
      format.js { ok ? index : edit }
      format.json do
        render_json_ci_response(ok, @policy)
      end
    end
  end

  def destroy
    ok = execute(@policy, :destroy)
    flash[:error] = 'Failed to delete policy.' unless ok

    respond_to do |format|
      format.js { index }
      format.json { render_json_ci_response(ok, @policy) }
    end
  end

  def evaluate
    find_policy
    @query      = params[:query].presence || (@policy && @policy.ciAttributes.query)
    min_ns_path = organization_ns_path
    @ns_path    = params[:ns_path]
    @ns_path    = min_ns_path unless @ns_path && @ns_path.start_with?(min_ns_path)
    @cis        = nil

    if @query.present?
      begin
        @cis = Cms::Ci.search(:nsPath => "#{@ns_path}/*", :query => @query, :size => (params[:max_size].presence || 1000).to_i)
      rescue Exception => e
        @error = e.message
      end
    end

    respond_to do |format|
      format.html
      format.js

      format.json do
        if @error.blank?
          render :json => @cis
        else
          render :json => {:errors => [@error]}, :status => :unprocessable_entity
        end
      end
    end
  end


  private

  def find_policy
    policy_id = params[:id]
    @policy = Cms::Ci.locate(policy_id, organization_ns_path) if policy_id.present?
  end
end
