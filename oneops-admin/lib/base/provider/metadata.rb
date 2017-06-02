name             "Provider"
description      "IaaS and PaaS Providers"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'account' ],
  :namespace => true

attribute 'description',
  :description => "Description",
  :format => {
    :help => 'Enter description for this provider',
    :category => '1.Global',
    :order => 1,
    :form => { 'field' => 'textarea' }
  }

attribute 'organization',
  :description => "Organization",
  :default => "",
  :format => {
    :help => 'Organization',
    :category => '1.Global',
    :order => 2
  }

attribute 'token',
  :description => "Token Type",
  :required => "required",
  :default => "",
  :format => {
    :help => 'Authentication token type required by this provider',
    :category => '1.Global',
    :order => 3
  }
  