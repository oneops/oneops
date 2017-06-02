name 'Version'
description 'Management pack version'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :namespace => true,
         :packages  => ['base', 'mgmt']

attribute 'enabled',
          :description => 'Enable Version',
          :default => 'true',
          :format => {
              :help => 'Enable/disable pack version.',
              :category => '1.Global',
              :form => {:field => 'checkbox'},
              :order => 1
          }

attribute 'description',
          :description => 'Description',
          :format      => {
            :help     => 'Enter description for the management pack version',
            :category => '1.Global',
            :order    => 2,
            :form     => {:field => 'textarea'}
          }

attribute 'commit',
          :description => 'Commit',
          :default     => '',
          :format      => {
            :help     => 'Internal commit ID for the version build',
            :category => '1.Global',
            :order    => 3
          }

# This is temporary until we migrate this digest to the mgmt.Pack for live data.  Will remove right after.
attribute 'admin_password_digest',
          :description => 'Admin Password Digest',
          :default     => '',
          :format      => {
            :help     => 'Key to be entered while doing pack admin changes',
            :filter   => {'all' => {'visible' => 'false'}},
            :category => '1.Global',
            :order    => 4
          }
