echo '==> Configuring OneOps for vagrant'

mkdir -p /home/oneops

mv /tmp/oneops-continuous.tar.gz /home/oneops/oneops-continuous.tar.gz

cd /home/oneops

tar zxf oneops-continuous.tar.gz -C /home/

azure=${azure_inductor:-false}

mkdir -p /home/oneops/dist/oneops-admin-inductor

if [ "$azure" = "true" ]; then
  tar zxf /home/oneops/dist/oneops-admin-*-inductor-az.tar.gz -C /home/oneops/dist/oneops-admin-inductor
else
  tar zxf /home/oneops/dist/oneops-admin-*-inductor.tar.gz -C /home/oneops/dist/oneops-admin-inductor
fi

mkdir -p /home/oneops/dist/oneops-admin-adapter && tar zxf /home/oneops/dist/oneops-admin-*-adapter.tar.gz -C /home/oneops/dist/oneops-admin-adapter

export BUILD_BASE='/home/oneops/build'
export OO_HOME='/home/oneops'
export GITHUB_URL='https://github.com/oneops'

mkdir -p $BUILD_BASE

if [ -d "$BUILD_BASE/dev-tools" ]; then
  echo "doing git pull on dev-tools"
  cd "$BUILD_BASE/dev-tools"
  git pull
else
  echo "doing dev tools git clone"
  cd $BUILD_BASE
  git clone "$GITHUB_URL/dev-tools.git"
fi
sleep 2

cd $OO_HOME

cp $BUILD_BASE/dev-tools/setup-scripts/* .

export PATH=$PATH:/usr/local/bin

# patch existing script from launching jenkins
sed -i 's/.*install_build_srvr.*/echo Skipping artifact build/' oneops_build.sh

# patch existing scripts
sed -i 's/tar.*xzvf.*oneops-continuous.*gz//' oneops_build.sh
sed -i 's/.*cms-db-pkg-continuous.*tar.*gz//' init_db.sh
sed -i 's/cd.*OO_HOME\/dist\/cms-db-pkg$//' init_db.sh
sed -i 's/cp.*single_db_schemas.*sql/cp \$OO_HOME\/dist\/oneops\/dist\/single_db_schemas.sql \/var\/lib\/pgsql\/single_db_schemas.sql/' init_db.sh
sed -i 's/cd.*OO_HOME\/dist\/cms-db-pkg\/db/cd \$OO_HOME\/dist\/oneops\/dist/' init_db.sh
sed -i 's/mv.*dist.*app.*gz .*gz/mv $OO_HOME\/dist\/app.tar.gz \/opt\/oneops\/app.tar.gz/' deploy_display.sh
sed -i 's/cp.*dist.*dist.*amq-config.*\/activemq.*tar.*gz/cp $OO_HOME\/dist\/amq-config.tar.gz  \/opt\/activemq\/amq-config.tar.gz/' deploy_amq.sh
sed -i 's/cp.*dist.*dist.*amqplugin.*\/activemq.*fat.*jar/cp $OO_HOME\/dist\/amqplugin-fat.jar  \/opt\/activemq\/lib\/amqplugin-fat.jar/' deploy_amq.sh
sed -i 's/service activemq stop/systemctl stop activemq/' deploy_amq.sh
sed -i 's/service activemq start/systemctl start activemq/' deploy_amq.sh
sed -i 's/tomcat7/tomcat/g' deploy_java.sh
sed -i 's/cp.*OO_HOME\/dist\/oneops\/dist.*war.*webapps/cp $OO_HOME\/dist\/\*\.war \/usr\/local\/tomcat\/webapps/' deploy_java.sh
sed -i 's/service.*stop/systemctl stop tomcat/' deploy_java.sh
sed -i 's/service.*start/systemctl start tomcat/' deploy_java.sh
sed -i 's/cp.*dist.*dist.*search.*jar/cp $OO_HOME\/dist\/search.jar \/opt\/oneops-search/' deploy_search.sh

cat > deploy_ooadmin.sh <<EOL
#!/bin/sh

set -e

echo "Deploying OneOps Admin "

cd $OO_HOME/dist/oneops-admin-inductor
INDUCTOR_GEM=\$(ls *.gem)
INDUCTOR_GEMFILE=\$(ls oneops-admin-inductor*.gemfile)
gem install \$INDUCTOR_GEM --ignore-dependencies --no-ri --no-rdoc
bundle install --gemfile=\$INDUCTOR_GEMFILE --local

# install forked fog-openstack
gem install fog-openstack/fog-openstack-0.1.24.gem --ignore-dependencies --no-ri --no-rdoc

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

echo "install inductor as ooadmin"
adduser ooadmin 2>/dev/null

chown -R ooadmin:ooadmin "$BUILD_BASE"

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
ln -sf /home/oneops/build/circuit-main-1 .
ln -sf /home/oneops/build/circuit-walmartlabs .
ln -sf /home/oneops/build/circuit-main-2 .
ln -sf /home/oneops/build/circuit-walmartlabs-2 .
inductor start
"
inductor install_initd
chkconfig --add inductor
chkconfig inductor on
echo "export INDUCTOR_HOME=/opt/oneops/inductor" > /opt/oneops/inductor_env.sh
echo "export PATH=$PATH:/usr/local/bin" >> /opt/oneops/inductor_env.sh

echo "done with inductor"

source /home/oneops/deploy_circuits.sh

EOL

chmod a+x /etc/init.d/display

./oneops_build.sh "$@"

if [ $? -ne 0 ]; then
  exit 1;
fi

source /tmp/create_user.sh
