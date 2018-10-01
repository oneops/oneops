module ExecOrderUtils

  class CustomRuby
    PARENT_DIR = '/home/oneops/ruby/'.freeze
    ARTIFACT_ID = 'ruby'.freeze
    METADATA_FILE = 'maven-metadata.xml'.freeze
    PREFIX = '[CUSTOM RUBY]'.freeze

    def initialize(workorder, component)
      require 'fileutils'
      @wo_hash = JSON.parse(File.read(workorder))
      set_env_vars
      @custom_ruby_dir = File.join(PARENT_DIR, @custom_ruby_version)
      @provider = provider
      @version_api = version_api
      @os_version = os_version
      @component = component
    end

    # The method determines if a custom ruby installation is required
    # These conditions are checked:
    # 1) Openstack API version > 2 && system Ruby version < 2 && Openstack
    # 2) compute deployment - component=compute
    # 3) Openstack API version > 2 && fast image && centos6
    #    - current fast image for centos6 does not have all the gems
    # Returns boolean
    # Ruby and API versions are hard-coded for now
    def custom_ruby_required?
      result = false
      if @provider == 'openstack' && !@os_version.nil?
        if RUBY_VERSION.to_i < 2 && @version_api > 2
          result = true
          info("Required for Openstack API version #{@version_api}")
        elsif RUBY_VERSION.to_i < 2 && @component == 'compute'
          result = true
          info('Required for compute deployments in Openstack')
        elsif @os_version == 'centos6' && File.exist?('/etc/oneops-tools-inventory.yml')
          result = true
          info('Required for Centos6 fast images in Openstack')
        end
      end

      # Custom ruby required but cannot be installed as one of the necessary
      # variables is not set
      missing = required_vars.detect do |var| 
        value = instance_variable_get("@#{var}")
        value.nil? || value.empty?
      end
      if result && !missing.nil?
        info("Variable #{missing} is not provided, skipping custom ruby \
        installation...")
        result = false
      elsif !result
        info('Is not required')
      end

      result
    end

    # The method returns boolean value if custom ruby installation is needed
    # to be performed, i.e. it's either not installed yet, or the version is
    # lower than reqired
    # TO-DO - if the same major version but diff patch level is installed -
    # use the latest patch version for the same major version
    def install_needed?
      v_req = Gem::Version.new(@custom_ruby_package_version.gsub('-SNAPSHOT', ''))
      pkg_v = installed_package_version ? installed_package_version.gsub('-SNAPSHOT', '') : nil
      v_cur = Gem::Version.new(pkg_v)
      result = v_req <= v_cur ? false : true
      info("Installation is needed: #{result}")
      result
    end

    def installed_package_version
      pkg_v = nil
      if File.directory?(@custom_ruby_dir)
        pkg_v_file = File.join(@custom_ruby_dir, 'package_version')
        pkg_v = File.read(pkg_v_file) if File.file?(pkg_v_file)
      end
      pkg_v
    end

    # The method downloads the latest package, and installs it onto PARENT_DIR
    # 0. Obtain exclusive lock
    # 1. Construct proximity url
    # 2. Download metadata xml
    # 3. Determine the package file full name from the metadata
    # 4. Download the package tarball
    # 5. unarchive the tarball
    def install_custom_ruby
      info('Installing...')

      # Obtain exclusive lock
      File.open('/tmp/exec_order.lock', File::RDWR | File::CREAT, 0644) do |f|
        f.flock(File::LOCK_EX)
        url, name = package_url
        download_file(url)
        FileUtils.mkdir PARENT_DIR unless File.directory?(PARENT_DIR)
        unpackage(name, PARENT_DIR)
      end
    end

    def package_url
      pkg_file = nil
      if @custom_ruby_package_version =~ /SNAPSHOT/
        md = File.join(base_url, METADATA_FILE)
        download_file(md)
        install_xmlsimple
        md_hash = XmlSimple.xml_in(METADATA_FILE)
        sv = md_hash['versioning'][0]['snapshotVersions'][0]['snapshotVersion'][0]
        pkg_file = md_hash['artifactId'][0] + '-' + sv['value'][0] + '.' +
                   sv['extension'][0]
        `rm -f #{METADATA_FILE}`
      else
        pkg_file = ARTIFACT_ID + '-' + @custom_ruby_package_version + '.tar.gz'
      end
      return File.join(base_url, pkg_file), pkg_file
    end

    private

    def base_url
      File.join(
        @proximity, proximity_repo, '/com/oneops/ruby',
        @custom_ruby_version.tr('.', '-'), @os_version,
        @custom_ruby_chef_version.tr('.', '-'),
        @provider, ARTIFACT_ID, @custom_ruby_package_version
      )
    end

    def install_xmlsimple
      out = `gem install xml-simple --no-ri --no-rdoc --version 1.1.5`
      if $?.to_i > 0
        error(out)
        raise 'Error instaling xml-simple'
      end
      out = `gem which xmlsimple`.chomp
      require out.to_s
    end

    def download_file(url)
      name = url.split('/').last
      FileUtils.rm_f(name)
      out = `wget #{url} 2> /dev/null`
      if $?.to_i > 0
        error(out)
        raise "Error downloading #{url}"
      end
    end

    def unpackage(name, dir)
      out = `tar zxf #{name} -C #{dir}`
      if $?.to_i > 0
        error(out)
        raise "Error un-packaging #{name}"
      end
      `rm -f #{name}`
    end

    # Currently returns nil for anything other than centos/redhat
    def os_version
      osv = nil
      # Check for redhat/centos
      if File.file?('/etc/redhat-release')
        rc = `cat /etc/redhat-release | grep -oP '(?<= )[0-9]+(?=\.)'`
        osv = "centos#{rc.chomp}" if $?.to_i == 0
      end
      # Check for debian/ubuntu
      if osv.nil?
        rc = `lsb_release -r -s`
        osv = "ubuntu#{rc.chomp.to_i}" if $?.to_i == 0
      end
      info("OS version: #{osv}")
      osv
    end

    def compute_service
      cs = nil
      if @wo_hash['workorder']['services'].key?('compute')
        cloud_name = @wo_hash['workorder']['cloud']['ciName']
        cs = @wo_hash['workorder']['services']['compute'][cloud_name]
      end
      cs
    end

    def provider
      provider = nil
      cs = compute_service
      provider = cs['ciClassName'].split('.').last.downcase if cs
      provider
    end

    def version_api
      version = nil
      cs = compute_service
      endpoint = cs['ciAttributes']['endpoint'] if cs
      version = endpoint.split('/').detect { |i| i =~ /^v[0-9.]+$/ } if endpoint
      version = version[1..-1] if version
      version.nil? ? 0 : version.to_f
    end

    # The method determines correct values for custom_ruby variables that are
    # configured globally - either as cms vars or cloud env variables
    # Cloud env variables take precedence over global cms vars
    # These 4 variables are being set as instance variables:
    # custom_ruby_version - custom ruby version
    # custom_ruby_package_version - proximity artifact version
    # custom_ruby_chef_version - the chef version for custom ruby
    # proximity - proximity url
    def set_env_vars
      cs_vars = JSON.parse(compute_service['ciAttributes']['env_vars'])
      cms_vars = @wo_hash['workorder']['config']
      required_vars.each do |name|
        value = cs_vars[name]
        value = cms_vars[name] if value.nil? || value.empty?
        instance_variable_set("@#{name}", value)
      end
    end

    def required_vars
      %w(custom_ruby_version custom_ruby_package_version
         custom_ruby_chef_version proximity)
    end

    def info(msg)
      puts PREFIX + msg
    end

    def error(msg)
      puts PREFIX + ' ERROR ' + msg
    end

    def proximity_repo
      if @custom_ruby_package_version =~ /SNAPSHOT/
        'pangaea_snapshots'
      else
        'pangaea_releases'
      end
    end
  end
end
