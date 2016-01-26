name             "Procedure"
description      "Procedure"
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
    :help => 'Enter description for this procedure',
    :category => '1.Global',
    :order => 1,
    :form => { 'field' => 'textarea' }
  }

attribute 'arguments',
  :description => "Input Arguments",
  :data_type => "hash",
  :default => "{}",
  :format => {
    :help => 'Specify input arguments to be passed to the procedure flow',
    :category => '1.Global',
    :order => 2
  }
  
attribute 'definition',
  :description => "Procedure Flow",
  :data_type => "struct",
  :format => {
    :help => 'Define custom data structure to describe the procedure flow (Note: see developer documentation for creating procedures)',
    :category => '1.Global',
    :order => 3
  }

