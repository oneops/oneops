class Cms::Namespace < Cms::Base
  self.prefix      = "#{Settings.cms_path_prefix}/ns/"
  self.primary_key = :nsId

  def to_param
    nsId.to_s
  end

  def self.build(attributes = {})
    nsParams = ActiveSupport::HashWithIndifferentAccess.new({:nsPath => ''})
    self.new(nsParams.merge(attributes))
  end
end
