name 'Security'
description 'Cloud Security Compliance'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :packages  => %w(base),
         :namespace => false

grouping 'cloud',
         :access    => 'global',
         :packages  => %w(cloud.compliance),
         :namespace => false

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Security description',
            :category => '1.General',
            :order    => 1
          }

attribute 'enabled',
          :description => 'Enable',
          :required    => 'required',
          :default     => 'true',
          :format      => {
            :help     => 'Enable/disable compliance',
            :category => '1.General',
            :form     => {:field => 'checkbox'},
            :order    => 2
          }

attribute 'approval',
          :description => 'Deployment Approval',
          :required    => 'required',
          :default     => 'false',
          :format      => {
            :help     => 'When selected requires deployment approval',
            :category => '2.Deployment',
            :form     => {:field => 'checkbox'},
            :order    => 1
          }

attribute 'version',
          :description => 'Version',
          :required    => 'required',
          :default     => '1',
          :format      => {
            :help     => 'Current version of this security compliance.',
            :category => '3.Execution',
            :order    => 1
          }

attribute 'asset_url',
          :description => 'Script URL',
          :required    => 'required',
          :format      => {
            :help     => 'URL to download scripts implementing this security compliance.',
            :category => '3.Execution',
            :order    => 2
          }

attribute 'filter',
          :description => 'Filter',
          :required    => 'required',
          :format      => {
            :help     => 'Expression to select components that are subject to applying this security compliance.',
            :category => '3.Execution',
            :order    => 3,
            :form     => {:field => 'textarea'},
          }
