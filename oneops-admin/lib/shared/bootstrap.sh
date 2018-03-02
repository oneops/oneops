#!/bin/bash

ruby_exists()
{
    ruby -v > /dev/null 2>&1
    if [ "$?" == "0" ]; then
        return 0
    else
        return 1
    fi
}

echo "Install ruby and bundle."

# sles or opensuse
if [ -e /etc/SuSE-release ] ; then
  zypper -n in sudo rsync file make gcc glibc-devel libgcc ruby ruby-devel rubygems libxml2-devel libxslt-devel perl
  zypper -n in rubygem-yajl-ruby

  # sles
  hostname=`cat /etc/HOSTNAME`
  grep $hostname /etc/hosts
  if [ $? != 0 ]; then
    ip_addr=`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1 -d'/' | xargs`
    echo "$ip_addr $hostname" >> /etc/hosts
  fi

# redhat / centos
elif [ -e /etc/redhat-release ] ; then
  echo "installing ruby, libs, headers and gcc"
  if ruby_exists; then
    yum -d0 -e0 -y install sudo rsync file make gcc gcc-c++ glibc-devel libgcc ruby-libs ruby-devel libxml2-devel libxslt-devel perl libyaml
  else
    yum -d0 -e0 -y install sudo file make gcc gcc-c++ glibc-devel libgcc ruby ruby-libs ruby-devel libxml2-devel libxslt-devel ruby-rdoc rubygems perl perl-Digest-MD5 nagios nagios-devel nagios-plugins
  fi

  # disable selinux
  if [ -e /selinux/enforce ]; then
    echo 0 >/selinux/enforce
    echo "SELINUX=disabled" >/etc/selinux/config
    echo "SELINUXTYPE=targeted" >>/etc/selinux/config
  fi

  # allow ssh sudo's w/out tty
  grep -v requiretty /etc/sudoers > /etc/sudoers.t
  mv -f /etc/sudoers.t /etc/sudoers
  chmod 440 /etc/sudoers

else
# debian
  export DEBIAN_FRONTEND=noninteractive
  echo "apt-get update ..."
  apt-get update >/dev/null 2>&1
  if [ $? != 0 ]; then
     echo "apt-get update returned non-zero result code. Usually means some repo is returning a 403 Forbidden. Try deleting the compute from providers console and retrying."
     exit 1
  fi
  apt-get install -q -y build-essential make libxml2-dev libxslt-dev libz-dev ruby ruby-dev nagios3
  
  # seperate rubygems - rackspace 14.04 needs it, aws doesn't
  #set +e
  apt-get -y -q install rubygems-integration
  rm -fr /etc/apache2/conf.d/nagios3.conf
  #set -e
fi

# Downgrade rubygems 
rubygems_ver=$((echo "1.8.26" && gem -v) | sort -V | head -n 1)
if [ -e /etc/redhat-release ] && [ $rubygems_ver = "1.8.26" ]; then
  # needed for rhel >= 7
  echo "Downgrading rubygems..."
  gem update --system 1.8.25
fi

# Install json gem
gem_version="1.7.7"
grep 16.04 /etc/issue
if [ $? -eq 0 ]
then
  gem_version="2.0.2"
fi

json_installed=$(gem list ^json$ -v $gem_version -i)
if [ $json_installed != "true" ]; then
  echo "Installing json..."
  gem install json --version $gem_version --no-ri --no-rdoc
fi

# 
bundler_installed=$(gem list ^bundler$ -i)
if [ $bundler_installed != "true" ]; then
  echo "Installing bundler..."
  ver=$((echo "1.8.25" && gem -v) | sort -V | head -n 1)
  if [ $ver != '1.8.25' ]; then
    gem install bundler -v 1.15.4 --bindir /usr/bin --no-ri --no-rdoc
  else
    gem install bundler --bindir /usr/bin --no-ri --no-rdoc
  fi
fi

mkdir -p /opt/oneops
echo "$rubygems_proxy" > /opt/oneops/rubygems_proxy

perl -p -i -e 's/ 00:00:00.000000000Z//' /var/lib/gems/*/specifications/*.gemspec 2>/dev/null

# oneops user
grep "^oneops:" /etc/passwd 2>/dev/null
if [ $? != 0 ] ; then
  set -e
  echo "*** ADD oneops USER ***"

  # create oneops user & group - deb systems use addgroup
  if [ -e /etc/lsb-release] ] ; then
    addgroup oneops
  else
    groupadd oneops
  fi

  useradd oneops -g oneops -m -s /bin/bash
  echo "oneops   ALL = (ALL) NOPASSWD: ALL" >> /etc/sudoers
else
  echo "oneops user already there..."
fi
