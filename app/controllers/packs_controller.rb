class PacksController < ApplicationController
  def index
    source_pack_map = Cms::Ci.all(:params => {:nsPath      => '/public',
                                              :ciClassName => 'mgmt.Pack',
                                              :recursive   => true}).inject({}) do |m, pack|
      root, public, source = pack.nsPath.split('/')
      m[source] ||= []
      m[source] << pack
      m
    end

    version_map = Cms::Ci.all(:params => {:nsPath      => '/public',
                                          :ciClassName => 'mgmt.Version',
                                          :recursive   => true}).inject({}) do |m, version|
      enabled = version.ciAttributes.attributes['enabled']
      unless enabled && enabled == 'false'
        m[version.nsPath] ||= []
        m[version.nsPath] << version.ciName
      end
      m
    end

    packs = Cms::Ci.all(:params => {:nsPath      => '/public',
                                    :ciClassName => 'mgmt.Source'}).map(&:ciName).inject({}) do |m, source|
      m[source] = source_pack_map[source].to_map_with_value do |pack|
        pack_name = pack.ciName
        [pack_name, version_map["#{pack.nsPath}/#{pack_name}"] || []]
      end
      m
    end

    render :json => {:packs => packs}
  end
end
