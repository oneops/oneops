name             "Iaas"
description      "Infrastructure as a service"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base' ]

grouping 'catalog',
  :access => "global",
  :packages => [ 'mgmt.catalog', 'catalog' ]

grouping 'manifest',
  :access => "global",
  :packages => [ 'mgmt.manifest', 'manifest' ]
  
attribute 'description',
  :description => "Description",
  :default => "",
  :format => {
    :help => 'Description',
    :category => '1.Global',
    :order => 1
  }

attribute 'source',
  :description => "Pack Source",
  :default => "",
  :format => {
    :help => 'Pack source name',
    :category => '2.Platform Pack',
    :order => 1
  }
  
attribute 'pack',
  :description => "Pack Name",
  :default => "",
  :format => {
    :help => 'Pack name',
    :category => '2.Platform Pack',
    :order => 2
  }
  
attribute 'version',
  :description => "Pack Version",
  :default => "",
  :format => {
    :help => 'Pack version',
    :category => '2.Platform Pack',
    :order => 3
  }

attribute 'services',
  :description => "Pack Services",
  :data_type => "array",
  :required => "required",
  :default => "[]",
  :format => {
    :help => 'Pack services',
    :category => '2.Platform Pack',
    :order => 4
  }
    