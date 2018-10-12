$:.unshift File.expand_path('../lib', __FILE__)

Gem::Specification.new do |s|
  s.name                  = 'oneops-admin-inductor-az'
  s.version               = '1.0.0'
  s.license               = 'Apache-2.0'
  s.author                = 'OneOps'
  s.email                 = 'support@oneops.com'
  s.homepage              = 'http://www.oneops.com'
  s.summary               = 'OneOps CLI for indcutor.'
  s.description           = 'Use to run inductor commands.'
  s.executables           = %w(inductor i)
  s.platform              = Gem::Platform::RUBY
  s.required_ruby_version = '>= 2.0'
  s.bindir                = 'bin'
  s.require_path          = 'lib'

  s.files                 = %w(oneops-admin-inductor-az.gemspec
                              Gemfile
                              bin/i
                              bin/inductor
                              lib/inductor.rb
                              .chef/knife.rb) +
      Dir.glob('bin/logstash-forwarder/**/*') +
      Dir.glob('target/inductor-*.jar') +
      Dir.glob('lib/templates/inductor/**/*') +
      Dir.glob('lib/templates/cloud/**/*') +
      (Dir.glob('lib/base/**/*', File::FNM_DOTMATCH) + Dir.glob('lib/shared/**/*', File::FNM_DOTMATCH).reject{ |f| f =~ /exec-gems(-chef|.yaml)/}).
          reject {|f| f =~ (/\.(\.|png)?$/)}

  s.extensions = ['Rakefile']

  s.add_dependency 'thor', '= 0.19.1'
  s.add_dependency 'chef', '= 11.18.12'
  s.add_dependency 'rack', '= 1.6.8'
  s.add_dependency 'json', '= 1.8.6'
  s.add_dependency 'fog', '= 1.38.0'
  s.add_dependency 'fog-openstack', '= 0.1.24'
  s.add_dependency 'fog-json', '= 1.0.2'
  #s.add_dependency 'ffi', '= 1.9.18'
  s.add_dependency 'nokogiri', '~> 1.6.0'
  s.add_dependency 'ohai', '= 7.4.1'
  s.add_dependency 'mixlib-shellout', '= 1.4.0'
  s.add_dependency 'net-ssh', '= 2.6.5'
  s.add_dependency 'net-scp', '= 1.1.2'
  s.add_dependency 'net-ldap', '= 0.6.1'
  s.add_dependency 'fog-azure-rm', '= 0.4.8'
  s.add_dependency 'crack', '= 0.4.3'
  s.add_dependency 'mixlib-config', '= 2.2.4'
  s.add_dependency 'fog-xenserver', '= 0.3.0'
end
