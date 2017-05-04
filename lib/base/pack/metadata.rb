name 'Pack'
description 'Management pack'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :namespace => true,
         :packages  => %w(base mgmt)

attribute 'pack_type',
          :description => 'Pack Type',
          :default     => 'Platform',
          :format      => {
            :help     => 'Specify pack type',
            :category => '1.Global',
            :order    => 2
          }

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Pack description',
            :category => '1.Global',
            :order    => 1
          }

attribute 'owner',
          :description => 'Owner',
          :default     => '',
          :format      => {
            :help     => 'Pack owner',
            :category => '1.Global',
            :order    => 3
          }


attribute 'category',
          :description => 'Category',
          :default     => 'Other',
          :format      => {
            :help     => 'Specify pack category for proper grouping',
            :category => '1.Global',
            :order    => 4
          }


attribute 'admin_password_digest',
          :description => 'Admin Password Digest',
          :default     => '',
          :format      => {
            :help     => 'Key to be entered while doing pack admin changes',
            :filter   => {'all' => {'visible' => 'false'}},
            :category => '1.Global',
            :order    => 4
          }

