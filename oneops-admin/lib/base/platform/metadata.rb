name             "Platform"
description      "Assembly platforms"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => 'global',
  :packages => [ 'base' ]

grouping 'catalog',
  :access => 'global',
  :packages => [ 'mgmt.catalog', 'catalog' ]

grouping 'manifest',
  :access => 'global',
  :packages => [ 'mgmt.manifest', 'manifest' ]


attribute 'description',
  :description => 'Description',
  :default => '',
  :format => {
    :help => 'Description',
    :category => '1.Global',
    :order => 1
  }

attribute 'source',
  :description => 'Pack Source',
  :default => '',
  :format => {
    :help => 'Pack source name',
    :category => '2.Platform Pack',
    :order => 1
  }

attribute 'pack',
  :description => 'Pack Name',
  :default => '',
  :format => {
    :help => 'Pack name',
    :category => '2.Platform Pack',
    :order => 2
  }

attribute 'version',
  :description => 'Pack Version',
  :default => '',
  :format => {
    :help => 'Pack version',
    :category => '2.Platform Pack',
    :order => 3
  }

attribute 'pack_digest',
  :description => 'Current pack digest',
  :default => '',
  :format => {
    :help => 'Current pack digest',
    :filter => {'all' => {'visible' => 'false'}},
    :category => '2.Platform Pack',
    :order => 4
  }



# support for version upgrades
attribute 'major_version',
  :description => 'Version',
  :default => '1',
  :format => {
    :help => 'Major version of the platform should only be increased if you are doing a full <em>upgrade</em> and replacing all component instances',
    :category => '3.Version',
    :order => 1
  }

attribute 'is_active',
  :grouping => 'manifest',
  :description => 'Active Version',
  :default => 'false',
  :format => {
    :help => 'Active version of the platform indicates which platform version instance is active and owns the DNS entrypoint name',
    :order => 2,
    :category => '3.Version'
  }

# environment overrides
attribute 'availability',
  :grouping => 'manifest',
  :description => 'Availability Mode',
  :default => 'default',
  :format => {
    :help => 'Custom availability for this platform (Note: default option is to inherit availability mode from the environment)',
    :order => 1,
    :category => '4.Availability'
  }

attribute 'replace_after_minutes',
  :description => 'Replace unhealthy after minutes',
  :grouping => 'manifest',
  :required => 'required',
  :default => '9999999',
  :format => {
    :help => 'If this component is unhealthy for these many minutes, it will be replaced provided the repairs were executed and it is still unhealthy',
    :category => '4.Availability',
    :order => 2,
    :pattern => '[0-9]+'
  }

attribute 'replace_after_repairs',
  :grouping => 'manifest',
  :description => 'Replace unhealthy after repairs #',
  :required => 'required',
  :default => '9999999',
  :format => {
    :help => 'If this component is unhealthy, it will be replaced provided the repairs were executed for as many number of times as this field value and it is still unhealthy since last X number of minutes you define above',
    :category => '4.Availability',
    :order => 3,
    :pattern => '[0-9]+'
  }

attribute 'autorepair',
  :description => 'Auto Repair',
  :grouping => 'manifest',
  :required => 'required',
  :default => 'true',
  :format => {
    :category => '4.Availability',
    :order => 4,
    :filter => {'all' => {'visible' => 'false'}},
    :help => 'Autorepair enables automatic repair of component instances based on monitors with enabled heartbeats and metrics you define with Unhealthy event triggers (Note: repairs are executed by invoking and action with the name <em>repair</em> for each component)',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autorepair_exponential_backoff',
  :description => 'Enable Auto Repair Exponential Interval',
  :grouping => 'manifest',
  :required => 'required',
  :default => 'true',
  :format => {
    :category => '4.Availability',
    :order => 5,
    :help => 'Exponentially increase auto repair interval if a component does not recover on first few auto-repairs',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autoscale',
  :description => 'Auto Scale',
  :grouping => 'manifest',
  :required => 'required',
  :default => 'false',
  :format => {
    :category => '4.Availability',
    :order => 6,
    :filter => {'all' => {'visible' => 'false'}},
    :tip => 'NOTE: Autoscale will apply only to platforms running in redundant mode in this environment.',
    :help => 'Autoscales enables scaling up and down based on metrics you define with Over-Utilized and Under-Utilized event triggers (Note: the scale step up and down along with the limits and metrics can be customized on a platform level after saving the environment)',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autoreplace',
  :description => 'Auto Replace',
  :grouping => 'manifest',
  :required => 'required',
  :default => 'true',
  :format => {
    :category => '4.Availability',
    :order => 7,
    :filter => {'all' => {'visible' => 'false'}},
    :tip => 'NOTE: Autoreplace works only if the auto-repair is ON for this environment. You also need to set valid values for the replace related 2 attributes in your platform configuration in order to enable auto-replace for that particular platform',
    :help => 'Replaces an unhealthy component  after some duration if the repair action can not recover the component',
    :form => { 'field' => 'checkbox' }
  }

attribute 'autocomply',
  :description => 'Auto Compliance',
  :grouping => 'manifest',
  :required => 'required',
  :default => 'false',
  :format => {
    :category => '4.Availability',
    :order => 8,
    :filter => {'all' => {'visible' => 'false'}},
    :help => 'Automatically apply compliance requirements during deployment for instances in clouds that have any compliances configured.',
    :form => { 'field' => 'checkbox' }
  }
  
attribute 'fail_on_delete_failure',
  :description => 'Fail Deployment On Failure of Delete Components',
  :grouping => 'manifest',
  :default => '[]',
  :format => {
    :category => '2.Platform Pack',
    :order => 5,
    :filter => {'all' => {'visible' => 'false'}},
    :help => 'Fail the deployment when a delete work order on any of the given components fail.'
  }
