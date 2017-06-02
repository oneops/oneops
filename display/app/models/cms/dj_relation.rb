class Cms::DjRelation < Cms::RfcRelation
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'relation'
  self.primary_key  = :ciRelationId

  def to_param
    ciRelationId.to_s
  end

  def find_or_create_resource_for(name)
    case name
    when :fromCi
      self.class.const_get(:Cms).const_get(:DjCi)
    when :toCi
      self.class.const_get(:Cms).const_get(:DjCi)
    else
      super
    end
  end
end
