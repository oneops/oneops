name             "Attachment"
description      "Attachment"
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."
depends          "shared"

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'catalog', 'manifest' ]

attribute 'content',
  :description => "Attachment Content (Leave empty to use a remote file)",
  :data_type => "text",
  :format => {
    :help => 'Use this field to directly specify content such as a script, configuration file, certificate etc (Note: this option supercedes the remote source)',
    :category => '1.Include Content',
    :order => 1
  }
  
attribute 'source',
  :description => "Source URL",
  :required => "optional",
  :format => {
    :category => '2.Remote File',
    :help => 'Specify remote URL location (ex. http://site/file or s3://bucket/file',
    :order => 1
  }

attribute 'basic_auth_user',
  :description => "Username",
  :format => {
    :category => '2.Remote File',
    :help => 'Basic authentication username for URLs or Access Key for S3',
    :order => 2
  }

attribute 'basic_auth_password',
  :description => "Password",
  :encrypted => true,
  :format => {
    :category => '2.Remote File',
    :help => 'Basic authentication password or Secret Key for S3',    
    :order => 3
  }
     
attribute 'headers',
  :description => "Custom Headers",
  :data_type => "hash",
  :format => {
    :help => 'Additional HTTP headers',
    :category => '2.Remote File',
    :order => 4
  }

attribute 'checksum',
  :description => "Checksum",
  :format => {
    :help => 'Optional checksum to verify against the downloaded attachment',
    :category => '2.Remote File',
    :order => 5
  } 

attribute 'path',
  :description => "Destination Path",
  :required => "required",
  :default => '/tmp/download_file',
  :format => {
    :help => 'Specify destination filename path where the attachment will be saved',
    :category => '3.Destination',
    :order => 1
  }

attribute 'exec_cmd',
  :description => "Execute Command",
  :data_type => "text",  
  :format => {
    :help => 'Optional commands to execute after downloading the file from remote source and/or saving the included file content',
    :category => '4.Run',
    :order => 1
  }

attribute 'run_on',
  :description => "Run on Event",
  :required => "required",
  :default => 'before-add',
  :format => {
    :help => 'The content will be downloaded and executed on these events.',
    :category => '4.Run',
    :order => 2,
    :form => { 'field' => 'checkbox', 'multiple' => 'true', 'options_for_select' => [['Before Add','before-add'],['After Add','after-add'],['Before Replace','before-replace'],['After Replace','after-replace'],['Before Update','before-update'],['After Update','after-update'],['Before Delete','before-delete'], ['After Delete','after-delete'], ['On Demand','on-demand']] }
  } 
  
attribute 'run_on_action',
          :description => 'Run on Action',
          :default => '[]',
          :data_type => 'array',
          :format => {
              :help => 'You can specify attachments to be run before any action. Specify using this format  before-repair,after-repair ',
              :category => '4.Run',
              :order => 3,
          }

attribute 'priority',
  :description => "Execution Priority",
  :required => "required",
  :default => '1',
  :format => {
    :help => 'If there are more then one attachments per event this is an order they will be processed.',
    :category => '4.Run',
    :order => 3
  }
