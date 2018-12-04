class Design::AttachmentsController < Base::AttachmentsController
  swagger_controller :attachments, 'Design Attachment Management'

  before_filter :find_parents_and_attachment

  def show
    respond_to do |format|
      format.html { redirect_to edit_assembly_design_platform_component_path(@assembly, @platform, @component, :anchor => "attachments/list_item/#{@attachment.ciId}") }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  def new
    @attachment = Cms::DjCi.build({:ciClassName => 'catalog.Attachment',
                                   :nsPath      => @component.nsPath},
                                  {:owner => {}})
    respond_to do |format|
      format.js { render :action => :edit }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  swagger_api :create do
    summary 'Add attachment to component'
    param_path_parent_ids :assembly, :platform, :component
    param :body, :body, :json, :required, 'Attachment CI structure (include only required and non-default value attributes).'
    notes <<-NOTE
JSON body payload example:
<pre>
{
  "cms_dj_ci": {
    "ciName": "say-hello",
    "ciAttributes": {
      "path": "/tmp/download_file",
      "exec_cmd": "echo hello everybody",
      "run_on": "after-add,after-replace,after-update,after-delete,on-demand"
    }
  }
}
</pre>
NOTE
    end
  def create
    attrs       = params[:cms_dj_ci].merge(:nsPath => @component.nsPath, :ciClassName => 'catalog.Attachment')
    attr_props  = attrs.delete(:ciAttrProps)
    @attachment = Cms::DjCi.build(attrs, attr_props)
    relation    = Cms::DjRelation.build(:nsPath       => @component.nsPath,
                                        :relationName => 'catalog.EscortedBy',
                                        :fromCiId     => @component.ciId,
                                        :toCi         => @attachment)
    ok          = execute_nested(@attachment, relation, :save)
    @attachment = relation.toCi if ok

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
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

  def find_parents_and_attachment
    @assembly     = locate_assembly(params[:assembly_id])
    @platform     = locate_design_platform(params[:platform_id], @assembly)
    component_id  = params[:component_id]
    @component    = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?
    attachment_id = params[:id]
    if attachment_id.present?
      @attachment = locate_ci_in_platform_ns(attachment_id, @platform, 'catalog.Attachment', :attrProps => 'owner')
      unless @component
        @component = Cms::DjRelation.first(:params => {:ciId              => @attachment.ciId,
                                                       :direction         => 'to',
                                                       :relationShortName => 'EscortedBy'}).fromCi

      end
    end
  end
end
