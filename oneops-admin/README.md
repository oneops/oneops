OneOps Admin
===============

Oneops admin gem consist of 2 gems.

1. inductor
2. circuit

Pre-requisites
-----
The `inductor.jar` artifact is required for the gem to build correctly. See [oneops/inductor](https://github.com/oneops/inductor).

Build
-----


Build the gem. It will create an `oneops-admin-VERSION.gem` in the root directory.

	gem build oneops-admin.gemspec
	
Install the gem from the root directory.

	gem install oneops-admin-VERSION.gem
	

Usage
-----

To check all the commands 
	inductor
	circuit
	
	
To verify the oneops-admin gem is installed and get usage info.

	 inductor help
	 circuit help
	

Inductor Usage
-----

To create and configure a new inductor.

	inductor create
	cd inductor

	
To add and enable a zone in the inductor.
> NOTE: The command below will prompt with few questions. The same values can be also specified as options - see `inductor help add`)

	inductor add
	

To start inductor with all enabled zones

	inductor start


To disable or enable configured zones

	inductor disable PATTERN
	inductor enable PATTERN
	
	
Circuit Usage
----

1. To create and configure a new circuit.

	circuit create
	cd circuit
	
Once the circuit is created use the knife commands to create the cookbooks and packs inside the circuit.


2. Initialize the circuit 

	circuit init
	
	

Misc
----

The following configurations are used to skip the deployment steps. 
> Use this only to cleanup/skip resources those are already decommissioned or removed.

```ruby

# Config file - /path/to/inductor/clouds-enabled/<cloud>/conf/inductor.properties

# The list of clouds, whose resources are already decommissioned or removed.
# Inductor will mark those work-order (no action orders) execution result as
# success (0) regardless of the execution outcome for the clouds listed in
# this (shutdown.clouds) inductor property.
     
    shutdown.clouds=prod-dfw1,prod-dfw2,���
     
# List of bom classes, whose process result status needs to keep intact.
# By default <b>bom.Fqdn</b>,<b>bom.Lb</b> are added to this list as it
# doesn't have any openstack hypervisor dependency.
    
    shutdown.skipClasses= ...  
    
# List of rfc actions for which the result need to be processed.
# <b>DELETE</b>action is added by default.
    
    shutdown.rfcActions= ...  
  
# Timeout  value for command execution for shutdown clouds.
    
    shutdown.cmdTimeout=10

```

