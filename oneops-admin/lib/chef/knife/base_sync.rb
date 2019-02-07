require 'cms'
require 'fog/core'
require 'fog/openstack'
require 'fog/local'
require 'kramdown'

ENV['LC_ALL'] = 'en_US.UTF-8'   # Needed for proper encoding during md -> html conversion.

class Chef::Knife::UI
  def debug?
    Chef::Log.debug?
  end

  def debug(message)
    Chef::Log.debug message if Chef::Log.debug?
  end
end

module BaseSync
  def self.included(base)
    base.option :msg,
                :short       => '-m MSG',
                :long        => '--msg MSG',
                :description => 'Append a message to the comments'

    base.option :cms_trace,
                :short       => '-t',
                :long        => '--trace',
                :description => 'Raw HTTP debug trace for CMS calls'

    base.option :skip_docs,
                :long        => '--skip-docs',
                :description => 'Do not sync documents and images'
  end


  protected

  @doc_store = nil

  def build(klass, options)
    begin
      object = klass.constantize.build(options)
    rescue Exception => e
      ui.info(object.to_yaml) unless ui.debug?
      ui.info(e)
    end
    object ? object : false
  end

  def save(object)
    ui.debug(object.to_yaml) if ui.debug?
    begin
      ok = object.save
      ui.warn(object.errors.full_messages.join('; ')) unless ok
    rescue Exception => e
      ui.info(object.to_yaml) unless ui.debug?
      ui.info(e.response.read_body)
    end
    ok ? object : false
  end

  def sync_docs?
    !config[:skip_docs] && get_doc_store
  end

  def get_doc_store
    return @doc_store unless @doc_store.nil?

    @doc_store = false
    object_store_provider = Chef::Config[:object_store_provider]
    if object_store_provider == 'OpenStack'
      conn = Fog::Storage.new({:provider           => object_store_provider,
                               :openstack_username => Chef::Config[:object_store_user],
                               :openstack_api_key  => Chef::Config[:object_store_pass],
                               :openstack_region   => Chef::Config[:object_region],
                               :openstack_auth_url => Chef::Config[:object_store_endpoint]})
      env_bucket = Chef::Config[:environment_name]
    elsif ENV['CIRCUIT_LOCAL_ASSET_STORE_ROOT'].present?
      conn = Fog::Storage.new({:provider   => 'Local',
                               :local_root => ENV['CIRCUIT_LOCAL_ASSET_STORE_ROOT']})
      env_bucket = '.'
    elsif object_store_provider.present?
      ui.warn "Unsupported object_store_provider: #{object_store_provider}, will NOT sync documents and images!"
      return @doc_store
    else
      ui.warn "object_store_provder is not configured, will NOT sync documents and images!\n"\
              "\tFor local store set CIRCUIT_LOCAL_ASSET_STORE_ROOT environment variable, i.e.\n"\
              "\t\texport CIRCUIT_LOCAL_ASSET_STORE_ROOT=/<path_to_display>/public/_circuit"
      return @doc_store
    end

    @doc_store = conn.directories.get(env_bucket) ||
                 conn.directories.create(:key => env_bucket)
    ui.debug "Object store dir:\n #{@doc_store.to_yaml}" if ui.debug?
    return @doc_store
  end

  def sync_doc_file(file, remote_file)
    content = File.read(file)
    if file.end_with?('.md')
      content = Kramdown::Document.new(content).to_html
      remote_file.gsub!(/.md$/, '.html')
    end

    obj = {:key => remote_file, :body => content}
    if remote_file.end_with?('.html')
      obj['content_type'] = 'text/html'
    elsif remote_file.end_with?('.png')
      obj['content_type'] = 'image/png'
    end

    @doc_store.files.create(obj)
    ui.info(" - #{remote_file}")
  end
end
