echo '==> Configuring cassandra for vagrant'

cd /opt

wget -nv https://archive.apache.org/dist/cassandra/2.2.5/apache-cassandra-2.2.5-bin.tar.gz

if [ $? -ne 0 ];
then
	echo "Unable to download cassandra archive"
	exit 1
fi

tar -zxvf apache-cassandra-2.2.5-bin.tar.gz

ln -sf ./apache-cassandra-2.2.5 cassandra
mkdir -p /opt/cassandra/log

mv /tmp/init-d-cassandra /etc/init.d/cassandra

chown root:root /etc/init.d/cassandra
chmod +x /etc/init.d/cassandra
chkconfig --add cassandra
chkconfig cassandra on
#service cassandra start