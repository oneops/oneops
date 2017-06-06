name             "ValueFor"
description      "Relation between variables and targets"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => ['base', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest', 'account']
  
# relation targets
attribute "catalog.Globalvar-base.ValueFor-account.Assembly",
  :relation_target => true,
  :package => 'base',
  :from_class => 'catalog.Globalvar',
  :to_class => 'account.Assembly',
  :link_type => 'many-to-one',
  :required => false

attribute "manifest.Globalvar-manifest.ValueFor-manifest.Environment",
  :relation_target => true,
  :package => 'manifest',
  :from_class => 'manifest.Globalvar',
  :to_class => 'manifest.Environment',
  :link_type => 'many-to-one',
  :required => false

['mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest'].each do |package|
  attribute "#{package}.Localvar-#{package}.ValueFor-#{package}.Platform",
    :relation_target => true,
    :package => package,
    :from_class => "#{package}.Localvar",
    :to_class => "#{package}.Platform",
    :link_type => 'many-to-one',
    :required => false
end

attribute "account.Cloudvar-account.ValueFor-account.Cloud",
  :relation_target => true,
  :package => 'account',
  :from_class => 'account.Cloudvar',
  :to_class => 'account.Cloud',
  :link_type => 'many-to-one',
  :required => false