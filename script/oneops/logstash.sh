#!/bin/bash -eux

echo '==> Configuring logstash for vagrant'

if ! grep -q -E "vagrant.oo.com" /etc/hosts; then
	echo '127.0.0.1 vagrant.oo.com' >> /etc/hosts
fi

mkdir -p /etc/pki/tls/logstash/certs
mkdir -p /etc/pki/tls/logstash/private

cd /etc/pki/tls/logstash
openssl req -x509 -batch -nodes -days 3650 -newkey rsa:2048 -keyout private/logstash-forwarder.key -out certs/logstash-forwarder.crt -subj '/CN=*.oo.com/'

mkdir -p /etc/logstash/conf.d

cat >/etc/logstash/conf.d/logstash.conf <<EOL
input {
	lumberjack {
		port => 5000 
		ssl_certificate => "/etc/pki/tls/logstash/certs/logstash-forwarder.crt"
		ssl_key => "/etc/pki/tls/logstash/private/logstash-forwarder.key"
		type => "inductor"
	}
}
filter {
	grok {
		match => ["message", "%{TIMESTAMP_ISO8601:ts}%{SPACE}%{LOGLEVEL:level}%{SPACE}%{WORD:class}:%{NUMBER:linenumber}%{SPACE}%{NUMBER:requestId}:%{NUMBER:ciId}%{SPACE}[-]*%{SPACE}%{GREEDYDATA:message_string}"]
	}
	mutate {
		gsub => ["ts", ",", "."]
	}
	date {
		locale => "en"
		match => ["ts", "ISO8601"] timezone => "UTC"
	}
	mutate {
		gsub => ["message_string", "cmd out:", "", "message_string", "cmd:", ""]
	}
	if "_grokparsefailure" in [tags] {
		mutate {
			remove_tag => ["_grokparsefailure"]
		}
	} else {
		mutate {
			replace => {
				"message" => "%{message_string}"
			}
		}
	}
	mutate {
		remove_field => ["message_string", "class", "ts"]
	}
}
output {
	elasticsearch {
		host => "localhost:9300"
		cluster => "oneops"
		document_type => "%{type}"
		protocol => "transport"
	}
}
EOL

cat >/etc/yum.repos.d/logstash.repo <<EOL
[logstash]
name=Logstash repository for 1.5.x packages
baseurl=http://packages.elastic.co/logstash/1.5/centos
gpgcheck=1
gpgkey=http://packages.elastic.co/GPG-KEY-elasticsearch
enabled=1
EOL

yum -y install logstash

chkconfig logstash on
service logstash start
