name             "BindsTo"
description      "Provider binding binds to provider zone"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
attribute "account.provider.Binding-base.BindsTo-account.provider.Zone",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.provider.Binding',
    :to_class => 'account.provider.Zone',
    :link_type => 'one-to-one',
    :required => false

