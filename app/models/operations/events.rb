class Operations::Events < ActiveResource::Base
  self.site         = Settings.events_site
  self.prefix       = '/sensor/rest/ops/'
  self.element_name = 'events'

  def self.for_instance(instance_id)
    begin
      Operations::Events.find(instance_id).attributes
    rescue Exception => e
      Rails.logger.warn("Failed to retrieve ops events for instance #{instance_id}: #{e}")
      return nil
    end
  end

  def find_or_create_resource_for(name)
    self.class.const_get(:Operations).const_get(:EventMap)
  end

  # modify standard paths to not include format extension
  # the format is already defined in the header
  def self.new_element_path(prefix_options = {})
    drop_extension(super)
  end

  def self.element_path(id, prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  def self.collection_path(prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  private

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end
end
