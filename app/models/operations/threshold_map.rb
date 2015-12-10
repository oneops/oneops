class Operations::ThresholdMap < ActiveResource::Base
  self.site = Settings.events_site

  def to_hash
    self.attributes
  end

  def at
    Time.at(self.timestamp)
  end
end
