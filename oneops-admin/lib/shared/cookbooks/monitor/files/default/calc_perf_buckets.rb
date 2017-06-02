#!/usr/bin/env ruby

log_dir = "/var/log/nagios"
nagios_perflog = log_dir + "/service.log"
buckets = ["1m","5m","15m","1h","6h","1d"]
agg_log = ""

def parse_sample (line)
  parts = line.split("\t")
  if parts.size < 4
    # ignore bad perf data
    return
  end

  sample = {}
  sample[:time] = parts[0].to_i
  sample[:pretty_time] = parts[1]
  sample[:perf_key] = parts[2]
  sample[:perf_blob] = parts[3]

  metric_config_dir = "/opt/oneops/perf/#{sample[:perf_key].gsub(":","-")}"
  metric_config_file = "#{metric_config_dir}/config"
  key_parts = parts[2].split(":")

  if !File::exists?(metric_config_file)
     return {"no_config" => true, :ci_name => key_parts[1] } 
  end  

  sample[:ci_id] = key_parts[0]
  sample[:ci_name] = key_parts[1]

  perf_parts = sample[:perf_blob].split(" ")
  if perf_parts.size < 1
    puts "invalid sample: " + sample.inspect
    return
  end
  metrics = {}
  perf_parts.each do |keqv|
    kvarr = keqv.split("=")
    next unless kvarr.size > 1
    key = kvarr[0]
    val = kvarr[1].gsub(/[^\d.-]/,"")
    metrics[key] = val
  end
  sample[:metrics] = metrics

  metric_config = {}
  File.open(metric_config_file,"r").each_line do |iline|
    parts=iline.chomp.split("=")
    k = parts[0]
    metric_config[k] = parts[1]
  end
  metric_config[:dir] = metric_config_dir
  sample[:config] = metric_config
  sample[:next] = {}

  return sample
end

def save_config (sample)
  config = sample[:config].merge(sample[:next])
  config[:last_update] = sample[:time]

  metric_config_file = "#{config[:dir]}/config"
  serialized_config = ""
  config.each_pair do |k,v|
    serialized_config += "#{k}=#{v}\n"
  end
  File.open(metric_config_file, 'w') { |file| file.write(serialized_config) }
end

# supports gauge and counter/derive dstypes
def append (sample,k,v,bucket)
  config = sample[:config]

  dstype = config["#{k}-dstype"]
  if dstype == "gauge" || dstype.nil?
    val = v.to_f
  else
    if !config.has_key?("#{k}-last_counter_value")
      sample[:next]["#{k}-last_counter_value"] = v
      return    
    end
    last_update = config["last_update"].to_i
    last_counter_value = config["#{k}-last_counter_value"].to_i
    delta_sec = sample[:time] - last_update
    delta_val = v.to_f - last_counter_value
    if delta_val < 0
      val = 0.0/0
    else
      val = delta_val / delta_sec
    end
    sample[:next]["#{k}-last_counter_value"] = v
  end
  bucket_file = "#{config[:dir]}/#{k}-#{bucket}"
  File.open(bucket_file, "a") do |f|
    if !val.nan?
      f.write sample[:time].to_s+":"+val.to_s+"\n"
    end
  end
end


def flush(sample,k,bucket)
  config = sample[:config]
  step = get_step bucket
  start_time = config[bucket].to_i - step
  end_time = config[bucket].to_i
  #puts "flush #{bucket} #{k} - #{start_time} - #{end_time}"
  
  bucket_file = "#{config[:dir]}/#{k}-#{bucket}"
  previous_start = start_time

  # 1.8.7 NaN
  min = (0/0.0)
  max = (0/0.0)
  avg = 0

  weight_sum = 0
  sum = 0
  count = 0
  # handle when new metrics are added to existing monitor
  return [] unless File.exists?(bucket_file)
  File.open(bucket_file,"r").each_line do |line|
    parts = line.split(":")
    ts = parts[0].to_i
    value = parts[1].to_f     
    next if ts < start_time   
    if ts > end_time
      ts = end_time
    end
    delta = ts - previous_start
    weight = delta.to_f / step.to_f
    weight_sum += weight
    avg += weight * value
    min = value if min.nan? || value < min
    max = value if max.nan? || value > max
    sum += value
    count += 1
    #puts "line: #{line.chomp} -- #{delta} # #{avg} += #{weight} * #{value}"
    previous_start = ts
  end
  # handle multi bucket loss
  return [] if count < 1
    
  if weight_sum < 0.99
    avg = sum / count
    puts "ERROR: total weight #{weight_sum} - using sum/count instead: #{avg}"
  end
  File.truncate(bucket_file, 0)
    
  # min,max on >5min
  if bucket == "1m" || bucket == "5m"
    return [{:key => k, :value => avg, :bucket => bucket, :bucket_end => end_time, :agg_type => "avg"}]  
  else
    return [{:key => k, :value => avg, :bucket => bucket, :bucket_end => end_time, :agg_type => "avg"},
            {:key => k, :value => min, :bucket => bucket, :bucket_end => end_time, :agg_type => "min"},  
            {:key => k, :value => max, :bucket => bucket, :bucket_end => end_time, :agg_type => "max"}]
  end
end


def get_step (bucket)
  case bucket
  when "1m"
    return 60
  when "5m"
    return 300
  when "15m"
    return 900
  when "1h"
    return 3600
  when "6h"
    return 21600
  when "1d"
    return 86400
  end
end


def get_bucket_start (ts, step)
  return ts - (ts % step)
end

count = 0

# client side aggregate each line in nagios_perflog
File.open(nagios_perflog,"r").each_line do |line|
  sample = parse_sample line.chomp  
  count +=1
  #puts "count: #{count}"
  # pass thru / create old format if config hasnt been written
  if !sample.nil? && sample.has_key?("no_config")
    agg_log += line.chomp + "\t" + sample[:ci_name] + "\n"
    next
  elsif sample.nil? || sample[:metrics].nil? || sample[:metrics].size < 1
    puts "bad line: #{line}"
    next
  end
  config = sample[:config]
  result_map = {}
  next_start = {}
  aggregates = []
    
  sample[:metrics].each_pair do |k,v|

    buckets.each do |bucket|

      if !config.has_key? bucket
        step = get_step bucket
        config[bucket] = get_bucket_start(sample[:time], step) + step
      end

      append sample,k,v,bucket
      # compare bucket end time to sample
      if config[bucket].to_i < sample[:time]

        # flush and append to aggregates
        aggregates += flush(sample,k,bucket)
        append sample,k,v, bucket
        step = get_step bucket

        # setup next bucket end time
        next_start[bucket] = get_bucket_start(sample[:time], step) + step
      end
      
    end

  end
  next_start.keys.each do |bucket|
    config[bucket] = next_start[bucket]
  end
  
  save_config sample

  aggregate_map = {}
  aggregates.each do |metric|
    bucket = metric[:bucket]
    key = metric[:key]
    agg = metric[:agg_type]
    if !aggregate_map.has_key?(bucket)
      aggregate_map[bucket] = {}
    end
    if !aggregate_map[bucket].has_key?(agg)
      aggregate_map[bucket][agg] = {}
    end
    aggregate_map[bucket][agg][key] = metric
  end

  #puts JSON.pretty_generate(aggregate_map)
  perf_key = sample[:perf_key]
  
  # serialize
  aggregate_map.keys.each do |bucket|

    aggregate_map[bucket].keys.each do |agg|
      perf_blob = ""
      bucket_end = nil
      bucket_type = nil
      perf_blob = ""
      perf_key = sample[:perf_key]
    
      aggregate_map[bucket][agg].each_pair do |k,v|
      
        perf_blob += "#{v[:key]}="+ sprintf('%.3f', v[:value])+' '
        if bucket_end.nil?
          bucket_end = v[:bucket_end]
          bucket_type = v[:bucket]+"-"+v[:agg_type]
        end
      end
      next if bucket_end.to_i == 0
      agg_log += "#{bucket_end}\t#{Time.at(bucket_end).strftime('%a %b %e %T')}\t#{perf_key}:#{bucket_type}\t#{perf_blob}\n"

    end
  
  end

end

# write to log which logstash will forward
File.open(log_dir+"/service.perflog", 'a') { |file| file.write(agg_log) }
File.truncate(nagios_perflog, 0)
