class Base::LocalVariablesController < ApplicationController
  def show
    render_json_ci_response(@variable.present?, @variable)
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
end
