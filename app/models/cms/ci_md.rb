class Cms::CiMd < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/md/"
  self.element_name = 'class'

  cattr_accessor :md_cache
  self.md_cache = {}

  cattr_accessor :expiration_timeout
  self.expiration_timeout = Settings.md_cache_expiration_timeout
  self.expiration_timeout = (self.expiration_timeout && self.expiration_timeout > 0 ? self.expiration_timeout : 1).minutes

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
      #md_wrapper = Rails.cache.read(key)
      md_wrapper = md_cache[key]
      if md_wrapper
        if Time.now > md_wrapper[:expires_at]
          md_cache.delete(key)
        else
          md = md_wrapper[:md]
        end
      end
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
      md_wrapper = {:md => md, :expires_at => Time.now + expiration_timeout}
      #Rails.cache.write(key, md_wrapper) if md_wrapper
      md_cache[key] = md_wrapper
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
