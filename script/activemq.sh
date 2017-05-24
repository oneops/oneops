#!/bin/bash -eux

echo '==> Configuring activemq for vagrant'

cd /opt

wget -nv https://archive.apache.org/dist/activemq/5.10.2/apache-activemq-5.10.2-bin.tar.gz

if [ $? -ne 0 ];
then
	echo "Unable to download activemq archive"
	exit 1
fi

tar -zxvf apache-activemq-5.10.2-bin.tar.gz

ln -sf ./apache-activemq-5.10.2 activemq

cat >/opt/activemq/conf/credentials.properties <<EOL
# Defines credentials that will be used by components (like web console) to access the broker

activemq.username=system
activemq.password=amqpass
EOL

cat >/usr/lib/systemd/system/activemq.service <<EOL
[Unit]
Description=activemq message queue
After=network.target

[Service]
Type=forking
ExecStart=/opt/activemq/bin/activemq start
ExecStop=/bin/kill -15 $MAINPID
Restart=on-abort
User=root
Group=root
Environment="JAVA_HOME=/usr"
Environment="KLOOPZ_AMQ_PASS=amqpass"

[Install]
WantedBy=multi-user.target
EOL

systemctl enable activemq.service
systemctl start activemq.service

#remove activemq binary file
rm -rf /opt/apache-activemq-5.10.2-bin.tar.gz

