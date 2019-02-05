#!/bin/bash -eux

echo '==> Configuring elasticsearch for vagrant'

rpm --import https://packages.elastic.co/GPG-KEY-elasticsearch

cat >/etc/yum.repos.d/elasticsearch.repo <<EOL
[elasticsearch-1.7]
name=Elasticsearch repository for 1.7.x packages
baseurl=http://packages.elastic.co/elasticsearch/1.7/centos
gpgcheck=1
gpgkey=http://packages.elastic.co/GPG-KEY-elasticsearch
enabled=1
EOL

yum -y install elasticsearch

export ES_HEAP_SIZE=512m

cat >/etc/profile.d/es.sh <<EOL
export ES_HEAP_SIZE=512m
EOL

sed -i -- 's/\#cluster\.name\: elasticsearch/cluster\.name\: oneops/g' /etc/elasticsearch/elasticsearch.yml
sed -i -- 's/\#index\.number_of_shards\: 1/index\.number_of_shards\: 1/g' /etc/elasticsearch/elasticsearch.yml
sed -i -- 's/\#index\.number_of_replicas\: 0/index\.number_of_replicas\: 0/g' /etc/elasticsearch/elasticsearch.yml

systemctl enable elasticsearch
systemctl start elasticsearch

cat >/tmp/cms_template.json <<EOL
{
    "order": 1,
    "template": "cms-*",
    "settings": {},
    "mappings": {
      "_default_": {
        "dynamic_templates": [
          {
            "time_fields": {
              "mapping": {
                "type": "float"
              },
              "match_mapping_type": "string",
              "match": "*Time"
            }
          },
          {
            "string_fields": {
              "mapping": {
                "index": "analyzed",
                "omit_norms": true,
                "type": "string",
                "fields": {
                  "keyword": {
                    "ignore_above": 256,
                    "index": "not_analyzed",
                    "type": "string"
                  }
                }
              },
              "match_mapping_type": "string",
              "match": "*"
            }
          }
        ]
      }
    },
    "aliases": {
      "cms": {}
    }
  }
EOL

cat >/tmp/event_template.json <<EOL
{
    "order": 1,
    "template": "event-*",
    "settings": {},
    "mappings": {
      "opsprocedure": {
        "properties": {
          "procedureCiId": {
            "type": "long"
          },
          "procedureState": {
            "type": "string"
          },
          "forceExecution": {
            "type": "boolean"
          },
          "maxExecOrder": {
            "type": "long"
          },
          "createdBy": {
            "index": "not_analyzed",
            "type": "string"
          },
          "created": {
            "format": "dateOptionalTime",
            "type": "date"
          },
          "arglist": {
            "type": "string"
          },
          "procedureName": {
            "index": "not_analyzed",
            "type": "string"
          },
          "updated": {
            "format": "dateOptionalTime",
            "type": "date"
          },
          "procedureId": {
            "type": "long"
          },
          "ciId": {
            "type": "long"
          }
        }
      },
      "deployment": {
        "properties": {
          "updatedBy": {
            "type": "string"
          },
          "comments": {
            "type": "string",
            "fields": {
              "keyword": {
                "index": "not_analyzed",
                "type": "string"
              }
            }
          },
          "nsPath": {
            "type": "string",
            "fields": {
              "keyword": {
                "index": "not_analyzed",
                "type": "string"
              }
            }
          },
          "created": {
            "format": "dateOptionalTime",
            "type": "date"
          },
          "description": {
            "type": "string"
          },
          "deploymentState": {
            "type": "string"
          },
          "maxExecOrder": {
            "type": "long"
          },
          "ops": {
            "type": "string",
            "fields": {
              "keyword": {
                "index": "not_analyzed",
                "type": "string"
              }
            }
          },
          "processId": {
            "type": "string"
          },
          "createdBy": {
            "index": "not_analyzed",
            "type": "string"
          },
          "releaseId": {
            "type": "long"
          },
          "deploymentId": {
            "type": "long"
          },
          "updated": {
            "format": "dateOptionalTime",
            "type": "date"
          }
        }
      },
      "ci": {
        "properties": {
          "ciClassId": {
            "type": "long"
          },
          "updatedBy": {
            "type": "string"
          },
          "comments": {
            "index": "not_analyzed",
            "type": "string"
          },
          "nsPath": {
            "type": "string"
          },
          "created": {
            "format": "dateOptionalTime",
            "type": "date"
          },
          "ciName": {
            "index": "not_analyzed",
            "type": "string"
          },
          "ciId": {
            "type": "long"
          },
          "ciGoid": {
            "type": "string"
          },
          "ciStateId": {
            "type": "long"
          },
          "impl": {
            "type": "string"
          },
          "nsId": {
            "type": "long"
          },
          "createdBy": {
            "type": "string"
          },
          "ciState": {
            "type": "string"
          },
          "ciClassName": {
            "index": "not_analyzed",
            "type": "string"
          },
          "lastAppliedRfcId": {
            "type": "long"
          },
          "updated": {
            "format": "dateOptionalTime",
            "type": "date"
          }
        }
      },
      "release": {
        "properties": {
          "relationRfcCount": {
            "type": "long"
          },
          "nsPath": {
            "type": "string",
            "fields": {
              "keyword": {
                "index": "not_analyzed",
                "type": "string"
              }
            }
          },
          "created": {
            "format": "dateOptionalTime",
            "type": "date"
          },
          "releaseName": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "releaseStateId": {
            "type": "long"
          },
          "ciRfcCount": {
            "type": "long"
          },
          "revision": {
            "type": "long"
          },
          "nsId": {
            "type": "long"
          },
          "commitedBy": {
            "type": "string"
          },
          "parentReleaseId": {
            "type": "long"
          },
          "createdBy": {
            "type": "string"
          },
          "releaseId": {
            "type": "long"
          },
          "releaseType": {
            "type": "string"
          },
          "releaseState": {
            "type": "string"
          },
          "updated": {
            "format": "dateOptionalTime",
            "type": "date"
          }
        }
      }
    },
    "aliases": {
      "events": {}
    }
  }
EOL

curl http://localhost:9200
while [ $? != 0 ]; do
	sleep 1
  curl http://localhost:9200
done

curl -d @/tmp/cms_template.json -X PUT http://localhost:9200/_template/cms_template
curl -d @/tmp/event_template.json -X PUT http://localhost:9200/_template/event_template

