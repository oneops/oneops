class Cms::Rfc < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'rfc'

  def self.count(ns_path)
    JSON.parse(get(:count, {:nsPath => ns_path}))
  end
end
