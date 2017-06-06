name             "DeployedTo"
description      "Dependency relation from platform resources to provider binding"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

attribute 'priority',
  :description => "Priority",
  :required => "required",
  :default => "1",
  :format => ""

attribute "Component-base.DeployedTo-account.provider.Binding",
      :relation_target => true,
      :package => "base",
      :from_class => "Component",
      :to_class => "account.provider.Binding",
      :link_type => 'many-to-one',
      :required => false

attribute "Component-base.DeployedTo-account.Cloud",
      :relation_target => true,
      :package => "base",
      :from_class => "Component",
      :to_class => "account.Cloud",
      :link_type => 'many-to-one',
      :required => false

recipe "deployed_to::default", "DeployedTo default recipe"

