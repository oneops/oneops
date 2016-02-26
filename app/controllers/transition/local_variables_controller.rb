class Transition::LocalVariablesController < Base::VariablesController

  protected

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
  end

  def find_variables
    @variables = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Localvar',
                                                 :attrProps         => 'owner'}).map(&:fromCi)

  end

  def find_variable
    @variable = Cms::DjCi.locate(params[:id], @platform.nsPath, 'manifest.Localvar', {:attrProps => 'owner'})
  end
end
