class Cms::AttrMap < Cms::Base
  self.prefix = "#{Settings.cms_path_prefix}/cm/simple/"

  def to_hash
  	self.attributes
  end
end
