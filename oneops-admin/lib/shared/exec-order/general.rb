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
  get_os_type =~ /windows/ ? 'c:\\opscode\\chef\\embedded\\bin\\' : ''
end

def get_file_from_parent_dir(filename)
  File.join(File.expand_path('..',File.dirname(__FILE__)),filename)
end

def get_proxy_file_name
  (get_os_type =~ /windows/ ? 'c:/cygwin64' : '') + '/opt/oneops/rubygems_proxy'
end
