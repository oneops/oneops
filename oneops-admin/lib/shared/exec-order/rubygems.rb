def get_gem_sources_from_file
  rubygems_proxy = []
  if File.file?(get_proxy_file_name)
    rubygems_proxy = File.read(get_proxy_file_name).split("\n").select{|l| (l =~ /^http/)}
  end

  rubygems_proxy
end

def get_gem_sources

  rubygems_proxy = get_gem_sources_from_file
  sources = [ENV['rubygems_proxy'] || rubygems_proxy[0] || ENV['rubygemsbkp_proxy'] || 'https://rubygems.org']

  if ENV['rubygemsbkp_proxy'] && sources[0] != ENV['rubygemsbkp_proxy']
    sources.push(ENV['rubygemsbkp_proxy'])
  elsif !rubygems_proxy[1].nil? && !rubygems_proxy[1].empty?
    sources.push(rubygems_proxy[1])
  end

  sources
end

def update_gem_sources (expected_sources, log_level = 'info')
  gem = "#{get_bin_dir}gem"

  puts "Expected gem sources: #{expected_sources.inspect}"
  actual_sources = `#{gem} source`.split("\n").select{|l| (l =~ /^http/)}
  puts "Actual gem sources: #{actual_sources.inspect}"

  if expected_sources != actual_sources
    puts 'Expected gem sources do not match the actual gem sources. Updating...'

    #it wouldn't delete the last source, so we want to add missing sources first, and we want the sources in correct order
    #1. Add primary
    `#{gem} source --add #{expected_sources[0]}` if !actual_sources.include?(expected_sources[0])
    #2. Delete everything else
    (actual_sources - [expected_sources[0]]).each do |source|
      `#{gem} source --remove #{source}`
    end
    #3. Add secondary
    `#{gem} source --add #{expected_sources[1]}` if expected_sources[1]

  else
    puts 'Expected gem sources match the actual gem sources.'
  end


  proxy_file = get_proxy_file_name
  if !File.exists?(proxy_file) || get_gem_sources_from_file != expected_sources
    puts 'Rubygems_proxy config file is outdated. Updating...'
    File.open(proxy_file, 'w') do |f|
      expected_sources.each {|s| f.puts s}
    end
  end
end


def get_gem_list (provisioner, version = nil, config_file_name = nil)
  require 'yaml'

  if config_file_name.nil?
    config_file_name = 'exec-gems.yaml'
  end
  config_file = get_file_from_parent_dir(config_file_name)
  gem_config = YAML::load(File.read(config_file))

  gem_list  = gem_config['common'] + [[provisioner,version]]
  gem_list += gem_config[provisioner] if gem_config[provisioner]
  gem_list += gem_config["#{provisioner}-#{version}"] if gem_config["#{provisioner}-#{version}"]

  gem_list.uniq
end


def create_gemfile(gem_sources, gems)
  gemfile_content = "source '#{gem_sources[0]}'\n"

  gems.each do |gem_set|
    if gem_set.size > 1
      gemfile_content += "gem '#{gem_set[0]}', '#{gem_set[1]}'\n"
    else
      gemfile_content += "gem '#{gem_set[0]}'\n"
    end
  end

  File.open('Gemfile', 'w') {|f| f.write(gemfile_content) }
end


def is_gem_installed?(gem, version = nil)
  cmd = "#{get_bin_dir}gem list ^#{gem}$ -i" + (version.nil? ? '' : "-v #{version}")
  out = `#{cmd}`.chomp
  out == 'true' ? true : false
end


def check_gem_update_needed (gems, log_level = 'info')
  update_needed = false

  gems.each do |g|
    if !is_gem_installed?(g[0],g[1])
      puts "Gem #{g[0]} version #{g[1]} is not installed."
      update_needed = true
      break
    end
  end

  update_needed
end

def gen_gemfile_and_install (gem_sources, gems, component, provisioner, log_level)

  #2 scenarions when need to run bundle install
  #  1) if running for the first time - determined by checking if provisioner gem (chef, puppet) is installed.
  # Not checking its version though, it would be done in the below check
  #  2) if any gems from exec-gems.yaml (including provisioner gem itself) have mismatching versions

  method = nil
  if !is_gem_installed?(provisioner)
    puts "Provisioner #{provisioner} is not installed, will run bundle install."
    method = 'install'
  elsif check_gem_update_needed(gems, log_level)
    if ['objectstore','compute','volume', 'os'].include?(component)
      puts "Gem update is required for component: #{component}"
      method = 'install'
    else
      puts "Gem update is required but will not be run for component: #{component}"
    end
  else
    puts 'No gem update is required.'
  end

  if !method.nil?
    start_time = Time.now.to_i
    ['Gemfile', 'Gemfile.lock'].each {|f| File.delete(f) if File.file?(f)}
    create_gemfile(gem_sources, gems)

    cmd = "#{get_bin_dir}bundle #{method} --local"
    ec = system cmd
    if !ec || ec.nil?
      puts "#{cmd} failed with, #{$?}"
      puts 'fetching gems from remote sources'

      cmd = "#{get_bin_dir}bundle #{method} --full-index"
      ec = system cmd
      if !ec || ec.nil?
        puts "#{cmd} failed with, #{$?}"
        exit 1
      end
    end

    puts "#{cmd} took: #{Time.now.to_i - start_time} sec"
  end
end

def install_using_prebuilt_gemfile (gem_sources, component, provisioner, provisioner_version)

  if ['objectstore','compute','volume', 'os'].include?(component)
    start_time = Time.now.to_i
    gemfile = "exec-gems-#{provisioner}-#{provisioner_version}.gemfile"
    if get_os_type =~ /windows/ && File.file?("#{gemfile}.lock")
      File.delete("#{gemfile}.lock")
    end
    cmd = "#{get_bin_dir}bundle install --local --gemfile #{gemfile}"
    ec = system cmd
    if !ec || ec.nil?
      puts "#{cmd} failed with, #{$?}"
      puts 'fetching gems from remote sources'

      cmd = "#{get_bin_dir}bundle install --full-index --gemfile #{gemfile}"
      ec = system cmd
      if !ec || ec.nil?
        puts "#{cmd} failed with, #{$?}"
        exit 1
      end
    end

    if get_os_type =~ /windows/
      bundler = `#{get_bin_dir}gem which bundler`
      libdir = File.dirname(bundler)
      $LOAD_PATH.unshift(libdir) unless $LOAD_PATH.include?(libdir)
    end
    require 'bundler'
    ENV['BUNDLE_GEMFILE'] = gemfile unless ENV['BUNDLE_GEMFILE']
    lockfile = Bundler::LockfileParser.new(Bundler.read_file("#{gemfile}.lock"))
    lockfile.specs.each do |s|
      if s.source.is_a?(Bundler::Source::Path) &&
      s.full_name == 'fog-openstack-0.1.24'
        puts "Installing gem #{s.full_name} from source."
        gem_dir = File.expand_path(s.source.path, File.dirname(gemfile))
        gem_path = File.join(gem_dir, "#{s.full_name}.gem")
        cmd = "#{get_bin_dir}gem install '#{gem_path}' --ignore-dependencies --no-ri --no-rdoc"
        ec = system cmd
        if !ec || ec.nil?
          puts "#{cmd} failed with, #{$?}"
          exit 1
        end

        cmd = "chown -R oneops:oneops ./vendor"
        ec = system cmd
        if !ec || ec.nil?
          puts "#{cmd} failed with, #{$?}"
          exit 1
        end
      end
    end

    puts "#{cmd} took: #{Time.now.to_i - start_time} sec"
  else
    puts "gem install doesn't run for component:#{component}"
  end

end
