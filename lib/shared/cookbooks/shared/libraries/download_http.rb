require 'chef/provider/remote_file'
require 'fileutils'

class Chef
  class Provider
    class SharedDownloadHttp < Chef::Provider::RemoteFile

       def action_create
        Chef::Log.debug("#{@new_resource} checking for changes")

        existing_file_checksum = ''
        existing_file_checksum = Chef::Digester.checksum_for_file(@new_resource.path) if ::File.exists?(@new_resource.path)
        puts "existing: #{existing_file_checksum}"
        # value of new checksum changes later on somehow
        desired_checksum = @new_resource.checksum || ''
        puts "desired: #{desired_checksum}"
        if desired_checksum == existing_file_checksum && !existing_file_checksum.empty?
          puts "existing file matches checksum."
          Chef::Log.info("#{@new_resource} checksum matches target checksum (#{@new_resource.checksum}) - not updating")
        else
          mirrors = @new_resource.source.split(",")
          mirrors.each do |mirror|
            begin
              url = URI.parse(mirror)
              url.user, url.password = @new_resource.basic_auth_user, @new_resource.basic_auth_password if @new_resource.basic_auth_user
              rest = Chef::REST.new(url.to_s,nil, nil, http_client_opts(url.to_s))
              raw_file = rest.streaming_request(url,@new_resource.headers,@new_resource.path)
              raw_checksum = Chef::Digester.checksum_for_file(raw_file.path) if !desired_checksum.empty?

              Chef::Log.info("Downloaded file checksum: #{raw_checksum}")

              if desired_checksum.empty? || desired_checksum == raw_checksum
                Chef::Log.info("Moving to: #{@new_resource.path} ")
                FileUtils.mv(raw_file.path, @new_resource.path)
                Chef::Log.info("Downloaded file: #{@new_resource.path}")
              else
                Chef::Log.error("Checksum of downloaded file: #{raw_checksum} expecting: #{@new_resource.checksum}")
                exit 1
              end
              @new_resource.updated_by_last_action(true)
              # successful download or checksum match
              break
            rescue Exception => e
              Chef::Log.error("got exception downloading: #{mirror} message:"+e.message)
              if e.message == "exit"
                exit 1
              end
            end
          end
        end
        enforce_ownership_and_permissions
        @new_resource.updated_by_last_action?
      end
    end
  end
end
