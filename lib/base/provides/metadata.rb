name             "Provides"
description      "Provider provides zones"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt' ]

attribute 'service',
  :description => "Cloud Service Type",
  :default => "",
  :format => {
    :help => 'Type of cloud service',
    :category => '1.Global',
    :order => 1
  }

attribute 'template_ns',
  :description => 'Base template namespace',
  :default => '',
  :format => {
    :help => 'Namespace of location where base template is stored.',
    :category => '1.Global',
    :order => 2
  }

# relation targets
attribute "account.Provider-base.BindsTo-account.provider.Region",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Provider',
    :to_class => 'account.provider.Region',
    :link_type => 'one-to-many',
    :required => true

attribute "account.Provider-base.BindsTo-account.provider.Zone",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Provider',
    :to_class => 'account.provider.Zone',
    :link_type => 'one-to-many',
    :required => true


# cloud
=begin
[ 'Route53', 'Netscaler' ].each do |service|
     attribute "account.Cloud-base.Provides-cloud.service.#{service}",
      :relation_target => true,
      :package => 'base',
      :from_class => 'account.Cloud',
      :to_class => "cloud.service.#{service}",
      :link_type => 'one-to-many',
      :required => false

    attribute "mgmt.Cloud-mgmt.Provides-mgmt.cloud.service.#{service}",
      :relation_target => true,
      :package => 'mgmt',
      :from_class => 'mgmt.Cloud',
      :to_class => "mgmt.cloud.service.#{service}",
      :link_type => 'one-to-many',
      :required => false
end
=end
