class Cms::AttrMap < ActiveResource::Base
  self.prefix = '/adapter/rest/cm/simple/'

  def to_hash
    self.attributes
  end
end
