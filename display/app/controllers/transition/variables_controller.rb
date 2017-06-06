class Transition::VariablesController < Base::VariablesController

  protected

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
  end

  def find_variables
    @variables = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Globalvar',
                                                 :attrProps         => 'owner'}).map(&:fromCi)
  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], environment_manifest_ns_path(@environment), 'manifest.Globalvar', {:attrProps => 'owner'})
  end
end
