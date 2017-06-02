name             "RealizedIn"
description      "Relation between assembly and environment"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
[ 'manifest' ].each do |class_package|
  attribute "account.Assembly-base.RealizedIn-#{class_package}.Environment",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Assembly',
    :to_class => [class_package,'Environment'].join('.'),
    :link_type => 'one-to-many',
    :required => true
end

recipe "realized_in::default", "RealizedIn default recipe"


