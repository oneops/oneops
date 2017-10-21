set -e

cd /tmp/oneops_display

/usr/local/bin/bundle package

tar zcvf vendor.tar.gz vendor/*

chown -R vagrant:vagrant /tmp/oneops_display

