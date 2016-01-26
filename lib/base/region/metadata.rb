name             "Region"
description      "Provider Region"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'account.provider' ]

attribute 'endpoint',
  :description => "Endpoint",
  :default => "",
  :format => {
    :help => 'Provider service endpoint URL for API calls',
    :category => '1.Global',
    :order => 1
  }
  
attribute 'sizemap',
  :description => "Sizes Map",
  :data_type => "hash",
  :default => "",
  :format => {
    :help => 'Map of generic compute sizes to provider specific',
    :category => '2.Mappings',
    :order => 1
  } 

attribute 'archmap',
  :description => "Architectures Map",
  :data_type => "hash",
  :default => "",
  :format => {
    :help => 'Map of OS architectures to each size',
    :category => '2.Mappings',
    :order => 2
  } 
 
attribute 'imagemap32',
  :description => "32-bit Images Map",
  :data_type => "hash",  
  :default => "",
  :format => {
    :help => 'Map of generic OS image types to provider specific 32-bit OS image types',
    :category => '2.Mappings',
    :order => 3
  } 

attribute 'imagemap64',
  :description => "64-bit Images Map",
  :data_type => "hash",
  :default => "",
  :format => {
    :help => 'Map of generic OS image types to provider specific 64-bit OS image types',
    :category => '2.Mappings',
    :order => 3
  }

