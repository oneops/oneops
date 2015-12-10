class Transition::AttachmentsController < Base::AttachmentsController
  before_filter :find_parent_cis
  before_filter :find_attachment, :only => [:show, :edit, :update]

  def show
    respond_to do |format|
      format.html { redirect_to edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, @component, :anchor => "attachments/list_item/#{@attachment.ciId}") }
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  private

  def find_parent_cis
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
    component_id = params[:component_id]
    @component   = Cms::DjCi.locate(component_id, @platform.nsPath) if component_id.present?
  end

  def find_attachment
    @attachment = Cms::DjCi.locate(params[:id], transition_platform_ns_path(@environment, @platform), 'manifest.Attachment', {:attrProps => 'owner'})
    unless @component
      @component = Cms::DjRelation.first(:params => {:ciId              => @attachment.ciId,
                                                     :direction         => 'to',
                                                     :relationShortName => 'EscortedBy'}).fromCi

    end
  end
end
