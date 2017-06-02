name             "BasedOn"
description      "Relation between account environment profile and manifest environment"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]
  
# relation targets
attribute "manifest.Environment-base.BasedOn-account.Environment",
    :relation_target => true,
    :package => 'base',
    :from_class => 'manifest.Environment',
    :to_class => 'account.Environment',
    :link_type => 'one-to-one',
    :required => false


