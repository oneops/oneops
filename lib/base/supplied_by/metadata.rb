name             "SuppliedBy"
description      "Organization supplied by provider token"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
=begin
[ "ec2" ].each do |package|
  attribute "account.Organization-base.SuppliedBy-account.provider.#{package}.Token",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Organization',
    :to_class => "account.provider.#{package}.Token",
    :link_type => 'one-to-many',
    :required => false
end
=end
recipe "supplied_by::default", "SuppliedBy default recipe"


