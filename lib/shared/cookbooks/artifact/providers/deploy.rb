#
# Cookbook Name:: artifact
# Provider:: deploy
#
# Author:: Jamie Winsor (<jamie@vialstudios.com>)
# Author:: Kyle Allan (<kallan@riotgames.com>)
# 
# Copyright 2013, Riot Games
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
require 'digest'
require 'pathname'
require 'yaml'

attr_reader :release_path
attr_reader :current_path
attr_reader :shared_path
attr_reader :artifact_cache
attr_reader :artifact_cache_version_path
attr_reader :manifest_file
attr_reader :previous_version_paths
attr_reader :previous_version_numbers
attr_reader :artifact_location
attr_reader :artifact_version

def load_current_resource
  recipe_eval do
    Chef::Log.info("creating user #{new_resource.owner} and group #{new_resource.group}")

    user new_resource.owner do
    end

    group new_resource.group do
      members new_resource.owner
    end
  end

  if Chef::Artifact.latest?(@new_resource.version) && Chef::Artifact.from_http?(@new_resource.artifact_location)
    Chef::Application.fatal! "You cannot specify the latest version for an artifact when attempting to download an artifact using http(s)!"
  end

  if @new_resource.name =~ /\s/
    Chef::Log.warn "Whitespace detected in resource name. Failing Chef run."
    Chef::Application.fatal! "The name attribute for this resource is significant, and there cannot be whitespace. The preferred usage is to use the name of the artifact."
  end

  chef_gem "i18n" do
    version "0.6.9"
  end

  chef_gem "activesupport" do
    version "3.2.11"
  end

  if @new_resource.version =~ /\s/
    Chef::Log.warn "Whitespaces detected in resource version. Current Element value is:"+@new_resource.version
    @new_resource.version.gsub!(/\s/, "")
    Chef::Log.info "Removed whitepsaces in resource version. New Element value is:"+@new_resource.version
  end

  if @new_resource.artifact_location =~ /\s/
    Chef::Log.warn "Whitespaces detected in resource artifact_location. Current Element value is:"+@new_resource.artifact_location
    @new_resource.artifact_location.gsub!(/\s/, "")
    Chef::Log.info "Removed whitepsaces in resource artifact_location. New Element value is:"+@new_resource.artifact_location
  end

  if Chef::Artifact.from_nexus?(@new_resource.artifact_location)
    chef_gem "nexus_cli" do
      version "3.0.0"
    end

    group_id, artifact_id, extension, classifier = @new_resource.artifact_location.split(':')
    @artifact_version  = Chef::Artifact.get_actual_version(node, [group_id, artifact_id, @new_resource.version, extension].join(':'), @new_resource.ssl_verify)
    @artifact_location = [group_id, artifact_id, artifact_version, extension, classifier].join(':')
  elsif Chef::Artifact.from_s3?(@new_resource.artifact_location)
    # disabled since we are using Fog
    # unless Chef::Artifact.windows?
      # %W{gcc make libxml2 libxslt libxml2-devel libxslt-devel}.each do |nokogiri_requirement|
        # package nokogiri_requirement do
          # action :nothing
        # end.run_action(:install)
      # end
    # end
 
    # chef_gem "aws-sdk" do
      # version "1.11.0"
    # end

    @artifact_version = @new_resource.version
    @artifact_location = @new_resource.artifact_location
  else
    @artifact_version = @new_resource.version
    @artifact_location = @new_resource.artifact_location
  end

  @release_path                = get_release_path
  @current_path                = @new_resource.current_path
  @shared_path                 = @new_resource.shared_path
  @artifact_cache              = ::File.join(@new_resource.artifact_deploys_cache_path, @new_resource.name)
  @artifact_cache_version_path = ::File.join(artifact_cache, artifact_version)
  @previous_version_paths      = get_previous_version_paths
  @previous_version_numbers    = get_previous_version_numbers
  @manifest_file               = ::File.join(@release_path, "manifest.yaml")
  @deploy                      = false
  @skip_manifest_check         = @new_resource.skip_manifest_check
  @remove_on_force             = @new_resource.remove_on_force
  @current_resource            = Chef::Resource::ArtifactDeploy.new(@new_resource.name)

  @current_resource
end

action :deploy do
  delete_current_if_forcing!
  setup_deploy_directories!
  setup_shared_directories!

  @deploy = manifest_differences?

  retrieve_artifact!

  run_proc :before_deploy

  if deploy?
    run_proc :before_extract
    if new_resource.is_tarball && new_resource.should_expand
      extract_artifact!
    else
      copy_artifact
    end
    run_proc :after_extract

    run_proc :before_symlink
    symlink_it_up!
    run_proc :after_symlink
  end

  run_proc :configure

  if deploy? && new_resource.should_migrate
    run_proc :before_migrate
    run_proc :migrate
    run_proc :after_migrate
  end

  if deploy? || manifest_differences? || current_symlink_changing?
    run_proc :restart
  end

  recipe_eval do
    if Chef::Artifact.windows?
      # Needed until CHEF-3960 is fixed.
      symlink_changing = current_symlink_changing?
      execute "delete the symlink at #{new_resource.current_path}" do
        command "rmdir #{new_resource.current_path}"
        only_if {Chef::Artifact.symlink?(new_resource.current_path) && symlink_changing}
      end
    end
    
    link new_resource.current_path do
      to release_path
      owner new_resource.owner
      group new_resource.group
    end
  end

  run_proc :after_deploy

  recipe_eval { write_manifest } unless skip_manifest_check?
  delete_previous_versions!

  new_resource.updated_by_last_action(true)
end

action :pre_seed do
  setup_deploy_directories!
  retrieve_artifact!
end

# Extracts the artifact defined in the resource call. Handles
# a variety of 'tar' based files (tar.gz, tgz, tar, tar.bz2, tbz)
# and a few 'zip' based files (zip, war, jar).
# 
# @return [void]
def extract_artifact!
  recipe_eval do
    case ::File.extname(cached_tar_path)
    when /gz|tgz|tar|bz2|tbz/
      execute "extract_artifact!" do
        command "tar xf #{cached_tar_path} -C #{release_path}"
        user new_resource.owner
        group new_resource.group
        retries 2
      end
    when /zip|war|jar/
      if Chef::Artifact.windows?
        windows_zipfile release_path do
          source    cached_tar_path
          overwrite true
          retries 2
        end
      else
        #package "unzip"
        execute "extract_artifact!" do
          command "unzip -q -u -o #{cached_tar_path} -d #{release_path}"
          user    new_resource.owner
          group   new_resource.group
          retries 2
        end
      end
    else
      Chef::Application.fatal! "Cannot extract artifact because of its extension. Supported types are [tar.gz tgz tar tar.bz2 tbz zip war jar]."
    end

    # Working with artifacts that are packaged under an extra top level directory
    # can be cumbersome. Remove it if a top level directory exists and the user 
    # says to
    release_pathname = Pathname.new(release_path)
    ruby_block "remove top level" do
      block do
        top_level_dir = release_pathname.children.first.to_s
        ::FileUtils.mv(release_pathname.children.first.children, release_path)
        ::FileUtils.rm_rf(top_level_dir)
      end
      only_if do
        new_resource.remove_top_level_directory &&
          release_pathname.children.size == 1 &&
          release_pathname.children.first.directory?
      end
    end
  end
end

# Copies the artifact from its cached path to its release path. The cached path is
# the configured Chef::Config[:file_cache_path]/artifact_deploys
# 
# @example
#   cp /tmp/vagrant-chef-1/artifact_deploys/artifact_test/1.0.0/my-artifact /srv/artifact_test/releases/1.0.0
# 
# @return [void]
def copy_artifact
  recipe_eval do
    execute "copy artifact" do
      command Chef::Artifact.copy_command_for(cached_tar_path, release_path)
      user new_resource.owner
      group new_resource.group
    end
  end
end

# Returns the file path to the cached artifact the resource is installing.
# 
# @return [String] the path to the cached artifact
def cached_tar_path
  ::File.join(artifact_cache_version_path, artifact_filename)
end

# Returns the filename of the artifact being installed when the LWRP
# is called. Depending on how the resource is called in a recipe, the
# value returned by this method will change. If Chef::Artifact.from_nexus?, return the
# concatination of "artifact_id-version.extension" otherwise return the
# basename of where the artifact is located.
# 
# @example
#   When: new_resource.artifact_location => "com.artifact:my-artifact:1.0.0:tgz"
#     artifact_filename => "my-artifact-1.0.0.tgz"
#   When: new_resource.artifact_location => "http://some-site.com/my-artifact.jar"
#     artifact_filename => "my-artifact.jar"
# 
# @return [String] the artifacts filename
def artifact_filename
  if Chef::Artifact.from_nexus?(new_resource.artifact_location)
    group_id, artifact_id, version, extension = artifact_location.split(":")
    unless extension
      extension = "jar"
    end
    "#{artifact_id}-#{version}.#{extension}"
  else
    ::File.basename(artifact_location)
  end
end

# Deletes the current version if and only if it is the same
# as the one to be installed, we are forcing, and remove_on_force is
# set. Only bad people will use this.
def delete_current_if_forcing!
  return unless @new_resource.force 
  return unless remove_on_force? 
  return unless get_current_release_version == artifact_version || previous_version_numbers.include?(artifact_version)

  recipe_eval do
    log "artifact_deploy[delete_current_if_forcing!] #{artifact_version} deleted because remove_on_force is true" do
      level :info
    end 

    directory ::File.join(new_resource.deploy_to, 'releases', artifact_version) do
      recursive true
      action :delete
    end
  end
end

# Deletes released versions of the artifact when the number of 
# released versions exceeds the :keep value.
def delete_previous_versions!
  recipe_eval do
    versions_to_delete = []

    keep = new_resource.keep
    delete_first = total = get_previous_version_paths.length
    Chef::Log.info "# of previouse artifacts to keep = #{keep} Current Total = #{total}"

    if total == 0 || total <= keep
      true
    else
      delete_first -= keep
      Chef::Log.info "artifact_deploy[delete_previous_versions!] is deleting #{delete_first} of #{total} old versions (keeping: #{keep})"
      versions_to_delete = get_previous_version_paths.shift(delete_first)
    end

    versions_to_delete.each do |version|
      log "artifact_deploy[delete_previous_versions!] #{version.basename} deleted" do
        level :info
      end

      directory ::File.join(artifact_cache, version.basename) do
        recursive true
        action    :delete
      end

      directory ::File.join(new_resource.deploy_to, 'releases', version.basename) do
        recursive true
        action    :delete
      end
    end
  end
end

private

  # A wrapper that adds debug logging for running a recipe_eval on the 
  # numerous Proc attributes defined for this resource.
  # 
  # @param name [Symbol] the name of the proc to execute
  # 
  # @return [void]
  def run_proc(name)
    proc = new_resource.send(name)
    proc_name = name.to_s
    Chef::Log.info "artifact_deploy[run_proc::#{proc_name}] Determining whether to execute #{proc_name} proc."
    if proc
      Chef::Log.debug "artifact_deploy[run_proc::#{proc_name}] Beginning execution of #{proc_name} proc."
      recipe_eval(&proc)
      Chef::Log.debug "artifact_deploy[run_proc::#{proc_name}] Ending execution of #{proc_name} proc."
    else
      Chef::Log.info "artifact_deploy[run_proc::#{proc_name}] Skipping execution of #{proc_name} proc because it was not defined."
    end
  end

  # Checks the various cases of whether an artifact has or has not been installed. If the artifact
  # has been installed let #has_manifest_changed? determine the return value.
  # 
  # @return [Boolean]
  def manifest_differences?
    if new_resource.force
      Chef::Log.info "artifact_deploy[manifest_differences?] Force attribute has been set for #{new_resource.name}."
      Chef::Log.info "artifact_deploy[manifest_differences?] Installing version, #{artifact_version} for #{new_resource.name}."
      return true
    elsif get_current_release_version.nil?
      Chef::Log.info "artifact_deploy[manifest_differences?] No current version installed for #{new_resource.name}."
      Chef::Log.info "artifact_deploy[manifest_differences?] Installing version, #{artifact_version} for #{new_resource.name}."
      return true
    elsif artifact_version != get_current_release_version && !previous_version_numbers.include?(artifact_version)
      Chef::Log.info "artifact_deploy[manifest_differences?] Currently installed version of artifact is #{get_current_release_version}."
      Chef::Log.info "artifact_deploy[manifest_differences?] Version #{artifact_version} for #{new_resource.name} has not already been installed."
      Chef::Log.info "artifact_deploy[manifest_differences?] Installing version, #{artifact_version} for #{new_resource.name}."
      return true
    elsif artifact_version != get_current_release_version && previous_version_numbers.include?(artifact_version)
      Chef::Log.info "artifact_deploy[manifest_differences?] Version #{artifact_version} of artifact has already been installed."
      return has_manifest_changed?
    elsif artifact_version == get_current_release_version
      Chef::Log.info "artifact_deploy[manifest_differences?] Currently installed version of artifact is #{artifact_version}."
      return has_manifest_changed?
    end
  end

  # Loads the saved manifest.yaml file and generates a new, current manifest. The
  # saved manifest is then parsed through looking for files that may have been deleted,
  # added, or modified.
  # 
  # @return [Boolean]
  def has_manifest_changed?
    require 'active_support/core_ext/hash'

    if skip_manifest_check?
      Chef::Log.info "artifact_deploy[has_manifest_changed?] Skip Manifest Check attribute is true. Skipping manifest check."
      return false
    end

    Chef::Log.info "artifact_deploy[has_manifest_changed?] Loading manifest.yaml file from directory: #{release_path}"
    begin
      saved_manifest = YAML.load_file(::File.join(release_path, "manifest.yaml"))
    rescue Errno::ENOENT
      Chef::Log.warn "artifact_deploy[has_manifest_changed?] Cannot load manifest.yaml. It may have been deleted. Deploying."
      return true
    end

    current_manifest = generate_manifest(release_path)
    Chef::Log.info "artifact_deploy[has_manifest_changed?] Comparing saved manifest from #{release_path} with regenerated manifest from #{release_path}."

    differences = !saved_manifest.diff(current_manifest).empty?
    if differences
      Chef::Log.info "artifact_deploy[has_manifest_changed?] Saved manifest from #{release_path} differs from regenerated manifest. Deploying."
      return true
    else
      Chef::Log.info "artifact_deploy[has_manifest_changed?] Saved manifest from #{release_path} is the same as regenerated manifest. Not Deploying."
      return false
    end
  end

  # Checks the not-equality of the current_release_version against the version of
  # the currently configured resource. Returns true when the current symlink will
  # be changed to a different release of the artifact at the end of the resource
  # call.
  # 
  # @return [Boolean]
  def current_symlink_changing?
    get_current_release_version != ::File.basename(release_path)
  end

  # @return [Boolean] the deploy instance variable
  def deploy?
    @deploy
  end

  # @return [Boolean] the skip_manifest_check instance variable
  def skip_manifest_check?
    @skip_manifest_check
  end

  # @return [Boolean] the remove_on_force instance variable
  def remove_on_force?
    @remove_on_force
  end

  # @return [String] the current version the current symlink points to
  def get_current_release_version
    Chef::Artifact.get_current_deployed_version(new_resource.deploy_to)
  end

  # Returns a path to the artifact being installed by
  # the configured resource.
  # 
  # @example
  #   When: 
  #     new_resource.deploy_to = "/srv/artifact_test" and artifact_version = "1.0.0"
  #       get_release_path => "/srv/artifact_test/releases/1.0.0"
  # 
  # @return [String] the artifacts release path
  def get_release_path
    ::File.join(new_resource.deploy_to, "releases", artifact_version)
  end

  # Searches the releases directory and returns an Array of version folders. After
  # rejecting the current release version from the Array, the array is sorted by mtime
  # and returned.
  # 
  # @return [Array] the mtime sorted array of currently installed versions
  def get_previous_version_paths
    versions = Dir[::File.join(new_resource.deploy_to, "releases", '**')].collect do |v|
      Pathname.new(v)
    end

    versions.reject! { |v| v.basename.to_s == get_current_release_version }

    versions.sort_by(&:mtime)
  end

  # Convenience method for returning just the version numbers of 
  # the currently installed versions of the artifact.
  # 
  # @return [Array] the currently installed version numbers
  def get_previous_version_numbers
    previous_version_paths.collect { |version| version.basename.to_s}
  end

  # Creates directories and symlinks as defined by the symlinks
  # attribute of the resource.
  # 
  # @return [void]
  def symlink_it_up!
    recipe_eval do
      new_resource.symlinks.each do |key, value|
        Chef::Log.info "artifact_deploy[symlink_it_up!] Creating and linking #{new_resource.shared_path}/#{key} to #{release_path}/#{value}"
        directory "#{new_resource.shared_path}/#{key}" do
          owner new_resource.owner
          group new_resource.group
          mode '0755'
          recursive true
        end

        link "#{release_path}/#{value}" do
          to "#{new_resource.shared_path}/#{key}"
          owner new_resource.owner
          group new_resource.group
        end
      end
    end
  end

  # Creates directories that are necessary for installing
  # the artifact.
  # 
  # @return [void]
  def setup_deploy_directories!
    recipe_eval do
      [ artifact_cache_version_path, release_path, shared_path ].each do |path|
        Chef::Log.info "artifact_deploy[setup_deploy_directories!] Creating #{path}"
        directory path do
          owner new_resource.owner
          group new_resource.group
          mode '0755'
          recursive true
        end
      end
    end
  end

  # Creates directories that are defined in the shared_directories
  # attribute of the resource.
  # 
  # @return [void]
  def setup_shared_directories!
    recipe_eval do
      new_resource.shared_directories.each do |dir|
        Chef::Log.info "artifact_deploy[setup_shared_directories!] Creating #{shared_path}/#{dir}"
        directory "#{shared_path}/#{dir}" do
          owner new_resource.owner
          group new_resource.group
          mode '0755'
          recursive true
        end
      end
    end
  end

  # Retrieves the configured artifact based on the
  # artifact_location instance variable.
  # 
  # @return [void]
  def retrieve_artifact!
    recipe_eval do
      if Chef::Artifact.from_http?(new_resource.artifact_location)
        Chef::Log.info "artifact_deploy[retrieve_artifact!] Retrieving artifact from #{artifact_location}"
        retrieve_from_http
      elsif Chef::Artifact.from_nexus?(new_resource.artifact_location)
        Chef::Log.info "artifact_deploy[retrieve_artifact!] Retrieving artifact from Nexus using #{artifact_location}"
        retrieve_from_nexus
      elsif Chef::Artifact.from_s3?(new_resource.artifact_location)
        Chef::Log.info "artifact_deploy[retrieve_artifact!] Retrieving artifact from S3 using #{artifact_location}"
        retrieve_from_s3
      elsif ::File.exist?(new_resource.artifact_location)
        Chef::Log.info "artifact_deploy[retrieve_artifact!] Retrieving artifact local path #{artifact_location}"
        retrieve_from_local
      else
        Chef::Application.fatal! "artifact_deploy[retrieve_artifact!] Cannot retrieve artifact #{artifact_location}! Please make sure the artifact exists in the specified location."
      end
    end
  end

  # Defines a resource call for downloading the remote artifact.
  #
  # @return [void]
  def retrieve_from_http
    artifact_file cached_tar_path do
      location new_resource.artifact_location
      owner new_resource.owner
      group new_resource.group
      checksum new_resource.artifact_checksum if !new_resource.artifact_checksum.nil?
      action :create
    end
  end

  # Defines a artifact_file resource call to download an artifact from Nexus.
  # 
  # @return [void]
  def retrieve_from_nexus
    artifact_file cached_tar_path do
      location artifact_location
      owner new_resource.owner
      group new_resource.group
      action :create
    end
  end

  # Defines a artifact_file resource call to download an artifact from S3.
  #
  # @return [void]
  def retrieve_from_s3
    artifact_file cached_tar_path do
      location new_resource.artifact_location
      owner new_resource.owner
      group new_resource.group
      checksum new_resource.artifact_checksum
      action :create
    end
  end

  # Defines a resource call for a file already on the file system.
  # 
  # @return [void]
  def retrieve_from_local
    execute "copy artifact from #{new_resource.artifact_location} to #{cached_tar_path}" do
      command Chef::Artifact.copy_command_for(new_resource.artifact_location, cached_tar_path)
      user    new_resource.owner
      group   new_resource.group
      only_if { !::File.exists?(cached_tar_path) || !FileUtils.compare_file(new_resource.artifact_location, cached_tar_path) }
    end
  end

  # Generates a manifest for all the files underneath the given files_path. SHA1 digests will be
  # generated for all files under the given files_path with the exception of directories and the 
  # manifest.yaml file itself.
  # 
  # @param  files_path [String] a path to the files that a manfiest will be generated for
  # 
  # @return [Hash] a mapping of file_path => SHA1 of that file
  def generate_manifest(files_path)
    Chef::Log.info "artifact_deploy[generate_manifest] Generating manifest for files in #{files_path}"
    files_in_release_path = Dir[::File.join(files_path, "**/*")].reject { |file| ::File.directory?(file) || file =~ /manifest.yaml/ || Chef::Artifact.symlink?(file) }

    {}.tap do |map|
      files_in_release_path.each { |file| map[file] = Digest::SHA1.file(file).hexdigest }
    end
  end

  # Generates a manfiest Hash for the files under the release_path and
  # writes a YAML dump of the created Hash to manifest_file.
  # 
  # @return [String] a String of the YAML dumped to the manifest.yaml file
  def write_manifest
    manifest = generate_manifest(release_path)
    Chef::Log.info "artifact_deploy[write_manifest] Writing manifest.yaml file to #{manifest_file}"
    ::File.open(manifest_file, "w") { |file| file.puts YAML.dump(manifest) }
  end
