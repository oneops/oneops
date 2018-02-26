class Search::Ci < Search::Base
  self.prefix       = ''
  self.element_name = ''
  self.timeout      = 10

  def self.compute_count_stats_by_org(conditions, ns_path = nil)
    result     = nil
    conditions = build_common_query(conditions, ns_path)

    aggs = {
      :assembly => {
        :cardinality => {
          :field => 'workorder.payLoad.Assembly.ciId',
        },
      },
      :environment => {
        :cardinality => {
          :field => 'workorder.payLoad.Environment.ciId',
        }
      },
      :platform => {
        :cardinality => {
          :field => 'workorder.box.ciId',
        }
      },
      :compute => {
        :cardinality => {
          :field => 'ciId',
        }
      }
    }

    aggs = aggs.merge({
                        :prod => {
                          :filter => {:terms => {'workorder.payLoad.Environment.ciAttributes.profile' => %w(prod production)}},
                          :aggs   => aggs
                        }
                      })

    search_params = {
      :query => {:bool => {:must => conditions}},
      :aggs  => aggs.merge({
        :by_org => {
          :terms => {
            :field => 'workorder.payLoad.Organization.ciName.keyword',
            :size  => 999,
          },
          :aggs  => aggs
        }
      }),
      :size  => 0
    }

    bucket_builder = lambda do |b|
      {:all => b, :prod => b['prod']}.inject({}) do |hh, (k, d)|
        hh[k] = {:assembly    => d['assembly']['value'],
                 :environment => d['environment']['value'],
                 :platform    => d['platform']['value'],
                 :compute     => d['compute']['value']}

        hh
      end
    end
    begin
      data = JSON.parse(post('/cms-all/ci/_search', {}, search_params.to_json).body)['aggregations']
      result = data['by_org']['buckets'].inject({'_ALL' => bucket_builder.call(data)}) do |h, b|
        h[b['key']] = bucket_builder.call(b)
        h
      end
    rescue Exception => e
      handle_exception e, "Failed to fetch ci count stats for nsPath=#{ns_path}."
    end
    return result
  end
end
