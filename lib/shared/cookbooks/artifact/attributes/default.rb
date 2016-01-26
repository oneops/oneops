#
# Cookbook Name:: artifact_test
# Attribute:: default
#

default[:artifact_test][:location]  = "http://artifacts.example.com/artifact_test-1.2.3.tgz"
default[:artifact_test][:version]   = "1.2.3"
default[:artifact_test][:deploy_to] = "/srv/artifact_test"

default[:artifact_test][:file_url] = "http://apache.mirrors.lucidnetworks.net/maven/maven-3/3.0.5/binaries/apache-maven-3.0.5-bin.tar.gz"
