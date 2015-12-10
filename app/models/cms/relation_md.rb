class Cms::RelationMd < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/md/"
  self.element_name = 'relation'

  cattr_accessor :md_cache
  self.md_cache = {}

  def find_or_create_resource_for_collection(name)
    case name
    when :mdAttributes
      self.class.const_get(:Cms).const_get(:AttrMd)
    else
      super
    end
  end

  def self.look_up(relation_name = nil)
    key = relation_name.present? ? "Cms::RelationMd:relation_name=#{relation_name}" : 'Cms::RelationMd:all'
    md = nil
    begin
      #md = Rails.cache.read(key)
      md = md_cache[key]
    rescue Exception => e
      Rails.logger.warn "Reading relation metadata '#{relation_name}' from cache failed: #{e}"
      md = nil
    end
    unless md
      md = relation_name.present? ? find(relation_name, :params => { :includeActions => true }) : all
      #Rails.cache.write(key, md) if md
      md_cache[key] = md
    end
    return md
  end

  def md_attribute(name)
    @md_attributes ||= attributes[:mdAttributes].inject({}) {|m, a| m[a.attributeName] = a; m}
    @md_attributes[name]
  end
end
