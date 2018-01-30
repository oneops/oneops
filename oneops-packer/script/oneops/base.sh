# vim-common is required for xxd command
yum -y install vim-common git libxml2 libxml2-devel libxslt libxslt-devel java-1.8.0-openjdk-devel nc bind-utils

#build-essential
yum -y install autoconf bison flex gcc gcc-c++ kernel-devel make m4 patch

#addtional rpms
yum -y install graphviz