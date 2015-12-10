class Transition::LocalVariablesController < Base::LocalVariablesController
  before_filter :find_assembly_and_environment_and_platform
  before_filter :find_variable, :only => [:show, :edit, :update]

  def index
    @variables = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Localvar',
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

  def find_assembly_and_environment_and_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], @platform.nsPath, 'manifest.Localvar', {:attrProps => 'owner'})
  end

  def do_lock(lock)
    ok = true
    message = ''
    ci_ids = params[:ciIds].to_map(&:to_i)
    @variables = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Localvar',
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
