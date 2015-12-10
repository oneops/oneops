class Catalog::ComponentsController < Base::ComponentsController
  private

  def find_platform
    @catalog  = Cms::Ci.locate(params[:catalog_id], catalogs_ns_path,         'account.Design') ||
                Cms::Ci.locate(params[:catalog_id], private_catalogs_ns_path, 'account.Design')
    @assembly = @catalog
    @platform = Cms::Ci.locate(params[:platform_id], catalog_ns_path(@catalog), 'catalog.Platform')
  end

  def find_component
    @component = Cms::Ci.locate(params[:id], catalog_platform_ns_path(@catalog, @platform))
    if @component.is_a?(Array)
      class_name = params[:class_name]
      @component = @component.find { |c| c.ciClassName.ends_with?(class_name) } if class_name.present?
    end
    @component = nil if @component && !@component.ciClassName.start_with?('catalog')
  end
end
