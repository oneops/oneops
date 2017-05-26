# OneOps Packer 

[packer.io](https://www.packer.io/) + [OneOps Build](https://github.com/oneops/oneops-build-converter) = OneOps Single Stand Alone Instance


## Requirements

A number of tools the typical development tools are required including

- git
- JDK 8
- Ruby

In addition Packer is needed and details follow below.

### packer.io

This project required [packer.io](https://www.packer.io) therefore it must be
installed on machine that is going to be running this tool.

Packer is easy to use and automates the creation of any type of machine
image. It embraces modern configuration management by encouraging you to use
automated scripts to install and configure the software within your Packer-made
images. Packer brings machine images into the modern age, unlocking untapped
potential and opening new opportunities.

Install following the [official instructions](https://www.packer.io/intro/getting-started/install.html) or 
if you're using OS X and [Homebrew](https://brew.sh), you can install Packer by running:

```
$ brew install packer
```

If you're using Windows and [Chocolatey](http://chocolatey.org/), you can install Packer by running:
```
choco install packer
```
## Initial Setup

First clone the repository.

Then init and update the submodule for the oneops-build-converter

```
cd oneops-packer
git submodule update --init oneops-build-converter/
```

Now you should have all the source code in place.

## Building

Simply run:

```
sh build-oneops.sh
```

Yes that's all to it!  If everything works [packer](https://packer.io) will
output a box file that can be imported into Vagrant.

This tool depends on
[OneOps Build](https://github.com/oneops/oneops-build-converter) of which you
can get most up-to-date by running:

```
sh build-oneops.sh -f
```

This will clean up everything and pull the latest
[OneOps Build](https://github.com/oneops/oneops-build-converter) into the
workspace.
