name             "Zone"
description      "Provider Zone"
long_description IO.read(File.join(File.dirname(__FILE__), 'README.md'))
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'account.provider' ]

# TODO should the key be required?
attribute 'authkey',
  :description => "Authorization Key",
  :default => "",
  :format => {
    :help => 'The authorization key is required to securely connect an inductor agent from inside a VPC',
    :category => '1.Security',
    :order => 1
  }

attribute 'subnet',
  :description => "Subnet ID",  
  :required => "required",
  :default => "",
  :format => {
    :help => 'Subnet ID is required for proper placement of compute instances (Note: only single subnet per environment is supported)',
    :category => '2.Network',
    :order => 1
  }

