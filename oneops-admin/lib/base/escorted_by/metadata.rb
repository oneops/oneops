name             "EscortedBy"
description      "Platform components escorted by attachments"
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
    attribute "Component-#{relation_package}.EscortedBy-#{class_package}.Attachment",
      :relation_target => true,
      :package => relation_package,
      :from_class => 'Component',
      :to_class => [class_package,'Attachment'].join('.'),
      :link_type => 'one-to-many',
      :required => true
end


