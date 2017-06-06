name             "ControlledBy"
description      "Relation between platform and the procedures in templates"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt.manifest' ]

# relation targets
attribute "mgmt.manifest.Platform-mgmt.manifest.ControlledBy-mgmt.manifest.Procedure",
      :relation_target => true,
      :package => 'mgmt.manifest',
      :from_class => 'mgmt.manifest.Platform',
      :to_class => 'mgmt.manifest.Procedure',
      :link_type => 'one-to-many',
      :required => false

