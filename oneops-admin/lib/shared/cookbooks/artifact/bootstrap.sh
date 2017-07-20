#!/bin/bash

#gem source --remove 'http://rubygems.org'
#gem source --add 'http://repos.walmart.com/gemrepo/gems/'
echo "downloading aws-s3 and parallel and i18n"
/opt/chef/embedded/bin/gem install aws-s3 -v 0.6.3 --conservative
/opt/chef/embedded/bin/gem install parallel -v 1.11.2 --conservative
/opt/chef/embedded/bin/gem install i18n -v 0.6.9 --conservative
/opt/chef/embedded/bin/gem install activesupport -v 3.2.11 --conservative
echo "Done"



echo "adding user app"
useradd app
echo "done"


#echo "nothing here"