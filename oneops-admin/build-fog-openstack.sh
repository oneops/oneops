#!/bin/bash

OPENSTACK_GIT_REPO=${GIT_REPO:=https://github.com/oneops/fog-openstack.git}

if [ -d "fog-openstack" ]; then
  \rm -rf fog-openstack
fi

git clone -b Ruby2.0.0 $OPENSTACK_GIT_REPO

cd fog-openstack

gem build fog-openstack.gemspec --force

mkdir -p ../vendor/cache

\cp -f fog-openstack-0.1.24.gem ../fog-openstack-0.1.24.gem

\cp -f fog-openstack-0.1.24.gem ../vendor/cache/fog-openstack-0.1.24.gem