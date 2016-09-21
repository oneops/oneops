class Operations::Events < Cms::Base
  self.site         = Settings.events_site
  self.prefix       = '/sensor/rest/ops/'
  self.element_name = 'events'

  def self.for_instance(instance_id)
    begin
      string_id = instance_id.to_s
      JSON.parse(get(string_id))[string_id]
    rescue Exception => e
      Rails.logger.warn("Failed to retrieve ops events for instance #{instance_id}: #{e}")
      return nil
    end
  end
end
