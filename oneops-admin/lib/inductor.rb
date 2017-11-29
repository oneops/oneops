require 'thor'
require 'json'

class Inductor < Thor
  include Thor::Actions

  desc "create", "Creates and configures a new inductor"
  method_option :path, :default => File.expand_path('inductor', Dir.pwd)
  method_option :force, :default => true
  def create

    if !File.exists?("inductor/user")
      current_user=`whoami`.chomp
      `mkdir -p inductor`
      File.write("inductor/user",current_user)
    else
      validate_user
    end

    directory File.expand_path('templates/inductor',File.dirname(__FILE__)), options[:path]
    empty_directory "#{options[:path]}/clouds-available"
    empty_directory "#{options[:path]}/clouds-enabled"
    empty_directory "#{options[:path]}/log"
    empty_directory "#{options[:path]}/shared"
    directory File.expand_path('shared', File.dirname(__FILE__)), "#{options[:path]}/shared"

    # local gem repo - remove remote gemrepo dependency and optimize speed
    empty_directory "#{options[:path]}/shared/cookbooks/vendor"
    empty_directory "#{options[:path]}/shared/cookbooks/vendor/cache"

    # chmod exec-order.rb
    `chmod +x #{options[:path]}/shared/exec-order.rb`

    if ENV.has_key?("USE_GEM_CACHE")
      gem_paths = `gem env path`.chomp.split(":")
      gem_paths.each do |path|
        run("cp #{path}/cache/* #{options[:path]}/shared/cookbooks/vendor/cache/")
      end
      Dir.chdir("#{options[:path]}/shared/cookbooks/vendor/cache/")
      run("gem generate_index")
    end

    say_status :success, "Next Step: cd inductor ; inductor add"
  end

  desc "add", "Add cloud to the inductor"
  method_option :mqhost, :type => :string, :default => 'localhost'
  method_option :mqport, :type => :numeric, :default => 61617
  method_option :daq_enabled, :type => :string
  method_option :tunnel_metrics, :type => :string
  method_option :collector_domain, :type => :string
  method_option :perf_collector_cert, :type => :string
  method_option :dns, :type => :string
  method_option :debug, :type => :string
  method_option :ip_attribute, :type => :string
  method_option :queue, :type => :string
  method_option :authkey, :type => :string
  method_option :mgmt_url, :type => :string
  method_option :logstash_cert_location, :type => :string
  method_option :logstash_hosts, :type => :string
  method_option :max_consumers, :type => :string
  method_option :local_max_consumers, :type => :string
  method_option :additional_java_args, :type => :string
  method_option :env_vars, :type => :string
  method_option :amq_truststore_location, :type => :string
  method_option :force, :default => true
  method_option :verifier_mode, :default => false
  def add
    @inductor_dir = File.expand_path(Dir.pwd)
    validate_user
    if options[:mqhost]
      @mqhost = options[:mqhost]
    else
      @mqhost = ask("What message queue host (if empty defaults to localhost)?")
      @mqhost = 'localhost' if @mqhost.empty?
    end

    if options[:verifier_mode]
      @verifier_mode = options[:verifier_mode]
    end

    if options[:dns]
      @dns = options[:dns]
    else
      @dns = ask("Manage dns? (on or off - defaults to off)")
      @dns = 'off' if @dns.empty?
    end

    if options[:debug]
      @debug = options[:debug]
    else
      @debug = ask("Debug mode? (keeps ssh keys and doesn't terminate compute on compute::add failure. on or off - defaults to off)")
      @debug = 'off' if @debug.empty?
    end

    if options[:daq_enabled]
      @daq_enabled = options[:daq_enabled]
    else
      @daq_enabled = ask("Metrics collections? (if empty defaults to false)?")
      @daq_enabled = 'false' if @daq_enabled.empty?
    end

    @collector_domain = ''
    @tunnel_metrics = ''
    if @daq_enabled == 'true'

      if options[:collector_domain]
        @collector_domain = options[:collector_domain]
      else
        @collector_domain = ask("What collector domain (the domain of your forge or collector)?")
      end

      if options[:tunnel_metrics]
        @tunnel_metrics = options[:tunnel_metrics]
      else
        @tunnel_metrics = ask("Tunnel metrics thru ssh tunnel (defaults to off)?")
        @tunnel_metrics = 'off' if @tunnel_metrics.empty?
      end

      if options[:perf_collector_cert]
        @perf_collector_cert_location = options[:perf_collector_cert]
      else
        @perf_collector_cert_location = ask("Perf Collector cert file location ? (If empty defaults to local cloud cert)")
      end
    end


    if options[:ip_attribute]
      @ip_attribute = options[:ip_attribute]
    else
      @ip_attribute = ask("What compute attribute to use for the ip to connect (if empty defaults to private_ip)?")
      @ip_attribute = 'private_ip' if @ip_attribute.empty?
    end

    @queue = options[:queue] || ask("Queue?")

    @mgmt_url = options[:mgmt_url] || ask("URL to the UI?")

    @logstash_cert_location = options[:logstash_cert_location] || ask("Logstash cert file location ? (If empty defaults to local cloud cert)")

    if options[:logstash_hosts]
      @logstash_hosts = options[:logstash_hosts]
    else
      @logstash_hosts = ask("Comma seperated list of logstash host:port ? (if empty defaults to localhost:5000)")
      @logstash_hosts = 'localhost:5000' if  @logstash_hosts.empty?
    end

    @max_consumers = options[:max_consumers] || ask("Max Consumers?")
    @local_max_consumers = options[:local_max_consumers] || ask("Max Local Consumers (ones for iaas)?")

    # convert if they copied cloud location from ui
    dot_name = ""
    @queue.split("/").each do |v|
      dot_name += "." if !dot_name.empty?
      dot_name += "#{v}" if !v.empty?
    end
    # if real queue name used
    if dot_name.empty?
      @queue_name = @queue
      if !@queue.end_with?(".ind-wo")
        @queue_name += ".ind-wo"
      end
      dot_name = @queue.gsub(".ind-wo","")
    else
      @queue_name = dot_name +".ind-wo"
    end
    @authkey = options[:authkey] || ask("What is the authorization key?")
    @additional_java_args= options[:additional_java_args] || ask("Additional Java args (default empty)?")
    @env_vars = options[:env_vars] || ask("Environment Variables to pass to Executor (default empty)?")

    @amq_truststore_location = options[:amq_truststore_location] || ask("Location of TrustStore to connect AMQ (If empty no trustStore is used)?")

    @home = File.expand_path("clouds-available/#{dot_name}")
    @logstash_cert_location = "#{@home}/logstash-forwarder/cert/logstash-forwarder.crt" if  @logstash_cert_location.empty?
    @perf_collector_cert_location = "#{@home}/certs/perf_collector_cert.crt" if @perf_collector_cert_location.nil? || @perf_collector_cert_location.empty?

    # copy template files
    directory File.expand_path('templates/cloud', File.dirname(__FILE__)), @home
    empty_directory "#{@home}/sandbox"
    empty_directory "#{@home}/cache"
    empty_directory "#{@home}/backup"
    empty_directory "#{@home}/data"
    empty_directory "#{@home}/retry"

    # enable cloud
    inside("clouds-available") do
      if File.symlink?("../clouds-enabled/#{dot_name}")
        say_status('exist',"clouds-enabled/#{dot_name}")
      else
        File.symlink("../clouds-available/#{dot_name}","../clouds-enabled/#{dot_name}")
        say_status('enable',"clouds-enabled/#{dot_name}")
      end
    end

    say_status :success, "Next Step: inductor start ; inductor tail"

  end

  desc "enable PATTERN", "Enable inductor clouds matching the PATTERN"
  def enable(pattern)
    inside("clouds-available") do
      Dir.glob(pattern).each do |long_cloud|
        if File.symlink?("../clouds-enabled/#{long_cloud}")
          say_status('exist',"clouds-enabled/#{long_cloud}")
        else
          File.symlink("../clouds-available/#{long_cloud}","../clouds-enabled/#{long_cloud}")
          say_status('enable',"clouds-enabled/#{long_cloud}")
        end
      end
    end
  end

  desc "disable PATTERN", "Disable inductor clouds matching the PATTERN"
  def disable(pattern)
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        if File.symlink?(long_cloud)
          File.unlink(long_cloud)
          say_status('disable',long_cloud)
        end
      end
    end
  end

  desc "list [PATTERN]", "List clouds in the inductor"
  def list(pattern='*')
    inside("clouds-available") do
      Dir.glob(pattern).each do |long_cloud|
        status = File.symlink?("../clouds-enabled/#{long_cloud}") ? 'enabled' : 'disabled'
        say_status(status,long_cloud)
      end
    end
  end

  no_commands do

    def validate_user
      current_user = `whoami`.chomp
      if File.exists?("inductor/user")
        user=`cat inductor/user`.chomp
      else
        user=`cat user`.chomp
        if $?.to_i != 0
          puts "There is no inductor installed in this directory."
          exit 1
        end
      end
      if current_user != user
        puts "Inductor was created using user: #{user} - Please sudo to that user."
        exit 1
      end
    end

    def status_by_cloud(long_cloud)
      ec = 0
      long_path = File.expand_path(long_cloud)
      cmd = "pgrep -f \"#{long_path}.*inductor-\""
      puts "cmd: #{cmd}" if options[:verbose]
      status_result = system(cmd)
      status = "consumer ok"
      color = :green
      if !status_result
        ec = 1
        status = "consumer down"
        color = :red
      end
      say_status(status, long_cloud, color)
      return ec
    end

    def start_by_cloud(long_cloud)
        long_path = File.expand_path(long_cloud)
        cmd = "pgrep -f \"#{long_path}.*inductor-\""
        status_result = `#{cmd}`.split("\n")
        if status_result.size > 0
          say_status('start','already running',:red)
        else
          args = "-Dconf.dir=#{long_path}/conf "
          args += "-Dlog4j.configuration=file://#{long_path}/conf/log4j.xml "
          if File.exist?("#{long_path}/conf/vmargs")
            additional_args =`cat #{long_path}/conf/vmargs`.chomp
            args += additional_args
          end
          run("java #{args} -jar #{Inductor.jar} >/dev/null 2>&1 &", :verbose => options[:verbose])
          say_status('start',long_cloud + " consumer")
        end

        start_logstash_forwarder_by_cloud(long_cloud)
    end

    def stop_by_cloud(long_cloud)
      long_path = File.expand_path(long_cloud)
      run("ps -ef | grep inductor |grep java |grep #{File.expand_path(long_cloud)} |grep -v grep |awk '{print \"kill\", $2}' |sh", :verbose => options[:verbose])
      say_status('stop',long_cloud + " consumer")

      stopping=true
      cmd = "pgrep -f \"#{long_path}.*inductor-\""

      # give the initial stop a sec
      sleep 1
      stop_attempt = 0
      max_stop_attempts = 30 # 5min
      while (stopping && stop_attempt < max_stop_attempts) do
        status_result = `#{cmd}`.split("\n")
        if status_result.size >0
          say_status('still',"shutdown in progress, sleeping 10s - see #{long_path}/log/inductor.log for activeThreads")
          sleep 10
        else
          stopping = false
          say_status('finally',"#{long_cloud} down") if stop_attempt >0
        end
        stop_attempt +=1
      end

      if stopping
        say_status('still',"shutting down after 5min - force shutdown now")
        force_stop_by_cloud(long_cloud)
      end

      stop_logstash_forwarder_by_cloud(long_cloud)
    end


    def force_stop_by_cloud(long_cloud)
      long_path = File.expand_path(long_cloud)
      run("ps -ef | grep inductor | grep #{File.expand_path(long_cloud)} | grep -v grep | awk '{print \"kill -9\", $2}' |sh", :verbose => false)
      say_status('force stop',long_cloud + " consumer")
      stop_logstash_forwarder_by_cloud(long_cloud)
    end

    def restart_by_cloud(cloud)
      stop_by_cloud(cloud)
      start_by_cloud(cloud)
    end

    def start_logstash_forwarder_by_cloud(long_cloud)
      long_path = File.expand_path(long_cloud)
      conf_file = "#{long_path}/logstash-forwarder/conf/logstash-forwarder.conf"
      log_file  = "#{long_path}/logstash-forwarder/log/output.log"
      cmd = "pgrep -af logstash-forwarder|grep #{File.expand_path(long_cloud)}|grep -v grep|wc -l"
      status_result =`#{cmd}`
      if status_result.to_i > 0
        say_status("start","#{long_cloud} logstash_agent already running",:green)
      else
        cmd = "exec nohup #{Inductor.logstash_forwarder} -config=#{conf_file} -spool-size 20 >#{log_file} 2>&1 &"
        inside(long_path) do
          run("#{cmd}", :verbose => options[:verbose])
        end
        say_status("start", "#{long_cloud} logstash agent",:green)
      end
   end

   def stop_logstash_forwarder_by_cloud(long_cloud)
      long_path = File.expand_path(long_cloud)
      cmd = "ps -ef |grep logstash-forwarder |grep #{long_path} |grep -v grep |awk '{print \"kill -9\", $2}' |sh"
      run(cmd, :verbose => options[:verbose])
      say_status('stop',"logstash agent " +long_path)
   end

   def status_logstash_forwarder_by_cloud(long_cloud)
      cmd = "pgrep -lf logstash-forwarder|grep #{File.expand_path(long_cloud)}|grep -v grep|wc -l"
      status_result =`#{cmd}`
      if status_result.to_i > 0
        status = "logstash agent ok"
        color = :green
      else
        status = "logstash agent down"
        color = :red
      end
      say_status(status,"#{long_cloud} " + status_result.to_s, color)
   end

   def restart_logstash_forwarder_by_cloud(long_cloud)
      stop_logstash_forwarder_by_cloud(long_cloud)
      start_logstash_forwarder_by_cloud(long_cloud)
   end

  end


  desc "start NAME", "Inductor start"
  method_option :verbose, :aliases => "-v", :default => false
  def start (pattern='*')
    validate_user
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        start_by_cloud(long_cloud)
      end
    end
  end

  desc "start_logstash_forwarder NAME", "Inductor logstash agent start"
  def start_logstash_forwarder(pattern='*')
    validate_user
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        start_logstash_forwarder_by_cloud(long_cloud)
      end
    end
  end

  desc "stop NAME", "Inductor stop (will finish processing active threads)"
  method_option :verbose, :aliases => "-v", :default => false
  def stop (pattern='*')
    validate_user
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        stop_by_cloud(long_cloud)
      end
    end
  end

  desc "force_stop NAME", "Inductor force stop (will kill -9)"
  def force_stop (pattern='*')
    validate_user
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        force_stop_by_cloud(long_cloud)
      end
    end
  end

  desc "stop_logstash_forwarder NAME", "Inductor logstash agent stop"
  def stop_logstash_forwarder (pattern='*')
    validate_user
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        stop_logstash_forwarder_by_cloud(long_cloud)
      end
    end
  end

  desc "restart NAME", "Inductor restart"
  def restart (pattern='*')
    pattern = "*" if pattern.nil?
    invoke :stop, pattern
    invoke :start, pattern
  end

  desc "restart_logstash_forwarder NAME", "Inductor logstash agent restart"
  def restart_logstash_forwarder(pattern='*')
    pattern = "*" if pattern.nil?
    invoke :stop_logstash_forwarder, pattern
    invoke :start_logstash_forwarder, pattern
  end

  desc "tail", "Inductor log tail"
  def tail
    logs = ""
    inside("clouds-enabled") do
      Dir.glob("*").each do |long_cloud|
        logs += File.expand_path(long_cloud)+'/log/inductor.log' + ' '
      end
    end
    cmd = "tail -f #{logs}"
    puts "#{cmd}"
    system(cmd)
  end

  desc "status_logstash_forwarder NAME", "Inductor logstash agent status"
  def status_logstash_forwarder (pattern='*')
    inside("clouds-enabled") do
      Dir.glob(pattern).each do |long_cloud|
        status_logstash_forwarder_by_cloud(long_cloud)
      end
    end
  end

  desc "status", "Inductor status"
  method_option :verbose, :aliases => "-v", :default => false
  def status
    ec = 0
    inside("clouds-enabled") do
      Dir.glob("*").each do |long_cloud|
        ec = status_by_cloud(long_cloud)
      end
    end
    exit ec
  end

  desc "check", "Inductor check"
  method_option :verbose, :aliases => "-v", :default => nil
  def check

    validate_user
    ec = 0
    inside("clouds-enabled") do
      Dir.glob("*").each do |long_cloud|
        ec = status_by_cloud(long_cloud)
        if ec != 0
          restart_by_cloud(long_cloud)
        end
      end
    end
    exit ec
  end

  desc "check_agent", "Inductor check logstash forwarder"
  method_option :verbose, :aliases => "-v", :default => nil
  def check_agent

    validate_user
    ec = 0
    inside("clouds-enabled") do
      Dir.glob("*").each do |long_cloud|
        ec = status_logstash_forwarder_by_cloud(long_cloud)
        if ec != 0
          restart_logstash_forwarder_by_cloud(long_cloud)
        end
      end
    end
    exit ec
  end


  desc "install_initd", "Install /etc/init.d/inductor"
  def install_initd
    script = File.expand_path("../lib/templates/inductor/init.d/inductor",File.dirname(__FILE__))
    cmd = "cp -f #{script} /etc/init.d/inductor"
    system(cmd)
    say_status("running", cmd)
  end


  def self.source_root
    File.dirname(__FILE__)
  end

  def self.jar
    inductor_file=Dir.glob("#{source_root}/../target/inductor*.jar")[0].to_s
    File.expand_path(inductor_file)
  end

  @kernel_type = `uname -s`.downcase.chomp("\n").strip
  def self.logstash_forwarder
    File.expand_path("../bin/logstash-forwarder/#{@kernel_type}/logstash-forwarder", File.dirname(__FILE__))
  end

end

Inductor.start

