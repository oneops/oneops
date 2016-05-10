class Catalog::AttachmentsController < Base::AttachmentsController
  before_filter :find_parents
  before_filter :find_attachment, :only => [:show]

  def show
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@attachment.present?, @attachment) }
    end
  end

  private

  def find_parents
    @design    = locate_catalog_design(params[:design_id])
    @platform  = locate_design_platform(params[:platform_id], @design)
    @component = locate_ci_in_platform_ns(params[:component_id], @platform)
  end

  def find_attachment
    @attachment = locate_ci_in_platform_ns(params[:id], @platform, 'catalog.Attachment')
  end
end
