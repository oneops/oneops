def get_os_type (log_level = 'info')
  if RUBY_PLATFORM =~ /mingw/
    ostype = 'windows'
  else
    ostype = 'linux'
  end

  if log_level == 'debug'
    puts "RUBY_PLATFORM: #{RUBY_PLATFORM}"
    puts "OS type: #{ostype}"
  end
  ostype
end

def get_bin_dir
  os_type = get_os_type
  bin_dir = (os_type =~ /windows/ ? 'c:\\opscode\\chef\\embedded\\bin\\' : '')
  bin_dir = File.file?("/home/oneops/ruby/ruby-2.0.0-p648/bin/ruby") ? "/home/oneops/ruby/ruby-2.0.0-p648/bin/" : bin_dir
  bin_dir = File.file?("/home/oneops/ruby/2.0.0-p648/bin/ruby") ? "/home/oneops/ruby/2.0.0-p648/bin/" : bin_dir
end

def get_file_from_parent_dir(filename)
  File.join(File.expand_path('..',File.dirname(__FILE__)),filename)
end

def get_proxy_file_name
  (get_os_type =~ /windows/ ? 'c:/cygwin64' : '') + '/opt/oneops/rubygems_proxy'
end

def update_ruby(component)
  #
  # Check to see if we have older Ruby version
  # We are not modifying anything. Let's the Ruby
  # system kicks off this, if we detects older
  # version when can kick off the update process
  # only if it's hasn't been done.  If the update
  # has been performed then it will use the newer
  # Ruby instead of system Ruby.
  #

  if (Gem::Version.new(RUBY_VERSION.dup) < Gem::Version.new("2.0.0")) && (['objectstore','compute','volume', 'os'].include?(component))
    install_ruby_cmd = "curl #{ENV['RUBY2_BINARY_proxy']} | tar Pxz  && /home/oneops/ruby/ruby-2.0.0-p648/bin/gem install bundler --no-ri --no-rdoc  && chown -R oneops:oneops /home/oneops/ruby"
    ruby_binary_path = "/home/oneops/ruby/ruby-2.0.0-p648/bin/ruby"
    ruby_bin = File.file?(ruby_binary_path) ? ruby_binary_path : "/usr/bin/ruby"

    updated = ruby_bin.eql?(ruby_binary_path) ? true : false

    if updated && File.exists?('/home/oneops/ruby/ruby-2.0.0-p648/lib/ruby/gems/2.0.0')
      ENV['GEM_PATH'] = '/home/oneops/ruby/ruby-2.0.0-p648/lib/ruby/gems/2.0.0'
      ENV['PATH'] = "/home/oneops/ruby/ruby-2.0.0-p648/bin:#{ENV['PATH']}"
    end

    impl = "oo::chef-11.18.12"

    unless File.file?(ruby_binary_path)
      # If RUBY2_BINARY_proxy is not available as ENV
      # then skip otherwise continue
      # This RUBY2_BINARY environment must be configured
      # in the cloud setup of the compute that point
      # to a stand alone pre-compiled Ruby 2.0.0
      unless ENV['RUBY2_BINARY_proxy'].to_s.empty?
        File.open("/tmp/ruby2.lock", File::RDWR|File::CREAT, 0644) { |f|
          # Put in exclusive write lock 
          # if we can't get a write lock then
          # we must not proceed with the update
          # this ensure that only one process
          # can run this update at any given time
          # 
          # If we can't get the exclusive lock it would
          # return false
          # This is a non-block call with File::LOCK_NB
          if f.flock(File::LOCK_EX|File::LOCK_NB)
            puts "Downloading and installing Ruby 2.0.0 ..."
            puts "Running command #{install_ruby_cmd}"
            system install_ruby_cmd
            if $?.exitstatus != 0
              puts "Failed to update Ruby to newer version"
            else
              if File.file?(ruby_binary_path)
                updated = true
                ruby_bin = ruby_binary_path

                if File.exists?('/home/oneops/ruby/ruby-2.0.0-p648/lib/ruby/gems/2.0.0')
                  ENV['GEM_PATH'] = '/home/oneops/ruby/ruby-2.0.0-p648/lib/ruby/gems/2.0.0'
                  ENV['PATH'] = "/home/oneops/ruby/ruby-2.0.0-p648/bin:#{ENV['PATH']}"
                end
              end
            end
            f.flock(File::LOCK_UN)
          end
        }
      end
    end
  end
  return { "updated" => updated, "ruby_bin" => ruby_bin, "impl" => impl }
end
