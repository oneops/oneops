name 'Policy'
description 'Policy'
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version '0.1'
maintainer 'OneOps'
maintainer_email 'support@oneops.com'
license 'Copyright OneOps, All rights reserved.'

grouping 'default',
         :access    => 'global',
         :packages  => %w(base mgmt.catalog mgmt.manifest account),
         :namespace => true


attribute 'description',
          :description => 'Description',
          :format      => {
            :help     => 'Enter description for this policy.',
            :category => '1.General',
            :order    => 1,
            :form     => {'field' => 'textarea'}
          }

attribute 'query',
          :description => 'Query',
          :required    => 'required',
          :format      => {
            :help     => 'Enter definition of the policy.',
            :category => '2.Definition',
            :order    => 1,
            :form     => {:field => 'textarea'}
          }

attribute 'mode',
          :description => 'Execution mode',
          :required    => 'required',
          :default     => 'passive',
          :format      => {
            :category => '2.Definition',
            :order    => 2,
            :help     => 'Choose execution mode.',
            :form     => {'field' => 'select', 'options_for_select' => [['Disabled (do not execute)', 'disabled'],
                                                                        ['Passive (execute on view)', 'passive'],
                                                                        ['Active (execute on save and view)', 'active']]
            }
          }

attribute 'docUrl',
          :description => 'URL to a page having policy information including resolution steps.',
          :format      => {
            :category => '3.Documentation',
            :order    => 1,
            :help     => 'Provide a url link to policy detailed documentation page.',
            :pattern  => '(http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.{0,500})?'
          }
