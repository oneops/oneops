#!/bin/sh

export ONEOPS_ARCHIVE=$1
export PACKER_CACHE_DIR=$HOME/.packer/cache
export ONEOPS_VERSION=$(mvn -q -N -Dexec.executable="echo" -Dexec.args='${project.version}' exec:exec)

if [ -z $ONEOPS_ARCHIVE ]
then
  ONEOPS_ARCHIVE=target/distribution-${ONEOPS_VERSION}-oneops.tar.gz
fi

if [ ! -z $ONEOPS_ARCHIVE ]
then
  # Using a -var oneops_version=${ONEOPS_VERSION} doesn't seem to work in the json
  # template so using an envar in the json template instead. JvZ
  packer build -var "oneops_artifact=${ONEOPS_ARCHIVE}" -var-file=centos73.json centos.json
  box=`ls target/oneops-centos73-*.box`
  vagrant box add -f --name oneops $box
  mkdir -p ~/.oneops 
  cp -r vagrant ~/.oneops/vagrant
else
  echo "Cannot find the OneOps archive for Packer image: ${ONEOPS_ARCHIVE}"
fi
