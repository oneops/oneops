name             "Exposes"
description      "Provider token exposes provider"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
[ 'ec2', 'rackspace','cloudstack', 'openstack' ].each do |token_package|
  attribute "account.provider.#{token_package}.Token-base.Exposes-account.Provider",
    :relation_target => true,
    :package => 'base',
    :from_class => "account.provider.#{token_package}.Token",
    :to_class => 'account.Provider',
    :link_type => 'one-to-one',
    :required => false
end



