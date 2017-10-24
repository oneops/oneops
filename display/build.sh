#!/bin/sh

set -e

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

if [ ! -f "${HOME}/.vagrant.d/boxes/box-cutter-VAGRANTSLASH-centos73/2.0.21/virtualbox/box.ovf" ]
then
  vagrant box add box-cutter/centos73 --box-version 2.0.21 --provider virtualbox
fi

b=oneops_display_tmp.tar.gz

[ -e ${b} ] && rm -rf ${b}

[ -e "vendor" ] && rm -rf "vendor"

tar --exclude="${b}" --exclude="packer" -zcf $b .

cd packer

[ -e "centos73-oneops-display-virtualbox-ovf" ] && rm -rf "centos73-oneops-display-virtualbox-ovf"

packer build -var "oneops_display_archive=${CURRENT_DIR}/oneops_display_tmp.tar.gz" \
-var "oneops_display_vendor=${CURRENT_DIR}/vendor.tar.gz" centos-oneops-display.json

[ -e "centos73-oneops-display-virtualbox-ovf" ] && rm -rf "centos73-oneops-display-virtualbox-ovf"

cd ${CURRENT_DIR}

tar zxf vendor.tar.gz && rm -rf vendor.tar.gz

rm -rf ${b}