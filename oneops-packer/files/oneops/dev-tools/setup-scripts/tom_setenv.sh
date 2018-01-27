#!/bin/sh

export CMS_DES_PEM=/usr/local/oneops/certs/oo.key
export IS_SEARCH_ENABLED=true
export KLOOPZ_NOTIFY_PASS=notifypass
export KLOOPZ_AMQ_PASS=amqpass	
export CMS_DB_HOST=localhost	
export CMS_DB_USER=kloopzcm
export CMS_DB_PASS=kloopzcm
export ACTIVITI_DB_HOST=localhost
export ACTIVITI_DB_USER=activiti
export ACTIVITI_DB_PASS=activiti
export CMS_API_HOST=localhost
export CONTROLLER_WO_LIMIT=500
export AMQ_USER=superuser
export ECV_USER=oneops-ecv
export ECV_SECRET=ecvsecret
export API_USER=oneops-api
export API_SECRET=apisecret
export API_ACESS_CONTROL=permitAll
export NOTIFICATION_SYSTEM_USER=admin
export JAVA_OPTS="-Doneops.url=http://localhost:3000 -Dcom.oneops.controller.use-shared-queue=true"
export CATALINA_PID=/var/run/tomcat7.pid
export SEARCHMQ_USER=superuser
export SEARCHMQ_PASS=amqpass
export MD_CACHE_ENABLED=false
