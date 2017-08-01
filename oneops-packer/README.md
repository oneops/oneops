# OneOps Vagrant

You need to have [Packer][1] installed. We assume you can figure that out by yourself. Once you have Packer installed you can run the build from the top-level directory using:

```
mvn clean package -Pvagrant
```

The Vagrant process takes 10+ minutes so it's not run as part of the default Maven build. As long as you've run `mvn clean package` from the top-level directory you can run the `./build.sh` script to build the Vagrant box.

Once the process is finished the Vagrant box will be installed in `~/.vagrant.d/boxes` for you and the `~/.oneops/vagrant` directory will be ready to use with standard Vagrant commands.

```
cd ~/.oneops/vagrant
vagrant up
```

The OneOps box will boot and be ready for you to use.

[1]: https://www.packer.io
