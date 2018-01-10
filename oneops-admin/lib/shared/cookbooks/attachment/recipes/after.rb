run_on_event = 'after'
action_name,class_name,attribute_to_look =get_attachment_context
windows_platform = node['platform_family'] == 'windows'



if node.workorder.payLoad.has_key?('EscortedBy') &&
    class_name !~ /\.Compute/

  attachments = node.workorder.payLoad.EscortedBy

  after = Array.new
  attachments.each do |a|
    if attribute_to_look =='run_on'
      a[:ciAttributes][attribute_to_look].split(",").each do |r|
        after.push(a) if ["#{run_on_event}-#{action_name}"].index(r)
      end
    else
      run_on_actions = JSON.parse(a[:ciAttributes][attribute_to_look])
      run_on_actions.each do |r|
      after.push(a) if ["#{run_on_event}-#{action_name}"].index(r)
      end
    end
  end

  after.sort_by { |a| a[:ciAttributes][:priority] }.each do |a|

    Chef::Log.info("Loading after-#{action_name} attachment #{a[:ciName]}")

    _path = a[:ciAttributes][:path] or "/tmp/#{a[:ciName]}"
    _d = File.dirname(_path)

    directory "#{_d}" do
      owner "root" unless windows_platform
      group "root" unless windows_platform
      mode "0755"  unless windows_platform
      recursive true
      action :create
      not_if { File.directory?(_d) }
    end

    _source = a[:ciAttributes][:source]

    if _source.nil? || _source.empty?

      _content = a[:ciAttributes][:content]

      file "#{_path}" do
        content _content.gsub(/\r\n?/,"\n")
        owner "root" unless windows_platform
        group "root" unless windows_platform
        mode "0755"  unless windows_platform
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
        owner "root" unless windows_platform
        group "root" unless windows_platform
        mode 0644    unless windows_platform
        action :create
        only_if do _source =~ /s3:\/\// end
      end

    end

    if a[:ciAttributes].has_key?("exec_cmd")
      _exec_cmd = a[:ciAttributes][:exec_cmd].gsub(/\r\n?/,"\n")
      if windows_platform
        batch "execute after-#{action_name} #{a[:ciName]} attachment" do
          code <<-EOH
            #{_exec_cmd}
          EOH
          not_if { _exec_cmd.empty? }
        end
      else
        bash "execute after-#{action_name} #{a[:ciName]} attachment" do
          code <<-EOH
            #{_exec_cmd}
          EOH
          not_if { _exec_cmd.empty? }
        end
      end
    end
  end
end
