name             "RealizedAs"
description      "Relation between manifest and bom resources"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base' ]
  
attribute 'last_manifest_rfc',
  :display_name => "Last Manifest RFC",
  :description => "Last Manifest RFC",
  :type => "string",
  :required => "optional",
  :recipes => [ 'realizad_as::default' ],
  :default => "",
  :format => ""
 
attribute 'priority',
  :description => "Priority",
  :required => "required",
  :default => "1",
  :format => ""

attribute "Component-base.RealizedAs-Component",
      :relation_target => true,
      :package => 'base',
      :from_class => 'Component',
      :to_class => 'Component',
      :link_type => 'one-to-many',
      :required => false

recipe "realized_as::default", "RealizedAs default recipe"

