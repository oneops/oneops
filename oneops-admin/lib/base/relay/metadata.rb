name             'Relay'
description      'Notification Relay'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          '0.1'
maintainer       'OneOps'
maintainer_email 'support@oneops.com'
license          'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :packages  => %w(base),
         :namespace => false

grouping 'email',
         :access    => 'global',
         :packages  => %w(account.relay.email manifest.relay.email),
         :namespace => false


attribute 'enabled',
          :description => 'Enable Delivery',
          :default => 'true',
          :format => {
              :help => 'Enable/disable notification delivery for this relay.',
              :category => '1.Delivery',
              :form => {:field => 'checkbox'},
              :order => 1
          }

attribute 'severity',
          :description => 'Severity Filter',
          :required    => 'required',
          :default     => 'info,warning,critical',
          :format      => {
            :category => '2.Filtering',
            :order    => 1,
            :help     => 'Allows to filter notifications by severity.',
            :form     => {:field              => 'checkbox',
                          :multiple           => 'true',
                          :options_for_select => [['Info', 'info'],
                                                  ['Warning', 'warning'],
                                                  ['Critical', 'critical']]
            }
          }

attribute 'source',
          :description => 'Source Filter',
          :required    => 'required',
          :default     => 'deployment,procedure,ci',
          :format      => {
            :category => '2.Filtering',
            :order    => 2,
            :help     => 'Allows to filter notifications by source.',
            :form     => {:field              => 'checkbox',
                          :multiple           => 'true',
                          :options_for_select => [['Deployment', 'deployment'],
                                                  ['Procedure', 'procedure'],
                                                  ['Monitor', 'ci']]
            }
          }

attribute 'ns_paths',
          :description => 'NS Paths',
          :data_type   => 'Array',
          :default     => '[]',
          :format      => {
            :help     => 'A list of NS paths to filter notifications.  Leave empty if no NS path filtering is needed.',
            :category => '2.Filtering',
            :order    => 3
          }

attribute 'text_regex',
          :description => 'Message Pattern',
          :default     => '',
          :format      => {
            :category => '2.Filtering',
            :order    => 4,
            :help     => 'Regexp to match against subject or text of notification. If blank notifications are not filtered based on subject or text content.'
          }

attribute 'correlation',
          :description => 'Component Correlation',
          :default     => 'true',
          :format      => {
            :category => '2.Filtering',
            :order    => 5,
            :tip      => 'NOTE: Enabling component correlation will relay operations notifications for instances only if it affects the associated component health.',
            :help     => 'Enabling component correlation will relay operations notifications for instances only if it affects the associated component health.',
            :form => {:field => 'checkbox'}
          }

attribute 'emails',
          :grouping    => 'email',
          :description => 'Destination Emails',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Comma separated list of target email addresses for notification delivery.',
            :category => '1.Delivery',
            :order    => 2,
            :pattern  => '[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}(,[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4})*'
          }

