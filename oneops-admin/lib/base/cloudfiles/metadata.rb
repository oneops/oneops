name             "Cloudfiles"
description      "Rackspace CloudFiles"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'mgmt.cloud.service', 'cloud.service' ]

  
attribute 'username',
  :description => "Username",
  :required => "required",
  :default => "",
  :format => { 
    :help => 'Email',
    :category => '1.Credentials',
    :order => 1
  }
  
attribute 'password',
  :description => "Password",
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => {
    :help => 'Password', 
    :category => '1.Credentials',
    :order => 2
  }

    
