name             "Source"
description      "Management source"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :namespace => true,
  :packages => [ 'base', 'mgmt' ]
  
attribute 'description',
  :description => "Description",
  :default => "",
  :format => {
    :help => "Management source description",
    :category => '1.Global',
    :order => 1
  }

