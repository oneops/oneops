class Cms::Namespace < ActiveResource::Base
  self.prefix       = '/adapter/rest/ns/'
  self.element_name = 'namespace'
  self.primary_key  = :nsId

  def self.build(attributes = {})
    self.new(ActiveSupport::HashWithIndifferentAccess.new({:nsPath => ''}).merge(attributes))
  end

  def created_timestamp
    Time.at(self.created / 1000)
  end

  def to_param
    nsId.to_s
  end
end
