name             "Monitor"
description      "Monitor"
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest' ]

attribute 'enable',
  :description => "Monitor Status",
  :required => "required",
  :default => "true",
  :format => {
    :help => 'Enable and Disable of Monitor',
    :category => '1.Status',
    :order => 1,
    :form => { 'field' => 'checkbox' },
    :filter => {"all" => {"visible" => "false", "editable" => "false"}}
  }

attribute 'custom',
  :description => "Custom",
  :required => "required",
  :default => "false",
  :format => {
    :help => 'Is monitor of type custom?',
    :category => '1.Status',
    :order => 2,
    :form => { 'field' => 'checkbox' },
    :filter => {"all" => {"visible" => "duration:eq:none"}}
  }
  
attribute 'description',
  :description => "Description",
  :format => {
    :help => 'Enter monitor description',
    :category => '2.Global',
    :order => 1,
    :form => { 'field' => 'textarea' },
    :filter => {"all" => {"editable" => "custom:eq:true"}}
  }
  
attribute 'cmd',
  :description => "Command",
  :required => "required",
  :default => "",
  :format => {
    :help => 'Command to be used to gather the defined metrics (Note: you can use one of the standard nagios checks)',
    :category => '3.Collection',
    :order => 1,
    :filter => {"all" => {"editable" => "custom:eq:true"}}
  }


attribute 'cmd_options',
  :description => "Command Line Options Map",
  :required => "required",
  :data_type => "hash",
  :default => "{}",
  :format => {
    :help => 'Command line options that will substituted',
    :category => '3.Collection',
    :order => 2,
    :fixed_keys => 'true'
  }

attribute 'cmd_line',
  :description => "Command Line",
  :required => "required",
  :default => "",
  :format => {
    :help => 'Command line to be execute to gather the defined metrics (Note: output format must be compliant with nagios checks)',
    :category => '3.Collection',
    :order => 3,
    :filter => {"all" => {"editable" => "custom:eq:true"}}
  }


attribute 'metrics',
  :description => "Metrics",
  :data_type => "struct",
  :default => "{}",
  :format => {
    :help => 'Data structure that holds the definition of the metrics associated with this monitor',
    :category => '3.Collection',
    :order => 4,
    :filter => {"all" => {"editable" => "custom:eq:true"}}
  }
  
attribute 'sample_interval',
  :description => "Sample Interval (in sec)",
  :default => "60",
  :format => {
    :help => 'Sample interval - how often it runs in seconds. Smaller buckets within the sample interval will be populated.',
    :category => '3.Collection',
    :order => 5
  }  

attribute 'heartbeat',
  :description => "Heartbeat",
  :default => 'false',
  :format => {
    :help => 'Enabling the heartbeat will cause a repair action to be invoked when the heartbeat is missing',
    :category => '4.Alerting',
    :order => 1,
    :form => { 'field' => 'checkbox' }
  }

attribute 'duration',
  :description => "Heartbeat Duration",
  :default => "3",
  :format => {
    :help => 'Heartbeat duration in minutes is the amount of time the system will allow for a missing heartbeat before triggering a repair action',
    :category => '4.Alerting',
    :order => 2
  }
  
attribute 'thresholds',
  :description => "Thresholds",
  :data_type => "struct",
  :default => "{}",
  :format => {
    :help => 'Data structure that holds the metrics threshold definitions',
    :category => '4.Alerting',
    :order => 3
  }

attribute 'chart',
  :description => "Chart",
  :data_type => "struct",
  :default => "{}",
  :format => {
    :help => 'Data structure that holds the chart parameters (Note: only used for charts in the UI)',
    :category => '5.Presentation',
    :order => 1,
    :filter => {"all" => {"visible" => "false", "editable" => "false"}}
  }
