class Search::WorkOrder < Search::Base
  self.prefix       = '/cms/workorder/_search'
  self.element_name = ''

  def self.time_stats(deployment)
    result        = nil
    search_params = {
      :query   => {
        :bool => {
          :must => [{:term => {:deploymentId => deployment.deploymentId}},
                    {:term => {:dpmtRecordState => 'complete'}}]
        }
      },
      :_source => %w(rfcId searchTags.*),
      #:sort  => ['rfcCi.execOrder', 'searchTags.responseDequeTS'],
      :aggs    => {
        :group_by_step => {
          :terms => {
            :field => 'rfcCi.execOrder',
            :order => {:_term => 'asc'},
            :size  => 9999
          },
          :aggs  => {
            :max_time => {
              :max => {
                :field => 'searchTags.responseDequeTS'
              }
            },
            :min_time => {
              :min => {
                :field => 'searchTags.requestEnqueTS'
              }
            }
          }
        }
      },
      :size    => 999999
    }
    begin
      data      = JSON.parse(post('', {}, search_params.to_json).body)
      rfc_times = data['hits']['hits'].inject({}) do |m, rfc|
        d             = rfc['_source']
        m[d['rfcId']] = d['searchTags']
        m
      end


      step_max_times = data['aggregations']['group_by_step']['buckets'].inject({}) do |m, bucket|
        m[bucket['key']] = {:max => bucket['max_time']['value'],
                            :min => bucket['min_time']['value']}
        m
      end

      step_duration = step_max_times.keys.inject({}) do |m, step_number|
        step           = step_max_times[step_number]
        prev_step      = step_max_times[(step_number.to_i - 1)]
        step_max_time = step[:max]
        m[step_number] = (step_max_time - (prev_step && prev_step[:max] < step_max_time ? prev_step[:max] : step[:min]))
        m
      end

      result = {:rfcs => rfc_times, :steps => step_duration}
    rescue Exception => e
      handle_exception e, "Failed to perform 'time_stats' for deployment #{deployment.deploymentId}"
    end
    return result
  end

  def self.state_info(deployment)
    result        = nil
    search_params = {
      :query   => {
        :bool => {
          :must => [{:term => {:deploymentId => deployment.deploymentId}}]
        }
      },
      :_source => %w(rfcCi.ciId dpmtRecordState comments),
      :size    => 999999
    }
    begin
      data   = JSON.parse(post('', {}, search_params.to_json).body)
      result = data['hits']['hits'].inject({}) do |m, rfc|
        d = rfc['_source']
        m[d['rfcCi']['ciId']] = {:state => d['dpmtRecordState'], :comments => d['comments']}
        m
      end
    rescue Exception => e
      handle_exception e, "Failed to perform 'state_info' for deployment #{deployment.deploymentId}"
    end
    return result
  end
end
