name "Assembly"
description "Assembly"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version "0.1"
maintainer "OneOps"
maintainer_email "support@oneops.com"
license "Copyright OneOps, All rights reserved."

grouping 'default',
         :access    => "global",
         :packages  => ['base', 'mgmt', 'account'],
         :namespace => true

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Assembly description',
            :category => '1.Configuration',
            :order    => 1
          }

attribute 'owner',
          :description => 'Owner (Email Address)',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Set the email address of the owner of this assembly',
            :category => '1.Configuration',
            :order    => 2,
            :pattern  => '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$'
          }

attribute 'tags',
          :description => 'Miscellaneous Tags',
          :data_type   => 'hash',
          :default     => '{}',
          :format      => {
            :help     => 'Various option key/value pairs to tag assembly.',
            :category => '1.Configuration',
            :order    => 3
          }
