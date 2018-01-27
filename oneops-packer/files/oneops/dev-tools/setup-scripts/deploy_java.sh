#!/bin/sh

now=$(date +"%T")

echo "Deploying Tomcat web apps: $now "

mkdir -p /usr/local/oneops/certs

if [ ! -e /usr/local/oneops/certs/oo.key ]; then
cd /usr/local/oneops/certs
dd if=/dev/urandom count=24 bs=1 | xxd -ps > oo.key
truncate -s -1 oo.key
chmod 400 oo.key
chown tomcat:root oo.key
fi

cd /usr/local/tomcat/webapps/

systemctl stop tomcat

rm -rf *

cp $OO_HOME/dist/*.war /usr/local/tomcat/webapps

cp $OO_HOME/tom_setenv.sh /usr/local/tomcat/bin/setenv.sh
chown tomcat:root /usr/local/tomcat/bin/setenv.sh

mkdir -p /opt/oneops/controller/antenna/retry
mkdir -p /opt/oneops/opamp/antenna/retry
mkdir -p /opt/oneops/cms-publisher/antenna/retry
mkdir -p /opt/oneops/transmitter/antenna/retry
mkdir -p /opt/oneops/transmitter/search/retry
mkdir -p /opt/oneops/controller/search/retry
mkdir -p /opt/oneops/opamp/search/retry

systemctl start tomcat

now=$(date +"%T")
echo "Done with Tomcat: $now "


