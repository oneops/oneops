#!/bin/sh

pidfile="/var/run/search-consumer.pid"
logfile=/opt/oneops-search/log/search-consumer.log
nohup java -jar -Dindex.name=cms-all -Dnodes=localhost:9300 -Damq.user=superuser -Damq.pass=amqpass -Dcluster.name=oneops -DKLOOPZ_AMQ_HOST=localhost -Dsearch.maxConsumers=10 /opt/oneops-search/search.jar >> $logfile 2>&1 &
echo $! > $pidfile

