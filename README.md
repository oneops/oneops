# OneOps Vagrant

[packer.io](https://www.packer.io/) + [OneOps Build](https://github.com/oneops/oneops) = OneOps Single Stand Alone Instance

## Requirements

The following tools are required for building OneOps Vagrant box:

- git
- JDK 8
- Ruby
- [Packer][1]

## Initial Setup

The Vagrant process depends on the full build being run, but the Vagrant image is not built by default in the main build because it takes 10+ minutes. Make sure from the main build you have at least run `mvn clean package`. This will ensure artifacts required by the Vagrant build are in place.

## Building

You can use Maven to build the Vagrant box:

`mvn clean package`

Alternatively you can use the `build.sh` script that Maven uses directly:

`./build.sh`

Once the OneOps Vagrant box is created, you and added it to your collection of boxes:

```
vagrant box add -f --name oneops target/oneops-centos73-${version}.box
```

This will place the just created box in ~/.vagrant.d/boxes

You need a Vagrantfile that looks like this:

```
Vagrant.configure(2) do |config|

 config.vm.box = "oneops"

 # Use the vagrant-cachier plugin, if installed, to cache downloaded packages
  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end

  config.vm.network "forwarded_port", guest: 3001, host: 3003
  config.vm.network "forwarded_port", guest: 3000, host: 9090
  config.vm.network "forwarded_port", guest: 8080, host: 9091
  config.vm.network "forwarded_port", guest: 8161, host: 8166

 config.vm.provider "virtualbox" do |vb|
   vb.gui = false
   vb.memory = 6144
   vb.customize ["modifyvm", :id, "--cpuexecutioncap", "70"]
  end
end
```

And then you can use standard Vagrant commands to start the VM.s

[1]: https://www.packer.io
