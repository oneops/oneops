class Catalog::VariablesController < Base::VariablesController

  protected

  def find_parents
    @design = locate_catalog_design(params[:design_id])
  end

  def find_variables
    @variables = Cms::Relation.all(:params => {:ciId              => @design.ciId,
                                               :direction         => 'to',
                                               :relationShortName => 'ValueFor',
                                               :attrProps         => 'owner'}).map(&:fromCi)
  end

  def find_variable
    @variable = Cms::Ci.locate(params[:id], catalog_design_ns_path(@design), 'catalog.Globalvar')
  end
end
