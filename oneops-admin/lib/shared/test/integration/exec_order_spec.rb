require File.join(File.dirname(__FILE__), 'spec_helper.rb')
require File.join(File.dirname(__FILE__), 'spec_utils.rb')

suite = ENV['KITCHEN_SUITE']
spec = ExecOrderTest::SpecUtils.new(suite)
gems = spec.gems

# set cwd to shared/cookbooks directory
Dir.chdir 'data'
`chmod +x #{spec.cache_exec_gems}`

describe 'cache-exec-gems.rb' do
  let(:cmd) { spec.cache_exec_gems }
  let(:cache_cmd) { Serverspec::Type::Command.new(cmd) }
  it 'executes successfully' do
    expect(cache_cmd.exit_status).to eq(0)
  end
end if RUBY_VERSION.to_i >= 2

describe 'exec-order.rb for os component' do
  let(:gem_source) { 'https://rubygems.org' }
  let(:cmd) { spec.cmd(gem_source, 'os') }
  let(:exec_order_cmd) { Serverspec::Type::Command.new(cmd) }
  let(:gem_source_cmd) do
    Serverspec::Type::Command.new('gem source | grep http')
  end

  it 'executes successfully' do
    expect(exec_order_cmd.exit_status).to eq(0)
  end

  it 'modifies gem source' do
    expect(gem_source_cmd.stdout.chomp).to eq(gem_source)
  end

  describe file(spec.chef_config) do
    it 'exists' do
      expect(subject).to exist
    end
  end
end

describe 'exec-order.rb for artifact component' do
  let(:gem_source) { 'https://rubygems-fake.org' }
  let(:cmd) { spec.cmd(gem_source, 'artifact') }
  let(:exec_order_cmd) { Serverspec::Type::Command.new(cmd) }
  let(:gem_source_cmd) do
    Serverspec::Type::Command.new('gem source | grep http')
  end

  it 'executes successfully' do
    expect(exec_order_cmd.exit_status).to eq(0)
  end

  it 'does not modify gem source' do
    expect(gem_source_cmd.stdout.chomp).not_to eq(gem_source)
  end
end

# use get_gem_list function from rubygems.org to generate a list of gems
# To test if gems are installed and activated we need to shell out, otherwise
# the assertions would run inside the rspec context, i.e. using gems like
# rspec, net-ssh etc, installed into /tmp/verifier/gems and activated, which
# may cause incorrect results

gems.each do |g_name, g_version|
  describe "Gem #{g_name}, version #{g_version}" do
    let(:g_cmd_sh) { "gem list ^#{g_name}$ -v #{g_version} -i" }
    let(:g_cmd) { Serverspec::Type::Command.new(g_cmd_sh) }
    it 'is installed' do
      expect(g_cmd.stdout).to match(/^true/)
    end

    context 'when activate' do
      let(:activate_cmd_sh) { "ruby -e \"gem '#{g_name}','#{g_version}' \"" }
      let(:activate_cmd) { Serverspec::Type::Command.new(activate_cmd_sh) }

      it 'is activated' do
        expect(activate_cmd.stdout).to be
      end
    end
  end
end

# Check if the test::test_recipe has run
describe file('/tmp/test') do
  it { should be_file }
  its(:content) { should eq 'test recipe has run successfully' }
end
