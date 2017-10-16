class Cms::AttrMd < ActiveResource::Base
  self.prefix = '/adapter/rest/md/'
  self.format = :json

  def to_hash
    self.attributes
  end
end
