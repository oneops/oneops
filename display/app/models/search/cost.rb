class Search::Cost < Search::Base
  self.prefix       = ''
  self.element_name = ''
  self.timeout      = 30

  def self.cost_rate(ns_path)
    result        = nil
    ns_path = "#{ns_path}#{'*bom/' unless ns_path.include?('/bom/') || ns_path.end_with?('/bom')}"
    ns_path = "#{ns_path}#{'/' unless ns_path.last == '/'}*"

    search_params = {
      :query   => {
        :wildcard => {'nsPath.keyword' => ns_path}
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
      :size    => 0
    }
    begin
      # data = JSON.parse(post('/cms-all/ci/_search', {}, search_params.to_json).body)
      # return data

      data  = JSON.parse(post('/cms-all/ci/_search', {}, search_params.to_json).body)['aggregations']
      total = data['data']['total']['value']
      if total > 0
        unit = data['data']['unit']['buckets'][0]
        result = {:total => total.round(2),
                  :unit  => unit.presence && "#{unit['key'].upcase}/hour"}
        [:by_ns, :by_cloud].each do |group_name|
          group = result[group_name] = {}
          data[group_name.to_s]['buckets'].each do |b|
            group[b['key']] = b['cost']['cost']['value'].round(2)
          end
        end
        group = result[:by_service] = {}
        data['data']['by_service']['buckets'].each do |b|
          group[b['key']] = b['cost']['value'].round(2)
        end
      else
        result = {}
      end
    rescue Exception => e
      handle_exception e, "Failed to fetch 'cost_rate' for nsPath #{ns_path}"
    end
    return result
  end

  def self.cost(ns_path, start_date, end_date)

    result     = nil
    start_date = start_date.to_date
    end_date   = end_date.to_date
    search_params = {
      :query => cost_query_conditions(ns_path, start_date, end_date),
      :_source => %w(ciId),
      :aggs => {
        :unit => {
          :terms => {:field => 'unit'}
        },
        :total => {
          :sum => {:field => 'cost'}
        },
        :by_ns => {
          :terms => {
            :field => 'nsPath.keyword',
            :order => {'cost' => 'desc'},
            # :order => {'_term' => 'asc'},
            :size  => 9999
          },
          :aggs  => {
            :cost => {
              :sum => {:field => 'cost'}
            }
          }
        },
        :by_service => {
          :terms => {
            :field => 'serviceType.keyword',
            :order => {'cost' => 'desc'},
            # :order => {'_term' => 'asc'},
            :size  => 9999
          },
          :aggs => {
            :cost => {
              :sum => {:field => 'cost'}
            }
          }
        },
        :by_cloud => {
          :terms => {
            :field => 'cloud.keyword',
            :order => {'cost' => 'desc'},
            # :order => {'_term' => 'asc'},
            :size  => 9999
          },
          :aggs => {
            :cost => {
              :sum => {:field => 'cost'}
            }
          }
        }
      },
      :size => 0
    }

    begin
      # data = JSON.parse(post('/cost/ci/_search', {}, search_params.to_json).body)
      # return data

      data  = JSON.parse(post('/cost/ci/_search', {}, search_params.to_json).body)['aggregations']
      total = data['total']['value']
      if total > 0
        unit = data['unit']['buckets'][0]
        result = {:start_date => start_date,
                  :end_date   => end_date,
                  :total      => total,
                  :unit       => unit.presence && unit['key'].upcase}
        [:by_ns, :by_cloud, :by_service].each do |group_name|
          group = result[group_name] = {}
          data[group_name.to_s]['buckets'].each do |b|
            group[b['key']] = b['cost']['value']
          end
        end
      else
        result = {}
      end
    rescue Exception => e
      handle_exception e, "Failed to fetch 'cost' for nsPath=#{ns_path}, date range=[#{start_date}, #{end_date}"
    end
    return result
  end

  def self.cost_time_histogram(ns_path, start_date, end_date, interval, tags = nil)
    result     = nil
    start_date = start_date.to_date
    end_date   = end_date.to_date
    # end_date += 1.day if interval == 'day'
    ranges     = [[start_date, start_date.send("next_#{interval}").send("beginning_of_#{interval}").to_date]]
    while ranges.last.last <= end_date
      ranges << [ranges.last.last, ranges.last.last + 1.send(interval)]
    end
    search_params = {
      :query => cost_query_conditions(ns_path, start_date, end_date),
      :_source => %w(ciId),
      :aggs => {
        :unit => {
          :terms => {:field => 'unit'}
        },
        :total => {
          :sum => {:field => 'cost'}
        },
        :time_histogram => {
          :range => {:field => 'date', :ranges => ranges.map{|r| {:from => r.first, :to => r.last}}},
          :aggs => {
            :total => {
              :sum => {:field => 'cost'}
            },
            :by_ns => {
              :terms => {
                :field => 'nsPath.keyword',
                :order => {'cost' => 'desc'},
                # :order => {'_term' => 'asc'},
                :size  => 99999
              },
              :aggs  => {
                :cost => {
                  :sum => {:field => 'cost'}
                }
              }
            },
            :by_service => {
              :terms => {
                :field => 'serviceType.keyword',
                :order => {'cost' => 'desc'},
                # :order => {'_term' => 'asc'},
                :size  => 9999
              },
              :aggs => {
                :cost => {
                  :sum => {:field => 'cost'}
                }
              }
            },
            :by_cloud => {
              :terms => {
                :field => 'cloud.keyword',
                :order => {'cost' => 'desc'},
                # :order => {'_term' => 'asc'},
                :size  => 9999
              },
              :aggs => {
                :cost => {
                  :sum => {:field => 'cost'}
                }
              }
            }
          }
        }
      },
      :size    => 0
    }

    if tags.present?
      aggs = search_params[:aggs][:time_histogram][:aggs]
      tags.each do |t|
        aggs["by_#{t}"] = {
          :terms => {
            :field => "tags.#{t}.keyword",
            :order => {'cost' => 'desc'},
            # :order => {'_term' => 'asc'},
            :size => 99999
          },
          :aggs => {
            :cost => {
              :sum => {:field => 'cost'}
            }
          }
        }
      end
    end

    begin
      # data = JSON.parse(post('/v2-*/_search', {}, search_params.to_json).body)
      # Rails.logger.info "=== #{data.to_yaml}"
      # return data

      data = JSON.parse(post('/cost-20*/_search', {}, search_params.to_json).body)['aggregations']
      unit = data['unit']['buckets'][0]
      result = {:buckets    => data['time_histogram']['buckets'],
                :start_date => start_date,
                :end_date   => end_date,
                :interval   => interval,
                :unit       => unit.presence && unit['key'].upcase,
                :total      => data['total']['value']}
    rescue Exception => e
      handle_exception e, "Failed to fetch 'cost_time_histogram' for nsPath=#{ns_path}, date range=[#{start_date}, #{end_date}], interval=#{interval}"
    end
    return result
  end

  # Experimental stuff - not used for now.
  def self.ha(ns_path = '/')
    result = nil

    search_params = {
      :query   => {
        :bool => {:must   => [{:wildcard => {'nsPath.keyword' => "#{ns_path}#{'/' unless ns_path.last == '/'}*"}},
                              {:regexp => {'workorder.payLoad.Environment.ciAttributes.profile.keyword' => '.*[pP][rR][oO][dD].*'}},
                              {:term => {'workorder.cloud.ciAttributes.priority' => '1'}},
                              {:wildcard => {'ciClassName.keyword' => 'bom*Compute'}}],
                  :should => [{:wildcard => {'workorder.cloud.ciName.keyword' => '*dfw*'}},
                              {:wildcard => {'workorder.cloud.ciName.keyword' => '*dal*'}}],
                  :minimum_number_should_match => 1
        }
      },
      :aggs => {
        :nsPath => {
          :terms => {
            :field => 'nsPath.keyword',
            :size  => 99999
          },
          :aggs    => {
            :clouds => {
              :filters => {
                :filters => {
                  :dfw => {:query => {:wildcard => {'workorder.cloud.ciName.keyword' => '*dfw*'}}},
                  :dal => {:query => {:wildcard => {'workorder.cloud.ciName.keyword' => '*dal*'}}}
                }
              }
            }
          }
        }
      },
      :size => 0
    }
    begin
      data  = JSON.parse(post('/cms-all/ci/_search', {}, search_params.to_json).body)['aggregations']['nsPath']['buckets']
      # return data
      result = data.sort_by {|b| b['key']}.inject('') do |s, b|
        root, org, assembly, env, bom, platform = b['key'].split('/', 6)
        s << "#{org},#{assembly},#{env},#{platform},#{b['clouds']['buckets']['dfw']['doc_count']},#{b['clouds']['buckets']['dal']['doc_count']}\n"
        s
      end

    rescue Exception => e
      handle_exception e, "Failed to fetch ha data for nsPath #{ns_path}"
    end
    return result
  end


  private

  def self.cost_query_conditions(ns_path, start_date, end_date)
    conditions = [{:range => {'date' => {:gte => start_date, :lte => end_date, :format => 'yyyy-MM-dd'}}},
                 {:range => {'cost' => {:gt => 0}}}]
    conditions << {:wildcard => {'nsPath.keyword' => "#{ns_path}#{'/' unless ns_path.last == '/'}*"}} unless ns_path.blank? || ns_path == '/'
    return {:bool => {:must => conditions}}
  end
end
