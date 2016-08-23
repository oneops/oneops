# encoding: utf-8
require "logstash/outputs/base"
require "logstash/namespace"
require 'java'

class LogStash::Outputs::OneopsDaq < LogStash::Outputs::Base
  milestone 1
  config_name "oneopsdaq"

  config :cluster_host_port, :validate => :string, :default => "daq:9160"
  config :cluster_name, :validate => :string, :default => "PerfAndLogCluster"
  config :keyspace_name, :validate => :string, :default => "mdb"
  config :sensor_cluster_name, :validate => :string, :default => "sensor_ksp"
  config :sensor_keyspace_name, :validate => :string, :default => "sensor_ksp"
  config :sensor_host_port, :validate => :string, :default => "opsdb:9160"
  public
  def register
      require '/opt/oneops/artifact/current/oneops/dist/daq-1.0.0.jar'
      logConfigFile = java.net.URL.new('jar:file:///opt/oneops/artifact/current/oneops/dist/daq-1.0.0.jar!/log4j-logstash.xml');
      org.apache.log4j.xml.DOMConfigurator.configure(logConfigFile);
      @perfEventProcessor = Java::com.oneops.daq::PerfEventProcessor.new(@cluster_host_port, @cluster_name, @keyspace_name, @sensor_cluster_name, @sensor_keyspace_name, @sensor_host_port)
  end # def register

  public
  def receive(event)
    return unless output?(event)
        ip = event["ip"]
        message = event["message"]
       begin
        @logger.info('logstash received message: ')
       @perfEventProcessor.process(message, ip)
       rescue => e
           @logger.error('daq threw exception',
           :exception => e)
       end
        #
  end # def event
end # class LogStash::Outputs::OneopsDaq

