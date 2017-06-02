name             "Requires"
description      "Relation between platform and the required resources"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'manifest' ]

grouping 'mgmt',
  :relation => true,
  :packages => [ 'mgmt' ]

attribute 'services',
  :description => "Cloud Services Required",
  :default => "",
  :format => { 
    :help => 'Comma separate list of cloud services needed by this component',
    :category => '1.Global',
    :order => 1
  }
  
attribute 'priority',
  :description => "Deployment Priority",
  :default => "",
  :format => { 
    :help => 'Force the deployment priority',
    :category => '1.Global',
    :order => 2
  }
  
attribute 'constraint',
  :display_name => "Cardinality Constraint",
  :description => "Cardinality Constraint",
  :type => "string",
  :required => "required",
  :recipes => [ 'requires::default' ],
  :default => "1..1",
  :format => ""

attribute 'template',
  :display_name => "Management Template",
  :description => "Management Template",
  :type => "string",
  :required => "required",
  :recipes => [ 'requires::default' ],
  :default => "",
  :format => ""
  
attribute 'help',
  :grouping => 'mgmt',
  :description => "Help",
  :default => "",
  :format => ""
  
# relation targets

{ 'mgmt.catalog'  => 'mgmt',
  'mgmt.manifest' => 'mgmt',
  'catalog'       => 'base',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|

    attribute "#{class_package}.Platform-#{relation_package}.Requires-Component",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Platform'].join('.'),
      :to_class => 'Component',
      :link_type => 'one-to-one',
      :required => true
  
    attribute "#{class_package}.Iaas-#{relation_package}.Requires-Component",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Iaas'].join('.'),
      :to_class => 'Component',
      :link_type => 'one-to-one',
      :required => true
end
=begin  
{ 'mgmt.manifest' => 'mgmt',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|

  [ 'Lb', 'Vservice', 'Ring', 'Crm', 'Cluster' ].each do |to|
    attribute "#{class_package}.Platform-#{relation_package}.Requires-#{class_package}.#{to}",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Platform'].join('.'),
      :to_class => [class_package,to].join('.'),
      :link_type => 'one-to-one',
      :required => true
  end

  [ 'Cluster', 'Keypair' ].each do |to|
    attribute "#{class_package}.Iaas-#{relation_package}.Requires-#{class_package}.#{to}",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Iaas'].join('.'),
      :to_class => [class_package,to].join('.'),
      :link_type => 'one-to-one',
      :required => true
  end
end
=end
recipe "requires::default", "Requires default recipe"
