if node.workorder.payLoad.has_key?('EscortedBy') &&
   node.workorder.rfcCi.ciClassName !~ /\.Compute/
   
  attachments = node.workorder.payLoad.EscortedBy
  after = Array.new
  attachments.each do |a|
    a[:ciAttributes][:run_on].split(",").each do |r|
      after.push(a) if ["after-#{node.workorder.rfcCi.rfcAction}"].index(r)
    end
  end 

  after.sort_by { |a| a[:ciAttributes][:priority] }.each do |a|
    
    Chef::Log.info("Loading after-#{node.workorder.rfcCi.rfcAction} attachment #{a[:ciName]}") 
    
    _path = a[:ciAttributes][:path] or "/tmp/#{a[:ciName]}"
    _d = File.dirname(_path)
    
    directory "#{_d}" do
      owner "root"
      group "root"
      mode "0755"
      recursive true
      action :create
      not_if { File.directory?(_d) }
    end
    
    _source = a[:ciAttributes][:source]
    
    if _source.empty?
      
      _content = a[:ciAttributes][:content]
      
      file "#{_path}" do
        content _content.gsub(/\r\n?/,"\n")
        owner "root"
        group "root"
        mode "0755"
        action :create
      end
      
    else
      _user = a[:ciAttributes][:basic_auth_user]
      _password = a[:ciAttributes][:basic_auth_password]
      _headers = a[:ciAttributes][:headers]
      
      _headers = _headers.empty? ? Hash.new : JSON.parse(_headers)
      _checksum = a[:ciAttributes][:checksum] or nil
      
      shared_download_http "#{_source}" do
        path _path
        checksum _checksum
        headers(_headers) if _headers
        basic_auth_user _user.empty? ? nil : _user
        basic_auth_password _password.empty? ? nil : _password
        # action :nothing
        action :create
        not_if do _source =~ /s3:\/\// end
      end
      
      shared_s3_file "#{_source}" do
        source _source
        path _path
        access_key_id _user
        secret_access_key _password
        owner "root"
        group "root"
        mode 0644
        action :create  
        only_if do _source =~ /s3:\/\// end
      end
      
    end

    if a[:ciAttributes].has_key?("exec_cmd")
      _exec_cmd = a[:ciAttributes][:exec_cmd].gsub(/\r\n?/,"\n")
      ruby_block "executing bash -c '#{_exec_cmd}' command for after-#{node.workorder.rfcCi.rfcAction} #{a[:ciName]} attachment" do
        block do
          Chef::Resource::RubyBlock.send(:include, Chef::Mixin::ShellOut)
          shell_out!("bash -c '#{_exec_cmd}'", :live_stream => Chef::Log::logger)
        end
        not_if { _exec_cmd.empty? }
      end
    end
  end
end
