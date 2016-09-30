name 'Zone'
description 'Cloud Zone'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :namespace => true,
         :packages  => %w(base cloud account.provider)

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Description',
            :category => '1.General',
            :order    => 1
          }

attribute 'tags',
          :description => 'Tags',
          :data_type   => 'array',
          :default     => '[]',
          :format      => {
            :help     => 'Various values to tag cloud zone.',
            :category => '1.General',
            :order    => 2
          }
