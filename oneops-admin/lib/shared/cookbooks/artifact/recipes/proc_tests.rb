#
# Cookbook Name:: artifact_test
# Recipe:: proc_tests
#
# Author:: Kyle Allan (<kallan@riotgames.com>)
# 
# Copyright 2013, Riot Games
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

group "artifact"
user "artifact" do
  group "artifact"
end

artifact_deploy "artifact_test" do
  version node[:artifact_test][:version]
  artifact_location node[:artifact_test][:location]
  artifact_checksum node[:artifact_test][:checksum]
  deploy_to "/srv/artifact_test"
  should_migrate true
  owner "artifact"
  group "artifact"

  before_deploy Proc.new {
    directory "/before_deploy" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end
  }

  before_extract Proc.new {
    directory "/before_extract" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure before_deploy worked" do
      block do
        Chef::Application.fatal! "before_deploy failed!" unless ::File.directory?("/before_deploy")
      end
    end
  }

  after_extract Proc.new {
    directory "/after_extract" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure before_extract worked" do
      block do
        Chef::Application.fatal! "before_extract failed!" unless ::File.directory?("/before_extract")
      end
    end
  }

  before_symlink Proc.new {
    directory "/before_symlink" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure after_extract worked" do
      block do
        Chef::Application.fatal! "after_extract failed!" unless ::File.directory?("/after_extract")
      end
    end
  }

  after_symlink Proc.new {
    directory "/after_symlink" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure before_symlink worked" do
      block do
        Chef::Application.fatal! "before_symlink failed!" unless ::File.directory?("/before_symlink")
      end
    end
  }

  configure Proc.new {
    directory "/configure" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure after_symlink worked" do
      block do
        Chef::Application.fatal! "after_symlink failed!" unless ::File.directory?("/after_symlink")
      end
    end
  }

  before_migrate Proc.new {
    directory "/before_migrate" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure configure worked" do
      block do
        Chef::Application.fatal! "configure failed!" unless ::File.directory?("/configure")
      end
    end
  }

  migrate Proc.new {
    directory "/migrate" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure before_migrate worked" do
      block do
        Chef::Application.fatal! "before_migrate failed!" unless ::File.directory?("/before_migrate")
      end
    end
  }

  after_migrate Proc.new {
    directory "/after_migrate" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure migrate worked" do
      block do
        Chef::Application.fatal! "migrate failed!" unless ::File.directory?("/migrate")
      end
    end
  }

  restart Proc.new {
    directory "/restart" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure after_migrate worked" do
      block do
        Chef::Application.fatal! "after_migrate failed!" unless ::File.directory?("/after_migrate")
      end
    end
  }

  after_deploy Proc.new {
    directory "/after_deploy" do
      owner "root"
      group "root"
      mode 00755
      action :create
    end

    ruby_block "make sure restart worked" do
      block do
        Chef::Application.fatal! "restart failed!" unless ::File.directory?("/restart")
      end
    end
  }

  action :deploy
end

ruby_block "make sure after_deploy worked" do
  block do
    Chef::Application.fatal! "after_deploy failed!" unless ::File.directory?("/after_deploy")
  end
end