# Artifact cookbook

Provides your cookbooks with the Artifact Deploy LWRP

# Requirements

* Chef 10
* Vagrant

# Platforms

* CentOS
* Fedora
* Windows >= 6.0
  * Windows Vista
  * Windows 2008 R2
  * Windows 7

# Vagrant

With Vagrant 1.1, there is no longer a Vagrant RubyGem to install. Instead, follow the instructions on the [VagrantUp](http://docs.vagrantup.com/v2/installation/index.html) documentation pages.

# Resources / Providers

## artifact_deploy

Deploys a collection of build artifacts packaged into a tar ball. Artifacts are extracted from
the package and managed in a deploy directory in the same fashion you've seen in the Opscode
deploy resource or Capistrano's default deploy strategy.

### Actions
Action   | Description                   | Default
-------  |-------------                  |---------
deploy   | Deploy the artifact package   | Yes
pre_seed | Pre-seed the artifact package |

### Attributes
Attribute                  | Description                                                                          |Type     | Default
---------                  |-------------                                                                         |-----    |--------
artifact_name              | Name of the artifact package to deploy                                               | String  | name
artifact_location          | URL, S3 path, local path, or Maven identifier of the artifact package to download    | String  |
artifact_checksum          | The SHA256 checksum of the artifact package that is being downloaded                 | String  |
deploy_to                  | Deploy directory where releases are stored and linked                                | String  |
version                    | Version of the artifact being deployed                                               | String  |
owner                      | Owner of files created and modified                                                  | String  |
group                      | Group of files created and modified                                                  | String  |
environment                | An environment hash used by resources within the provider                            | Hash    | Hash.new
symlinks                   | A hash that maps files in the shared directory to their paths in the current release | Hash    | Hash.new
shared_directories         | Directories to be created in the shared folder                                       | Array   | %w{ log pids }
force                      | Forcefully deploy an artifact even if the artifact has already been deployed         | Boolean | false
should_migrate             | Notify the provider if it should perform application migrations                      | Boolean | false
keep                       | Specify a number of artifacts deployments to keep on disk                            | Integer | 2
before_deploy              | A proc containing resources to be executed before the deploy process begins          | Proc    |
before_extract             | A proc containing resources to be executed before the artifact package is extracted  | Proc    |
after_extract              | A proc containing resources to be executed after the artifac package is extracted    | Proc    |
before_symlink             | A proc containing resources to be executed before the symlinks are created           | Proc    |
after_symlink              | A proc containing resources to be executed after the symlinks are created            | Proc    |
configure                  | A proc containing resources to be executed to configure the artifact package         | Proc    |
before_migrate             | A proc containing resources to be executed before the migration Proc                 | Proc    |
migrate                    | A proc containing resources to be executed during the migration stage                | Proc    |
after_migrate              | A proc containing resources to be executed after the migration Proc                  | Proc    |
restart                    | A proc containing resources to be executed at the end of a successful deploy         | Proc    |
after_deploy               | A proc containing resources to be executed after the deploy process ends             | Proc    |
ssl_verify                 | Used to set whether or not communications with a Nexus server should be SSL verified | Boolean | true
remove_top_level_directory | Deletes a top level directory from the extracted zip file                            | Boolean | false
skip_manifest_check        | Skips the manifest check for idempotency when the version attribute is not changing  | Boolean | false 
remove_on_force            | Removes the current version directory contents when force is set                     | Boolean | false

### Deploy Flow, the Manifest, and Procs

The deploy flow is outlined in the Artifact Deploy flow chart below. 

![Artifact Deploy](http://riotgames.github.com/artifact-cookbook/images/ArtifactDeployFlow.png)

For a more detailed flow of what happens when we check with `deploy?`, see the [Manifest Differences Flow chart.](http://riotgames.github.com/artifact-cookbook/images/ManifestDifferencesFlow.png)

The 'happy-path' of this flow is the default path when an artifact has already been deploy - there will be no need to
execute many of the Procs. That being said, there are a few 'choice' paths through the flow where a Proc may affect the
flow.

There are two checks in the artifact deploy flow where a *manifest* check is executed - at the beginning, before the *before_deploy* proc,
and just after the *configure* proc (and after the *migrate* procs). When the latter check returns true, the *restart* proc will execute.

The *manifest* is a YAML file with a mapping of files in the deploy path to their SHA1 checksum. For example:

```
/srv/artifact_test/releases/2.0.68/log4j.xml: 96be5753fbf845e30b643fa04008f2c4fe6956a7
/srv/artifact_test/releases/2.0.68/readme.txt: fcb8d816b062565930f19f9bdb954f5ac43c5039
/srv/artifact_test/releases/2.0.68/my-artifact.jar: 42ad63cc883afad010573d3d8eea4e5a4011e5d4
```

There are numerous Procs placed throughout the flow of the artifact_deploy resource. They are meant to give the user many different
ways to configure the artifact and execute resources during the flow. Some good examples include executing a resource to stop a service
in the *before_deploy* proc, or placing configuration files in the deployed artifact during the *configure* proc.

**Please note** the *before_deploy*, *configure*, and *after_deploy* procs are executed on every Chef run. It is recommended that any *template*
(or configuration changing resource calls) take place within those procs. In particular, the *configure* proc was added for this very purpose. Following
this pattern will ensure that the templates will change, and the *restart* proc will execute (perhaps restarting the service the configured artifact provides
in order to pick up the configuration changes).

Procs can also utilize the internal methods of the provider class, because they are evaluated inside of the instance of the provider class. For example:

```
artifact_deploy "artifact_test" do
  # omitted for brevity
  configure Proc.new {
    # release_path is an attr_reader on the @release_path variable
    template "#{release_path}/conf/config.properties" do
      source "config.properties.erb"
      variables(:config => config)
    end
  }
end
```

## artifact_file

Downloads a file from a provided location and then verifies that the integrity of the file is intact. Artifact files from Nexus
will check with the Nexus Server to verify the SHA1 of the downloaded file. Artifact files from an HTTP or S3 source will either use
the provided SHA256 checksum to verify integrity or skip the check if no checksum is given.

### Actions
Action   | Description                   | Default
-------  |-------------                  |---------
create   | Download the artifact file    | Yes

### Attributes
Attribute              | Description                                                                          |Type     | Default
---------              |-------------                                                                         |-----    |--------
path                   | The path to download the artifact to                                                 | String  | name
location               | The location to the artifact file. Either a nexus identifier, S3 path or URL         | String  |
checksum               | The SHA256 checksum for verifying URL downloads. Not used when location is Nexus     | String  |
owner                  | Owner of the downloaded file                                                         | String  |
group                  | Group of the downloaded file                                                         | String  |
download_retries       | The number of times to attempt to download the file if it fails its integrity check  | Integer | 1

### Downloading files using artifact_file

In its simplest state, the artifact_file resource is a wrapper for the remote_file resource for Nexus and URL locations. The key addition is retry logic and integrity checking
for the downloaded files. Below is a brief description of the logic flow for the resource:

* Download the file using remote_file resource.
* Check the file's integrity
  * Is it from the Nexus?
      * Check the SHA1 of the downloaded file against Nexus Server's SHA1. Returns false if they are not equal.
  * Not from Nexus - Is the checksum attribute defined for the resource?
      * If defined - Check the SHA256 of the downloaded file against the checksum attribute. Returns false if they are not equal.
      * If not defined - log a message and return true.

When the logic returns true, the downloaded file is considered good and the resource will exit. When the logic above returns false, the downloaded file is considered
corrupt and an attempt will be made to download the file again. The number of retries can be controlled with the `download_retries` attribute.

### Documentation

The RDocs for the deploy.rb provider can be found under the [Top Level Namespace](http://riotgames.github.com/artifact-cookbook/doc/top-level-namespace.html) page
for this repository.

### Nexus Usage

In order to deploy an artifact from a Nexus repository, you must first create
an [encrypted data bag](http://wiki.opscode.com/display/chef/Encrypted+Data+Bags) that contains
the credentials for your Nexus repository.

    knife data bag create artifact _wildcard -c <your chef config> --secret-file=<your secret file>

Your data bag should look like the following:

    {
      "id": "_wildcard",
      "nexus": {
        "username": "nexus_user",
        "password": "nexus_user_password",
        "url": "http://nexus.yourcompany.com:8081/nexus/",
        "repository": "your_repository"
      }
    }

After your encrypted data bag is setup you can use Maven identifiers
for your artifact_location. A Maven identifier is shown as a colon-separated string
that includes three elemens - groupId:artifactId:extension - ex. "com.my.artifact:my-artifact:tgz". 
If many environments share the same configuration, you can provide environment specific configuration in
separate data_bag items:

    knife data bag create artifact production -c <your chef config> --secret-file=<your secret file>

    {
      "id": "production",
      "nexus": {
        "username": "nexus_production_user",
        "password": "nexus_production_user_password",
        "url": "http://nexus.yourcompany.com:8081/nexus/",
        "repository": "your_repository"
      }
    }

    knife data bag create artifact development -c <your chef config> --secret-file=<your secret file>

    {
      "id": "development",
      "nexus": {
        "username": "nexus_dev_user",
        "password": "nexus_dev_user_password",
        "url": "http://nexus-dev.yourcompany.com:8081/nexus/",
        "repository": "your_repository"
      }
    }

#### S3 Usage

S3 can be used as a source of an archive.  The location path must be in the form ```s3://bucket-name/path/to/archive.tar.gz```.  You can provide AWS credentials in the data_bag,
or if you are running on EC2 and are using IAM Instance Roles - you may omit the credentials and use the Instance Role. Alternatively, if the credentials are available on the
environment they will be used from there (more information on the Environment variable keys an be found <http://docs.aws.amazon.com/AWSSdkDocsRuby/latest/DeveloperGuide/ruby-dg-roles.html>).

This is example IAM policy to get an artifact from S3. Note that this policy will limit permissions to just the files contained under the ```deploys/``` path.
If you wish to keep them in the root of your bucket, just omit the ```deploys/``` portion and put ```<your-s3-bucket>/*```:

    {
      "Statement": [
        {
          "Sid": "Stmt1357328135477",
          "Action": [
            "s3:GetObject",
            "s3:ListBucket"
          ],
          "Effect": "Allow",
          "Resource": [
            "arn:aws:s3:::<your-s3-bucket>/deploys/*",
            "arn:aws:s3:::<your-s3-bucket>"
          ]
        }
      ]
    }

If you wish to provide your AWS credentials in a data_bag, the format is:

    {
      "id": "_wildcard",
      "aws": {
        "access_key_id": "my_access_key",
        "secret_access_key": "my_secret_access_key"
      }
    }

Your data_bag can contain both ```nexus``` and ```aws``` configuration.

### Examples

##### Deploying a Rails application

    artifact_deploy "pvpnet" do
      version "1.0.0"
      artifact_location "https://artifacts.location.riotgames.com/pvpnet-1.0.0.tar.gz"
      deploy_to "/srv/pvpnet"
      owner "riot"
      group "riot"
      environment { 'RAILS_ENV' => 'production' }
      shared_directories %w{ data log pids system vendor_bundle assets }

      before_deploy Proc.new {
        bluepill_service 'pvpnet-unicorn' do
          action :stop
        end
      }

      before_migrate Proc.new {
        template "#{shared_path}/database.yml" do
          source "database.yml.erb"
          owner node[:merlin][:owner]
          group node[:merlin][:group]
          mode "0644"
          variables(
            :environment => environment,
            :options => database_options
          )
        end
        
        execute "bundle install --local --path=vendor/bundle --without test development cucumber --binstubs" do
          environment { 'RAILS_ENV' => 'production' }
          user "riot"
          group "riot"
        end
      }

      migrate Proc.new {
        execute "bundle exec rake db:migrate" do
          cwd release_path
          environment { 'RAILS_ENV' => 'production' }
          user "riot"
          group "riot"
        end
      }

      after_migrate Proc.new {
        ruby_block "remove_run_migrations" do
          block do
            Chef::Log.info("Migrations were run, removing role[pvpnet_run_migrations]")
            node.run_list.remove("role[pvpnet_run_migrations]")
          end
        end
      }

      configure Proc.new {
        template "/srv/pvpnet/current/config.properties" do
          source "config.properties.erb"
          owner 'riot'
          group 'riot'
          variables(:database_config => node[:pvpnet_cookbook][:database_config])
        end
      }

      restart Proc.new {
        bluepill_service 'pvpnet-unicorn' do 
          action :restart
        end
      }

      keep 2
      should_migrate (node[:pvpnet][:should_migrate] ? true : false)
      force (node[:pvpnet][:force_deploy] ? true : false)
      action :deploy
    end

##### Deploying the latest from Nexus (Changed in > 1.0.0)

    artifact_deploy "my-artifact" do
      version           "latest"
      artifact_location "com.foo:my-artifact:tgz"
      deploy_to         "/opt/my-artifact"
      owner             "artifact"
      group             "artifact"

      before_extract Proc.new {
        service "my-artifact" do
          action :stop
        end
      }

      configure Proc.new {
        template "#{release_path}/conf/config.properties" do
          source "config.properties.erb"
          owner  "artifact"
          group  "artifact"
          variables(:config => node[:my_artifact_cookbook][:config])
        end
      }

      restart Proc.new {
        service "my-artifact" do
          action :start
        end
      }
    end

##### Deploying an artifact from Amazon S3

    artifact_deploy "my-artifact" do
      version           "1.0.0"
      artifact_location "s3://my-website-deployments/deploys/my-artifact-1.0.0.tgz"
      deploy_to         "/srv/my-artifact"
      owner             node[:artifact_owner]
      group             node[:artifact_group]
      symlinks({
        "log" => "log"
      })
    end

##### Configuring an artifact_deploy that may need to change over many Chef runs

    artifact_deploy "my-artifact" do
      version           "1.0.0"
      artifact_location "http://www.fooo.com/my-artifact-1.0.0.tgz"
      deploy_to         "/srv/my-artifact"
      owner             node[:artifact_owner]
      group             node[:artifact_group]
      symlinks({
        "log" => "log"
      })
      force             node[:force_deploy]
    end

##### Using artifact_file to download a file from a URL

    artifact_file "/tmp/my-artifact.tgz" do
      location "http://www.my-website.com/my-artifact-1.0.0.tgz"
      owner "me"
      group "mes"
      action :create
    end

#### Using artifact_file to download a file from Nexus

    artifact_file "/tmp/my-artifact.tgz" do
      location "com.test:my-artifact:1.0.0:tgz"
      owner "me"
      group "mes"
      action :create
    end

  Configuring your resource in this manner will allow you to ensure it can always change when you need it to. In other words,
  configuring the `force` attribute to a node attribute, will allow you to change some of the more finer grained aspects of the
  resource. For example, when force is true, you can also change the value of owner and group to remap the deployed artifact to
  a new permissions scheme.

##### Using artifact_file to download a file from an S3 bucket

    artifact_file "/tmp/my-artifact.tgz" do
      location "s3://my-website-deployments/deploys/my-artifact-1.0.0.tgz"
      owner "me"
      group "mes"
      checksum "fcb188ed37d41ff2cbf1a52d3a11bfde666e036b5c7ada1496dc1d53dd6ed5dd"
      action :create
    end

# Testing

A sample cookbook is available in `fixtures`. You can package it with mkartifact.sh, and
upload it to Nexus as artifact_cookbook:test:1.2.3:tgz.

Set the artifact_test_location and artifact_test_version environment variables when running
vagrant to change how they'll be provisioned. Default is 1.2.3 from a file URL.

* artifact_test_location=artifact_cookbook:test:1.2.3:tgz artifact_test_version=1.2.3 bundle exec vagrant

# Releasing

1. Install the prerequisite gems
    
        $ gem install chef
        $ gem install thor

2. Increment the version number in the metadata.rb file

3. Run the Thor release task to create a tag and push to the community site

        $ thor release

# License and Author

Author:: Jamie Winsor (<jamie@vialstudios.com>)<br/>
Author:: Kyle Allan (<kallan@riotgames.com>)

Copyright 2013, Riot Games

See LICENSE for license details
