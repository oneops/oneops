$:.unshift File.expand_path('../lib', __FILE__)

Gem::Specification.new do |s|
  s.name                  = 'oneops-admin-adapter'
  s.version               = '1.0.0'
  s.license               = 'Apache-2.0'
  s.author                = 'OneOps'
  s.email                 = 'support@oneops.com'
  s.homepage              = 'http://www.oneops.com'
  s.summary               = 'OneOps Admin for CMS (adapter).'
  s.description           = 'Use to load CMS content via circuit commands: components metadata, pack definitions, cloud templates.'
  s.executables           = %w(circuit)
  s.platform              = Gem::Platform::RUBY
  s.required_ruby_version = ['>= 1.9.0', '<= 2.1.0']
  s.bindir                = 'bin'
  s.require_path          = 'lib'
  s.files                 = %w(oneops-admin-adapter.gemspec
                               Gemfile
                               bin/circuit
                               lib/chef/knife/base_sync.rb
                               lib/chef/knife/model_sync.rb
                               lib/chef/knife/pack.rb
                               lib/chef/knife/pack_sync.rb
                               lib/chef/knife/cloud.rb
                               lib/chef/knife/cloud_sync.rb
                               lib/chef/knife/register.rb
                               lib/circuit.rb
                               lib/cms.rb
                               .chef/knife.rb) +
                            Dir.glob('lib/cms/*.rb') +
                            Dir.glob('lib/base/**/*') +
                            Dir.glob('lib/shared/**/*') +
                            Dir.glob('test/**/*')

  s.add_dependency 'thor', '= 0.19.1'

  s.add_dependency 'activeresource', '= 4.0.0'
  s.add_dependency 'rails-observers', '0.1.2'

  s.add_dependency 'chef', '= 11.18.12'
  s.add_dependency 'rack', '= 1.6.8'

  s.add_dependency 'json', '= 1.8.6'
  s.add_dependency 'kramdown', '= 1.9.0'

  s.add_dependency 'fog', '= 1.38.0'
  s.add_dependency 'nokogiri', '= 1.5.11'
  s.add_dependency 'fog-openstack', '= 0.1.24'
  s.add_dependency 'fog-json', '= 1.0.2'
  s.add_dependency 'mixlib-config', '= 2.2.4'
  s.add_dependency 'fog-xenserver', '= 0.3.0'
end
