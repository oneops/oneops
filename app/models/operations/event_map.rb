class Operations::EventMap < ActiveResource::Base
  self.site = Settings.events_site

  def find_or_create_resource_for(name)
    self.class.const_get(:Operations).const_get(:ThresholdMap)
  end

  def to_hash
    self.attributes
  end

  def at
    Time.at(self.timestamp)
  end

end
