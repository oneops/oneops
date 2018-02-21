$:.unshift File.expand_path("../lib", __FILE__)

require 'version'

Gem::Specification.new do |s|
  s.name        = 'oneops-admin'
  s.version     = '1.0.0' # This should also be taken form the parent pom.xml, but don't want to break anything.
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
  s.add_dependency 'rails-observers', '0.1.2'
  s.add_dependency "activesupport", '= 4.1.10'
  s.add_dependency "activeresource", '= 4.0.0'
  s.add_dependency "activemodel", '= 4.1.10'
  s.add_dependency "ffi", '= 1.9.18'
  s.add_dependency "fog", '= 1.38.0'
  s.add_dependency "aws-s3", '= 0.6.3'
  s.add_dependency "chef", '= 11.18.12'
  s.add_dependency "ohai", '= 7.4.1'
  s.add_dependency "mime-types", '= 1.25.1'
  s.add_dependency "mixlib-shellout", '= 1.4.0'
  s.add_dependency "net-ssh", '= 2.6.5'
  s.add_dependency "net-scp", '= 1.1.2'
  s.add_dependency "net-ldap", '= 0.6.1'
  s.add_dependency "json", '~> 1.8.3'
  s.add_dependency "nokogiri", '= 1.5.11'
  s.add_dependency "kramdown", '= 1.9.0'
  s.add_dependency "route53", '= 0.3.2'
  s.add_dependency "ms_rest", '= 0.1.1'
  s.add_dependency "ms_rest_azure", '= 0.1.1'
  s.add_dependency "azure_mgmt_compute", '= 0.1.1'
  s.add_dependency "azure_mgmt_storage", '= 0.1.1'
  s.add_dependency "azure_mgmt_network", '= 0.1.1'
  s.add_dependency "azure_mgmt_resources", '= 0.1.1'
  s.add_dependency "azure", '= 0.6.4'
  s.add_dependency "fog-vsphere", '= 1.5.1'
  s.add_dependency "fog-openstack", '= 0.1.21'
  s.add_dependency "crack", '= 0.4.3'
  s.add_dependency "rack", '= 1.6.4'

  s.add_dependency "rake", '= 10.1.1'
  s.add_dependency "fog-aliyun", '= 0.1.0'
  s.bindir       = 'bin'
  s.require_path = 'lib'
  s.files        = %w() + ["oneops-admin.gemspec"] + ["Gemfile"] + Dir.glob(".chef/**/*") + Dir.glob("lib/**/*", File::FNM_DOTMATCH).reject { |f| f =~ /\.$/ || f =~ /\.\.$/ } + ["#{InductorUtil::JAR}"] + Dir.glob('bin/**/*')
end
