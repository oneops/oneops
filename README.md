# CMSDAL
## Introduction

This is shared Java library with OneOps data access layer and Java beans. 

## Usage

The service layer in cmsdal exposes different methods to retrieve and modify configuration stored. 

Refer following  for 
* [Model][]
* [Namespace][]
* [Deployment][]

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

## Building the Source

* git clone https://github.com/oneops/cmsdal.git
* cd cmsdal
* mvn clean package 


This will build the jar which can be included in pom for projects requiring cmsdal

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



[model]:src/main/java/com/oneops/cms/cm
[namespace]:src/main/java/com/oneops/cms/ns
[deployment]:src/main/java/com/oneops/cms/dj
[notification sinks]: /src/main/java/com/oneops/antenna/senders/generic
