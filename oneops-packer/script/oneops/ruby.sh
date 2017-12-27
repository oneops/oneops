
echo '==> Configuring ruby for vagrant'

grep -s -i 'no-document' /root/.gemrc

if [ $? -ne 0 ];
then
	echo 'gem: --no-document' >> /root/.gemrc
fi

yum -y install ruby ruby-devel rubygems

gem update --system 1.8.25
gem install json -v 1.7.7 --no-ri --no-rdoc
gem install bundler -v 1.15.4 --no-ri --no-rdoc
gem install net-ssh -v 2.6.5 --no-ri --no-rdoc
gem install net-ssh-gateway -v 1.3.0 --no-ri --no-rdoc # this is the last version that can use net-ssh 2.6.5s
gem install mixlib-log -v '1.6.0' --no-ri --no-rdoc
gem install aws-s3 -v 0.6.3 --no-ri --no-rdoc
