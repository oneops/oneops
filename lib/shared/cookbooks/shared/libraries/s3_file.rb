require 'aws/s3'

class Chef
  class Provider
    class SharedS3File < Chef::Provider::RemoteFile
      def action_create
        Chef::Log.debug("Checking #{@new_resource} for changes")

        if current_resource_matches_target_checksum?
          Chef::Log.debug("File #{@new_resource} checksum matches target checksum (#{@new_resource.checksum}), not updating")
        else
          Chef::Log.debug("File #{@current_resource} checksum didn't match target checksum (#{@new_resource.checksum}), updating")
          fetch_from_s3(@new_resource.source,@new_resource.path)
        end
        enforce_ownership_and_permissions

        @new_resource.updated
      end

      def fetch_from_s3(source,path)
        begin
          protocol, bucket, name = URI.split(source).compact
          name = name[1..-1]
          AWS::S3::Base.establish_connection!(
              :access_key_id     => @new_resource.access_key_id,
              :secret_access_key => @new_resource.secret_access_key
          )
          #obj = AWS::S3::S3Object.find name, bucket
          
          Chef::Log.debug("Downloading #{name} from S3 bucket #{bucket} ...")
          open(path, 'w') do |file|
              AWS::S3::S3Object.stream(name, bucket) do |chunk|
              file.write chunk
            end
          end          
          
        rescue URI::InvalidURIError
          Chef::Log.warn("Expected an S3 URL but found #{source}")
        end
      end
    end
  end
end

class Chef
  class Resource
    class S3File < Chef::Resource::RemoteFile
      def initialize(name, run_context=nil)
        super
        @resource_name = :s3_file
      end

      def provider
        Chef::Provider::S3File
      end

      def access_key_id(args=nil)
        set_or_return(
          :access_key_id,
          args,
          :kind_of => String
        )
      end
        
      def secret_access_key(args=nil)
        set_or_return(
          :secret_access_key,
          args,
          :kind_of => String
        )
      end
    end 
  end
end

