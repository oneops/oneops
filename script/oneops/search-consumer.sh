
echo '==> Configuring search-consumer for vagrant'

mv /tmp/init-d-search-consumer /etc/init.d/search-consumer

chown root:root /etc/init.d/search-consumer
chmod +x /etc/init.d/search-consumer

chkconfig --add search-consumer
chkconfig search-consumer on

