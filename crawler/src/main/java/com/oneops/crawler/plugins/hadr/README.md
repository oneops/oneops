# OneOps Environment HADR Crawler Plugin

This plugin create HADR (High Availability & Disaster Recovery) compliance report

1. Get list of all environments production environments (environments whose profile name contain "prod" as a keyword).  
2. create a cache of organizations information. 
3. Process each environment one by one as following. 
	
		A. Get list of all platforms deployed in the environment
		B. Check if platform is HA Compliant or not, Mark HA compliant if deployed on more than 1 clouds
		C. Check if platform is DR Compliant or not, Mark DR compliant if deployed on more than 1 data centers
		D. Populate HADR record with platform, platform's organization and platform's cloud information.  
		E. Send HADR record to elastic search 	
	
Setup Kibana dashboard to visualize data sent to Elastic Search. 

#### Configuration params 

Java system properties:

`-Dhadr.plugin.enabled=true` ; if set as true, plugin will process environments

`-Dhadr.es.enabled=true`  ; if true, processed records will be sent to elastic search

`hadr.oo.baseurl=https://localhost:3000` ; This URL will be attached as prefix to nspath of the platform to create fully qualified URl for platform

`hadr.env.profile.regex=prod` ; This property is used to check if environment profile name contain `prod` as key word, if true then consider environment as production.

`-Dhadr.prod.datacenters.list=datacenter1~datacenter2~datacenter3` ; list of data centers separated by `~`. This list will be used to identify if platform is deployed in any 2 of data centers from the list. Any platform deployed on more than 2 data centers will be marked as DR compliant.