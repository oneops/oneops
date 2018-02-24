# OneOps Vagrant

You need to have [Packer][1] installed. We assume you can figure that out by yourself. Once you have Packer installed you can run the build from the top-level directory using:

```
mvn clean package -Pvagrant
```
By default Packer will build non-Azure inductor as part of the build, but if you need Azure build you must override environment variable **AZURE_INDUCTOR**.

```
AZURE_INDUCTOR=true mvn clean package -Pvagrant
```

The Vagrant process takes 10+ minutes so it's not run as part of the default Maven build. As long as you've run `mvn clean package` from the top-level directory you can run the `./build.sh` script to build the Vagrant box.

Once the process is finished the Vagrant box will be installed in `~/.vagrant.d/boxes` for you and the `~/.oneops/vagrant` directory will be ready to use with standard Vagrant commands.

```
cd ~/.oneops/vagrant
vagrant up
```

The OneOps box will boot and be ready for you to use.

# Technical Details

## MacOS X

## [Homebrew][4]
#### Install
```
/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```
## [Virtualbox][2]
#### Install
```
brew cask install virtualbox
```
#### Install
## [Vagrant][3]

```
brew cask install vagrant
```

We are going to use oneops/centos73 as the minimal image as our starting point.

```
vagrant box add oneops/centos73 --box-version 2.0.21 --provider virtualbox
```

URL: https://vagrantcloud.com/oneops/boxes/centos73/versions/2.0.21/providers/virtualbox.box

---
## Ubuntu 17.10


## [Virtualbox][2]
#### Install
```
apt-get install virtualbox
```
#### Install
## [Vagrant][3]

```
apt-get install vagrant
```

We are going to use oneops/centos73 as the minimal image as our starting point.

```
vagrant box add oneops/centos73 --box-version 2.0.21 --provider virtualbox
```

URL: https://vagrantcloud.com/oneops/boxes/centos73/versions/2.0.21/providers/virtualbox.box

---
## Packer Templates
-  **centos-oneps-base.json** : Build the initial .ovf container which has oneops/centos73 along with all the pre-requisites for OneOps.
-  **centos-oneops.json** : Install OneOps's runtime application along with various circuits.

## Build Image

### Base .ovf

The first step is to build base image with all the pre-requisites pre-installed from oneops/centos73 image.  Normally we can boostrap from iso but that process itself is long and unncessary.   Instead of using virtualbox-iso builder we are going to use virtualbox-ovf.  We are going to configure our builder to rely on existing ovf file.

**e.g:** "source_path": "{{ user `home_dir` }}/.vagrant.d/boxes/oneops-VAGRANTSLASH-centos73/2.0.21/virtualbox/box.ovf"

This step is necessary because we can't use **env** helper in builder definition section.

**home_dir** is defined as "{{ env `HOME`}}" - this effectively will translate into the value of home directory where vagrant stores the .ovf for the pre-installed image.
	
```packer build centos-oneop-base.json```

If the command completed successfully it will create an ovf file in the directory **centos73-oneops-base-virtualbox-ovf**.  This is important because this file is required to build the final image of AIO OneOps.

### OneOps .box

```packer build -var "oneops_artifact=<oneops_artifact>" -var "oneops_base_image=<oneops_base_image>" centos-oneops.json```

Please substitute the value of **<oneops_artifact>** and **<oneops_base_image>** with the desired values.

**e.g:** "oneops_base_image=/Users/myuserid/oneops/oneops-packer/centos73-oneops-base-virtualbox-ovf/centos73-oneops-base.ovf"

**e.g:** "oneops_artifact=../oneops-distribution/target/distribution-17.09.20-21-SNAPSHOT.tar.gz"

*Important* Packer doesn't understand relative path for source_path attribute in the builder therefore **oneops_base_image** has to be a full path.


#### Circuits

By default **oneops-circuit-1** will be the only circuit configured to be installed, however we have provide a mean to install other circuits.  In **centos-oneps.json** template there is a variable called **oneops_circuits** that has default value of **circuits** that can be used to add additional circuits.

There are two ways to add other circuits:

1.   Copy other circuits into **circuits** directory under **oneops-packer**.
2.   Override **oneops_circuits** with different values. e.g: "-var oneops_circuits=(path to other circuits)"


#### Troubleshooting

By default if Packer encounters any issues it will halt and destroy the current Virtualbox's instance.  This behavior will ensure that it doesn't leave any orphaned Virtualbox's VM around.  However in the event if faults are detected and Packer aborted prematurely we must configure Packer to not destroy the VM.  We need the VM in question to troubleshoot therefore we must deviate from normal build flow.

```
Same as above command, but this time we will insert an argument to force Packer to either cleanup|abort|ask, please notice that cleanup is the normal behavior without using this argument.

-on-error=abort   Abort immediately but leave the VM intact.
-on-error=ask     Prompt us to decide if we want to cleanup or abort.

```

[1]: https://www.packer.io
[2]: https://www.virtualbox.org
[3]: https://www.vagrantup.com
[4]: https://brew.sh
