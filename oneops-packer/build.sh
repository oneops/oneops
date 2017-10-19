#!/bin/sh

set -e

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
BASE_IMAGE_FILE="$CURRENT_DIR/centos73-oneops-base-virtualbox-ovf/centos73-oneops-base.ovf"

export ONEOPS_ARCHIVE=$1
export PACKER_CACHE_DIR=$HOME/.packer/cache
export ONEOPS_VERSION=$(mvn -q -N -Dexec.executable="echo" -Dexec.args='${project.version}' exec:exec)

if [ -z $ONEOPS_ARCHIVE ]
then
  ONEOPS_ARCHIVE=target/distribution-${ONEOPS_VERSION}-oneops.tar.gz
fi

if [ ! -f $BASE_IMAGE_FILE ]
then
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
