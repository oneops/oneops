
echo '==> Configuring ruby for vagrant'

grep -s -i 'no-document' /root/.gemrc

if [ $? -ne 0 ];
then
	echo 'gem: --no-document' >> /root/.gemrc
fi

# install rvm since most distro has older version of ruby

curl -sSL https://rvm.io/mpapis.asc | gpg --import -

curl -L get.rvm.io | bash -s stable

source /etc/profile.d/rvm.sh

rvm reload

rvm requirements run

# install pre-compiled ruby to save time
# pin default 2.0.0 as default
if [ -f /tmp/ruby-2.3.3.tar.bz2 ]
then
	rvm mount /tmp/ruby-2.3.3.tar.bz2
	rvm use 2.3.3 --default
elif [ -f /tmp/ruby-2.0.0-p648.tar.bz2 ]
then
	rvm mount /tmp/ruby-2.0.0-p648.tar.bz2
	rvm use 2.0.0 --default
fi

#gem update --system 2.6.1
gem install json -v 1.8.6
gem install bundler
gem install rake
gem install net-ssh -v 2.6.5
gem install net-ssh-gateway -v 1.3.0 # this is the last version that can use net-ssh 2.6.5s
gem install mixlib-log -v '1.6.0'
