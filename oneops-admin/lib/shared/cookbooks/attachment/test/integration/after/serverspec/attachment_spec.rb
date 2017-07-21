require 'spec_helper'

describe file('/tmp/download_file') do
  puts "------after-------"
  it { should exist }
  it { should be_executable }
end
describe command('/tmp/download_file') do
  its(:exit_status) { should eq 0 }
end
