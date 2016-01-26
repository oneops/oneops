name             "ComposedOf"
description      "Relation between assembly and platform"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]

grouping 'manifest',
  :relation => true,
  :packages => [ 'manifest' ]
  
attribute 'enabled',
  :grouping => 'manifest',
  :display_name => "Enabled",
  :description => "Enabled",
  :type => "string",
  :data_type => "boolean",
  :required => "required",
  :recipes => [ 'composed_of::default' ],
  :default => "true",
  :format => "{ 'boolean':'' }"
  
# relation targets
attribute "account.Design-base.ComposedOf-catalog.Platform",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Design',
    :to_class => 'catalog.Platform',
    :link_type => 'one-to-many',
    :required => true
    
[ 'manifest', 'catalog' ].each do |class_package|
  attribute "account.Assembly-base.ComposedOf-#{class_package}.Platform",
    :relation_target => true,
    :package => 'base',
    :from_class => 'account.Assembly',
    :to_class => [class_package,'Platform'].join('.'),
    :link_type => 'one-to-many',
    :required => true
end

attribute "manifest.Environment-manifest.ComposedOf-manifest.Platform",
    :relation_target => true,
    :package => 'manifest',
    :from_class => 'manifest.Environment',
    :to_class => 'manifest.Platform',
    :link_type => 'one-to-many',
    :required => true

attribute "manifest.Environment-manifest.ComposedOf-manifest.Iaas",
    :relation_target => true,
    :package => 'manifest',
    :from_class => 'manifest.Environment',
    :to_class => 'manifest.Iaas',
    :link_type => 'one-to-many',
    :required => true

