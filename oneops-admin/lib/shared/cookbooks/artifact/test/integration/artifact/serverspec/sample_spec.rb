require 'serverspec'

describe command('echo "test"') do
  its(:exit_status) { should eq 0 }
end

