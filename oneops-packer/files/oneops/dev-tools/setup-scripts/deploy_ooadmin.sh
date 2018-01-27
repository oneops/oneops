#!/bin/sh

set -e

echo "Deploying OneOps Admin "

cd $OO_HOME/dist/oneops-admin-inductor
INDUCTOR_GEM=\$(ls *.gem)
INDUCTOR_GEMFILE=\$(ls oneops-admin-inductor*.gemfile)
gem install \$INDUCTOR_GEM --ignore-dependencies --no-ri --no-rdoc
bundle install --gemfile=\$INDUCTOR_GEMFILE --local

cd $OO_HOME/dist/oneops-admin-adapter
gem install oneops-admin-adapter-1.0.0.gem --ignore-dependencies --no-ri --no-rdoc
bundle install --gemfile=oneops-admin-adapter.gemfile --local

cd $OO_HOME/dist/oneops-admin-inductor
# install test-kitchen
bundle install --gemfile=test-kitchen.gemfile

mkdir -p /opt/oneops-admin
cd /opt/oneops-admin

export CIRCUIT_LOCAL_ASSET_STORE_ROOT=/opt/oneops/app/public/_circuit

rm -fr circuit
circuit init

cd "$BUILD_BASE"

if [ -d "$BUILD_BASE/circuit-oneops-1" ]; then
  echo "doing git pull on circuit-oneops-1"
  cd "$BUILD_BASE/circuit-oneops-1"
  git pull
else
  echo "doing git clone"
  git clone "$GITHUB_URL/circuit-oneops-1.git"
fi
sleep 2

cd "$BUILD_BASE/circuit-oneops-1"
circuit install

echo "install inductor as ooadmin"
adduser ooadmin 2>/dev/null

chown -R ooadmin:ooadmin "$BUILD_BASE/circuit-oneops-1"

cd /opt/oneops
chown ooadmin /opt/oneops
su ooadmin -c "
inductor create
cd inductor
# add inductor using shared queue
inductor add --mqhost localhost \
--dns on \
--debug on \
--daq_enabled true \
--collector_domain localhost \
--tunnel_metrics on \
--perf_collector_cert /etc/pki/tls/logstash/certs/logstash-forwarder.crt \
--ip_attribute public_ip \
--queue shared \
--mgmt_url http://localhost:9090 \
--logstash_cert_location /etc/pki/tls/logstash/certs/logstash-forwarder.crt \
--logstash_hosts vagrant.oo.com:5000 \
--max_consumers 10 \
--local_max_consumers 10 \
--authkey superuser:amqpass \
--amq_truststore_location /opt/oneops/inductor/lib/client.ts \
--additional_java_args \"\" \
--env_vars \"\" \
--verifier_mode true
mkdir -p /opt/oneops/inductor/lib
\cp /opt/activemq/conf/client.ts /opt/oneops/inductor/lib/client.ts
ln -sf /home/oneops/build/circuit-oneops-1 .
inductor start
"
inductor install_initd
chkconfig --add inductor
chkconfig inductor on
echo "export INDUCTOR_HOME=/opt/oneops/inductor" > /opt/oneops/inductor_env.sh
echo "export PATH=$PATH:/usr/local/bin" >> /opt/oneops/inductor_env.sh

echo "done with inductor"

source /home/oneops/deploy_circuits.sh
