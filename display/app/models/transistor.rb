class Transistor < ActiveResource::Base
  self.site                   = Settings.transistor_site
  self.prefix                 = '/transistor/rest'
  self.element_name           = ''
  self.include_format_in_path = false
  self.timeout                = Settings.transistor_http_timeout

  def self.export_design(assembly, platform_ids = nil)
    begin
      return get("assemblies/#{assembly.ciId}/export#{"?#{platform_ids.map {|i| "platformIds=#{i}"}.join('&')}" if platform_ids.present?}")
    rescue Exception => e
      return nil, handle_exception(e, "Failed to export design for assembly #{assembly.ciId} :")
    end
  end

  def self.import_design(assembly, design)
    begin
      return JSON.parse(post("assemblies/#{assembly.ciId}/import", {}, design.to_json).body)['result'] == 'success'
    rescue Exception => e
      return false, handle_exception(e, 'Failed to import design:')
    end
  end

  def self.export_environment(env, platform_ids = nil)
    begin
      return get("environments/#{env.ciId}/export#{"?#{platform_ids.map {|i| "platformIds=#{i}"}.join('&')}" if platform_ids.present?}")
    rescue Exception => e
      return nil, handle_exception(e, "Failed to export environment #{env.ciId} :")
    end
  end

  def self.import_environment(env, data)
    begin
      return JSON.parse(post("environments/#{env.ciId}/import", {}, data.to_json).body)['result'] == 'success'
    rescue Exception => e
      return false, handle_exception(e, 'Failed to import environment:')
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
      id = JSON.parse(post("assemblies/#{assembly_id}/clone", {}, ci.attributes.to_json).body)['resultCiId']
    rescue Exception => e
      message = handle_exception e, "Failed to clone assembly '#{assembly_id}'"
      return nil, message
    end
    return id
  end

  def self.design_platform_rfcs(platform_id, opts = {})
    begin
      rfcs = get("platforms/#{platform_id}/rfcs", opts)
      rfcs['cis'] = rfcs['cis'].map {|rfc| Cms::RfcCi.new(rfc, true)}
      rfcs['relations'] = rfcs['relations'].map {|rfc| Cms::RfcRelation.new(rfc, true)}
      return rfcs
    rescue Exception => e
      message = handle_exception(e, "Failed to fetch platform RFCs in design for '#{platform_id}'.")
      return nil, message
    end
  end

  def self.commit_design_platform_rfcs(platform_id, desc = '')
    begin
      return JSON.parse(put("platforms/#{platform_id}/rfcs/commit", {}, {:desc => desc}.to_json).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to commit platform RFCs in design for '#{platform_id}'.")
      return nil, message
    end
  end

  def self.discard_design_platform_rfcs(platform_id)
    begin
      return JSON.parse(put("platforms/#{platform_id}/rfcs/discard", {}).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to discard platform RFCs in design for '#{platform_id}'.")
      return nil, message
    end
  end

  def self.pack_refresh(platform_id)
    begin
      return JSON.parse(put("platforms/#{platform_id}/pack_refresh", {}).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to peform pack refresh for platform '#{platform_id}'.")
      return nil, message
    end
  end

  def self.pack_update(platform_id, version)
    begin
      return JSON.parse(put("platforms/#{platform_id}/packs/versions/#{version}", {}).body)['releaseId']
    rescue Exception => e
      message = handle_exception(e, "Failed to peform pack update for platform '#{platform_id}', version '#{version}'.")
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

  def self.deploy(environent_id, deployment, exclude)
    begin
      params = {:deployment => deployment, :exclude => exclude}
      exit_code = JSON.parse(post("environments/#{environent_id}/deploy", {}, params.to_json).body)['exit_code']
      return exit_code == 0
    rescue Exception => e
      message = handle_exception(e, "Failed to deploy environment '#{environent_id}' with #{deployment}.")
      return nil, message
    end
  end

  def self.preview_bom(environent_id, opts = {:commit => false})
    begin
      data = JSON.parse(post("environments/#{environent_id}/deployments/preview", {}, opts.to_json).body)
      release  = data['release']
      data['release'] = Cms::ReleaseBom.new(release) if release
      rfcs = data['rfcs']
      if rfcs
        cis = rfcs['cis']
        rfcs['cis'] = cis ? cis.map {|rfc| Cms::RfcCi.new(rfc, true)} : []
        relations = rfcs['relations']
        rfcs['relations'] = relations ? relations.map {|rfc| Cms::RfcRelation.new(rfc, true)} : []
      else
        data['rfcs'] = {'cis' => [], 'relations' => []}
      end

      return data, nil
    rescue Exception => e
      message = handle_exception(e, "Failed to preview bom for environment '#{environent_id}'")
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

  def self.create_component(platform_id, requires_rel)
    result = requires_rel
    if requires_rel.valid?
      begin
        data = JSON.parse(post("platforms/#{platform_id}/components", {}, requires_rel.to_json).body)
        result = Cms::DjRelation.new(data, true)
      rescue Exception => e
        error = handle_exception(e, "Failed to create component [#{requires_rel.inspect}] for platform '#{platform_id}'.")
        result.errors.add(:base, error)
        result.toCi.errors.add(:base, error)
      end
    else
      Rails.logger.info "Create component validation failed: #{requires_rel.errors.full_messages}; #{requires_rel.toCi.errors.full_messages}"
    end
    return result
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

  def self.toggle_platforms(platform_ids, enable)
    action = enable ? 'enable' : 'disable'
    payload = { platforms: platform_ids.join(",") }
    begin
      release_id = JSON.parse(put("platforms/#{action}", {}, payload.to_json ).body)['releaseId']
    rescue Exception => e
      message = handle_exception e, "Failed to #{enable} platforms #{payload[:platforms]}"
      return nil, message
    end
    return release_id
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
    ok = nil
    begin
      ok = JSON.parse(put("platforms/#{platform_id}/clouds", {}, cloud_rel.to_json).body)['result']
    rescue Exception => e
      message = "Failed to change cloud priority for #{cloud_rel.toCi.ciName} in platform: '#{platform_id}'"
      cloud_rel.errors.add(:base, message)
      handle_exception e, message
    end
    return ok
  end

  def self.restore_release(snapshot, release_id)
    begin
      response = JSON.parse(post('snapshot/import', {:release => release_id}, snapshot.to_json).body)
      return response['result'] == 'success', response['errors']
    rescue Exception => e
      return false, handle_exception(e, "Failed to restore release #{release_id} for '#{snapshot['namespace'] if snapshot}':")
    end
  end

  def self.environment_cost(env, pending = false, details = false)
    begin
      return get("environments/#{env.respond_to?(:ciId) ? env.ciId : env}/#{'estimated_' if pending}cost#{'_data' if details}"), nil
    rescue Exception => e
      return nil, handle_exception(e, "Failed to get cost for environment #{env.ciId} :")
    end
  end


  private

  def self.handle_exception(exception, message)
    body = nil
    error_message = exception.message
    if exception.respond_to?(:response) && exception.response.body
      begin
        body = JSON.parse(exception.response.body)
        error_message = body['message']
        Rails.logger.warn "#{message}: #{"[#{body['code']} - #{error_message}]"}"
      rescue Exception => e
        Rails.logger.warn "#{message}: #{error_message}"
      end
    else
      Rails.logger.warn "#{message}: #{error_message}"
    end
    return error_message
  end
end
