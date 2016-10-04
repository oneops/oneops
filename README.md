# search
## Introduction

Search queue listener for processing and feeding data into search engine (current choice elastic search)

## Usage

used  for posting cms ci ,notifications, cms events to backend elastic search.  
 

## Contributing

refer [contributing][]. 

## Help

check out the oneops tags on [stack overflow][] or [slack-dev][] 

### Requirements

[maven][]  and [jdk 8  or later][jdk8 build] 

be sure that your `java_home` environment variable points to the `jdk1.8.0` folder
extracted from the jdk download.
 
the project is dependent on [oo-commons][] and [cms-dal][] . 
 

## Building the source

* git clone https://github.com/oneops/search.git
* cd search
* mvn clean package 

This will build the search jar which can start consuming messages from search queue.

### Configuration

For starting up search refer [search-configuration][] 

### Installation
for quick setup refer [setup][] 


Load Elastic search templates 
curl -d @./cms_template.json -X PUT http://localhost:9200/_template/cms_template
curl -d @./event_template.json -X PUT http://localhost:9200/_template/event_template
curl -d @./cost_template.json -X PUT http://localhost:9200/_template/cost_template

Percolator mapping:
curl -d @./percolator_mapping.json -X PUT http://localhost:9200/cms-all/.percolator/_mapping

## license
the project is released under version 2.0 of the [apache license](http://www.apache.org/licenses/license-2.0).




[maven]: http://maven.apache.org/
[git]: http://help.github.com/set-up-git-redirect
[jdk8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[apache license]: http://www.apache.org/licenses/license-2.0
[stack overflow]: http://stackoverflow.com/tags/oneops
[slack-dev]:https://oneops.slack.com/messages/devel/messages
[oo-commons]:../../../oo-commons
[cms-dal]:../../../cmsdal
[oneops ui]:../../../display
[setup]:../../../setup
[client]:../../../cli
[contributing]:https://github.com/oneops/developer-doc/blob/master/_contribution/index.md
[tomcat environment variables]:../../../dev-tools/blob/master/setup-scripts/tom_setenv.sh
[search-configuration]:../../../dev-tools/blob/master/setup-scripts/start-consumer.sh
[sink metadata]:../../../oneops-admin/blob/master/lib/base/sink/metadata.rb
[notification sinks]: /src/main/java/com/oneops/antenna/senders/generic
