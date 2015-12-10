class Cms::AttrMd < Cms::Base
  self.prefix = "#{Settings.cms_path_prefix}/cm/simple/"

  def to_hash
  	self.attributes
  end

  def options
    begin
      @options ||= HashWithIndifferentAccess.new(ActiveSupport::JSON.decode(valueFormat))
    rescue
      @options = HashWithIndifferentAccess.new
    end
  end
end
