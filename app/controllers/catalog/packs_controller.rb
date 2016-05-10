class Catalog::PacksController < ApplicationController
  def index
    pack_cis = Cms::Ci.all(:params => {:nsPath      => '/public',
                                       :ciClassName => 'mgmt.Pack',
                                       :recursive   => true})
    version_cis = Cms::Ci.all(:params => {:nsPath      => '/public',
                                          :ciClassName => 'mgmt.Version',
                                          :recursive   => true}).reject { |v| v.ciAttributes.attributes['enabled'] == 'false' }

    version_map = version_cis.inject({}) do |m, version|
      (m[version.nsPath] ||= []) << version
      m
    end

    respond_to do |format|
      format.html {redirect_to catalog_path(:anchor => 'packs')}
      format.js do
        @packs = pack_cis.inject([]) do |a, pack|
          (version_map["#{pack.nsPath}/#{pack.ciName}"] || []).each {|version| a << {:pack => pack, :version => version}}
          a
        end
      end

      format.json do
        source_pack_map = pack_cis.inject({}) do |m, pack|
          root, public, source = pack.nsPath.split('/')
          (m[source] ||= []) << pack
          m
        end

        packs = source_pack_map.keys.inject({}) do |m, source|
          m[source] = source_pack_map[source].to_map_with_value do |pack|
            pack_name = pack.ciName
            [pack_name, (version_map["#{pack.nsPath}/#{pack_name}"] || []).map(&:ciName)]
          end
          m
        end

        render :json => {:packs => packs}
      end
    end
  end

  def show
    pack_id = params[:id]
  end
end
