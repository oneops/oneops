name             "Manages"
description      "Relation between organization and assembly"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation attributes

# relation targets
attribute "account.Organization-base.Manages-account.Assembly",
  :package => 'base',
  :relation_target => true,
  :from_class => 'account.Organization',
  :to_class => 'account.Assembly',
  :link_type => 'one-to-many',
  :required => true
  
attribute "account.Organization-base.Manages-account.Design",
  :package => 'base',
  :relation_target => true,
  :from_class => 'account.Organization',
  :to_class => 'account.Design',
  :link_type => 'one-to-many',
  :required => true

