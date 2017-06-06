#!/usr/bin/env ruby
#
# gets topic metrics from builtin xml apis
#

require 'net/http'
require 'rubygems'
require 'xmlsimple'

url =''
ignore_cert = ''

if ARGV[0] == 'https'
  ignore_cert = ' -k '
end

if ARGV[4] == 'false'
  url = ARGV[0] +"://localhost:"+ ARGV[1] + ARGV[2] + "#{ignore_cert}"
else
  url = ARGV[0]+"://localhost:"+ ARGV[1] + ARGV[2] +" -u "+ ARGV[5]+ ":" + ARGV[6] + "#{ignore_cert}"
end

topic_name =ARGV[3]

topics = XmlSimple.xml_in(`curl -s #{url}`,{ 'KeyAttr' => 'name', 'ForceArray' => false })

topic_pending_count = 0
topic_enqueue_count = 0
topic_dequeue_count = 0
topic_consumer_count = 0

real_topics = topics["topic"];
topic=''
if real_topics != nil
        real_topics.keys.each do |key|
           if key == "#{topic_name}"
              topic = real_topics[key]
           elsif key == "name"
               topic = real_topics
           end
end
           topic_pending_count += (topic["stats"]["size"]).to_i
           topic_consumer_count += (topic["stats"]["consumerCount"]).to_i
           topic_enqueue_count += (topic["stats"]["enqueueCount"]).to_i
           topic_dequeue_count += (topic["stats"]["dequeueCount"]).to_i
end
perf =  "topic_pending_count=#{topic_pending_count} "
perf +=  "topic_consumer_count=#{topic_consumer_count} "
perf += "topic_enqueues=#{topic_enqueue_count} "
perf += "topic_dequeues=#{topic_dequeue_count} "

puts perf + "|"+ perf