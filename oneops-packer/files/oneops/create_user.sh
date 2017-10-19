#!/bin/bash

set -e 

PASSWORD=$(openssl rand -base64 12)
TOKEN=$(openssl rand -base64 20)

export RAILS_ENV=development
export OODB_USERNAME=kloopz
export OODB_PASSWORD=kloopz
export LOG_DATA_SOURCE=es
export SESSION_INACTIVITY_TIMEOUT=999999
export PATH=${PATH}:/usr/local/bin
echo 'User.create!({ :email => "oneops@oneops.com", :authentication_token => '\"${TOKEN}\"', :name => "Demo User", :username => "demo", :eula_accepted_at => Time.now, :password => '\"${PASSWORD}\"' })' > /opt/oneops/db/seeds.rb
cd /opt/oneops
rake db:seed

if [ $? -eq 0 ]; then
  echo "${PASSWORD}" > /opt/oneops/demo_credential
  echo "${TOKEN}" > /opt/oneops/demo_api_token
fi

cat > /etc/motd <<EOL
----------------------------------------------------------------
Your OneOps credential
======================

Username:  demo
Password:  ${PASSWORD}
API Token: ${TOKEN}

----------------------------------------------------------------
EOL

curl -s -X POST -H "Content-Type: application/json" -H "Accept: application/json" -u demo:${PASSWORD} -d '{ "name": "demo"}' http://localhost:3000/account/organizations

rm -f /opt/oneops/db/seeds.rb

