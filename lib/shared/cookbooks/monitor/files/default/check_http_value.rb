#!/usr/bin/env ruby
#
# gets queue metrics from builtin xml apis
#

require 'net/http'
require 'rubygems'

URL=ARGV[0]
FORMAT=ARGV[1]
RES_KEY=ARGV[2]
perf =''

puts "#{FORMAT}"
puts "#{URL}"


case FORMAT
  when 'xml'
    require 'xmlsimple'
    data = XmlSimple.xml_in(`curl -s #{URL}`,{ 'KeyAttr' => 'name', 'ForceArray' => false })
  when 'json'
    require 'json'
    data =JSON.parse(`curl -s #{URL}`)[RES_KEY]
  else
    data = `curl -s #{URL}` 
end

if data == "true"
 data = 100.00
elsif data == "false"
 data = 0.00
end

perf = "value=#{data}"

puts perf + "|"+ perf