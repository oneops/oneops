class Base::VariablesController < ApplicationController
  before_filter :find_parents
  before_filter :find_variables, :only => [:index, :lock, :unlock]
  before_filter :find_variable, :only => [:show, :edit, :update, :destroy]

  def index
    find_variables unless @variables
    respond_to do |format|
      format.js   { render :action => :index }
      format.json { render :json => @variables }
    end
  end

  def show
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@variable.present?, @variable)}
    end
  end

  def edit
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@variable.present?, @variable)}
    end
  end

  def update
    ok = execute(@variable, :update_attributes, params[:cms_dj_ci])

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

  def lock
    do_lock(true)
  end

  def unlock
    do_lock(false)
  end


  protected

  def find_parents
    # Abstract, implemented by subclass.
  end

  def find_variable
    # Abstract, implemented by subclass.
  end

  def find_variables
    # Abstract, implemented by subclass.
  end

  def do_lock(lock)
    ok = true
    message = ''
    ci_ids = params[:ciIds].to_map(&:to_i)
    owner_value = @environment ? 'manifest' : 'design'
    @variables.each do |var|
      if ci_ids[var.ciId]
        var.ciAttrProps.owner.value = lock ? owner_value : ''
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
