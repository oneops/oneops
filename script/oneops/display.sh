
echo '==> Configuring display for vagrant'

mv /tmp/init-d-display /etc/init.d/display

chkconfig --add display
chkconfig display on