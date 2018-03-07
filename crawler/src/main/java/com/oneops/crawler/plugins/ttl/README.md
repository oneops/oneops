# OneOps Environment TTL Crawler Plugin

This plugin processes the Environment, Platforms and Deployments data received from the CMSCrawler and then does following:

1. Checks if the Environment is a non-production environment. If yes, proceed, otherwise abort.
2. Checks if the Environment is dormant for long time (configurable). If yes, proceed, otherwise abort.
3. Send notification using OneOps antenna that the Env will be TTLed after certain date.
4. If the TTL date is reached, disable all platforms and peform a forced deploy on that
   environment to decommission it and recover the resources.


#### Criteria for deciding if the environment can be auto-TTLed:

1. Environment Profile is non-null and does not contain "prod" (case-insensitive) in it
2. Environment is not deployed since last X days (configured using system property)
3. None of its platforms have "prod" in their active clouds. 


#### Configuration params 

Java system properties:

```ttl.deployed.before.days``` : TTL those Envs which are not deployed since these # of days

```ttl.plugin.enabled``` : if set to ```true```, the plugin will actually go ahead with decommissioning in an env is found eligible. If not set, it will just scan the environments

```ttl.es.enabled``` : if set to ```true```, this plugin will save all of its data to the ES instance configured 

```ttl.grace.period.days``` : The number of days the Env will be alerted for TTL before it is actually purged by this plugin

```ttl.config``` : json string for enabling the TTL only for certain orgs and packs. Example: ```"{ orgs : [\"stgqe\" , \"oneops\"], packs : [\"tomcat\", \"apache\"] }"```

