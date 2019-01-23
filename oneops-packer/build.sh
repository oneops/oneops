#!/bin/bash

set -e

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASE_IMAGE_FILE="$CURRENT_DIR/centos73-oneops-base-virtualbox-ovf/centos73-oneops-base.ovf"

export ONEOPS_ARCHIVE=$1
export PACKER_CACHE_DIR=$HOME/.packer/cache
export ONEOPS_VERSION=$(mvn -q -N -Dexec.executable="echo" -Dexec.args='${project.version}' exec:exec)

declare -a circuits=(circuit-oneops-1 circuit-main-1 circuit-walmartlabs-1 circuit-main-2 circuit-walmartlabs-2)

echo "Cloning all circuits..."
sleep 2

circuits_path=$HOME/oneops_circuits

for circuit in "${circuits[@]}"
do
  if [ -d "$circuits_path/$circuit" ]; then
    echo -e "\ndoing git pull on $circuit\n"
    cd $circuits_path/$circuit
    git pull
  else
    mkdir -p $circuits_path
    cd $circuits_path
    git clone git@gecgithub01.walmart.com:walmartlabs/$circuit.git
  fi
  sleep 2
done

cd $CURRENT_DIR

if [ -z $ONEOPS_ARCHIVE ]
then
  ONEOPS_ARCHIVE=target/distribution-${ONEOPS_VERSION}-oneops.tar.gz
fi

if [ ! -f $BASE_IMAGE_FILE ]
then
  if [ ! -f "${HOME}/.vagrant.d/boxes/oneops-VAGRANTSLASH-centos73/2.0.21/virtualbox/box.ovf" ]
  then
    vagrant box add oneops/centos73 --box-version 2.0.21 --provider virtualbox
  fi
  packer build centos-oneops-base.json
fi

if [ ! -z $ONEOPS_ARCHIVE ]
then
  if [ -f $BASE_IMAGE_FILE ]
  then
    # Using a -var oneops_version=${ONEOPS_VERSION} doesn't seem to work in the json
	# template so using an envar in the json template instead. JvZ
	packer build -var "oneops_artifact=${ONEOPS_ARCHIVE}" -var "oneops_base_image=$BASE_IMAGE_FILE" centos-oneops.json
	box=`ls box/centos73-oneops*.box`
	vagrant box add -f --name oneops $box
	mkdir -p ~/.oneopsuni 
	cp -r vagrant ~/.oneopsuni/vagrant
  fi
else
  echo "Cannot find the OneOps archive for Packer image: ${ONEOPS_ARCHIVE}"
fi
