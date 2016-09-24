class Operations::Sensor < ActiveResource::Base
  self.site         = Settings.events_site
  self.prefix       = '/sensor/rest/ops'
  self.element_name = ''
  self.timeout      = 300

  def self.states(instances)
    return {} if instances.blank?
    begin
      response = Operations::Sensor.post('states', {}, (instances.first.respond_to?(:ciId) ? instances.map(&:ciId) : instances).to_json).body
      return JSON(response).inject({}) { |h, i| h.update(i['id'].to_i => i['state']) }
    rescue Exception => e
      Rails.logger.warn "Failed to retrieve instances operations states: #{e}"
      return {}
    end
  end

  def self.component_states(component_ids)
    return {} if component_ids.blank?
    begin
      #response = Operations::Sensor.post('components/states/count', {}, component_ids.to_json).body
      response = Operations::Sensor.post(Settings.sensor_health_path, {}, component_ids.to_json).body
      return JSON.parse(response)
    rescue Exception => e
      Rails.logger.warn "Failed to retrieve component operations states: #{e}"
      return {}
    end
  end

  def self.events(instance_id)
    begin
      get("events/#{instance_id}")[instance_id.to_s]
    rescue Exception => e
      Rails.logger.warn("Failed to retrieve ops events for instance #{instance_id}: #{e}")
      return nil
    end
  end

  def self.custom_method_collection_url(method_name, options = {})
    super.gsub(/.#{self.format.extension}/, '')
  end
end
