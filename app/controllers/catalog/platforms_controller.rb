class Catalog::PlatformsController < Base::PlatformsController
  before_filter :find_parent
  before_filter :find_platform, :except => [:index]

  def index
    @platforms = Cms::Relation.all(:params => {:ciId              => @design.ciId,
                                               :direction         => 'from',
                                               :targetClassName   => 'catalog.Platform',
                                               :relatioShortnName => 'ComposedOf'}).map(&:toCi)
    render :json => @platforms
  end

  private

  def find_parent
    design_id = params[:design_id]
    if design_id
      # Catalog design scope.
      @design = locate_catalog_design(design_id)
    else
      # Packs scope.
    end
  end

  def find_platform
    if @design
      # Catalog design scope.
      @platform = locate_catalog_design_platform(params[:id], @design)
    else
      # Packs scope.
      @platform = locate_pack_platform(params[:id], params[:source], params[:pack], params[:version], params[:availability])

      # TODO - this is to address the inconsistency with capitalization in the "pack" attribute of "mgmt.*.Platform"
      @platform.ciAttributes.pack = @platform.ciName unless @platform.ciAttributes.pack == @platform.ciName
    end
  end
end
