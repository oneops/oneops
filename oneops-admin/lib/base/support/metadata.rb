name 'Support'
description 'Cloud Support'
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
         :packages  => %w(cloud),
         :namespace => false

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Support description',
            :category => '1.Configuration',
            :order    => 1
          }

attribute 'enabled',
          :description => 'Enable',
          :default     => 'true',
          :format      => {
            :help     => 'Enable/disable support',
            :category => '2.State',
            :form     => {:field => 'checkbox'},
            :order    => 1
          }

attribute 'approval',
          :description => 'Require Approval',
          :required    => 'required',
          :default     => 'true',
          :format      => {
            :help     => 'When selected requires deployment approval',
            :category => '3.Deployment',
            :form     => {:field => 'checkbox'},
            :order    => 1
          }

attribute 'approval_auth_type',
          :description => 'Approval Auth',
          :required    => 'required',
          :default     => 'none',
          :format => {
              :help     => 'Security scheme for approval settlement requests.',
              :category => '3.Deployment',
              :order    => 2,
              :form     => {'field'              => 'select',
                            'options_for_select' => [['None', 'none'],
                                                     ['Token', 'token']]
              }
          }

attribute 'approval_token',
          :description => 'Approval Token',
          :encrypted => true,
          :format => {
              :help => 'Approval settlement requests must pass this token.',
              :category => '3.Deployment',
              :order    => 3,
              :filter   => {'all' => {'visible' => 'approval_auth_type:eq:token'}},
          }

