#!/bin/sh

now=$(date +"%T")
echo "Deploying Search consumer: $now "

export OO_HOME=/home/oneops

mkdir -p /opt/oneops-search
mkdir -p /opt/oneops-search/log

service search-consumer stop

cp $OO_HOME/dist/search.jar /opt/oneops-search /opt/oneops-search
cp $OO_HOME/start-consumer.sh /opt/oneops-search


service search-consumer start

now=$(date +"%T")
echo "Done with search-consumer: $now "


