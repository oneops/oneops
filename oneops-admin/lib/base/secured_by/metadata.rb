name             "SecuredBy"
description      "Key pairs used for securing environments and other resources"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps, Inc."
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'manifest', 'bom', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest' ]

# relation targets
=begin
[ 'manifest.Environment' ].each do |source|
  attribute "#{source}-base.SecuredBy-account.Keypair",
    :relation_target => true,
    :package => 'base',
    :from_class => "#{source}",
    :to_class => 'account.Keypair',
    :link_type => 'one-to-many',
    :required => true
end

[ 'manifest.Environment' ].each do |source|
  attribute "#{source}-manifest.SecuredBy-manifest.Keypair",
    :relation_target => true,
    :package => 'manifest',
    :from_class => "#{source}",
    :to_class => 'manifest.Keypair',
    :link_type => 'one-to-many',
    :required => true
end
=end

['mgmt.catalog',
  'mgmt.manifest',
  'catalog',
  'manifest',
  'bom'].each do |relation_package|
      attribute "Component-#{relation_package}.SecuredBy-Component",
        :relation_target => true,
        :package => relation_package,
        :from_class => "Component",
        :to_class => "Component",
        :link_type => 'one-to-one',
        :required => false
end


=begin
{ 'mgmt.catalog'  => 'mgmt.catalog',
  'mgmt.manifest' => 'mgmt.manifest',
  'catalog'       => 'catalog',
  'manifest'      => 'manifest',
  'bom'           => 'bom' }.each do |class_package,relation_package|
 [{"Cluster" => "Keypair"},
  {"Ring" => "Keypair"},
  {"Compute" => "Keypair"},
  {"Compute" => "Secgroup"}
    ].each do |kv|      
     kv.each do |from,to|
      attribute "#{class_package}.#{from}-#{relation_package}.SecuredBy-#{class_package}.#{to}",
        :relation_target => true,
        :package => relation_package,
        :from_class => [class_package,from].join('.'),
        :to_class => [class_package,to].join('.'),
        :link_type => 'one-to-one',
        :required => false
     end
  end
    
end

=end
recipe "secured_by::default", "SecuredBy default recipe"
