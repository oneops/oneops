#!/bin/bash -eux

echo '==> Configuring tomcat for vagrant'

cd /opt

wget -nv https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.67/bin/apache-tomcat-7.0.67.tar.gz

if [ $? -ne 0 ];
then
	echo "Unable to download tomcat archive"
	exit 1
fi

tar -zxvf apache-tomcat-7.0.67.tar.gz

ln -sf ./apache-tomcat-7.0.67 tomcat

ln -s /opt/apache-tomcat-7.0.67 /usr/local/tomcat

useradd -U -d /usr/local/tomcat -M -s /bin/false tomcat

chown -R tomcat /opt/apache-tomcat-7.0.67

# remove any existing webapps
cd /usr/local/tomcat/webapps
rm -rf *
# upload sample.war file to make sure tomcat is running
wget -nv https://tomcat.apache.org/tomcat-7.0-doc/appdev/sample/sample.war

cat >/usr/lib/systemd/system/tomcat.service <<EOL
[Unit]
Description=tomcat
After=network.target

[Service]
Type=forking
ExecStart=/opt/tomcat/bin/startup.sh
ExecStop=/opt/tomcat/bin/shutdown.sh
Restart=on-abort
User=tomcat
Group=tomcat
LimitNOFILE=9000
Environment="JAVA_HOME=/usr"
Environment="CATALINA_HOME=/usr/local/tomcat"
Environment="TOMCAT_USER=tomcat"
Environment="TOMCAT_GROUP=tomcat"
Environment="CMS_DES_PEM=/usr/local/oneops/certs/oo.key"
Environment="IS_SEARCH_ENABLED=true"
Environment="KLOOPZ_NOTIFY_PASS=notifypass"
Environment="KLOOPZ_AMQ_PASS=amqpass"
Environment="CMS_DB_HOST=localhost"
Environment="CMS_DB_USER=kloopzcm"
Environment="CMS_DB_PASS=kloopzcm"
Environment="ACTIVITI_DB_HOST=localhost"
Environment="ACTIVITI_DB_USER=activiti"
Environment="ACTIVITI_DB_PASS=activiti"
Environment="CMS_API_HOST=localhost"
Environment="CONTROLLER_WO_LIMIT=500"
Environment="AMQ_USER=superuser"
Environment="ECV_USER=oneops-ecv"
Environment="ECV_SECRET=ecvsecret"
Environment="API_USER=oneops-api"
Environment="API_SECRET=apisecret"
Environment="API_ACESS_CONTROL=permitAll"
Environment="NOTIFICATION_SYSTEM_USER=admin"
Environment="JAVA_OPTS=\"-Doneops.url=http://localhost:3000 -Dcom.oneops.controller.use-shared-queue=true\""
Environment="CATALINA_PID=/var/run/tomcat.pid"
Environment="SEARCHMQ_USER=superuser"
Environment="SEARCHMQ_PASS=amqpass"
Environment="MD_CACHE_ENABLED=false"

[Install]
WantedBy=multi-user.target
EOL

systemctl enable tomcat.service
systemctl start tomcat.service

#remove tomcat binary file
rm -rf /opt/apache-tomcat-7.0.67.tar.gz

