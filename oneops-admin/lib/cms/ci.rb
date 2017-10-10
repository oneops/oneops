class Cms::Ci < ActiveResource::Base
  self.prefix       = '/adapter/rest/cm/simple/'
  self.element_name = 'ci'
  self.primary_key  = :ciId

  def self.build(attributes = {})
    attrs = self.from_ci_md(attributes[:ciClassName]).merge(attributes)
    self.new(attrs)
  end

  def relations(options = {})
    Cms::Relation.all(:params => {:ciId => self.id}.merge(options))
  end

  def find_or_create_resource_for(name)
    case name
      when :ciAttributes
        self.class.const_get(:Cms).const_get(:AttrMap)
      when :created
        self.class.const_get(:Time)
      else
        super
    end
  end

  def created_timestamp
    Time.at(self.created / 1000)
  end

  def updated_timestamp
    Time.at(self.created / 1000)
  end

  def meta
    self.class.get_ci_md(self.ciClassName)
  end

  def to_param
    ciId.to_s
  end

  private

  def self.get_ci_md(ci_class_name)
    Cms::CiMd.look_up(ci_class_name)
  end

  def self.from_ci_md(ci_class_name)
    attrs = ActiveSupport::HashWithIndifferentAccess.new
    props = ActiveSupport::HashWithIndifferentAccess.new({:ciName       => '',
                                                          :ciClassName  => ci_class_name,
                                                          :nsPath       => '/',
                                                          :comments     => '',
                                                          :ciAttributes => attrs})

    if ci_class_name
      self.get_ci_md(ci_class_name).attributes[:mdAttributes].each do |a|
        attrs[a.attributeName] = a.defaultValue
      end
    end
    return props
  end
end
