# Antenna
## Introduction

This project is responsible for persisting and sending  notifications(related to deployments, healthy) to the  
configured Notification Sinks. Following [notification sinks][]  are supported  
  
  *GenericHTTPMessage  
  *XMPPSubscriber
   
Also refer [sink metadata][]

## Usage

Its used internally from opamp , controller to send notifications(deployments, unhealthy events) to configured sinks.


## Documentation
Coming soon

## Contributing

Refer [Contributing][]. 

## Help

Check out the oneops tags on [Stack Overflow][] or [slack-dev][] 

### Requirements

[Maven][]  and [JDK 8  or later][JDK8 build] 

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.
 
The project is dependent on [oo-commons][] and [cms-dal][] . 
 

## Building the Source

* git clone https://github.com/oneops/antenna.git
* cd antenna
* mvn clean package 

This will build the war deployable on standard java servlet container like tomcat.

### Configuration

Antenna war would require environment variables to be passed refer [tomcat environment variables][]  

### Installation
For quick setup refer [setup][] 


## License
The project is released under version 2.0 of the [Apache License](http://www.apache.org/licenses/LICENSE-2.0).



[Maven]: http://maven.apache.org/
[Git]: http://help.github.com/set-up-git-redirect
[JDK8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[Stack Overflow]: http://stackoverflow.com/tags/oneops
[slack-dev]:https://oneops.slack.com/messages/devel/messages
[oo-commons]:../../../oo-commons
[cms-dal]:../../../cmsdal
[Oneops UI]:../../../display
[setup]:../../../setup
[Client]:../../../cli
[Contributing]:https://github.com/oneops/developer-doc/blob/master/_contribution/index.md
[tomcat environment variables]:../../../dev-tools/blob/master/setup-scripts/tom_setenv.sh
[sink metadata]:../../../oneops-admin/blob/master/lib/base/sink/metadata.rb
[notification sinks]: /src/main/java/com/oneops/antenna/senders/generic
