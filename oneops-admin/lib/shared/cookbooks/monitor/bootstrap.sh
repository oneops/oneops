/opt/chef/embedded/bin/gem install aws-s3
yum -y install yum-utils
yum-config-manager --add-repo http://repos.walmart.com/epel/7/
echo gpgcheck=0 >> /etc/yum.repos.d/repos.walmart.com_epel_7_.repo
mkdir -p /opt/nagios/libexec/
mkdir -p /home/oneops/shared/cookbooks/monitor/files/default/
touch /home/oneops/shared/cookbooks/monitor/files/default/check_port.sh
