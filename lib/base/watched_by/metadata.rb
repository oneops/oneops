name             "WatchedBy"
description      "Pack resources watched by monitors"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :relation => true,
  :packages => [ 'base', 'mgmt.manifest', 'manifest', 'mgmt.catalog', 'catalog' ]


{ 'mgmt.manifest' => 'mgmt.manifest',
  'manifest'      => 'manifest',
  'mgmt.catalog'  => 'mgmt.catalog',
  'catalog'       => 'catalog' }.each do |class_package,relation_package|

    attribute "Component-#{relation_package}.WatchedBy-#{class_package}.Monitor",
      :relation_target => true,
      :package => relation_package,
      :from_class => 'Component',
      :to_class => [class_package,'Monitor'].join('.'),
      :link_type => 'one-to-many',
      :required => true

attribute 'docUrl',
        :grouping => 'default',
        :description => "URL to a page having resolution or escalation details",
	:default => " ",
        :format => {
          :pattern => '(http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.{0,500})?'
        }
        
   

attribute 'notifyOnlyOnStateChange',
        :grouping => 'default',
        :description => 'Recieve Email Notifications only On state change. ',
        :default => 'true',
       :format => {
         :help => 'Checking this checkbox, will enable only notifiactions to be sent once per state change.By default checked. ',
         :form => { 'field' => 'checkbox' },    
         }

attribute 'source',
        :grouping => 'default',
        :description => 'Source',
        :default => ' ',
        :type => 'string',
        :format => ''
         
end
