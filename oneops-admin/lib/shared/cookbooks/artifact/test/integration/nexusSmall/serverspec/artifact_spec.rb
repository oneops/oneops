require 'spec_helper'
require 'digest'

group_id, artifact_id, extension, dep = $node['artifact']['location'].split(":")
file_path = $node['artifact']['install_dir'] + "/artifact_deploys/" + $node['workorder']['rfcCi']['ciName'] + "/" + $node['artifact']['version'] + "/#{artifact_id}-#{$node['artifact']['version']}.#{extension}"

# Checkes the checksum of the file if it exists
if $node['artifact'].has_key?('checksum') && !$node['artifact']['checksum'].to_s.empty?
  sha1 = Digest::SHA1.file file_path
  md5  = Digest::MD5.file file_path
  describe "checksum" do
    it "should have matching checksum" do
      bool1 = ($node['artifact']['checksum'] == sha1.base64digest)
      bool2 = ($node['artifact']['checksum'] == sha1.hexdigest)
      bool3 = ($node['artifact']['checksum'] == md5.base64digest)
      bool4 = ($node['artifact']['checksum'] == md5.hexdigest)
      (true).should be == (bool1 || bool2 || bool3 || bool4)
    end
  end
end

describe file(file_path) do
  it { should exist }
  it { should be_file }
  it { should be_mode 600 }
end

describe file($node['artifact']['install_dir'] + "/current") do
  it { should be_symlink }
end

describe file($node['artifact']['install_dir'] + "/releases/" + $node['artifact']['version'] + "/#{artifact_id}-#{$node['artifact']['version']}.#{extension}") do
  it { should exist }
  it { should be_file }
  it { should be_mode 600 }
end

describe file("/log/restart.txt") do
  it { should exist }
  it { should be_file }
  its(:content) { should match /Works/ }
end

describe file("/log/configure.txt") do
  it { should exist }
  it { should be_file }
  its(:content) { should match /Works/ }
end
