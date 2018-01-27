#!/bin/sh


echo "Deploying AMQ plugin: $now "

cp $OO_HOME/dist/amq-config.tar.gz  /opt/activemq/amq-config.tar.gz 
cp $OO_HOME/dist/amqplugin-fat.jar  /opt/activemq/lib/amqplugin-fat.jar

cd /opt/activemq

systemctl stop activemq

tar -xzvf amq-config.tar.gz

cd /opt/activemq/conf

if [ -e activemq.orig ]; then
cp activemq.xml activemq.orig
fi

cp amq_local_kahadb.xml activemq.xml

cp /opt/activemq/keys/* .

systemctl start activemq

cd $OO_HOME
echo "Done with AMQ"

