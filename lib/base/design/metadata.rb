name             "Design"
description      "Design"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'account' ],
  :namespace => true

attribute 'description',
  :description => "Description",
  :default => "",
  :format => {
    :help => 'Design description',
    :category => '1.Properties',
    :order => 1
  }
