name "base"
description "Base"
ignore true
type "Platform"
category "General"

environment "single", {}
environment "redundant", {}

entrypoint "fqdn"

platform :attributes => {
  "replace_after_minutes" => 60,
  "replace_after_repairs" => 3
}

# dns service needed for ptr record cleanup on replace

resource "filebeat",
         :cookbook => "oneops.1.filebeat",
         :design   => true,
         :requires => {
           "constraint" => "0..10",
           :services    => "mirror"
         },
         :monitors => {
           'filebeatprocess' => {:description => 'FilebeatProcess',
                                 :source      => '',
                                 :enable      => 'true',
                                 :chart       => {'min' => '0', 'max' => '100', 'unit' => 'Percent'},
                                 :cmd         => 'check_process_count!filebeat',
                                 :cmd_line    => '/opt/nagios/libexec/check_process_count.sh "$ARG1$"',
                                 :metrics     => {
                                   'count' => metric(:unit => '', :description => 'Running Process'),
                                 },
                                 :thresholds  => {
                                   'FilebeatProcessLow'  => threshold('1m', 'avg', 'count', trigger('<', 1, 1, 1), reset('>=', 1, 1, 1)),
                                   'FilebeatProcessHigh' => threshold('1m', 'avg', 'count', trigger('>=', 200, 1, 1), reset('<', 200, 1, 1))
                                 }
           }
         }

resource "telegraf",
         :cookbook => "oneops.1.telegraf",
         :design   => true,
         :requires => {
           "constraint" => "0..10",
           :services    => "mirror"
         },
         :monitors => {
           'telegrafprocess' => {:description => 'TelegrafProcess',
                                 :source      => '',
                                 :enable      => 'true',
                                 :chart       => {'min' => '0', 'max' => '100', 'unit' => 'Percent'},
                                 :cmd         => 'check_process_count!telegraf',
                                 :cmd_line    => '/opt/nagios/libexec/check_process_count.sh "$ARG1$"',
                                 :metrics     => {
                                   'count' => metric(:unit => '', :description => 'Running Process'),
                                 },
                                 :thresholds  => {
                                   'TelegrafProcessLow'  => threshold('1m', 'avg', 'count', trigger('<', 1, 1, 1), reset('>=', 1, 1, 1)),
                                   'TelegrafProcessHigh' => threshold('1m', 'avg', 'count', trigger('>=', 200, 1, 1), reset('<', 200, 1, 1))
                                 }
           }
         }

resource "compute",
         :cookbook   => "oneops.1.compute",
         :design     => true,
         :requires   => {"constraint" => "1..1", "services" => "compute,dns,*mirror"},
         :attributes => {"size" => "S"
         },
         :monitors   => {
           'ssh' => {:description => 'SSH Port',
                     :chart       => {'min' => 0},
                     :cmd         => 'check_port',
                     :cmd_line    => '/opt/nagios/libexec/check_port.sh',
                     :heartbeat   => true,
                     :duration    => 5,
                     :metrics     => {
                       'up' => metric(:unit => '%', :description => 'Up %')
                     },
                     :thresholds  => {
                     },
           }
         },
         :payloads   => {
           'os' => {
             'description' => 'os',
             'definition'  => '{
         "returnObject": false,
         "returnRelation": false,
         "relationName": "base.RealizedAs",
         "direction": "to",
         "targetClassName": "manifest.oneops.1.Compute",
         "relations": [
           { "returnObject": true,
             "returnRelation": false,
             "relationName": "manifest.DependsOn",
             "direction": "to",
             "targetClassName": "manifest.oneops.1.Os"
           }
         ]
      }'
           }
         }

resource "os",
         :cookbook   => "oneops.1.os",
         :design     => true,
         :requires   => {"constraint" => "1..1", "services" => "compute,dns,*mirror,*ntp,*windows-domain"},
         :attributes => {"ostype"   => "centos-7.2",
                         "dhclient" => 'true'
         },
         :monitors   => {
           'cpu'     => {:description => 'CPU',
                         :source      => '',
                         :chart       => {'min' => 0, 'max' => 100, 'unit' => 'Percent'},
                         :cmd         => 'check_local_cpu!10!5',
                         :cmd_line    => '/opt/nagios/libexec/check_cpu.sh $ARG1$ $ARG2$',
                         :metrics     => {
                           'CpuUser'   => metric(:unit => '%', :description => 'User %'),
                           'CpuNice'   => metric(:unit => '%', :description => 'Nice %'),
                           'CpuSystem' => metric(:unit => '%', :description => 'System %'),
                           'CpuSteal'  => metric(:unit => '%', :description => 'Steal %'),
                           'CpuIowait' => metric(:unit => '%', :description => 'IO Wait %'),
                           'CpuIdle'   => metric(:unit => '%', :description => 'Idle %', :display => false)
                         },
                         :thresholds  => {
                           'HighCpuPeak' => threshold('5m', 'avg', 'CpuIdle', trigger('<=', 10, 5, 1), reset('>', 20, 5, 1)),
                           'HighCpuUtil' => threshold('1h', 'avg', 'CpuIdle', trigger('<=', 20, 60, 1), reset('>', 30, 60, 1))
                         }
           },
           'load'    => {:description => 'Load',
                         :chart       => {'min' => 0},
                         :cmd         => 'check_local_load!5.0,4.0,3.0!10.0,6.0,4.0',
                         :cmd_line    => '/opt/nagios/libexec/check_load -w $ARG1$ -c $ARG2$',
                         :duration    => 5,
                         :metrics     => {
                           'load1'  => metric(:unit => '', :description => 'Load 1min Average'),
                           'load5'  => metric(:unit => '', :description => 'Load 5min Average'),
                           'load15' => metric(:unit => '', :description => 'Load 15min Average'),
                         },
                         :thresholds  => {
                         },
           },
           'disk'    => {'description' => 'Disk',
                         'chart'       => {'min' => 0, 'unit' => '%'},
                         'cmd'         => 'check_disk_use!/',
                         'cmd_line'    => '/opt/nagios/libexec/check_disk_use.sh $ARG1$',
                         'metrics'     => {'space_used' => metric(:unit => '%', :description => 'Disk Space Percent Used'),
                                           'inode_used' => metric(:unit => '%', :description => 'Disk Inode Percent Used')},
                         :thresholds   => {
                           'LowDiskSpace' => threshold('5m', 'avg', 'space_used', trigger('>', 90, 5, 1), reset('<', 90, 5, 1)),
                           'LowDiskInode' => threshold('5m', 'avg', 'inode_used', trigger('>', 90, 5, 1), reset('<', 90, 5, 1)),
                         },
           },
           'mem'     => {'description' => 'Memory',
                         'chart'       => {'min' => 0, 'unit' => 'KB'},
                         'cmd'         => 'check_local_mem!90!95',
                         'cmd_line'    => '/opt/nagios/libexec/check_mem.pl -Cu -w $ARG1$ -c $ARG2$',
                         'metrics'     => {
                           'total'  => metric(:unit => 'KB', :description => 'Total Memory'),
                           'used'   => metric(:unit => 'KB', :description => 'Used Memory'),
                           'free'   => metric(:unit => 'KB', :description => 'Free Memory'),
                           'caches' => metric(:unit => 'KB', :description => 'Cache Memory')
                         },
                         :thresholds   => {
                         },
           },
           'network' => {:description => 'Network',
                         :source      => '',
                         :chart       => {'min' => 0, 'unit' => ''},
                         :cmd         => 'check_network_bandwidth',
                         :cmd_line    => '/opt/nagios/libexec/check_network_bandwidth.sh',
                         :metrics     => {
                           'rx_bytes' => metric(:unit => 'bytes', :description => 'RX Bytes', :dstype => 'DERIVE'),
                           'tx_bytes' => metric(:unit => 'bytes', :description => 'TX Bytes', :dstype => 'DERIVE')
                         }
           }
         },
         :payloads   => {
           'linksto' => {
             'description' => 'LinksTo',
             'definition'  => '{
        "returnObject": false,
        "returnRelation": false,
        "relationName": "base.RealizedAs",
        "direction": "to",
        "relations": [
          { "returnObject": false,
            "returnRelation": false,
            "relationName": "manifest.Requires",
            "direction": "to",
            "targetClassName": "manifest.Platform",
            "relations": [
              { "returnObject": false,
                "returnRelation": false,
                "relationName": "manifest.LinksTo",
                "direction": "from",
                "targetClassName": "manifest.Platform",
                "relations": [
                  { "returnObject": true,
                    "returnRelation": false,
                    "relationName": "manifest.Entrypoint",
                    "direction": "from"
                  }
                ]
              }
            ]
          }
        ]
      }'
           }
         }


resource 'logstash',
         :cookbook   => 'oneops.1.logstash',
         :design     => true,
         :requires   => {'constraint' => '0..*', 'services' => 'mirror'},
         :attributes => {
         },
         :monitors   => {
           'logstashprocess' => {:description => 'LogstashProcess',
                                 :source      => '',
                                 :chart       => {'min' => '0', 'max' => '100', 'unit' => 'Percent'},
                                 :cmd         => 'check_process!logstash!false!logstash',
                                 :cmd_line    => '/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$"',
                                 :metrics     => {
                                   'up' => metric(:unit => '%', :description => 'Percent Up'),
                                 },
                                 :thresholds  => {
                                   'LogstashProcessDown' => threshold('1m', 'avg', 'up', trigger('<=', 98, 1, 1), reset('>', 95, 1, 1), 'unhealthy')
                                 }
           }
         }


resource "fqdn",
         :cookbook   => "oneops.1.fqdn",
         :design     => true,
         :requires   => {"constraint" => "1..1", "services" => "compute,dns,*gdns"},
         :attributes => {"aliases" => '[]'},
         :payloads   => {
           'environment'   => {
             'description' => 'Environment',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment"
             }
           ]
         }
       ]
    }'
           },
           'activeclouds'  => {
             'description' => 'activeclouds',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationAttrs":[{"attributeName":"priority", "condition":"eq", "avalue":"1"},
                                {"attributeName":"adminstatus", "condition":"eq", "avalue":"active"}],
               "relationName": "base.Consumes",
               "direction": "from",
               "targetClassName": "account.Cloud",
               "relations": [
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.Netscaler"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Route53"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Designate"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationName": "base.Provides",
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                 },
                 { "returnObject": true,
                   "returnRelation": false,
                   "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                   "relationName": "base.Provides",
                   "direction": "from",
                   "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                 }
               ]
             }
           ]
         }
       ]
    }'
           },
           'organization'  => {
             'description' => 'Organization',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.ComposedOf",
               "direction": "to",
               "targetClassName": "manifest.Environment",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.RealizedIn",
                   "direction": "to",
                   "targetClassName": "account.Assembly",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Manages",
                       "direction": "to",
                       "targetClassName": "account.Organization"
                     }
                   ]
                 }
               ]
             }
           ]
         }
       ]
    }'
           },
           'lb'            => {
             'description' => 'all loadbalancers',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "bom.DependsOn",
       "direction": "from",
       "targetClassName": "bom.oneops.1.Lb",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Lb",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "base.RealizedAs",
               "direction": "from",
               "targetClassName": "bom.oneops.1.Lb"
             }
           ]
         }
       ]
    }'
           },
           'remotedns'     => {
             'description' => 'Other clouds dns services',
             'definition'  => '{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Infoblox"
                     },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Route53"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Designate"
                    },
                   { "returnObject": true,
                      "returnRelation": false,
                      "relationName": "base.Provides",
                      "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                      "direction": "from",
                      "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                    },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"dns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Infoblox"
                     }
                   ]
                 }
               ]
             }
           ]
      }'
           },
           'remotegdns'    => {
             'description' => 'Other clouds gdns services',
             'definition'  => '{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Netscaler"
                     },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Netscaler"
                     },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Route53"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Designate"
                      },
                     { "returnObject": true,
                        "returnRelation": false,
                        "relationName": "base.Provides",
                        "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                        "direction": "from",
                        "targetClassName": "cloud.service.oneops.1.Rackspacedns"
                      },
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"gdns"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.oneops.1.Azuretrafficmanager"
                     }
                   ]
                 }
               ]
             }
           ]
      }'
           },
           'os_payload'    => {
             'description' => 'Os payload',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Fqdn",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.Requires",
           "direction": "to",
           "targetClassName": "manifest.Platform",
           "relations": [
             { "returnObject": true,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "from",
               "targetClassName": "manifest.oneops.1.Os"
             }
            ]
         }
       ]
     }'
           },
           'windowsdomain' => {
             'description' => 'Windows-domain service',
             'definition'  => '{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"windows-domain"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Windows-domain"
                     }
                   ]
                 }
               ]
             }
           ]
      }'
           }
         }

resource "user",
         :cookbook => "oneops.1.user",
         :design   => true,
         :requires => {"constraint" => "0..*"}

resource "job",
         :cookbook => "oneops.1.job",
         :design   => true,
         :requires => {"constraint" => "0..*"}

resource "objectstore",
         :cookbook   => "oneops.1.objectstore",
         :design     => true,
         :requires   => {"constraint" => "0..1", :services => "filestore"},
         :attributes => {
           "username" => "",
           "password" => ""
         }

resource "storage",
         :cookbook   => "oneops.1.storage",
         :design     => true,
         :attributes => {
           "size"        => '20G',
           "slice_count" => '1'
         },
         :requires   => {"constraint" => "0..*", "services" => "storage"},
         :payloads   => {
           'volumes' => {
             'description' => 'volumes',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Storage",
       "relations": [
         { "returnObject": true,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Volume"
         }
       ]
     }'
           }
         }

resource "volume",
         :cookbook   => "oneops.1.volume",
         :design     => true,
         :requires   => {"constraint" => "0..*", "services" => "compute"},
         :attributes => {"mount_point" => '/data',
                         "device"      => '',
                         "fstype"      => 'xfs',
                         "options"     => ''
         },
         :monitors   => {
           'usage' => {'description' => 'Usage',
                       'chart'       => {'min' => 0, 'unit' => 'Percent used'},
                       'cmd'         => 'check_disk_use!:::node.workorder.rfcCi.ciAttributes.mount_point:::',
                       'cmd_line'    => '/opt/nagios/libexec/check_disk_use.sh $ARG1$',
                       'metrics'     => {'space_used' => metric(:unit => '%', :description => 'Disk Space Percent Used'),
                                         'inode_used' => metric(:unit => '%', :description => 'Disk Inode Percent Used')},
                       :thresholds   => {
                         'LowDiskSpace' => threshold('5m', 'avg', 'space_used', trigger('>', 90, 5, 1), reset('<', 90, 5, 1)),
                         'LowDiskInode' => threshold('5m', 'avg', 'inode_used', trigger('>', 90, 5, 1), reset('<', 90, 5, 1)),
                       },
           },
         }

resource "share",
         :cookbook   => "oneops.1.glusterfs",
         :design     => true,
         :requires   => {
           :constraint => "0..1",
           :services   => "mirror"
         },
         :attributes => {
           "store"       => '/data',
           "volopts"     => '{}',
           "replicas"    => "2",
           "mount_point" => '/share'
         }

resource "library",
         :cookbook => "oneops.1.library",
         :design   => true,
         :requires => {"constraint" => "0..*"}

resource "file",
         :cookbook => "oneops.1.file",
         :design   => true,
         :requires => {
           :constraint => "0..*",
           :help       => <<-eos
The optional <strong>file</strong> component can be used to create customized files.
For example, you can create configuration file needed for your applications or other components.
A file can also be a shell script which can be executed with the optional execute command attribute.
           eos
         }

resource "download",
         :cookbook => "oneops.1.download",
         :design   => true,
         :requires => {"constraint" => "0..*"}

resource "daemon",
         :cookbook => "oneops.1.daemon",
         :design   => true,
         :requires => {"constraint" => "0..*"},
         :monitors => {
           'process' => {:description => 'Process',
                         :source      => '',
                         :chart       => {'min' => '0', 'max' => '100', 'unit' => 'Percent'},
                         :cmd         => 'check_process!:::node.workorder.rfcCi.ciAttributes.service_name:::!:::node.workorder.rfcCi.ciAttributes.use_script_status:::!:::node.workorder.rfcCi.ciAttributes.pattern:::!:::node.workorder.rfcCi.ciAttributes.secondary_down:::',
                         :cmd_line    => '/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$" "$ARG3$" "$ARG4$"',
                         :metrics     => {
                           'up' => metric(:unit => '%', :description => 'Percent Up'),
                         },
                         :thresholds  => {
                           'ProcessDown' => threshold('1m', 'avg', 'up', trigger('<=', 98, 1, 1), reset('>', 95, 1, 1))
                         }
           }
         }

resource "sshkeys",
         :cookbook   => "oneops.1.keypair",
         :design     => false,
         :attributes => {
           "private" => 'keygen',
           "public"  => 'keygen'
         },
         :requires   => {"constraint" => "1..1", "services" => "compute", "priority" => "1"},
         :payloads   => {'secures' => {
           'description' => 'Secures',
           'definition'  => '{
       "returnObject": true,
       "returnRelation": false,
       "relationName": "bom.SecuredBy",
       "direction": "to"
    }'
         }

         }

resource "secgroup",
         :cookbook   => "oneops.1.secgroup",
         :design     => true,
         :attributes => {
         },
         :requires   => {
           :constraint => "1..1",
           :services   => "compute"
         }

resource "certificate",
         :cookbook   => "oneops.1.certificate",
         :design     => true,
         :requires   => {"constraint" => "0..*", 'services' => '*certificate'},
         :attributes => {},
         :monitors   => {
           'ExpiryMetrics' => {:description => 'ExpiryMetrics',
                               :source      => '',
                               :chart       => {'min' => 0, 'unit' => 'Per Minute'},
                               :charts      => [
                                 {'min' => 0, 'unit' => 'Current Count', 'metrics' => ["days_remaining"]}
                               ],
                               :cmd         => 'check_cert!:::node.expiry_date_in_seconds:::',
                               :cmd_line    => '/opt/nagios/libexec/check_cert $ARG1$',
                               :metrics     => {
                                 'minutes_remaining' => metric(:unit => 'count', :description => 'Minutes remaining to Expiry', :dstype => 'GAUGE'),
                                 'hours_remaining'   => metric(:unit => 'count', :description => 'Hours remaining to Expiry', :dstype => 'GAUGE'),
                                 'days_remaining'    => metric(:unit => 'count', :description => 'Days remaining to Expiry', :dstype => 'GAUGE')
                               },
                               :thresholds  => {
                                 'cert-expiring-soon' => threshold('1m', 'avg', 'days_remaining', trigger('<=', 30, 1, 1), reset('>', 90, 1, 1))
                               }
           }
         }


resource "hostname",
         :cookbook => "oneops.1.fqdn",
         :design   => true,
         :requires => {
           :constraint => "0..1",
           :services   => "dns",
           :help       => "optional hostname dns entry"
         },
         :payloads => {
           'windowsdomain' => {
             'description' => 'Windows-domain service',
             'definition'  => '{
           "returnObject": false,
           "returnRelation": false,
           "relationName": "base.RealizedAs",
           "direction": "to",
           "targetClassName": "manifest.oneops.1.Fqdn",
           "relations": [
             { "returnObject": false,
               "returnRelation": false,
               "relationName": "manifest.Requires",
               "direction": "to",
               "targetClassName": "manifest.Platform",
               "relations": [
                 { "returnObject": false,
                   "returnRelation": false,
                   "relationName": "base.Consumes",
                   "direction": "from",
                   "targetClassName": "account.Cloud",
                   "relations": [
                     { "returnObject": true,
                       "returnRelation": false,
                       "relationName": "base.Provides",
                       "relationAttrs":[{"attributeName":"service", "condition":"eq", "avalue":"windows-domain"}],
                       "direction": "from",
                       "targetClassName": "cloud.service.Windows-domain"
                     }
                   ]
                 }
               ]
             }
           ]
      }'
           }
         }
resource "sensuclient",
         :cookbook => "oneops.1.sensuclient",
         :design   => true,
         :requires => {"constraint" => "0..1"}

resource "firewall",
         :cookbook => "oneops.1.firewall",
         :design   => true,
         :requires => {
           "constraint" => "0..1",
           'services'   => 'firewall'
         },
         :payloads => {
           'computes' => {
             'description' => 'computes',
             'definition'  => '{
       "returnObject": false,
       "returnRelation": false,
       "relationName": "base.RealizedAs",
       "direction": "to",
       "targetClassName": "manifest.oneops.1.Firewall",
       "relations": [
         { "returnObject": false,
           "returnRelation": false,
           "relationName": "manifest.DependsOn",
           "direction": "from",
           "targetClassName": "manifest.oneops.1.Compute",
           "relations": [
             { "returnObject": true,
             "returnRelation": false,
             "relationName": "base.RealizedAs",
             "direction": "from",
             "targetClassName": "bom.oneops.1.Compute"
             }
           ]
         }
       ]
     }'
           }
         }

resource "secrets-client",
         :cookbook => "oneops.1.secrets-client",
         :design   => true,
         :requires => {"constraint" => "0..1", 'services' => '*certificate,*secret'},
         :monitors => {
           'SecretsClientProcess' => {:description => 'SecretsClientProcess',
                                      :source      => '',
                                      :chart       => {'min' => '0', 'max' => '100', 'unit' => 'Percent'},
                                      :cmd         => 'check_process!:::node.secrets_client_service_name:::!true',
                                      :cmd_line    => '/opt/nagios/libexec/check_process.sh "$ARG1$" "$ARG2$"',
                                      :metrics     => {
                                        'up' => metric(:unit => '%', :description => 'Percent Up'),
                                      },
                                      :thresholds  => {
                                        'SecretsClientProcessDown' => threshold('1m', 'avg', 'up', trigger('<=', 98, 1, 1), reset('>', 95, 1, 1), 'unhealthy')
                                      }
           }
         }

resource "artifact",
         :cookbook => "oneops.1.artifact",
         :design   => true,
         :requires => {"constraint" => "0..*"}

# depends_on
[{:from => 'compute', :to => 'secgroup'}].each do |link|
  relation "#{link[:from]}::depends_on::#{link[:to]}",
           :relation_name => 'DependsOn',
           :from_resource => link[:from],
           :to_resource   => link[:to],
           :attributes    => {"flex" => false, "converge" => true, "min" => 1, "max" => 1}
end

[{:from => 'hostname', :to => 'os'},
 {:from => 'user', :to => 'os'},
 {:from => 'job', :to => 'user'},
 {:from => 'job', :to => 'os'},
 {:from => 'volume', :to => 'os'},
 {:from => 'certificate', :to => 'os'},
 {:from => 'share', :to => 'os'},
 {:from => 'logstash', :to => 'os'},
 {:from => 'logstash', :to => 'compute'},
 {:from => 'telegraf', :to => 'os'},
 {:from => 'telegraf', :to => 'compute'},
 {:from => 'filebeat', :to => 'os'},
 {:from => 'filebeat', :to => 'compute'},
 {:from => 'storage', :to => 'compute'},
 {:from => 'share', :to => 'volume'},
 {:from => 'volume', :to => 'user'},
 {:from => 'daemon', :to => 'os'},
 {:from => 'daemon', :to => 'download'},
 {:from => 'daemon', :to => 'library'},
 {:from => 'download', :to => 'os'},
 {:from => 'file', :to => 'volume'},
 {:from => 'file', :to => 'os'},
 {:from => 'artifact', :to => 'os'},
 {:from => 'sensuclient', :to => 'compute'},
 {:from => 'library', :to => 'os'},
 {:from => 'objectstore', :to => 'compute'},
 {:from => 'secrets-client', :to => 'os'},
 {:from => 'secrets-client', :to => 'user'},
 {:from => 'secrets-client', :to => 'certificate'},
 {:from => 'secrets-client', :to => 'volume'},
 {:from => 'objectstore', :to => 'user'}
].each do |link|
  relation "#{link[:from]}::depends_on::#{link[:to]}",
           :relation_name => 'DependsOn',
           :from_resource => link[:from],
           :to_resource   => link[:to],
           :attributes    => {"flex" => false, "min" => 1, "max" => 1}
end

['fqdn'].each do |from|
  relation "#{from}::depends_on::compute",
           :only          => ['_default', 'single'],
           :relation_name => 'DependsOn',
           :from_resource => from,
           :to_resource   => 'compute',
           :attributes    => {"propagate_to" => 'both', "flex" => false, "min" => 1, "max" => 1}
end

['firewall'].each do |from|
  relation "#{from}::depends_on::compute",
           :only          => ['_default', 'single'],
           :relation_name => 'DependsOn',
           :from_resource => from,
           :to_resource   => 'compute',
           :attributes    => {"propagate_to" => 'both', "flex" => false, "min" => 1, "max" => 1}
end

[{:from => 'volume', :to => 'storage'}
].each do |link|
  relation "#{link[:from]}::depends_on::#{link[:to]}",
           :relation_name => 'DependsOn',
           :from_resource => link[:from],
           :to_resource   => link[:to],
           :attributes    => {"propagate_to" => 'from', "flex" => false, "min" => 1, "max" => 1}
end

# propagation rule for replace and updating /etc/profile.d/oneops.sh
['hostname', 'os'].each do |from|
  relation "#{from}::depends_on::compute",
           :relation_name => 'DependsOn',
           :from_resource => from,
           :to_resource   => 'compute',
           :attributes    => {'propagate_to' => 'from'}
end

# managed_via
['os', 'telegraf', 'filebeat', 'user', 'job', 'file', 'volume', 'share', 'download', 'library', 'daemon',
 'certificate', 'logstash', 'sensuclient', 'artifact', 'objectstore', 'secrets-client'].each do |from|
  relation "#{from}::managed_via::compute",
           :except        => ['_default'],
           :relation_name => 'ManagedVia',
           :from_resource => from,
           :to_resource   => 'compute',
           :attributes    => {}
end

# secured_by
['compute'].each do |from|
  relation "#{from}::secured_by::sshkeys",
           :except        => ['_default'],
           :relation_name => 'SecuredBy',
           :from_resource => from,
           :to_resource   => 'sshkeys',
           :attributes    => {}
end
