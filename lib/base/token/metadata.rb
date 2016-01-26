name             "Token"
description      "Provider authentication token"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base' ],
  :namespace => true

grouping 'keys',
  :access => "global",
  :packages => [ 'account.provider.ec2', 'account.provider.cloudstack', 'account.provider.openstack' ],
  :namespace => true
  
grouping 'rackspace',
  :access => "global",
  :packages => [ 'account.provider.rackspace' ],
  :namespace => true

grouping 'openstack-v2',
  :access => "global",
  :packages => [ 'account.provider.openstack-v2' ],
  :namespace => true

grouping 'cloudstack',
  :access => "global",
  :packages => [ 'account.provider.cloudstack' ],
  :namespace => true
 
 grouping 'ibm',
  :access => "global",
  :packages => [ 'account.provider.ibm' ],
  :namespace => true
 
grouping 'hp',
  :access => "global",
  :packages => [ 'account.provider.hp' ],
  :namespace => true 

grouping 'virtualbox',
  :access => "global",
  :packages => [ 'account.provider.virtualbox' ],
  :namespace => true 

grouping 'vagrant',
  :access => "global",
  :packages => [ 'account.provider.vagrant' ],
  :namespace => true


# ec2  
attribute 'key',
  :grouping => 'keys',
  :description => "Access Key",
  :required => "required",
  :default => "",
  :format => { 
    :help => 'Access key from the provider security credentials page',
    :category => '1.Credentials',
    :order => 1
  }
  
attribute 'secret',
  :grouping => 'keys',
  :description => "Secret Key",
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => {
    :help => 'Secret key from the provider security credentials page', 
    :category => '1.Credentials',
    :order => 2
  }

# rackspace
attribute 'rackspace_username',
  :grouping => 'rackspace',
  :description => "Username",
  :required => "required",
  :default => "",
  :format => { 
    :help => 'Username used to access provider account',
    :category => '1.Credentials',
    :order => 1
  }
  
attribute 'rackspace_api_key',
  :grouping => 'rackspace',
  :description => "API Key",  
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => {
    :help => 'API key used to access provider account',
    :category => '1.Credentials',
    :order => 2
  }
    
# keystone / openstack-v2
attribute 'openstack_username',
  :grouping => 'openstack-v2',
  :description => "Username",
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 1,
    :help => 'The username used to login to openstack dashboard'
  }

attribute 'openstack_api_key',
  :grouping => 'openstack-v2',
  :description => "Password",
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 2,
    :help => 'The password used to login to openstack dashboard'
  }

# cloudstack 3.x
attribute 'cloudstack_username',
  :grouping => 'cloudstack',
  :description => "Username",
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 1,
    :help => 'CloudStack username used to login to the management console'
  }

attribute 'cloudstack_password',
  :grouping => 'cloudstack',
  :description => "Password",
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 2,
    :help => 'CloudStack password used to login to the management console'
  }


# ibm
attribute 'ibm_username',
  :grouping => 'ibm',
  :description => "Username",
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 1,
    :help => 'The username used to login to IBM SmartCloud'
  }

attribute 'ibm_password',
  :grouping => 'ibm',
  :description => "Password",
  :encrypted => true,
  :required => "required",
  :default => "",
  :format => { 
    :category => '1.Credentials',
    :order => 2,
    :help => 'The password used to login to IBM SmartCloud'
  }


# hp
attribute 'hp_account_id',
  :grouping => 'hp',
  :description => "Account ID",
  :required => "required",
  :default => "",
  :format => { 
    :help => 'HP account ID used to access provider account',
    :category => '1.Credentials',
    :order => 1
  }
attribute 'hp_secret_key',
  :grouping => 'hp',
  :description => "Secret Key",
  :required => "required",
  :default => "",
  :format => {
    :help => 'HP secret key used to access provider account',
    :category => '1.Credentials',
    :order => 2
  }
  
attribute 'hp_tenant_id',
  :grouping => 'hp',
  :description => "Tenant ID",
  :required => "required",
  :default => "",
  :format => { 
    :help => 'HP tenant ID from the account management page (Note: do not use tenant name, must use the numeric ID)',
    :category => '1.Credentials',
    :order => 3
  }

attribute 'hp_auth_uri',
  :grouping => 'hp',
  :description => "Identity Service Endpoint",
  :required => "required",
  :default => "https://region-a.geo-1.identity.hpcloudsvc.com:35357/v2.0/",
  :format => {
    :help => 'HP identity URL from the account management page',
    :category => '2.Service Endpoints',
    :order => 1
  }
    
attribute 'hp_avl_zone',
  :grouping => 'hp',
  :description => "Availability Zone",
  :required => "required",
  :default => "https://az-1.region-a.geo-1.compute.hpcloudsvc.com/v1.1/15581186097523",
  :format => { 
    :help => 'HP availability zone service enpoint URL',
    :category => '2.Service Endpoints',
    :order => 2
  }

# vagrant
attribute 'path',
  :grouping => 'vagrant',
  :description => "Project Path",
  :required => "required",
  :format => { 
    :category => '1.Configuration',
    :order => 1,
    :help => 'Specify path where vagrant project directories should be stored'
  }