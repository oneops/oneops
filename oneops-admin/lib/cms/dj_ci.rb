class Cms::DjCi < Cms::RfcCi
  self.prefix = "/adapter/rest/dj/simple/"
  self.format = :json
  self.element_name = "ci"
  self.primary_key = :ciId

  def relations(options = {})
    Cms::DjRelation.all( :params => { :ciId => self.id }.merge(options) )
  end
  
  def to_param
    ciId.to_s
  end
  
end