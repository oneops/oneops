# OneOps

OneOps is a multi-cloud Platform as a Service tool sponsored by
WalmartLabs. Find out more at [http://oneops.com](http://oneops.com).

This repository contains the main codebase for all components.

## Building

Requirements for building include JDK 8, Maven, Ruby, Bundler and a number of
other tools.

A simple buld can be performed with


```
mvn clean install
```

A running system on a VirtualBox system can be created with Vagrant and the
command

```
mvn clean install -P vagrant
```

Further details can be found in the
[core development section on our website](http://oneops.com/developer/core-development/index.html).
