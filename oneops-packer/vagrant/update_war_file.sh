#!/bin/bash

[ -z "$OO_WAR_DIR" ] && echo "Missing OO_WAR_DIR" && exit 1;

HOST=$(vagrant ssh-config|egrep 'HostName'|awk '{print $2}')
USER=$(vagrant ssh-config|egrep 'User '|awk '{print $2}')
PORT=$(vagrant ssh-config|egrep 'Port'|awk '{print $2}')
IDENTITY=$(vagrant ssh-config|egrep 'IdentityFile'|awk '{print $2}')
TEMP_DIR=/tmp/$(date +%s)
DEST=/usr/local/tomcat/webapps

ssh -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $PORT $USER@$HOST mkdir -p $TEMP_DIR
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/adapter.war $USER@$HOST:$TEMP_DIR/adapter.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/antenna.war $USER@$HOST:$TEMP_DIR/antenna.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/cms-admin.war $USER@$HOST:$TEMP_DIR/cms-admin.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/controller.war $USER@$HOST:$TEMP_DIR/controller.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/daq-api-1.0.0.war $USER@$HOST:$TEMP_DIR/daq-api-1.0.0.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/opamp.war  $USER@$HOST:$TEMP_DIR/opamp.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/sensor.war $USER@$HOST:$TEMP_DIR/sensor.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/transistor.war $USER@$HOST:$TEMP_DIR/transistor.war
scp -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -P $PORT $OO_WAR_DIR/transmitter.war $USER@$HOST:$TEMP_DIR/transmitter.war
ssh -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $PORT $USER@$HOST sudo cp $TEMP_DIR/*.* $DEST
ssh -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $PORT $USER@$HOST sudo systemctl restart tomcat.service
ssh -qi $IDENTITY -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p $PORT $USER@$HOST rm -rf $TEMP_DIR

