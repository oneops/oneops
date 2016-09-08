#!/usr/bin/env ruby
require 'json'
require 'time'
require 'date'
require 'time_diff'

es_host = ARGV[0]
start_day = ARGV[1]
end_day = ARGV[2]

def index_unchanged_ci_cost (td , index_name, host)

  wos = JSON.parse(`curl -s -XGET 'http://#{host}:9200/cms-all/ci/_search' -d '{
  "_source": {
    "include": [
      "nsPath",
      "ciClassName",
      "ciId",
      "workorder.cloud.ciName",
      "workorder.box.ciAttributes.pack",
      "workorder.box.ciAttributes.major_version",
      "workorder.box.ciAttributes.source",
      "workorder.payLoad.offerings",
      "workorder.payLoad.RealizedAs",
      "workorder.payLoad.Environment",
      "workorder.payLoad.Organization",
      "workorder.searchTags.responseDequeTS"
    ]
  },
  "size": 1000000,
  "query": {
    "filtered": {
      "query": {
        "nested": {
          "path": "workorder.payLoad.offerings",
          "filter": {
            "exists": {
              "field": "workorder.payLoad.offerings"
            }
          }
        }
      },
      "filter": {
        "range": {
          "workorder.searchTags.responseDequeTS": {
            "lt": "#{td}T00:00:00"
          }
        }
      }
    }
  }
}'`)["hits"]["hits"]

  # puts wos.size
 
  wos.each do |wo|
  
    nsPath = wo["_source"]["nsPath"]
    ciClassName = wo["_source"]["ciClassName"]
    ciId = wo["_source"]["ciId"]
    cloud = wo["_source"]["workorder"]["cloud"]["ciName"]

    packName = wo["_source"]["workorder"]["box"]["ciAttributes"]["pack"]
    packVersion = wo["_source"]["workorder"]["box"]["ciAttributes"]["major_version"]
    packSource = wo["_source"]["workorder"]["box"]["ciAttributes"]["source"]

    envProfile = wo["_source"]["workorder"]["payLoad"]["Environment"][0]["ciAttributes"]["profile"]

    manifestId = wo["_source"]["workorder"]["payLoad"]["RealizedAs"][0]["ciId"]

    organization = wo["_source"]["workorder"]["payLoad"]["Organization"][0]["ciName"]

    offerings = wo["_source"]["workorder"]["payLoad"]["offerings"]
    offerings.each do |o|
      cost_unit = o["ciAttributes"]["cost_unit"]
      service_type = o["ciAttributes"]["service_type"]
      cost_rate = o["ciAttributes"]["cost_rate"]
      service_nsPath = o["nsPath"]
      cost = "#{'%.03f' % (cost_rate.to_f * 24)}".to_f

      cost_record = {"ts" => Time.now.utc.iso8601, "ciId" => ciId ,"cloud" => cloud, "cost" => cost.to_f , "unit" => cost_unit, "nsPath" => nsPath ,
                     "organization" => organization , "ciClassName" => ciClassName, "serviceType" => service_type, "servicensPath" => service_nsPath, "manifestId" => manifestId,
                     "packName" => packName, "packVersion" => packVersion , "packSource" => packSource , "envProfile" => envProfile,
                     "date" => DateTime.parse("#{td}").to_time.utc.iso8601}

      open('ci_cost.json', 'a') do |c|
        c << cost_record.to_json
        c << "\n"
      end

    end

  end

  ind = `cat ci_cost.json | ./stream2es stdin --target "http://#{host}:9200/#{index_name}/ci"`
  # puts ind

  File.delete("ci_cost.json")

end

def fetch_prev_day_last_wo_cost_rate (td , ciId, host,init_rate)

  # puts "prev day wo for ci #{ciId}"

  wos = `curl -s -XGET 'http://#{host}:9200/cms-20*/workorder/_search' -d '{
  "_source": ["payLoad.offerings","searchTags.responseDequeTS"],
  "size": 1,
  "query": {
    "filtered": {
      "filter": {
        "bool": {
          "must": [
            {
              "term": {
                "rfcCi.ciId": #{ciId}
              }
            },
            {
              "exists": {
                "field": "payLoad.offerings"
              }
            },
            {
              "range": {
                "searchTags.responseDequeTS": {
                  "lt": "#{td}"
                }
              }
            }
          ]
        }
      }
    }
  },
   "sort": [
    {
      "searchTags.responseDequeTS": {
        "order": "desc"
      }
    }
  ]
}'`

  hits = JSON.parse(wos)["hits"]["hits"]

  if !hits.empty?
    # puts hits
    cr = hits[0]["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["cost_rate"].to_f/60
  else
    cr = init_rate
  end

  return cr

end

def fetch_prev_day_last_wo_cost_rate_for_del (td , ciId, host)

  # puts "prev day wo for ci #{ciId}"

  wos = `curl -s -XGET 'http://#{host}:9200/cms-20*/workorder/_search' -d '{
  "_source": ["payLoad.offerings","searchTags.responseDequeTS"],
  "size": 1,
  "query": {
    "filtered": {
      "filter": {
        "bool": {
          "must": [
            {
              "term": {
                "rfcCi.ciId": #{ciId}
              }
            },
            {
              "exists": {
                "field": "payLoad.offerings"
              }
            },
            {
              "range": {
                "searchTags.responseDequeTS": {
                  "lt": "#{td}"
                }
              }
            }
          ]
        }
      }
    }
  },
   "sort": [
    {
      "searchTags.responseDequeTS": {
        "order": "desc"
      }
    }
  ]
}'`

  hits = JSON.parse(wos)["hits"]["hits"]

  if !hits.empty?
    # puts hits
    #cr = hits[0]["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["cost_rate"].to_f/60
    lwo = hits[0]["_source"]["payLoad"]["offerings"][0]
  else
    return nil
  end

  return lwo

end



def calculate_cost(wos,init_cost_rate,td,end_ts)

  init_time = Date.parse(td.to_s).to_time.utc
  cost = 0
  cost_rate = 0
  action = ""

  # puts "wo size = #{wos.size}"

  wos.each do |wo|
    action = wo["_source"]["searchTags"]["rfcAction"]
    # puts action
    ts = wo["_source"]["searchTags"]["responseDequeTS"]
    # puts ts

    if action == "add"
      init_time = Time.parse("#{ts}").utc
    end

    mins = ((Time.parse("#{ts}").utc - init_time)/60)
    # puts "tot mins #{mins}"

    if action == "delete"
      if mins > 0
        cost = "#{'%.03f' % (cost + (mins * cost_rate))}".to_f
      end
      next
    end

    if cost_rate == 0
      cost_rate = init_cost_rate
    else
      cost_rate = (wo["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["cost_rate"].to_f/60)
    end

    if mins > 0
      cost = "#{'%.03f' % (cost + (mins * cost_rate))}".to_f
    end

    init_time = Time.parse("#{ts}").utc
  end

  if action != "delete"
    cost = "#{'%.03f' % (cost + ((((end_ts - init_time)/60)) * cost_rate))}".to_f
  end

  # puts "final cost is #{cost}"
  return cost

end


def index_target_day_ci_cost(td,index_name,host,class_name)

  end_ts = Date.parse(td.to_s).end_of_day.utc
  wos = `curl -s -XGET 'http://#{host}:9200/cms-20*/workorder/_search' -d '{
  "_source": false,
  "query": {
    "filtered": {
      "filter": {
        "bool": {
           "must": [
            {
              "term" : { "rfcCi.ciClassName.keyword" : "#{class_name}" }
            },
            {
              "range": {
                "searchTags.responseDequeTS": {
                  "gt": "#{td}T00:00:00"
                }
              }
            }
          ],
          "must_not": {
              "term" : { "dpmtRecordState" : "failed" }
          },
          "should" : [
            {
              "exists": {
                "field": "payLoad.offerings"
              }
            },
            {
              "term" : { "searchTags.rfcAction" : "delete" }
            }
           ]
        }
      }
    }
  },
  "aggs": {
    "ci": {
      "terms": {
        "field": "rfcCi.ciId",
        "size": 100000
      },
      "aggs": {
        "wos": {
          "top_hits": {
            "sort": [
              {
                "searchTags.responseDequeTS": {
                  "order": "asc"
                }
              }
            ],
            "_source": {
              "include": [
                "rfcCi.ciId",
                "rfcCi.nsPath",
                "cloud.ciName",
                "rfcCi.ciClassName",
                "searchTags.rfcAction",
                "payLoad.RealizedAs",
                "payLoad.Environment",
                "payLoad.Organization",
                "box.ciAttributes.pack",
                "box.ciAttributes.major_version",
                "box.ciAttributes.source",
                "payLoad.offerings",
                "searchTags.responseDequeTS"
              ]
            },
            "size": 5000
          }
        }
      }
    }
  }
}'`

  # open('tmp.json', 'a') do |c|
  #   c << wos
  # end

  ci_buckets = JSON.parse(wos)["aggregations"]["ci"]["buckets"]

  # puts "bucket size #{ci_buckets.size}"

  ci_buckets.each do |bucket|
    ciId = bucket["key"]
    wos = bucket["wos"]["hits"]["hits"]

    first_wo = wos.first
    rfcAction = first_wo["_source"]["searchTags"]["rfcAction"]
    resDequeTS = first_wo["_source"]["searchTags"]["responseDequeTS"]
    nsPath = first_wo["_source"]["rfcCi"]["nsPath"]
    ciClassName = first_wo["_source"]["rfcCi"]["ciClassName"]
    cloud = first_wo["_source"]["cloud"]["ciName"]
    manifestId = first_wo["_source"]["payLoad"]["RealizedAs"][0]["ciId"]

    packName = first_wo["_source"]["box"]["ciAttributes"]["pack"]
    packVersion = first_wo["_source"]["box"]["ciAttributes"]["major_version"]
    packSource = first_wo["_source"]["box"]["ciAttributes"]["source"]

    envProfile = first_wo["_source"]["payLoad"]["Environment"][0]["ciAttributes"]["profile"]

    organization = first_wo["_source"]["payLoad"]["Organization"][0]["ciName"]

    if rfcAction != "delete"
      service_type = first_wo["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["service_type"]
      cost_unit = first_wo["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["cost_unit"]
      service_nsPath = first_wo["_source"]["payLoad"]["offerings"][0]["nsPath"]
      init_rate = first_wo["_source"]["payLoad"]["offerings"][0]["ciAttributes"]["cost_rate"].to_f
    end

    # puts ciId

    if rfcAction == "add"
      if Time.parse("#{resDequeTS}").utc > Date.parse(td.to_s).end_of_day.utc
        next
      else
        init_cost_rate = (init_rate/60)
        cost = calculate_cost(wos , init_cost_rate,td,end_ts)
      end
    elsif (rfcAction == "update" || rfcAction == "replace")
      init_cost_rate = fetch_prev_day_last_wo_cost_rate(td,ciId,host,init_rate/60)
      if Time.parse("#{resDequeTS}").utc > Date.parse(td.to_s).end_of_day.utc
        cost = init_cost_rate * 24 * 60
      else
        cost = calculate_cost(wos , init_cost_rate,td,end_ts)
      end
    elsif rfcAction == "delete"
      lw = fetch_prev_day_last_wo_cost_rate_for_del(td,ciId,host)
      if !lw.nil?
        init_cost_rate = lw["ciAttributes"]["cost_rate"].to_f/60
        service_type = lw["ciAttributes"]["service_type"]
        cost_unit = lw["ciAttributes"]["cost_unit"]
        service_nsPath = lw["nsPath"]
      else
        init_cost_rate = 0
      end

      if Time.parse("#{resDequeTS}").utc > Date.parse(td.to_s).end_of_day.utc
        cost = init_cost_rate * 24 * 60
      else
        cost = calculate_cost(wos , init_cost_rate,td,end_ts)
      end
    end

    if cost > 0
      cost_record = {"ts" => Time.now.utc.iso8601, "ciId" => ciId ,"cloud" => cloud, "cost" => cost.to_f , "unit" => cost_unit, "nsPath" => nsPath ,
                     "organization" => organization, "ciClassName" => ciClassName, "serviceType" => service_type, "servicensPath" => service_nsPath, "manifestId" => manifestId,
                     "packName" => packName, "packVersion" => packVersion , "packSource" => packSource , "envProfile" => envProfile,
                     "date" => DateTime.parse("#{td}").to_time.iso8601}


      open('ci_cost.json', 'a') do |c|
        c << cost_record.to_json
        c << "\n"
      end
    else
      # puts "0 cost ci #{ciId} for action #{rfcAction} and type #{ciClassName}"
    end


  end

  if ci_buckets.size > 0
    if File.exist?("ci_cost.json")
      ind = `cat ci_cost.json | ./stream2es stdin --target "http://#{host}:9200/#{index_name}/ci"`
      # puts "target day ci cost done: #{ind}"
      File.delete("ci_cost.json")
    end
  end

  end

File.delete("ci_cost.json") if File.exist?("ci_cost.json")

day_range = (DateTime.parse(start_day).to_date..DateTime.parse(end_day).to_date).map(&:to_s)

day_range.each do |d|

  puts "indexing cost for day #{d}"

  index_name = "cost-"+Time.parse("#{d}").strftime("%Y%m%d")
  res = `curl -s -XHEAD -i "http://#{es_host}:9200/#{index_name}"`

  # create index if doesnt exist. Delete index if already exists
  if res.include?("404 Not Found")
    `curl -s -XPOST -i 'http://#{es_host}:9200/#{index_name}'`
  elsif res.include?("200 OK")
    `curl -s -XDELETE 'http://#{es_host}:9200/#{index_name}'`
  end

  #stage 1
  index_unchanged_ci_cost(d,index_name,es_host)


  #stage 2
  index_target_day_ci_cost(d,index_name,es_host,'bom.Compute')
  puts "done with all v1 computes"
  index_target_day_ci_cost(d,index_name,es_host,'bom.oneops.1.Compute')
  puts "done with all oneops1 computes"
  index_target_day_ci_cost(d,index_name,es_host,'bom.main.2.Compute')
  puts "done with all v2 computes"


end

puts "cost indexer job done"

