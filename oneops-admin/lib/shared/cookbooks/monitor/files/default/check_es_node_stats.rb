#!/usr/bin/env ruby

require 'net/https'
require 'rubygems'
require 'json'
require 'bigdecimal'
require 'open-uri'

PORT=ARGV[0]

class RestClient
  attr_accessor :body, :uri

  def initialize(host_name, port)
    @host_name = host_name
    @port = port
    @http = Net::HTTP.new(@host_name, @port)
    @http.open_timeout = 5
    @http.use_ssl = false
    @resp = nil
  end

  def getData()
    @resp = nil

    @http.start do |http|
      req = Net::HTTP::Get.new(@uri)
      #http.open_timeout = 5
      @resp, data = http.request(req)
    end

    setRepToJson()
    return @body

  end

  def setRepToJson()
    if @resp != nil then
      @body = JSON.parse(@resp.body())
    end
  end


end

class Node

  attr_accessor :name, :status, :indexed_doc_count, :heap_used_percent, :search_rate, :filter_cache_evictions, :gc_old_collections, :index_rejections, :search_rejections, :disk_reads, :disk_writes
   @node_name=nil 
   @node_status=nil 
   @indexed_doc_count=nil 
   @heap_used_percent=nil 
   @search_rate=nil 
   @filter_cache_evictions=nil 
   @gc_old_collections=nil 
   @index_rejections=nil 
   @search_rejections=nil 

end


class NumberFormat
  num = nil

  def toFloat(string)
    num = sprintf("%.3f", string)
    if(num == "NaN") then
      num = 0.00
    end
    num
  end

  num
end

rs = String.new

begin
  restClient = RestClient.new("localhost", "#{PORT}")
  numberFormat = NumberFormat.new
  node = Node.new

  restClient.uri ='/'
  res = restClient.getData()
  status = res["status"]
  node.status = (status == 200 ? numberFormat.toFloat(0) : numberFormat.toFloat(100))
  node.name = res["name"]
  restClient.uri = URI::encode("_nodes/#{node.name}/stats")
  res = restClient.getData()
  stats = res["nodes"].first[1]

  # Indexed doc count
  node.indexed_doc_count = stats["indices"]["docs"]["count"]

  # search rate (time per query)
  query_total = stats["indices"]["search"]["query_total"]
  if query_total > 0  
    query_time_total = stats["indices"]["search"]["query_time_in_millis"]
    node.search_rate = numberFormat.toFloat(query_time_total/query_total)
  else
    node.search_rate = 0  
  end

  # filter cache evictions
  node.filter_cache_evictions = stats["indices"]["filter_cache"]["evictions"]
  
  # Heap percent used
  node.heap_used_percent = stats["jvm"]["mem"]["heap_used_percent"]
  
  # old tenured gc collection count
  node.gc_old_collections = stats["jvm"]["gc"]["collectors"]["old"]["collection_count"]
  
  # index/delete requests rejection rate
  node.index_rejections = stats["thread_pool"]["index"]["rejected"]
  
  # search requests rejection rate
  node.search_rejections = stats["thread_pool"]["search"]["rejected"]
  
  # disk read/writes
  node.disk_reads = stats["fs"]["total"]["disk_reads"]
  node.disk_writes = stats["fs"]["total"]["disk_writes"]
  
  rs.concat("status=#{node.status.to_s}; ")
  rs.concat("indexed_doc_count=#{node.indexed_doc_count}; ")
  rs.concat("search_rate=#{node.search_rate}; ")
  rs.concat("filter_cache_evictions=#{node.filter_cache_evictions}; ")
  rs.concat("heap_used_percent=#{node.heap_used_percent}; ")
  rs.concat("gc_old_collections=#{node.gc_old_collections}; ")
  rs.concat("index_rejections=#{node.index_rejections}; ")
  rs.concat("search_rejections=#{node.search_rejections}; ")
  rs.concat("disk_reads=#{node.disk_reads}; ")
  rs.concat("disk_writes=#{node.disk_writes}; ")

rescue Exception => msg
  rs.concat("#{msg} status=1.00 ")
end

puts "#{rs} | #{rs}"
