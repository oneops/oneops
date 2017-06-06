name             "Utilizes"
description      "Assembly environment utilizes provider binding"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

attribute 'priority',
  :display_name => "Priority",
  :description => "Priority",
  :type => "string",
  :required => "required",
  :recipes => [ 'utilizes::default' ],
  :default => "1",
  :format => ""

attribute 'services',
  :display_name => "Services",
  :description => "Services",
  :type => "string",
  :data_type => "hash",
  :required => "optional",
  :recipes => [ 'utilizes::default' ],
  :default => "{ 'lb':'' }",
  :format => ""
  
# relation targets
attribute "manifest.Environment-base.Utilizes-account.provider.Binding",
    :relation_target => true,
    :package => 'base',
    :from_class => 'manifest.Environment',
    :to_class => 'account.provider.Binding',
    :link_type => 'one-to-many',
    :required => false

attribute "manifest.Iaas-base.Utilizes-account.provider.Binding",
    :relation_target => true,
    :package => 'base',
    :from_class => 'manifest.Iaas',
    :to_class => 'account.provider.Binding',
    :link_type => 'one-to-one',
    :required => false
    
recipe "utilizes::default", "Utilizes default recipe"


