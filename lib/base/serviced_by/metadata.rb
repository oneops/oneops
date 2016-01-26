name             "ServicedBy"
description      "Dependency relation between platform components and IaaS services"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt.manifest', 'manifest' ]
=begin  

attribute 'services',
  :display_name => "Services",
  :description => "Services",
  :type => "string",
  :data_type => "array",
  :required => "optional",
  :recipes => [ 'serviced_by::default' ],
  :default => "[]",
  :format => ""
  
{ 'mgmt.manifest' => 'mgmt.manifest',
  'manifest'      => 'manifest' }.each do |class_package,relation_package|
  [ 'Lb', 'Vservice', 'Platform' ].each do |from|
    attribute "#{class_package}.#{from}-#{relation_package}.ServicedBy-#{class_package}.Iaas",
        :relation_target => true,
        :package => relation_package,
        :from_class => "#{class_package}.#{from}",
        :to_class => "#{class_package}.Iaas",
        :link_type => 'many-to-many',
        :required => false
  end
end
=end
recipe "serviced_by::default", "ServicedBy default recipe"
