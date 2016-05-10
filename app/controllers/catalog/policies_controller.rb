class Catalog::PoliciesController < Base::PoliciesController

  protected

  def find_parents
    @platform = locate_pack_platform(params[:platform_id], params[:source], params[:pack], params[:version], params[:availability])
  end

  def find_policies
    @policies = Cms::Ci.all(:params => {:nsPath => @platform.nsPath, :ciClassName => 'Policy'})
  end

  def find_policy
    @policy = Cms::Ci.locate(params[:id], @platform.nsPath, 'Policy')
  end
end
