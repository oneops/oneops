class Chef
  module Artifact
    class ArtifactError < StandardError; end

    class DataBagNotFound < ArtifactError
      attr_reader :data_bag_key

      def initialize(data_bag_key)
        @data_bag_key = data_bag_key
      end

      def message
        "[artifact] Unable to locate the Artifact data bag '#{DATA_BAG}' or data bag item '#{data_bag_key}' for your environment."
      end
    end

    class EnvironmentNotFound < ArtifactError
      attr_reader :data_bag_key
      attr_reader :environment

      def initialize(data_bag_key, environment)
        @data_bag_key = data_bag_key
        @environment = environment
      end

      def message
        "[artifact] Unable to locate the Artifact data bag item '#{data_bag_key}' for your environment '#{environment}'."
      end
    end

    class NexusArtifactDownloadError < ArtifactError
      attr_reader :file_location
      attr_reader :err
      def initialize(file_location, err)
        @file_location = file_location
        @err = err
      end

      def message
        Chef::Log.error("Failed to download the artifact from remote URL: #{file_location}")
        if err.response.code == '403'
        "***FAULT:FATAL= Unable to download artifact from Nexus.Validate artifact permission"
        else
        "***FAULT:FATAL= Unable to download artifact from Nexus.Validate artifact identifier/Repo"
        end
      end
    end

    class DataBagEncryptionError < ArtifactError
      def message
        "[artifact] An error occured while decrypting the data bag item. Your secret key may be incorrect or you may be using Chef 11 to read a Chef 10 data bag."
      end
    end

    class ArtifactChecksumError < ArtifactError
      def message
        "[artifact] Downloaded file checksum does not match the provided checksum. Your download may be corrupted or your checksum may not be correct."
      end
    end

    class S3BucketNotFoundError < ArtifactError
      attr_reader :bucket_name

      def initialize(bucket_name)
        @bucket_name = bucket_name
      end

      def message
        "[artifact] Unable to locate the S3 bucket: #{bucket_name}"
      end
    end

    class S3ArtifactNotFoundError < ArtifactError
      attr_reader :bucket_name
      attr_reader :object_path

      def initialize(bucket_name, object_path)
        @bucket_name = bucket_name
        @object_path = object_path
      end

      def message
        "[artifact] Unable to locate the artifact on S3 at the path #{bucket_name}/#{object_path}"
      end
    end
  end
end
