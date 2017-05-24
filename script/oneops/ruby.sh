
echo '==> Configuring ruby for vagrant'

grep -s -i 'no-document' /root/.gemrc

if [ $? -ne 0 ];
then
	echo 'gem: --no-document' >> /root/.gemrc
fi

gem update --system 2.6.1
gem install json -v 1.8.3
gem install bundler
gem install rake
gem install net-ssh -v 2.9.1
gem install mixlib-log -v '1.6.0'
echo "export PATH=$PATH:/usr/local/bin" > /etc/profile.d/gem_bin.sh