$:.unshift File.expand_path("../lib", __FILE__)

require 'version'

Gem::Specification.new do |s|
  s.name        = 'oneops-admin'
  s.version     = '1.0.0'
  s.license     = 'Apache 2.0'
  s.author      = 'OneOps'
  s.email       = 'support@oneops.com'
  s.homepage    = 'http://www.oneops.com'
  s.summary     = 'OneOps Admin'
  s.description = 'Oneops-admin gem'
  s.executables = %w(circuit inductor i)

  s.platform         = Gem::Platform::RUBY
  s.extra_rdoc_files = %w()

  s.add_dependency "thor", ">= 0.16.0"
  s.add_dependency "activesupport", "= 3.1.12"
  s.add_dependency "activeresource", "= 3.1.12"
  s.add_dependency "activemodel", "= 3.1.12"
  s.add_dependency "route53", ">= 0.3.0"
  s.add_dependency "net-scp", "= 1.1.2"
  s.add_dependency "fog", "= 1.29.0"
  s.add_dependency "aws-s3", ">= 0.6.3"

  s.add_development_dependency "rake"

  s.bindir       = 'bin'
  s.require_path = 'lib'
  s.files        = %w() + ["Gemfile"] + Dir.glob(".chef/**/*") + Dir.glob("lib/**/*") + ["target/inductor-#{Inductor::VERSION}.jar"] + Dir.glob('bin/**/*')
end
