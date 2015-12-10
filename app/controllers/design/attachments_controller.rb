class Design::AttachmentsController < Base::AttachmentsController
  before_filter :find_parent_cis
  before_filter :find_attachment, :only => [:show, :edit, :update, :destroy]

  def show
    respond_to do |format|
      format.html { redirect_to edit_assembly_design_platform_component_path(@assembly, @platform, @component, :anchor => "attachments/list_item/#{@attachment.ciId}") }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  def new
    @attachment = Cms::DjCi.build(:ciClassName  => 'catalog.Attachment',
                                  :nsPath       => @component.nsPath,
                                  :ciName       => 'New_Attachment_Name')
    respond_to do |format|
      format.js   { render :action => :edit }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  def create
    @attachment = Cms::DjCi.build(params[:cms_dj_ci].merge(:nsPath => @component.nsPath, :ciClassName => 'catalog.Attachment'))
    relation    = Cms::DjRelation.build(:nsPath       => @component.nsPath,
                                        :relationName => 'catalog.EscortedBy',
                                        :fromCiId     => @component.ciId,
                                        :toCi         => @attachment)
    ok = execute_nested(@attachment, relation, :save)
    @attachment = relation.toCi if ok

    respond_to do |format|
      format.js   { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @attachment) }
    end
  end

  def destroy
    ok = execute(@attachment, :destroy)
    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @attachment) }
    end
  end


  private

  def find_parent_cis
    @assembly    = locate_assembly(params[:assembly_id])
    @platform    = Cms::DjCi.locate(params[:platform_id], assembly_ns_path(@assembly), 'catalog.Platform')
    component_id = params[:component_id]
    @component = Cms::DjCi.locate(component_id, design_platform_ns_path(@assembly, @platform)) if component_id.present?
  end

  def find_attachment
    @attachment = Cms::DjCi.locate(params[:id], design_platform_ns_path(@assembly, @platform), 'catalog.Attachment')
    unless @component
      @component = Cms::DjRelation.first(:params => {:ciId              => @attachment.ciId,
                                                     :direction         => 'to',
                                                     :relationShortName => 'EscortedBy'}).fromCi

    end
  end
end
