class Cms::CiMd < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/md/"
  self.element_name = 'class'

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

  def self.look_up(ci_class_name = nil)
    key = ci_class_name.present? ? "Cms::CiMd:ci_class_name=#{ci_class_name}" : 'Cms::CiMd:all'
    md = nil
    begin
      #md = Rails.cache.read(key)
      md = md_cache[key]
    rescue Exception => e
      Rails.logger.warn "Reading ci metadata '#{ci_class_name}' from cache failed: #{e}"
      md = nil
    end
    unless md
      if ci_class_name.blank?
        md = all
      elsif ci_class_name.index('.')
        md = find(ci_class_name, :params => { :includeActions => true })
      else
        md = all(:params => {:package => ci_class_name})
      end
      #Rails.cache.write(key, md) if md
      md_cache[key] = md
    end
    return md
  end

  def self.look_up!(ci_class_name = nil)
    begin
      look_up(ci_class_name)
    rescue Exception => e
      return nil
    end
  end

  def md_attribute(name)
    @md_attributes ||= attributes[:mdAttributes].inject({}) {|m, a| m[a.attributeName] = a; m}
    @md_attributes[name]
  end
end
