#!/bin/sh

export RAILS_ENV=development
export OODB_USERNAME=kloopz
export OODB_PASSWORD=kloopz
export LOG_DATA_SOURCE=es
export SESSION_INACTIVITY_TIMEOUT=999999
export PATH=${PATH}:/usr/local/bin

pidfile="/var/run/display.pid"
nohup rails server >> /opt/oneops/log/rails.log 2>&1 &
echo $! > $pidfile
