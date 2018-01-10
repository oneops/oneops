name "Cloud"
description "Collection of Cloud Services"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version "0.1"
maintainer "OneOps"
maintainer_email "support@oneops.com"
license "Copyright OneOps, All rights reserved."

grouping 'default',
         :access    => 'global',
         :namespace => true,
         :packages  => %w(base)

grouping 'mgmt',
         :access    => 'global',
         :namespace => true,
         :packages  => %w(mgmt)

grouping 'account',
         :access    => 'global',
         :namespace => true,
         :packages  => %w(account)


attribute 'description',
          :description => 'Description',
          :default     => '',
          :format      => {
            :help     => 'Description',
            :category => '1.Global',
            :order    => 1
          }

attribute 'adminstatus',
          :description => 'Status',
          :grouping    => 'account',
          :default     => 'active',
          :format      => {
            :important => true,
            :help      => "Indicates admin status of the cloud. Possible values: 'active' - normal operations, 'inert' - being phazed out and may not be added to environments, 'offine' - decomissioned and should not be used.",
            :category  => '1.Global',
            :order     => 2,
            :form      => {:field              => 'select',
                           :options_for_select => [['Active (normal operations)', 'active'],
                                                   ['Inert (being phased out)', 'inert'],
                                                   ['Offline (decommissioned)', 'offline']]},
            :filter    => {:all => {:visible => 'false'}}
          }

attribute 'location',
          :description => 'Location',
          :grouping    => 'account',
          :data_type   => 'struct',
          :default     => '',
          :required    => 'required',
          :format      => {
            :important => true,
            :editable  => false,
            :help      => 'Management cloud location path for delivering workorders',
            :category  => '2.Management',
            :order     => 1
          }

attribute 'auth',
          :description => 'Authorization Key',
          :data_type   => 'struct',
          :default     => '',
          :format      => {
            :help     => 'Authorization key for the specified location path',
            :category => '2.Management',
            :order    => 2
          }

attribute 'is_location',
          :description => 'Location flag',
          :grouping    => 'mgmt',
          :default     => 'false',
          :format      => {
            :help     => 'If set to true it will show up in locations drop down',
            :category => '2.Management',
            :order    => 3
          }
