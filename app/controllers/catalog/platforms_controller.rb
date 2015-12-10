class Catalog::PlatformsController < Base::PlatformsController
  before_filter :find_catalog_and_platform

  def index
    @platforms = Cms::Relation.all(:params => {:ciId              => @catalog.ciId,
                                               :direction         => 'from',
                                               :targetClassName   => 'catalog.Platform',
                                               :relatioShortnName => 'ComposedOf'}).map(&:toCi)
    render :json => @platforms
  end


  private

  def find_catalog_and_platform
    @catalog = Cms::Ci.locate(params[:catalog_id], catalogs_ns_path,         'account.Design') ||
               Cms::Ci.locate(params[:catalog_id], private_catalogs_ns_path, 'account.Design')
    platform_id = params[:id]
    if platform_id.present?
      @platform = Cms::Ci.locate(platform_id, catalog_ns_path(@catalog), 'catalog.Platform')
      @platform = nil if @platform && @platform.ciClassName != 'catalog.Platform'
    end
  end
end
