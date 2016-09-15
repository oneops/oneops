name             "DependsOn"
description      "Dependency relation between platform resources"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'bom' ]

grouping 'manifest',
  :relation => true,
  :packages => [ 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest' ]
    
attribute 'flex',
  :grouping => 'manifest',
  :description => "Flexible Capacity",
  :required => "required",
  :default => "false",
  :format => ""

attribute 'converge',
  :grouping => 'manifest',
  :description => "Converge",
  :required => "optional",
  :default => "false",
  :format => ""

attribute 'step_up',
  :grouping => 'manifest',
  :description => "Scale Up Step",
  :required => "required",
  :default => "1",
  :format => ""

attribute 'step_down',
  :grouping => 'manifest',
  :description => "Scale Down Step",
  :required => "required",
  :default => "1",
  :format => ""


attribute 'min',
  :grouping => 'manifest',
  :description => "Minimum Capacity",
  :required => "required",
  :default => "1",
  :format => ""

attribute 'current',
  :grouping => 'manifest',
  :description => "Current Capacity",
  :required => "required",
  :default => "1",
  :format => ""
    
attribute 'max',
  :grouping => 'manifest',
  :description => "Maximum Capacity",
  :required => "required",
  :default => "1",
  :format => ""

attribute 'pct_dpmt',
  :grouping => 'manifest',
  :description => "Deployment Percentage",
  :default => "100",
  :format => ""

attribute 'source',
  :grouping => 'manifest',
  :description => "Source",
  :format => ""

attribute 'propagate_to',
	:grouping => 'manifest',
	:description => "Propagate Change to one or both directions",
	:format => ""

['mgmt.catalog',
  'mgmt.manifest',
  'catalog',
  'manifest',
  'bom'].each do |relation_package|
      attribute "Component-#{relation_package}.DependsOn-Component",
        :relation_target => true,
        :package => relation_package,
        :from_class => "Component",
        :to_class => "Component",
        :link_type => 'one-to-one',
        :required => false
end

recipe "depends_on::default", "DependsOn default recipe"
