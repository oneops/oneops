name 'Organization'
description 'Organization'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :packages  => %w(base account),
         :namespace => true

attribute 'full_name',
          :description => 'Full Name',
          :default     => '',
          :format      => {
            :help     => 'Organization long name',
            :category => '1.Information',
            :order    => 1,
            :pattern  => '(.*){1,100}'
          }

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Additional information about the organization',
            :category => '1.Information',
            :order    => 2,
            :form     => {:field => 'textarea'}
          }

attribute 'owner',
          :description => 'Owner Email',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Set the email address of the admin/owner for this organization',
            :category => '1.Information',
            :order    => 3,
            :pattern  => '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$'
          }

attribute 'tags',
          :description => 'Miscellaneous Tags',
          :data_type   => 'hash',
          :default     => '{}',
          :format      => {
            :help     => 'Various option key/value pairs to tag organization.',
            :category => '1.Information',
            :order    => 4
          }
