name 'SupportedBy'
description 'Links cloud to support'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :relation => true,
         :packages => %w(base)

attribute 'account.Cloud-cloud.SupportedBy-cloud.Support',
          :relation_target => true,
          :package         => 'base',
          :from_class      => 'account.Cloud',
          :to_class        => 'cloud.Support',
          :link_type       => 'one-to-many',
          :required        => false
