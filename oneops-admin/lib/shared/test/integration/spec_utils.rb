module ExecOrderTest
  # Helper class to run integration tests for exec-order.rb
  class SpecUtils
    def initialize(suite)
      @data_dir = File.expand_path('../../../', __FILE__)
      Dir.glob(File.join(@data_dir, 'exec-order/*.rb')).each { |f| require f }

      @suite = suite
      @impl = impl
      @json = json
      @circuit = @impl == 'chef-11.4.0' ? 'circuit-main-1' : 'circuit-oneops-1'

      prepare_dirs
      rename_az_files if azure?
    end

    # Returns a shell command to execute exec-order.rb
    # with all required environmental variables and arguments
    def cmd(gem_source, component)
      [
        env_vars(gem_source, component),
        exec_order,
        "oo::#{@impl}",
        @json,
        @circuit
      ].join(' ')
    end

    # Repeats logic from exec-order.rb (which is not incapsulated in a method)
    # Returns a full path to config file for chef-solo command
    def chef_config
      ci = @json.split('/').last.gsub('.json', '')
      "#{prefix_root}/home/oneops/#{@circuit}/components/cookbooks/" \
      "chef-#{ci}.rb"
    end

    # Use gem_gem_list method from exec-order/rubygems
    # Returns an array of gems that are supposed to be installed
    # for this version of provisioner (ex: chef 11.18.12)
    def gems
      provisioner, version = @impl.split('-')
      get_gem_list(provisioner, version)
    end

    private

    # Mimic rsync-ing circuits from an inductor to VM
    # Sets correct permissions
    # Creates circuit folders and copies necessary files from data dir
    def prepare_dirs
      require 'fileutils'
      recipe = File.join(@data_dir, '/test/integration/test_recipe.rb')

      FileUtils.chmod(755, exec_order)
      FileUtils.chown_R('vagrant', 'vagrant', '/home/oneops')
      FileUtils.mkdir_p("/home/oneops/#{@circuit}/components/cookbooks")
      FileUtils.mkdir_p('/home/oneops/shared/cookbooks/test/recipes')
      FileUtils.cp_r(recipe, '/home/oneops/shared/cookbooks/test/recipes/')
    end

    def rename_az_files
      require 'fileutils'

      az_files.each do |f|
        orig = File.join(@data_dir, f[:dir], f[:original])
        az = File.join(@data_dir, f[:dir], f[:azure])

        FileUtils.mv(orig, "#{orig}.bak", :force => true)
        FileUtils.mv(az, orig, :force => true)
      end
    end

    def az_files
      [{ :dir => '',
         :original => 'exec-gems.yaml',
         :azure => 'exec-gems-az.yaml' },
       { :dir => 'cookbooks',
         :original => "exec-gems-#{@impl}.gemfile",
         :azure => "exec-gems-az-#{@impl}.gemfile" },
       { :dir => 'cookbooks',
         :original => "exec-gems-#{@impl}.gemfile.lock",
         :azure => "exec-gems-az-#{@impl}.gemfile.lock" }]
    end

    def env_vars(gem_source, component)
      "rubygems_proxy=#{gem_source} class=#{component} pack=#{@circuit}"
    end

    def windows?
      get_os_type('info') =~ /windows/
    end

    def azure?
      @suite[-3..-1] == '-az'
    end

    def impl
      if windows?
        'oo::chef-12.11.18'
      elsif azure?
        @suite[0..-4]
      else
        @suite
      end
    end

    def prefix_root
      windows? ? 'c:/cygwin64' : ''
    end

    def json
      File.join(
        prefix_root,
        @data_dir,
        'test/integration/test_workorder.json'
      )
    end

    def exec_order
      File.join(@data_dir, 'exec-order.rb')
    end
  end
end
