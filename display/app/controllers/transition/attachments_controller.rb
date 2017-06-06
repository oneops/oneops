class Transition::AttachmentsController < Base::AttachmentsController
  before_filter :find_parent_and_attachment

  def show
    respond_to do |format|
      format.html { redirect_to edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, @component, :anchor => "attachments/list_item/#{@attachment.ciId}") }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  private

  def find_parent_and_attachment
    @assembly     = locate_assembly(params[:assembly_id])
    @environment  = locate_environment(params[:environment_id], @assembly)
    @platform     = locate_manifest_platform(params[:platform_id], @environment)
    component_id  = params[:component_id]
    @component    = locate_ci_in_platform_ns(component_id, @platform) if component_id.present?
    attachment_id = params[:id]
    if attachment_id.present?
      @attachment = locate_ci_in_platform_ns(attachment_id, @platform, 'manifest.Attachment', :attrProps => 'owner')
      unless @component
        @component = Cms::DjRelation.first(:params => {:ciId              => @attachment.ciId,
                                                       :direction         => 'to',
                                                       :relationShortName => 'EscortedBy'}).fromCi

      end
    end
  end
end
