name             "ForwardsTo"
description      "Links to notification sinks"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

# relation targets
[ 'account.Organization' ].each do |source|
  attribute "#{source}-base.ForwardsTo-account.notification.sns.Sink",
    :relation_target => true,
    :package => 'base',
    :from_class => "#{source}",
    :to_class => 'account.notification.sns.Sink',
    :link_type => 'one-to-many',
    :required => false
end

[ 'account.Organization' ].each do |source|
  attribute "#{source}-base.ForwardsTo-account.notification.url.Sink",
    :relation_target => true,
    :package => 'base',
    :from_class => "#{source}",
    :to_class => 'account.notification.url.Sink',
    :link_type => 'one-to-many',
    :required => false
end