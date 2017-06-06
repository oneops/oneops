Data AQuisition - DAQ - integrates with logstash to collect perf data.  Inserts into cassandra mdb keyspace and publishes to activemq perf-in-q queues.  There is a spring mvcweb Controller to map /daq-api/getPerfData, getLogData, getChart, etc  

steps to build/use:
1) mvn install 
2) cp war artifact from target/ to tomcat webapps and jar can be used in logstash (modeled and configured in the daq platform in oneops design)
