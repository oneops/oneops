name 'Cloudvar'
description 'Cloud Variables'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access   => 'global',
         :packages => %w(base account)

attribute 'secure',
          :description => 'Secure variable',
          :default     => 'false',
          :format      => {
            :category => 'Value',
            :order    => 1,
            :tip      => 'NOTE: Make sure to always re-enter the variable value when changing this attribute.',
            :help     => 'Secure variable values are encrypted on save and stored in encrypted format.',
            :form     => {:field => 'checkbox'}
          }

attribute 'value',
          :description => 'Value',
          :default     => '',
          :format      => {
            :category => 'Value',
            :pattern  => '\S(.*\S)?',
            :order    => 2,
            :help     => 'Enter the variable value',
            :form     => {:field => 'textarea'},
            :filter   => {'all' => {'visible' => 'secure:neq:true', 'editable' => 'secure:neq:true'}}
          }

attribute 'encrypted_value',
          :description => 'Encrypted value',
          :encrypted   => true,
          :default     => '',
          :format      => {
            :category => 'Value',
            :pattern  => '\S(.*\S)?',
            :order    => 3,
            :help     => 'Enter the variable value. The provided value will be encrypted on save and stored securely.',
            :filter   => {'all' => {'visible' => 'secure:eq:true', 'editable' => 'secure:eq:true'}}
          }
