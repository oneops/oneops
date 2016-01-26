name 'Offering'
description 'Cloud Service Offering'
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
         :packages  => %w(cloud mgmt.cloud),
         :namespace => false

attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Offering description',
            :category => '1.Description',
            :order    => 1,
            :form     => {:field => 'textarea'}
          }

attribute 'cost_unit',
          :description => 'Cost unit',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Cost unit',
            :category => '2.Pricing',
            :order    => 1
          }

attribute 'cost_rate',
          :description => 'Cost rate per hour',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Cost rate should be normalized per hour.',
            :category => '2.Pricing',
            :order    => 2
          }


attribute 'criteria',
          :description => 'Matching criteria',
          :default     => '',
          :required    => 'required',
          :format      => {
            :help     => 'Expression used to match offering against demand.',
            :category => '3.Matching',
            :order    => 1
          }

attribute 'specification',
          :description => 'Resource specification',
          :data_type   => 'hash',
          :default     => '{}',
          :required    => 'required',
          :format      => {
            :help     => 'Specification details for any resource created by this offering.',
            :category => '4.Specification',
            :order    => 2
          }
