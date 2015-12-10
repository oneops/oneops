class Search::Cost < Search::Base
  self.prefix       = '/cms-all/ci/_search'
  self.element_name = ''

  def self.cost_rate(ns_path)
    result        = nil
    search_params = {
      :query   => {
        :wildcard => {'nsPath.keyword' => "#{ns_path}/*"}
        # :filtered => {
        #   :query  => {:wildcard => {'nsPath.keyword' => "#{ns_path}/*"}},
        #   :filter => {:exists => {:field => 'workorder'}}
        # }
      },
      :_source => %w(ciId ciName ciClassName nsPath workorder.cloud.ciName workorder.payLoad.offerings),
      :aggs    => {
        :data     => {
          :nested => {
            :path => 'workorder.payLoad.offerings'
          },
          :aggs   => {
            :total      => {
              :sum => {
                :field => 'workorder.payLoad.offerings.ciAttributes.cost_rate.number',
                # :script => "Float.parseFloat(doc['workorder.payLoad.offerings.ciAttributes.cost_rate'].value)"
              },
            },
            :unit       => {
              :terms => {
                :field => 'workorder.payLoad.offerings.ciAttributes.cost_unit',
              }
            },
            :by_service => {
              :terms => {
                :field => 'workorder.payLoad.offerings.ciAttributes.service_type.keyword',
                :order => {'cost' => 'desc'},
                :size  => 9999
              },
              :aggs  => {
                :cost => {
                  :sum => {
                    :field => 'workorder.payLoad.offerings.ciAttributes.cost_rate.number',
                    # :script => "Float.parseFloat(doc['workorder.payLoad.offerings.ciAttributes.cost_rate'].value)"
                  }
                }
              }
            }
          }
        },
        :by_ns    => {
          :terms => {
            :field => 'nsPath.keyword',
            :order => {'cost>cost' => 'desc'},
            :size  => 9999
          },
          :aggs  => {
            :cost => {
              :nested => {
                :path => 'workorder.payLoad.offerings'
              },
              :aggs   => {
                :cost => {
                  :sum => {
                    :field => 'workorder.payLoad.offerings.ciAttributes.cost_rate.number',
                    # :script => "Float.parseFloat(doc['workorder.payLoad.offerings.ciAttributes.cost_rate'].value)"
                  }
                }
              }
            }
          }
        },
        :by_cloud => {
          :terms => {
            :field => 'workorder.cloud.ciName.keyword',
            :order => {'cost>cost' => 'desc'},
            :size  => 9999
          },
          :aggs  => {
            :cost => {
              :nested => {
                :path => 'workorder.payLoad.offerings'
              },
              :aggs   => {
                :cost => {
                  :sum => {
                    :field => 'workorder.payLoad.offerings.ciAttributes.cost_rate.number',
                    # :script => "Float.parseFloat(doc['workorder.payLoad.offerings.ciAttributes.cost_rate'].value)"
                  }
                }
              }
            }
          }
        }
      },
      :size => 0
    }
    begin
      # return {:total => 5.5,
      #         :unit       => 'usd',
      #         :by_cloud   => {'qa-dfw2a' => 1.5, 'qa-dfw2b' => 3},
      #         :by_service => {'compute' => 4.5, 'dns' => 1.5}}

      # data = JSON.parse(post('', {}, search_params.to_json).body)
      # return data

      data  = JSON.parse(post('', {}, search_params.to_json).body)['aggregations']
      total = data['data']['total']['value']
      if total > 0
        result = {:total => total,
                  :unit  => data['data']['unit']['buckets'][0]['key']}
        [:by_ns, :by_cloud].each do |group_name|
          group = result[group_name] = {}
          data[group_name.to_s]['buckets'].each do |b|
            group[b['key']] = b['cost']['cost']['value']
          end
        end
        group = result[:by_service] = {}
        data['data']['by_service']['buckets'].each do |b|
          group[b['key']] = b['cost']['value']
        end
      else
        result = {}
      end
    rescue Exception => e
      handle_exception e, "Failed to perform 'cost_rate' for nsPath #{ns_path}"
    end
    return result
  end
end
