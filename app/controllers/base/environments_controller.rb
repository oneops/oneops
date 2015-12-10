class Base::EnvironmentsController < ApplicationController
  protected

  def load_platform_cloud_instances_map
    @platform_clouds = Cms::Relation.all(:params => {:nsPath          => environment_ns_path(@environment),
                                                     :relationName    => 'base.Consumes',
                                                     :targetClassName => 'account.Cloud',
                                                     :recursive       => true,
                                                     :includeToCi     => true}).inject({}) do |m, rel|
      slash, org, assembly, env, manifest, plat_name, plat_ver = rel.nsPath.split('/')
      key                                                      = "#{plat_name}/#{plat_ver}"
      m[key]                                                   ||= {}
      m[key][rel.toCiId]                                       = {:consumes => rel, :instances => 0}
      m
    end

    @deloyed_to_rels = Cms::Relation.all(:params => {:nsPath            => environment_bom_ns_path(@environment),
                                                     :relationShortName => 'DeployedTo',
                                                     :recursive         => true})
    @deloyed_to_rels.each do |rel|
      slash, org, assembly, env, bom, plat_name, plat_ver                  = rel.nsPath.split('/')
      @platform_clouds["#{plat_name}/#{plat_ver}"][rel.toCiId][:instances] += 1
    end
  end
end
