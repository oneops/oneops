name             "Artifact"
description      "Installs/Configures software artifacts"
version          "0.1"
maintainer       "OneOps"
maintainer_email "support@oneops.com"
license          "Copyright OneOps, All rights reserved."
depends          "shared"

grouping 'default',
  :access => "global",
  :packages => [ 'base', 'mgmt.catalog', 'mgmt.manifest', 'catalog', 'manifest', 'bom' ]

### Attributes
# Attribute                  | Description                                                                          |Type     | Default
# ---------                  |-------------                                                                         |-----    |--------
# artifact_name              | Name of the artifact package to deploy                                               | String  | name
# artifact_location          | URL, S3 path, local path, or Maven identifier of the artifact package to download    | String  |
# artifact_checksum          | The SHA256 checksum of the artifact package that is being downloaded                 | String  |
# deploy_to                  | Deploy directory where releases are stored and linked                                | String  |
# version                    | Version of the artifact being deployed                                               | String  |
# owner                      | Owner of files created and modified                                                  | String  |
# group                      | Group of files created and modified                                                  | String  |
# environment                | An environment hash used by resources within the provider                            | Hash    | Hash.new
# symlinks                   | A hash that maps files in the shared directory to their paths in the current release | Hash    | Hash.new
# shared_directories         | Directories to be created in the shared folder                                       | Array   | %w{ log pids }
# force                      | Forcefully deploy an artifact even if the artifact has already been deployed         | Boolean | false
# should_migrate             | Notify the provider if it should perform application migrations                      | Boolean | false
# keep                       | Specify a number of artifacts deployments to keep on disk                            | Integer | 2
# before_deploy              | A proc containing resources to be executed before the deploy process begins          | Proc    |
# before_extract             | A proc containing resources to be executed before the artifact package is extracted  | Proc    |
# after_extract              | A proc containing resources to be executed after the artifac package is extracted    | Proc    |
# before_symlink             | A proc containing resources to be executed before the symlinks are created           | Proc    |
# after_symlink              | A proc containing resources to be executed after the symlinks are created            | Proc    |
# configure                  | A proc containing resources to be executed to configure the artifact package         | Proc    |
# before_migrate             | A proc containing resources to be executed before the migration Proc                 | Proc    |
# migrate                    | A proc containing resources to be executed during the migration stage                | Proc    |
# after_migrate              | A proc containing resources to be executed after the migration Proc                  | Proc    |
# restart                    | A proc containing resources to be executed at the end of a successful deploy         | Proc    |
# after_deploy               | A proc containing resources to be executed after the deploy process ends             | Proc    |
# ssl_verify                 | Used to set whether or not communications with a Nexus server should be SSL verified | Boolean | true
# remove_top_level_directory | Deletes a top level directory from the extracted zip file                            | Boolean | false
# skip_manifest_check        | Skips the manifest check for idempotency when the version attribute is not changing  | Boolean | false 
# remove_on_force            | Removes the current version directory contents when force is set                     | Boolean | false

attribute 'url',
  :description => "Repository URL",
  :default => '',
  :format => { 
    :help => 'Nexus repository URL',
    :category => '1.Repository',
    :order => 1
  }

attribute 'repository',
  :description => "Repository Name",
  :required => "required",
  :default => '',
  :format => { 
    :help => 'Repository',
    :category => '1.Repository',
    :order => 2
  }
  
attribute 'username',
  :description => "Username",
  :format => {
    :help => 'Username to authenticate against the SCM source repository',
    :category => '2.Authentication',
    :order => 1
  }

attribute 'password',
  :description => "Password",
  :encrypted => true,
  :format => {
    :help => 'Password to authenticate against the SCM source repository',
    :category => '2.Authentication',
    :order => 2
  }

attribute 'location',
  :description => "Identifier",
  :required => "required",
  :default => '',
  :format => { 
    :help => 'URL, S3 path, local path, or Maven identifier of the artifact package to download',
    :important => true,
    :category => '3.Artifact',
    :order => 1
  }
  
attribute 'version',
  :description => "Version",
  :required => "required",
  :default => 'latest',
  :format => {
    :help => 'Version of the artifact being deployed',
    :important => true,
    :category => '3.Artifact',
    :order => 2
  }

attribute 'checksum',
  :description => "Checksum",
  :default => '',
  :format => {
    :help => 'The SHA256 checksum of the artifact package that is being downloaded',
    :category => '3.Artifact',
    :order => 3
  }

attribute 'path',
  :description => "Path",
  :default => '/nexus',
  :format => {
    :help => 'The repository path prefix',
    :category => '3.Artifact',
    :order => 4
  }

attribute 'install_dir',
  :description => "Install Directory",  
  :format => {
    :help => 'Directory path where the artifact will be downloaded and versions will be kept',
    :category => '4.Destination',
    :order => 1,
    :pattern => '^((?:[\/\$][\$\{\}a-zA-Z0-9]+(?:_[\$\{\}a-zA-Z0-9]+)*(?:\-[\$\{\}a-zA-Z0-9]+)*)+)$'
  }

attribute 'as_user',
  :description => "Deploy as user",
  :default => 'root',
  :format => {
    :help => 'System user to run the deploy as (root if not specified)',
    :category => '4.Destination',
    :order => 2
  }

attribute 'as_group',
  :description => "Deploy as group",
  :default => 'root',
  :format => {
    :help => 'System group to run the deploy as (root if not specified)',
    :category => '4.Destination',
    :order => 3
  }
  
attribute 'environment',
  :description => "Environment Variables",
  :data_type => 'hash',
  :default => '{}',
  :format => {
    :help => 'Specify variables that will be available in the environment during deployment',
    :category => '4.Destination',
    :order => 4
  }

attribute 'persist',
  :description => "Persistent Directories",
  :data_type => 'array',
  :default => '[]',
  :format => {
    :help => 'List of directories to be persisted across code updates (for example logs, tmp etc)',
    :category => '4.Destination',
    :order => 5
  }
  
attribute 'should_expand',
  :description => "Expand",
  :default => 'true',
  :format => {
    :help => 'By default (from open source version) will expand tgz, tar.gz, tar, zip, jar, war.',
    :category => '4.Destination',
    :form => { 'field' => 'checkbox' },    
    :order => 6
  }  

attribute 'configure',
  :description => "Configure",
  :data_type => "text",
  :format => {
    :help => 'Chef Resources to be executed to configure the artifact. Common resources: execute, bash, and service. examples) https://docs.chef.io/resources.html',
    :category => '5.Stages',
    :order => 1
  }

attribute 'migrate',
  :description => "Migrate",
  :data_type => "text",
  :format => {
    :help => 'Chef Resources to be executed to migrate the artifact. Common resources: execute, bash, and service. examples) https://docs.chef.io/resources.html',
    :category => '5.Stages',
    :order => 2
  }

attribute 'restart',
  :description => "Restart",
  :data_type => "text",
  :format => {
    :help => 'Chef Resources to be executed to restart the artifact. Common resources: execute, bash, and service. examples) https://docs.chef.io/resources.html',
    :category => '5.Stages',
    :order => 3
  }
    
recipe "redeploy", "Re-Deploy"
recipe "repair", "Repair"
