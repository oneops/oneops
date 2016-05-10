class Catalog::ComponentsController < Base::ComponentsController
  private

  def find_platform
    design_id = params[:design_id]
    if design_id
      # Catalog design scope.
      @design   = locate_catalog_design(design_id)
      @platform = locate_design_platform(params[:platform_id], @design)
    else
      # Packs scope.
      @platform = locate_pack_platform(params[:platform_id], params[:source], params[:pack], params[:version], params[:availability])

      # TODO - this is to address the inconsistency with capitalization in the "pack" attribute of "mgmt.*.Platform"
      @platform.ciAttributes.pack = @platform.ciName unless @platform.ciAttributes.pack == @platform.ciName
    end
  end

  def find_component
    if @design
      # Catalog design scope.
      @component = locate_ci_in_platform_ns(params[:id], @platform, params[:class_name])
    else
      # Packs scope.
      @component = Cms::Ci.locate(params[:id], @platform.nsPath, params[:class_name])

      @template = @component
      @template_name = @component.ciName
      @template.requires = Cms::Relation.first(:params => {:ciId              => @component.ciId,
                                                           :direction         => 'to',
                                                           :relationShortName => 'Requires'})
      @requires = @template.requires
    end
  end

  def requires_relation
    if @design
      @requires = Cms::Relation.first(:params => {:ciId              => @component.ciId,
                                                  :direction         => 'to',
                                                  :relationShortName => 'Requires'})
    end
  end
end
