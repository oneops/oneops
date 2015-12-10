class Transition::RelaysController < ApplicationController
  before_filter :find_assembly_and_environment
  before_filter :find_relay, :only => [:show, :edit, :update, :destroy, :toggle]

  def index
    @relays = Cms::Relation.all(:params => {:ciId              => @environment.ciId,
                                            :relationShortName => 'Delivers',
                                            :direction         => 'from',
                                            :nsPath            => environment_ns_path(@environment)}).map(&:toCi)
    respond_to do |format|
      format.js {render :action => :index}
      format.json {render :json => @relays}
    end
  end

  def show
    render_json_ci_response(@relay.present?, @relay)
  end


  def new
    load_available_types
    @relay_type = @available_types.size > 1 ? params[:relay_type] : @available_types.first.className
    @relay = Cms::Ci.build(:ciClassName => @relay_type, :nsPath => environment_ns_path(@environment)) if @available_types.find { |t| t.className == @relay_type }
    render(:action => :new)
  end

  def create
    ns_path  = environment_ns_path(@environment)
    @relay   = Cms::Ci.build(params[:cms_ci].merge(:nsPath => ns_path))
    relation = Cms::Relation.build(:relationName => 'manifest.Delivers',
                                   :nsPath       => ns_path,
                                   :fromCiId     => @environment.ciId,
                                   :toCi         => @relay)

    ok = execute(relation, :save)
    respond_to do |format|
      format.js do
        if ok then
          index
        else
          load_available_types
          @relay_type = @relay.ciClassName
          render(:action => :new)
        end
      end

      format.json { render_json_ci_response(ok, @relay) }
    end
  end

  def edit
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@relay.present?, @relay)}
    end
  end

  def update
    ok = execute(@relay, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @relay) }
    end
  end

  def destroy
    ok = execute(@relay, :destroy)

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @relay) }
    end
  end

  def toggle
    enabled  = @relay.ciAttributes.enabled == 'true'
    @relay.ciAttributes.enabled = enabled ? 'false' : 'true'
    ok = execute(@relay, :save)

    respond_to do |format|
      format.js do
        flash[:error] = "Failed to #{enabled ? 'disable' : 'enable'} relay #{@relay.ciName}: #{@relay.errors.full_messages.join(' ')}." unless ok
        index
      end

      format.json { render_json_ci_response(ok, @relay) }
    end
  end


  private

  def find_assembly_and_environment
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
  end

  def find_relay
    @relay = Cms::Ci.locate(params[:id], environment_ns_path(@environment))
  end

  def load_available_types
    @available_types = Cms::CiMd.all(:params => {:package => 'manifest.relay'})
  end
end
