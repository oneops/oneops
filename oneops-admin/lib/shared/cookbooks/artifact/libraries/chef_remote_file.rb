require 'chef/provider/file'
require 'chef/rest'
require 'uri'
require 'tempfile'
require 'net/https'


class Chef
  class Provider
    class RemoteFile < Chef::Provider::File

      include Chef::Mixin::EnforceOwnershipAndPermissions

      def action_create
        Chef::Log.debug("#{@new_resource} checking for changes")

        if current_resource_matches_target_checksum?
          Chef::Log.debug("#{@new_resource} checksum matches target checksum (#{@new_resource.checksum}) - not updating")
        else
          sources = @new_resource.source
          source = sources.shift

          begin
            rest = Chef::REST.new(source, nil, nil, http_client_opts(source))
            raw_file = rest.streaming_request(rest.create_url(source),{},@new_resource.name)
          rescue SocketError, Errno::ECONNREFUSED, Timeout::Error, Net::HTTPFatalError => e
            Chef::Log.debug("#{@new_resource} cannot be downloaded from #{source}")
            if source = sources.shift
              Chef::Log.debug("#{@new_resource} trying to download from another mirror")
              retry
            else
              raise e
            end
          end
          if matches_current_checksum?(raw_file)
            Chef::Log.debug "#{@new_resource} target and source checksums are the same - not updating"
          else
            description = []
            description << "copy file downloaded from #{@new_resource.source} into #{@new_resource.path}"
            description << diff_current(raw_file.path)
            converge_by(description) do
              backup_new_resource
              FileUtils.cp raw_file.path, @new_resource.path
              Chef::Log.info "#{@new_resource} updated"
              raw_file.close!
            end
            # whyrun mode cleanup - the temp file will never be used,
            # so close/unlink it here.
            if whyrun_mode?
              raw_file.close!
            end
          end
        end
        set_all_access_controls
        update_new_file_state
      end
    end
  end
end
