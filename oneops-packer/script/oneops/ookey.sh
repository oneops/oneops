echo '==> Configuring ookey for vagrant'

restorecon -r /usr/local

mkdir -p /usr/local/oneops/certs

cd /usr/local/oneops/certs
if [ ! -e oo.key ]; then
	dd if=/dev/urandom count=24 bs=1 | xxd -ps > oo.key
	truncate -s -1 oo.key
fi

