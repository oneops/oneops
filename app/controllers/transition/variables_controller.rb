class Transition::VariablesController < Base::VariablesController
  before_filter :find_assembly_and_environment
  before_filter :find_variable, :only => [:show, :edit, :update]

  def index
    @variables = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Globalvar',
                                                 :attrProps         => 'owner'}).map(&:fromCi)
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @variables }
    end
  end

  def lock
    do_lock(true)
  end

  def unlock
    do_lock(false)
  end


  protected

  def find_assembly_and_environment
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], environment_manifest_ns_path(@environment), 'manifest.Globalvar', {:attrProps => 'owner'})
  end

  def do_lock(lock)
    ok = true
    message = ''
    ci_ids = params[:ciIds].to_map(&:to_i)
    @variables = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Globalvar',
                                                 :attrProps         => 'owner'}).map(&:fromCi)
    @variables.each do |var|
      if ci_ids[var.ciId]
        var.ciAttrProps.owner.value = lock ? 'manifest' : ''
        ok &&= execute(var, :save)
        unless ok
          message = "Failed to #{lock ? 'lock' : 'unlock'} specified variables."
          break
        end
      end
    end

    respond_to do |format|
      format.js do
        flash[:error] = message unless ok
        render :action => :index
      end

      format.json do
        if ok
          render :json => @variables
        else
          render :json => {:errors => [message]}, :status => :unprocessable_entity
        end
      end
    end
  end
end
