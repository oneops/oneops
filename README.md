# Adapter
## Introduction

Adapter exposes set of rest apis to perform CRUD operations on the configuration stored in cms-db.    

## Usage

Primarily used via [Oneops UI][] and knife plugin  to read and update the cms configuration. Following are the main set of API's  
 
|Entity| URL   |
|---|---|
|  Model |/rest/md/*  |
| CM  |/rest/cm/*|
|  Deployment | /rest/dj/*  |
|  Namespace |/rest/ns/*  |


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

* git clone https://github.com/oneops/adapter.git
* cd adapter 
* mvn clean package 

This will build the war deployable on standard java servlet container like tomcat.

### Configuration

Adapter war would require environment variables to be passed refer [tomcat environment variables][]  

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
