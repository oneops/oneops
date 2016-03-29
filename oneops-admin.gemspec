$:.unshift File.expand_path("../lib", __FILE__)

require 'version'

Gem::Specification.new do |s|
  s.name        = 'oneops-admin'
  s.version     = '1.0.0'
  s.license     = 'Apache-2.0'
  s.author      = 'OneOps'
  s.email       = 'support@oneops.com'
  s.homepage    = 'http://www.oneops.com'
  s.summary     = 'OneOps Admin'
  s.description = 'OneOps circuit and inductor commands.'
  s.executables = %w(circuit inductor i)

  s.platform         = Gem::Platform::RUBY
  s.extra_rdoc_files = %w()

  s.add_dependency "thor", '= 0.19.1'
  s.add_dependency "activesupport", '= 4.1.10'
  s.add_dependency "activeresource", '= 4.0.0'
  s.add_dependency "activemodel", '= 4.1.10'
  s.add_dependency "ffi", '= 1.9.10'
  s.add_dependency "fog", '= 1.29.0'
  s.add_dependency "aws-s3", '= 0.6.3'
  s.add_dependency "chef", '= 11.18.12'
  s.add_dependency "ohai", '= 7.4.1'
  s.add_dependency "mime-types", '= 1.25.1'
  s.add_dependency "mixlib-shellout", '= 1.4.0'
  s.add_dependency "net-ssh", '= 2.6.5'
  s.add_dependency "net-scp", '= 1.1.2'
  s.add_dependency "net-ldap", '= 0.6.1'   
  s.add_dependency "json", '= 1.8.3'
  s.add_dependency "nokogiri", '= 1.5.11'
  s.add_dependency "kramdown", '= 1.9.0'
  s.add_dependency "route53", '= 0.3.2'
  s.add_dependency "azure_mgmt_compute", '= 0.1.1'
  s.add_dependency "azure_mgmt_storage", '= 0.1.1'
  s.add_dependency "azure_mgmt_network", '= 0.1.1'
  s.add_dependency "azure_mgmt_resources", '= 0.1.1'
  s.add_dependency "rake", '= 10.1.1'

  s.bindir       = 'bin'
  s.require_path = 'lib'
  s.files        = %w() + ["oneops-admin.gemspec"] + ["Gemfile"] + Dir.glob(".chef/**/*") + Dir.glob("lib/**/*") + ["target/inductor-#{Inductor::VERSION}.jar"] + Dir.glob('bin/**/*')
end
