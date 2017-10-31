#!/usr/bin/env bash
if [ ! -L "/opt/chef/embedded/bin/gem" ] ; then
    sudo mkdir -p /opt/chef/embedded/bin/
    sudo ln -s /usr/bin/gem /opt/chef/embedded/bin/gem
    sudo ln -s /usr/bin/ruby /opt/chef/embedded/bin/ruby
fi
