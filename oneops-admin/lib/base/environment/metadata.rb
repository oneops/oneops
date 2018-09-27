name             "Environment"
description      "Environment"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base' ],
  :namespace => true

grouping 'account',
  :access => "global",
  :packages => [ 'account' ],
  :namespace => true

grouping 'manifest',
  :access => "global",
  :packages => [ 'manifest' ],
  :namespace => true


attribute 'profile',
  :grouping => 'manifest',
  :description => "Profile",
  :data_type => 'struct',
  :default => "",
  :format => {
    :help => 'Organization environment profile used for this environment',
    :category => '1.Global',
    :order => 1,
    :filter => {"new" => {"visible" => "availability:eq:none"}}
  }

attribute 'adminstatus',
  :grouping => 'manifest',
  :description => "Administrative Status",
  :required => "required",
  :default => "active",
  :format => {
    :category => '1.Global',
    :order => 2,
    :help => 'Select administrative status',
    :form => { 'field' => 'select', 'options_for_select' => [ ['Provision','provision'],
                                                              ['Active','active'],
                                                              ['Inactive','inactive'],
                                                              ['Decommission','decommission'] ]
    }
  }

attribute 'description',
  :description => "Description",
  :format => {
    :help => 'Enter description for this environment',
    :category => '1.Global',
    :order => 3,
    :form => { 'field' => 'textarea' }
  }


# deployment
attribute 'codpmt',
  :description => "Continuous Deployment",
  :default => 'false',
  :required => "required",
  :format => {
    :category => '2. Deployment',
    :order => 1,
    :help => 'Enable continuous deployment by using webhooks (for ex. integration with Project and SCM tools) or our API to update components and let our system automatically deploy those updates',
    :form => { 'field' => 'checkbox' },
    :filter => {:all => {:visible => 'false'}}
  }

attribute 'dpmtdelay',
  :description => "Continuous Deployment Delay",
  :default => '60',
  :format => {
    :category => '2. Deployment',
    :order => 2,
    :help => 'Delay in seconds for an automated deployment to start after an update was received and a new release was created',
    :pattern => "[0-9]+",
    :filter => {:all => {:visible => 'false'}}
  }


# dns
attribute 'subdomain',
  :description => "DNS Subdomain",
  :data_type => 'struct',
  :default => "",
  :format => {
    :help => 'Modify the full subdomain name to ensure uniqueness of the platform entrypoint names in your environment',
    :category => '3.DNS',
    :order => 1,
    :pattern => "[a-zA-Z0-9\-]+(\.[a-zA-Z0-9\-]+)",
    :editable => false
  }

attribute 'global_dns',
  :description => "Global DNS",
  :default => 'false',
  :format => {
    :help => 'Create global DNS names when using multiple clouds',
    :category => '3.DNS',
    :order => 2,
    :form => { 'field' => 'checkbox' }
  }


# availability
attribute 'monitoring',
  :description => "Monitoring",
  :required => "required",
  :default => "true",
  :format => {
    :category => '4.Availability',
    :order => 1,
    :help => 'This disables monitoring and prevents deployment of the monitoring agent software (nagios and flume) on each compute instance (Note: autorepair and autoscale will not function properly without monitoring enabled)',
    :editable => false,
    :form => { 'field' => 'checkbox' },
    :filter => {"all" => {"visible" => "availability:eq:none"}}
  }

attribute 'autorepair',
  :description => "Auto Repair",
  :required => "required",
  :default => "true",
  :format => {
    :category => '4.Availability',
    :order => 2,
    :filter => {'all' => {'visible' => 'false'}},
    :help => 'Autorepair enables automatic repair of component instances based on monitors with enabled heartbeats and metrics you define with Unhealthy event triggers (Note: repairs are executed by invoking and action with the name <em>repair</em> for each component)',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autoscale',
  :description => "Auto Scale",
  :required => "required",
  :default => "false",
  :format => {
    :category => '4.Availability',
    :order => 3,
    :filter => {'all' => {'visible' => 'false'}},
    :tip => 'NOTE: Autoscale will apply only to platforms running in redundant mode in this environment.',
    :help => 'Autoscales enables scaling up and down based on metrics you define with Over-Utilized and Under-Utilized event triggers (Note: the scale step up and down along with the limits and metrics can be customized on a platform level after saving the environment)',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autoreplace',
  :description => "Auto Replace",
  :required => "required",
  :default => "false",
  :format => {
    :category => '4.Availability',
    :order => 4,
    :tip => 'NOTE: Autoreplace works only if the auto-repair is ON for this environment. You also need to set valid values for the replace related 2 attributes in your platform configuration in order to enable auto-replace for that particular platform',
    :filter => {'all' => {'visible' => 'false'}},
    :help => 'Replaces an unhealthy component  after some duration if the repair action can not recover the component',
    :form => { 'field' => 'checkbox' }
  }

attribute 'availability',
  :description => "Availability Mode",
  :required => "required",
  :default => "single",
  :format => {
    :category => '4.Availability',
    :order => 5,
    :editable => false,
    :help => 'The Availability Mode <b>Single</b> will generate an environment without loadbalancers, clusters, etc.
 <b>Redundant</b> will insert and configure clusters, loadbalancers, rings, etc depending on whats best practice for each platform.
 <b>High-Availability</b> will add multi-provider or multi-region to a redundant environment.
 You can change availability mode on a per-platform basis below.',
    :form => { 'field' => 'select', 'options_for_select' => [ ['Single','single'],
                                                              ['Redundant','redundant'] ]
    }
  }

attribute 'debug',
  :description => "Debug Mode",
  :required => "required",
  :default => "false",
  :format => {
    :category => '5.Other',
    :order => 1,
    :help => 'For developers troubleshooting',
    :form => { 'field' => 'checkbox' },
    :filter => {:all => {:visible => 'false'}}
  }

attribute 'logging',
  :description => "Enable Logging",
  :required => "required",
  :default => "false",
  :format => {
    :category => '5.Other',
    :order => 2,
    :help => 'To enable logging',
    :form => { 'field' => 'checkbox' },
    :filter => {"all" => {"visible" => "availability:eq:none"}}
  }

attribute 'verify',
  :description => "Run KitchenCI verification",
  :required => "required",
  :default => "default",
  :format => {
    :category => '5.Other',
    :order => 3,
    :help => 'For running integrated KitchenCI tests',
    :form => { 'field' => 'select', 'options_for_select' => [ ['Default','default'],
                                                              ['False','false'],
                                                              ['True','true'] ]
             },
    :filter => {:all => {:visible => 'false'}}
  }
