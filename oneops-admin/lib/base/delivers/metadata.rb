name             'Delivers'
description      'Links environment to notification relays'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          '0.1'
maintainer       'OneOps'
maintainer_email 'support@oneops.com'
license          'Copyright OneOps, All rights reserved.'

grouping 'default',
         :relation => true,
         :packages => %w(base account manifest)

%w(account manifest).each do |package|
  %w(email).each do |relay_type|
    attribute "#{package}.Environment-#{package}.Delivers-#{package}.relay.#{relay_type}.Relay",
              :relation_target => true,
              :package         => 'base',
              :from_class      => "#{package}.Environment",
              :to_class        => "#{package}.relay.#{relay_type}.Relay",
              :link_type       => 'one-to-many',
              :required        => false
  end
end
