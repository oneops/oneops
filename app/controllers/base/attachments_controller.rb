class Base::AttachmentsController < ApplicationController
  def index
    @attachments = Cms::DjRelation.all(:params => {:ciId              => @component.ciId,
                                                   :direction         => 'from',
                                                   :relationShortName => 'EscortedBy'}).map(&:toCi).sort_by { |r| r.ciName }
    respond_to do |format|
      format.js   { render :action => :index }
      format.json { render :json => @attachments }
    end
  end

  def edit
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  def update
    ok = execute(@attachment, :update_attributes, params[:cms_dj_ci])

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @attachment) }
    end
  end
end
