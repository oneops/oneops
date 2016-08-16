class Transistor < ActiveResource::Base
  self.site         = Settings.cms_site
  self.prefix       = '/transistor/rest'
  self.timeout      = 600
  self.element_name = ''
  self.include_format_in_path = false

  def self.export_design(assembly)
    begin
      return get("assemblies/#{assembly.ciId}/export")
    rescue Exception => e
      return nil, handle_exception(e, 'Failed to export design:')
    end
  end

  def self.import_design(assembly, design)
    begin
      return JSON.parse(post("assemblies/#{assembly.ciId}/import", {}, design.to_json).body)['result'] == 'success'
    rescue Exception => e
      return false, handle_exception(e, 'Failed to import design:')
    end
  end

  def self.export_catalog(design_id)
    get("catalogs/#{design_id}/export")
  end

  def self.import_catalog(data)
    id = nil
    begin
      id = JSON.parse(post('catalogs/import', {}, data.to_json).body)['catalogCiId']
    rescue Exception => e
      handle_exception e, 'Failed to import catalog:'
    end
    return id
  end

  def self.create_assembly_from_catalog(design_id, assembly_ci)
    id = nil
    begin
      id = JSON.parse(post("catalogs/#{design_id}/assemblies", {}, assembly_ci.to_json).body)['resultCiId']
    rescue Exception => e
      handle_exception e, "Failed to create assembly [#{assembly_ci}] from catalog '#{design_id}'"
    end
    return  id
  end

  def self.clone_assembly(assembly_id, ci)
    id = nil
    begin
      id = JSON.parse(post("assemblies/#{assembly_id}/clone", {}, ci.to_json).body)['resultCiId']
    rescue Exception => e
      message = handle_exception e, "Failed to clone assembly '#{assembly_id}'"
      return nil, message
    end
    return id
  end

  def self.pack_refresh(platform_id)
    begin
      return JSON.parse(put("platforms/#{platform_id}/pack_refresh", {}).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to peform pack refresh for platform '#{platform_id}'")
      return nil, message
    end
  end

  def self.pull_design(environent_id, platform_availability = {})
    begin
      return JSON.parse(put("environments/#{environent_id}", {}, platform_availability.to_json).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to pull design for environment '#{environent_id}'")
      return nil, message
    end
  end

  def self.generate_bom(environent_id, description, commit, exclude = '')
    begin
      params = {:description => description}
      params[:exclude] = exclude if exclude.present?
      params[:commit]  = 'false' unless commit
      exit_code = JSON.parse(post("environments/#{environent_id}/deployments", {}, params.to_json).body)['exit_code']
      return exit_code == 0
    rescue Exception => e
      message = handle_exception(e, "Failed to commmit for environment '#{environent_id}'")
      return nil, message
    end
  end

  def self.discard_manifest(environent_id)
    begin
      return JSON.parse(put("environments/#{environent_id}/manifest/discard", {}, {}.to_json).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to discard manifest release for environment '#{environent_id}'")
      return nil, message
    end
  end

  def self.discard_bom(environent_id)
    begin
      return JSON.parse(put("environments/#{environent_id}/bom/discard", {}, {}.to_json).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to discard bom for environment '#{environent_id}'")
      return nil, message
    end
  end

  def self.create_platform(assembly_id, platform_ci)
    platform = platform_ci
    if platform_ci.valid?
      begin
        platform_id = JSON.parse(post("assemblies/#{assembly_id}/platforms", {}, platform_ci.to_json).body)['platformCiId']
        platform = Cms::DjCi.find(platform_id)
      rescue Exception => e
        error = handle_exception(e, "Failed to create platform [#{platform_ci.inspect}] for assembly '#{assembly_id}'")
        platform_ci.errors.add(:base, error)
      end
    end
    return platform
  end

  def self.clone_platform(platform_id, platform_ci)
    new_platform_id = nil
    begin
      new_platform_id = JSON.parse(post("platforms/#{platform_id}/clone", {}, platform_ci.to_json).body)['platformCiId']
    rescue Exception => e
      handle_exception e, "Failed to clone platform '#{platform_id}' to new platform [#{platform_ci}]"
    end
    return new_platform_id
  end

  def self.delete_platform(assembly_id, platform_ci)
    deleted_platform_id = nil
    begin
      deleted_platform_id = JSON.parse(delete("assemblies/#{assembly_id}/platforms/#{platform_ci.ciId}", {}).body)['platformCiId']
    rescue Exception => e
      error = handle_exception e, "Failed to delete platform [#{platform_ci.inspect}] for assembly '#{assembly_id}'"
      platform_ci.errors.add(:base, error)
    end
    return deleted_platform_id
  end

  def self.activate_platform(platform_id)
    id = nil
    begin
      id = JSON.parse(put("platforms/#{platform_id}/activate", {}).body)['releaseId']
    rescue Exception => e
      handle_exception e, "Failed to activate platform '#{platform_id}'"
    end
    return id
  end

  def self.toggle_platform(platform, enable)
    id = nil
    platform_id = platform.ciId
    action = enable ? 'enable' : 'disable'
    begin
      id = JSON.parse(put("platforms/#{platform_id}/#{action}", {}).body)['releaseId']
    rescue Exception => e
      platform.errors.add(:base, handle_exception(e, "Failed to #{action} platform '#{platform_id}'"))
    end
    return id
  end

  def self.set_environment_clouds(environment_id, cloud_rels)
    release_id = nil
    begin
      release_id = JSON.parse(put("environments/#{environment_id}/clouds", {}, cloud_rels.to_json).body)['releaseId']
    rescue Exception => e
      message = handle_exception e, "Failed to set clouds for environment: '#{environment_id}'"
      return nil, message
    end
    return release_id
  end

  def self.update_platform_cloud(platform_id, cloud_rel)
    result = nil
    begin
      result = JSON.parse(put("platforms/#{platform_id}/clouds", {}, cloud_rel.to_json).body)['result']
    rescue Exception => e
      handle_exception e, "Failed to update cloud for platform: '#{platform_id}'"
    end
    return result
  end

  def self.custom_method_collection_url(method_name, options = {})
    super.gsub(/.#{self.format.extension}/, '')
  end


  private

  def self.handle_exception(exception, message)
    body = nil
    error_message = ''
    if exception.respond_to?(:response) && exception.response.body
      begin
        body = JSON.parse(exception.response.body)
        error_message = body['message']
        Rails.logger.warn "#{message}: #{"[#{body['code']} - #{error_message}]"}"
      rescue Exception => e
        error_message = exception.message
        Rails.logger.warn "#{message}: #{exception.message}"
      end
    end
    return error_message
  end
end
