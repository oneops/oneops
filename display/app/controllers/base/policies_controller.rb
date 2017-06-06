class Base::PoliciesController < ApplicationController
  before_filter :find_parents
  before_filter :find_policy, :only => [:show, :edit]

  def index
    find_policies
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @policies }
    end
  end

  def show
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(true, @policy) }
    end
  end

  def edit
    render :action => :edit
  end


  protected

  def find_parents
    # Abstract, implemented by subclass.
  end

  def find_policies
    # Abstract, implemented by subclass.
  end

  def find_policy
    # Abstract, implemented by subclass.
  end
end
