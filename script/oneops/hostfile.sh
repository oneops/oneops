
echo '==> Configuring hostfile for vagrant'

hosts=(search api antenna opsmq daq opsdb sysdb kloopzappdb kloopzcmsdb cmsapi sensor activitidb kloopzmq searchmq)

for i in ${hosts[@]} /etc/hosts; do
	if ! grep -q -E ${i}; then
		echo "127.0.0.1"	$i >> /etc/hosts
	fi
done