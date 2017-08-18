#!/usr/bin/env ruby

exit 1 if ARGV.size < 1

@host               = ARGV[0]
bad_index_threshold = ARGV[1].to_f
bad_index_threshold = 0.1 unless bad_index_threshold > 0
look_behind_days    = ARGV[2].to_i
look_behind_days    = 7 unless look_behind_days > 0 && look_behind_days < 31

def get_record_count(date, retries = 3)
  result = 0
  cmd = %(curl -s -i "http://#{@host}:9200/_cat/indices/cost-#{date}")
  retries.times do
    r = `#{cmd}`
    if r.include?('200 OK')
      data = r.split("\n").last.split(/\s/)
      result = data[0] == 'green' && data[1] == 'open' ? data[5].to_i : 0
      break
    elsif r.include?('404 Not Found')
      break
    end
  end
  return result
end

missing_index_count = 0
bad_index_count     = 0
date                = Time.at(Time.now.to_i - 24 * 60 * 60)

record_count        = get_record_count(date.strftime("%Y%m%d"))
missing_index_count += 1 if record_count == 0

results = "record_count=#{record_count}"

look_behind_days.times do |i|
  date = Time.at(date.to_i - 24 * 60 * 60)
  prev_record_count = get_record_count(date.strftime("%Y%m%d"))
  if prev_record_count == 0
    missing_index_count += 1
  elsif record_count > 0 && (1 - record_count.to_f / prev_record_count).abs > bad_index_threshold
    bad_index_count += 1
  end
  record_count = prev_record_count
end

results += " missing_index_count=#{missing_index_count} bad_index_count=#{bad_index_count}"

puts "#{results}|#{results}"
