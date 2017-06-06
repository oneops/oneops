maintainer       "Dan Crosta"
maintainer_email "dcrosta@late.am"
license          "BSD"
description      "Simple LWRP and recipe for managing iptables rules"
version          "0.3.0"
name             "simple_iptables"

supports "debian", ">= 6.0"
supports "centos", ">= 5.8"
supports "redhat", ">= 5.8"
supports "ubuntu", ">= 10.04"

# needed for model sync
grouping 'default',
  :access => "global",
  :packages => [ 'base', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest' ]