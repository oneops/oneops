class Cms::CiDetail < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/cm/"
  self.element_name = 'ci'
  self.primary_key  = :ciId

  def to_param
    ciId.to_s
  end
end
