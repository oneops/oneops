
echo '==> Configuring ruby for vagrant'

grep -s -i 'no-document' /root/.gemrc

if [ $? -ne 0 ];
then
	echo 'gem: --no-document' >> /root/.gemrc
fi

yum -y install ruby ruby-devel rubygems

gem update --system 1.8.25
gem install json -v 1.7.7
gem install bundler -v 1.15.4
gem install net-ssh -v 2.6.5
gem install net-ssh-gateway -v 1.3.0 # this is the last version that can use net-ssh 2.6.5s
gem install mixlib-log -v '1.6.0'
