class Cms::Var < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/cm/simple/"
  self.element_name = 'var'
  self.primary_key = 'name'
end
