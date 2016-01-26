# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name = %q{cms}
  s.version = "0.0.4"

  s.required_rubygems_version = Gem::Requirement.new(">= 0") if s.respond_to? :required_rubygems_version=
  s.authors = ["Kire Filipovski"]
  s.date = %q{2011-10-07}
  s.description = %q{cms gem is used to access the configuration management service via REST interface}
  s.email = ["kire@kloopz.com"]
  s.executables = ["cms"]
  s.extra_rdoc_files = ["History.txt", "Manifest.txt", "PostInstall.txt"]
  s.files = [".gemtest", "Gemfile", "Gemfile.lock", "History.txt", "Manifest.txt", "PostInstall.txt", "README.rdoc", "Rakefile", "bin/cms", "cms.gemspec", "lib/cms.rb", "lib/cms/attr_map.rb", "lib/cms/attr_md.rb", "lib/cms/ci.rb", "lib/cms/ci_md.rb", "lib/cms/dj_ci.rb", "lib/cms/dj_relation.rb", "lib/cms/namespace.rb", "lib/cms/relation.rb", "lib/cms/relation_md.rb", "lib/cms/release.rb", "lib/cms/rfc_ci.rb", "lib/cms/rfc_relation.rb", "lib/cms/target_md.rb", "script/console", "script/destroy", "script/generate", "test/test_cms.rb", "test/test_helper.rb"]
  s.homepage = %q{http://github.com/#{github_username}/#{project_name}}
  s.post_install_message = %q{PostInstall.txt}
  s.rdoc_options = ["--main", "README.rdoc"]
  s.require_paths = ["lib"]
  s.rubyforge_project = %q{cms}
  s.rubygems_version = %q{1.3.6}
  s.summary = %q{cms gem is used to access the configuration management service via REST interface}
  s.test_files = ["test/test_cms.rb", "test/test_helper.rb"]

  if s.respond_to? :specification_version then
    current_version = Gem::Specification::CURRENT_SPECIFICATION_VERSION
    s.specification_version = 3

    if Gem::Version.new(Gem::RubyGemsVersion) >= Gem::Version.new('1.2.0') then
      s.add_runtime_dependency(%q<activesupport>, [">= 3.0.6"])
      s.add_runtime_dependency(%q<activemodel>, [">= 3.0.6"])
      s.add_runtime_dependency(%q<activeresource>, [">= 3.0.6"])
      s.add_development_dependency(%q<hoe>, [">= 2.9.4"])
    else
      s.add_dependency(%q<activesupport>, [">= 3.0.6"])
      s.add_dependency(%q<activemodel>, [">= 3.0.6"])
      s.add_dependency(%q<activeresource>, [">= 3.0.6"])
      s.add_dependency(%q<hoe>, [">= 2.9.4"])
    end
  else
    s.add_dependency(%q<activesupport>, [">= 3.0.6"])
    s.add_dependency(%q<activemodel>, [">= 3.0.6"])
    s.add_dependency(%q<activeresource>, [">= 3.0.6"])
    s.add_dependency(%q<hoe>, [">= 2.9.4"])
  end
end
