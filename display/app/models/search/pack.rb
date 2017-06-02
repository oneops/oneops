class Search::Pack < Search::Base
  self.prefix       = ''
  self.element_name = ''
  self.timeout      = 10

  def self.count_stats(source, pack = nil, version = nil)
    result     = nil
    conditions = [{:term => {'workorder.box.ciAttributes.source.keyword' => source}}]

    conditions << {:term => {'workorder.box.ciAttributes.pack.keyword' => pack}} if pack.present?
    conditions << {:term => {'workorder.box.ciAttributes.version.keyword' => version}} if version.present?

    aggs = {
      :org_count => {
        :cardinality => {
          :field => 'workorder.payLoad.Organization.ciId',
        },
      },
      :assembly_count => {
        :cardinality => {
          :field => 'workorder.payLoad.Assembly.ciId',
        },
      },
      :environment_count => {
        :cardinality => {
          :field => 'workorder.payLoad.Environment.ciId',
        }
      },
      :platform => {
        :terms  => {
          :field => 'workorder.box.ciAttributes.availability'
        },
        :aggs => {
          :count => {
            :cardinality => {
              :field => 'workorder.box.ciId',
            },
          }
        }
      },
      :compute => {
        :filter => {:regexp => {'workorder.resultCi.ciClassName.keyword' => '.*\.Compute'}},
        :aggs   => {
          :count => {
            :cardinality => {
              :field => 'ciId',
            },
          },
          :cloud => {
            :terms => {
              :field => 'workorder.cloud.ciAttributes.location.keyword',
              :size  => 99,
            }
          }
        }
      }
    }

    search_params = {
      :query => {:bool => {:must => conditions}},
      :aggs  => aggs.merge({
                             :prod => {
                               :filter => {:terms => {'workorder.payLoad.Environment.ciAttributes.profile' => %w(prod production)}},
                               :aggs   => aggs
                             }
                           }),
      :size  => 0
    }

    begin
      data = JSON.parse(post('/cms-all/ci/_search', {}, search_params.to_json).body)['aggregations']
      result = {:all => data, :prod => data['prod']}.inject({}) do |h, (k, d)|
        h[k] = {:org         => d['org_count']['value'],
                :assembly    => d['assembly_count']['value'],
                :environment => d['environment_count']['value'],
                :compute     => d['compute']['count']['value'],
                :cloud       => d['compute']['cloud']['buckets'].to_map_with_value { |c| [c['key'], c['doc_count']] }}
        d['platform']['buckets'].each { |b| h[k]["platform_#{b['key']}".to_sym] = b['count']['value'] }
        h
      end
    rescue Exception => e
      handle_exception e, "Failed to fetch pack stats for source=#{source}, pack=#{pack}, version=#{version}"
    end
    return result
  end
end
