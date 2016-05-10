class Catalog::LocalVariablesController < Base::VariablesController

  protected

  def find_parents
    design_id = params[:design_id]
    if design_id
      # Catalog design scope.
      @design   = locate_catalog_design(design_id)
      @platform = locate_design_platform(params[:platform_id], @design)
    else
      # Packs scope.
      @platform = locate_pack_platform(params[:platform_id], params[:source], params[:pack], params[:version], params[:availability])
    end
  end

  def find_variables
    if @design
      # Catalog design scope.
      pack_ns_path = platform_pack_ns_path(@platform)
      @variables   = Cms::Relation.all(:params => {:ciId              => @platform.ciId,
                                                   :direction         => 'to',
                                                   :relationShortName => 'ValueFor',
                                                   :attrProps         => 'owner'}).map do |r|
        variable = r.fromCi
        variable.add_policy_locations(pack_ns_path)
        variable
      end
    else
      # Packs scope.
      @variables = Cms::Ci.all(:params => {:nsPath      => @platform.nsPath,
                                           :ciClassName => 'Localvar'})
    end
  end

  def find_variable
    @variable = Cms::Ci.locate(params[:id], @platform.nsPath, 'Localvar')
  end
end
