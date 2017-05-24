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

[Install]
WantedBy=multi-user.target
EOL

systemctl enable tomcat.service
systemctl start tomcat.service

#remove tomcat binary file
rm -rf /opt/apache-tomcat-7.0.67.tar.gz

