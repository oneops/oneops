name             "ManagedVia"
description      "Configuration proxy relation"
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
  :packages => [ 'mgmt.manifest', 'manifest' ]


['mgmt.catalog',
  'mgmt.manifest',
  'catalog',
  'manifest',
  'base',
  'bom'].each do |relation_package|
      attribute "Component-#{relation_package}.ManagedVia-Component",
        :relation_target => true,
        :package => relation_package,
        :from_class => "Component",
        :to_class => "Component",
        :link_type => 'one-to-one',
        :required => false
end

recipe "managed_via::default", "ManagedVia default recipe"

