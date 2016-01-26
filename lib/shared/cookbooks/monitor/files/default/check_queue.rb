#!/usr/bin/env ruby
#
# gets queue metrics from builtin xml apis
#

# check for secondary or standby
adminstatus = `grep ONEOPS_CLOUD_ADMINSTATUS /etc/profile.d/oneops.sh`.split("=").last.chomp
if adminstatus == "secondary"
  puts "standby|queue_pending_count=0 queue_consumer_count=0 queue_enqueues=0 queue_dequeues=0"
  exit 0
else
  last_log_line = `tail -1 /opt/activemq/data/activemq.log`
  if last_log_line =~ /Attempting to acquire the exclusive lock/
    puts "standby|queue_pending_count=0 queue_consumer_count=0 queue_enqueues=0 queue_dequeues=0"
    exit 0
  end
end

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

queue_name =ARGV[3]

queues = XmlSimple.xml_in(`curl -s #{url}`,{ 'KeyAttr' => 'name', 'ForceArray' => false })

queue_pending_count = 0
queue_enqueue_count = 0
queue_dequeue_count = 0
queue_consumer_count = 0

real_queues = queues["queue"];
queue=''
if real_queues != nil
        real_queues.keys.each do |key|
           if key == "#{queue_name}"
              queue = real_queues[key]
           elsif key == "name"
               queue = real_queues
           end
end
           queue_pending_count += (queue["stats"]["size"]).to_i
           queue_consumer_count += (queue["stats"]["consumerCount"]).to_i
           queue_enqueue_count += (queue["stats"]["enqueueCount"]).to_i
           queue_dequeue_count += (queue["stats"]["dequeueCount"]).to_i
end
perf =  "queue_pending_count=#{queue_pending_count} "
perf += "queue_consumer_count=#{queue_consumer_count} "
perf += "queue_enqueues=#{queue_enqueue_count} "
perf += "queue_dequeues=#{queue_dequeue_count} "

puts perf + "|"+ perf