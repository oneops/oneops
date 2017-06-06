name             "LinksTo"
description      "Dependency relation between platforms"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'catalog', 'manifest' ]

{ 'catalog'       => 'catalog',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|

  attribute "#{class_package}.Platform-#{relation_package}.LinksTo-#{class_package}.Platform",
      :relation_target => true,
      :package => relation_package,
      :from_class => "#{class_package}.Platform",
      :to_class => "#{class_package}.Platform",
      :link_type => 'many-to-many',
      :required => false
end

recipe "links_to::default", "LinksTo default recipe"

