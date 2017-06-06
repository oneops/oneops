name             "Authenticates"
description      "Provider token authenticates provider binding"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
[ 'ec2','rackspace','openstack','cloudstack'].each do |token_package|
  attribute "account.provider.#{token_package}.Token-base.Authenticates-account.provider.Binding",
    :relation_target => true,
    :package => 'base',
    :from_class => "account.provider.#{token_package}.Token",
    :to_class => 'account.provider.Binding',
    :link_type => 'one-to-many',
    :required => true
end



