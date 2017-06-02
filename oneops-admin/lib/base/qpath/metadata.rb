name             "Qpath"
description      "Query Path"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'mgmt.manifest' ]


attribute 'description',
  :description => "Description",
  :format => {
    :help => 'Enter description for this environment',
    :category => '1.Global',
    :order => 1,
    :form => { 'field' => 'textarea' }
  }

  
attribute 'definition',
  :description => "Query Path",
  :data_type => "struct",
  :required => "required",
  :default => "{}",
  :format => {
    :help => 'Define custom data structure to describe the query path (Note: see developer documentation for creating query paths)',
    :category => '1.Global',
    :order => 2
  }


