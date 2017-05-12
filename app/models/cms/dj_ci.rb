class Cms::DjCi < Cms::RfcCi
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'ci'
  self.primary_key  = :ciId

  def relations(options = {})
    Cms::DjRelation.all(:params => {:ciId => ciId}.merge(options))
  end

  def self.build(attributes = {}, attr_props = nil)
    ci = super(attributes)
    attr_props = attributes.delete(:ciAttrProps) || attributes.delete('ciAttrProps') if attributes && !attr_props
    ci.ciAttrProps = Cms::AttrMap.new(attr_props || {})
    ci
  end

  def to_param
    ciId.to_s
  end

  def touch(data = {})
    load(JSON.parse(put(:touch, {}, data.to_json).body))
    self
  end

  def history
    get(:history).map {|e| Cms::RfcCi.new(e, true)}
  end

  def records(state = nil)
    get(:records, state && {:state => state}).map {|r| Cms::DeploymentRecord.new(r, true)}
  end
end
