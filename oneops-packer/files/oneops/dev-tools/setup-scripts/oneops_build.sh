#!/bin/sh

export BUILD_BASE='/home/oneops/build'
export OO_HOME='/home/oneops'
export SEARCH_SITE=localhost
export GITHUB_URL='https://github.com/oneops'
export GITHUB_CIRCUIT_URL=git@gecgit:walmartlabs

now=$(date +"%T")
echo "Starting at : $now"

echo "Stopping services to free up memory for build"
service cassandra stop

echo Skipping artifact build

if [ $? -ne 0 ]; then
	exit 1;
fi

now=$(date +"%T")
echo "Completed git build : $now"

echo "Starting services after build before deploy"
service cassandra start

source $OO_HOME/init_db.sh

now=$(date +"%T")
echo "Deploying artifacts: $now "

cd $OO_HOME/dist


cd $OO_HOME

export RAILS_ENV=development
export OODB_USERNAME=kloopz
export OODB_PASSWORD=kloopz

source $OO_HOME/deploy_display.sh

source $OO_HOME/deploy_amq.sh

source $OO_HOME/deploy_java.sh

source $OO_HOME/deploy_search.sh

source $OO_HOME/deploy_ooadmin.sh

cd /opt/oneops

#nohup rails server >> /opt/oneops/log/rails.log 2>&1 &
service display start

echo "OneOps installation completed."
echo "The user interface is mapped to port 3000 and available at http://localhost:9090 on the host machine."
echo "Configure your port forwarding and shut down iptables service (or configure it) if needed."
