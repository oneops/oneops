name             "Mode"
description      "Management environment mode"
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
    :category => '1.Global',
    :help => 'Service level packaging namespace to capture a bundle of pack resources for a given mode',
    :order => 1
  }

