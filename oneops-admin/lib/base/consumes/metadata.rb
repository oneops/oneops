name 'Consumes'
description 'Relation to indicate cloud usage'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :relation => true,
         :packages => ['base']

attribute 'dpmt_order',
          :description => 'Deployment order',
          :default     => '1',
          :format      => {
            :help     => 'Indicates the deployment order of the cloud',
            :category => '1.Global',
            :order    => 1,
            :pattern  => '-?\d+'
          }

attribute 'pct_scale',
          :description => 'Percent scale',
          :default     => '100',
          :format      => {
            :help     => 'Percent of target scale for this cloud',
            :category => '1.Global',
            :order    => 1,
            :pattern  => '(([1-9]\d*)|(\d+\.\d+))'
          }

attribute 'priority',
          :description => 'Priority',
          :default     => '',
          :format      => {
            :help     => 'Indicates the priority of the cloud',
            :category => '1.Global',
            :order    => 1
          }

attribute 'adminstatus',
          :description => 'admin status',
          :default     => 'active',
          :format      => {
            :help     => 'Indicates admin status of the cloud',
            :category => '1.Global',
            :order    => 1
          }

# relation targets
attribute 'account.Environment-base.Consumes-account.Cloud',
          :relation_target => true,
          :package         => 'base',
          :from_class      => 'account.Environment',
          :to_class        => 'account.Cloud',
          :link_type       => 'one-to-many',
          :required        => false

attribute 'manifest.Environment-base.Consumes-account.Cloud',
          :relation_target => true,
          :package         => 'base',
          :from_class      => 'manifest.Environment',
          :to_class        => 'account.Cloud',
          :link_type       => 'one-to-many',
          :required        => false

attribute 'manifest.Platform-base.Consumes-account.Cloud',
          :relation_target => true,
          :package         => 'base',
          :from_class      => 'manifest.Platform',
          :to_class        => 'account.Cloud',
          :link_type       => 'one-to-many',
          :required        => false


