class Transition::LocalVariablesController < Base::VariablesController

  protected

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @platform    = locate_manifest_platform(params[:platform_id], @environment)
  end

  def find_variables
    pack_ns_path = platform_pack_ns_path(@platform)
    @variables = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ValueFor',
                                                 :targetClassName   => 'manifest.Localvar',
                                                 :attrProps         => 'owner'}).map do |r|
      variable = r.fromCi
      variable.add_policy_locations(pack_ns_path)
      variable
    end

  end

  def find_variable
    @variable = locate_ci_in_platform_ns(params[:id], @platform, 'manifest.Localvar', :attrProps => 'owner')
  end
end
