name             "Payload"
description      "Relation between platform components and query path in templates"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt.manifest' ]

    
{ 'mgmt.manifest' => 'mgmt.manifest'}.each do |class_package,relation_package|
    attribute "Component-#{relation_package}.Payload-#{class_package}.Qpath",
      :relation_target => true,
      :package => relation_package,
      :from_class => 'Component',
      :to_class => [class_package,'Qpath'].join('.'),
      :link_type => 'one-to-many',
      :required => false
end


# relation targets
=begin    
{ 'mgmt.manifest' => 'mgmt.manifest' }.each do |class_package,relation_package|
  [ 'Artifact', 'Compute', 'Library', 'Download', 'File', 'Haproxy', 'Build', 'Tomcat', 'Geronimo', 'Wasce', 'Ruby', 'Nodejs', 'Java', 'Python', 'Php', 'Perl', 'Nginx', 'Apache', 'Solr',
    'Database', 'Postgresql', 'Db2', 'Mysql', 'Netscaler', 'Oracle', 'Activemq', 'Cassandra', 'Nfs', 'Filesystem', 'Volume', 'Drbd', 'Fqdn', 'Powerdns',
    'Lb', 'Vservice', 'Ring', 'Cluster', 'Crm', 'Oneops' ].each do |from|
    attribute "#{class_package}.#{from}-#{relation_package}.Payload-#{class_package}.Qpath",
      :relation_target => true,
      :package => relation_package,
      :from_class => [class_package,from].join('.'),
      :to_class => [class_package,'Qpath'].join('.'),
      :link_type => 'one-to-many',
      :required => false
  end
end
=end
