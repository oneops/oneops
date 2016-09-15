name 'PlacedIn'
description 'Relation bom instance to cloud zone.'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :relation => true,
         :packages => ['base']

attribute 'Component-base.PlacedIn-cloud.Zone',
          :relation_target => true,
          :package         => 'base',
          :from_class      => 'Component',
          :to_class        => 'cloud.Zone',
          :link_type       => 'many-to-one',
          :required        => false
