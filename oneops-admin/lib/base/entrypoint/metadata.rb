name             "Entrypoint"
description      "Relation between platform and the entrypoint resources"
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
# relation targets
    
['mgmt.catalog',
  'mgmt.manifest',
  'catalog',
  'manifest',
  'base',
  'bom'].each do |relation_package|
      attribute "Component-#{relation_package}.Entrypoint-Component",
        :relation_target => true,
        :package => relation_package,
        :from_class => "Component",
        :to_class => "Component",
        :link_type => 'one-to-one',
        :required => false
end
    
=begin
{ 'mgmt.manifest' => 'mgmt',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|

  [ 'Compute', 'Lb', 'Cluster', 'Fqdn' ].each do |to|
    attribute "#{class_package}.Platform-#{relation_package}.Entrypoint-#{class_package}.#{to}",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Platform'].join('.'),
      :to_class => [class_package,to].join('.'),
      :link_type => 'one-to-one',
      :required => false
  end

  [ 'Compute', 'Lb', 'Cluster', 'Fqdn' ].each do |to|
    attribute "#{class_package}.Iaas-#{relation_package}.Entrypoint-#{class_package}.#{to}",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,'Iaas'].join('.'),
      :to_class => [class_package,to].join('.'),
      :link_type => 'one-to-one',
      :required => false
  end
  
end

[ 'Compute', 'Lb', 'Cluster', 'Fqdn' ].each do |short|
  attribute "manifest.Platform-base.Entrypoint-bom.#{short}",
      :relation_target => true,
      :package => 'base',
      :from_class => 'manifest.Platform',
      :to_class => ['bom',short].join('.'),
      :link_type => 'one-to-one',
      :required => false
end

[ 'Compute', 'Lb', 'Cluster', 'Fqdn' ].each do |short|
  attribute "manifest.Iaas-base.Entrypoint-bom.#{short}",
      :relation_target => true,
      :package => 'base',
      :from_class => 'manifest.Iaas',
      :to_class => ['bom',short].join('.'),
      :link_type => 'one-to-one',
      :required => false
end
=end