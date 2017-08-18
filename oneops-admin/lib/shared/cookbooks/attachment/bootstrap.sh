#!/bin/bash
echo "setting up proxy for user Vagrant"
grep "http_proxy" /home/vagrant/.bashrc
if [ $? -eq 1 ]
    then
    sudo printf "export http_proxy=http://proxy.wal-mart.com:9080 \nexport https_proxy=http://proxy.wal-mart.com:9080 \n" >> /home/vagrant/.bashrc
    sudo printf "export no_proxy=.local,.wal-mart.com,.wamnetNAD,.walmart.com,.wmlink,.walmartlabs.com\n" >> /home/vagrant/.bashrc
    echo "setting yum to use proxy"
    echo "removing default repos from yum"
    sudo rm -rf /etc/yum.repos.d/*
    echo "adding walmart repo to yum"
    sudo printf "[myrepo] \nname=walmartrepo \nbaseurl=http://repos.walmart.com/base/centos/6.8 \n" > /etc/yum.repos.d/Centos-Base.repo
fi

echo "setting url for internal gem repo"
sudo /opt/chef/embedded/bin/gem sources --add http://sourcerepos.walmart.com/gembox/
sudo /opt/chef/embedded/bin/gem sources -r https://rubygems.org/
sudo runuser -l vagrant -c "/opt/chef/embedded/bin/gem sources --add http://sourcerepos.walmart.com/gembox/"
sudo runuser -l vagrant -c "/opt/chef/embedded/bin/gem sources -r https://rubygems.org/"

echo "downloading base gems"
sudo /opt/chef/embedded/bin/gem install aws-s3 -v 0.6.3 --conservative
sudo /opt/chef/embedded/bin/gem install parallel -v 1.9.0 --conservative
sudo /opt/chef/embedded/bin/gem install i18n -v 0.6.9 --conservative
sudo /opt/chef/embedded/bin/gem install activesupport -v 3.2.11 --conservative
echo "Done"

echo "install zip and unzip"
sudo yum clean metadata
sudo yum install zip unzip -y