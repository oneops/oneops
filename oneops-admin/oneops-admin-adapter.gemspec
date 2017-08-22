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
  s.files                 = ['oneops-admin-adapter.gemspec', 'Gemfile'] +
                            Dir.glob('.chef/**/*') +
    Dir.glob('lib/base/**/*') +
    # Dir.glob('lib/chef/**/*') +
                            Dir.glob('lib/chef/catalog_export.rb') +
                            Dir.glob('lib/chef/model_sync.rb') +
                            Dir.glob('lib/chef/model_metadata.rb') +
                            Dir.glob('lib/cms/**/*') +
                            Dir.glob('lib/shared/**/*') +
    Dir.glob('lib/circuit') +
                            Dir.glob('bin/circuit')
  # s.files        = %w() + ["oneops-admin-adapter.gemspec"] + ["Gemfile"] +
  #   Dir.glob(".chef/**/*") +
  #   Dir.glob("lib/**/*") +
  #   Dir.glob('bin/**/*')

  s.add_dependency 'thor', '= 0.19.1'

  s.add_dependency 'activeresource', '= 4.0.0'
  s.add_dependency 'rails-observers', '0.1.2'

  s.add_dependency 'chef', '= 11.18.12'
  s.add_dependency 'rack', '= 1.6.8'

  s.add_dependency 'json', '= 1.8.6'
  s.add_dependency 'kramdown', '= 1.9.0'

  s.add_dependency 'fog', '= 1.38.0'
  s.add_dependency 'nokogiri', '= 1.5.11'
end
