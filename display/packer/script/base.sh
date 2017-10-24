set -e 

# build-essential
yum -y install autoconf bison flex gcc gcc-c++ kernel-devel make m4 patch

yum -y install ruby ruby-devel postgresql-devel

gem install bundle --no-ri --no-rdoc

