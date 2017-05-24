
echo '==> Configuring OneOps for vagrant'

mkdir -p /home/oneops

mv /tmp/oneops-continuous.tar.gz /home/oneops/oneops-continuous.tar.gz

cd /home/oneops

tar zxvf oneops-continuous.tar.gz -C /home/

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
sed -i 's/cd.*OO_HOME\/dist\/cms-db-pkg.*//' init_db.sh
sed -i 's/cp.*single_db_schemas.*sql/cp \$OO_HOME\/dist\/oneops\/dist\/single_db_schemas.sql \/var\/lib\/pgsql\/single_db_schemas.sql/' init_db.sh
sed -i 's/cd.*OO_HOME\/dist\/oneops\/dist\/db/cd \$OO_HOME\/dist\/oneops\/dist/' init_db.sh
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
sed -i 's/gem install.*oneops-admin.*/gem install $OO_HOME\/dist\/oneops-admin-1.0.0.gem --no-ri --no-rdoc/' deploy_ooadmin.sh

./oneops_build.sh "$@"

if [ $? -ne 0 ]; then
  exit 1;
fi

