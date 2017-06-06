name             "LoggedBy"
description      "Components loggedBy Log"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt.manifest', 'manifest' ]


{ 'mgmt.manifest' => 'mgmt.manifest',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|

    attribute "Component-#{relation_package}.LoggedBy-#{class_package}.Log",
      :relation_target => true,
      :package => relation_package,
      :from_class => 'Component',
      :to_class => [class_package,'Log'].join('.'),
      :link_type => 'one-to-one',
      :required => true

end
