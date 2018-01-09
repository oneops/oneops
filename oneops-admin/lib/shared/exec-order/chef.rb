def is_chef_installed? (ostype, version)
  if ostype =~ /windows/ && !File.exists?("#{get_bin_dir}gem")
    false
  else
    is_gem_installed?('chef', version)
  end
end

def install_chef_windows(version)
  start_time = Time.now.to_i
  ec = system("c:/programdata/chocolatey/choco.exe install -y --no-progress --allow-downgrade --allowEmptyChecksums chef-client -version #{version}")
  if !ec || ec.nil?
    puts "choco install result #{$?}"
    exit 1
  end
  duration = Time.now.to_i - start_time
  puts "installed chef-client #{version} in #{duration} seconds"
end

def patch_chef_121118
  # patch the bug in chef 12.11.18
  # https://github.com/chef/chef/issues/5027
  # fixed here https://github.com/chef/chef/blame/master/lib/chef/chef_fs/file_system/multiplexed_dir.rb#L44
  # but the chef-client msi was built and uploaded to chocolatey before it was fixed, so we need to temporarily add this
  # until we can get a new version of the msi uploaded to chocolatey.
  puts "Patch the bug in chef client!!!"
  puts "run a substitute command and put the source back"
  puts "SED command is: sed -i '44s/unless.*//' c:/opscode/chef/embedded/lib/ruby/gems/2.1.0/gems/chef-12.11.18-universal-mingw32/lib/chef/chef_fs/file_system/multiplexed_dir.rb"
  rc = system("sed -i '44s/unless.*//' c:/opscode/chef/embedded/lib/ruby/gems/2.1.0/gems/chef-12.11.18-universal-mingw32/lib/chef/chef_fs/file_system/multiplexed_dir.rb")
  if !rc || rc.nil?
    puts "SED command failed with #{$?}"
    exit 1
  else
    puts "SED Command Success!, #{$?}"
  end

  puts "DONE PATCHING THE CHEF BUG"
end