class Cms::TargetMd < ActiveResource::Base
  self.prefix = '/adapter/rest/md/'

  def to_hash
    self.attributes
  end
end
