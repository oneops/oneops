class Cms::TargetMd < ActiveResource::Base
  self.prefix = "/adapter/rest/md/"
  self.format = :json
  self.include_root_in_json = false

  def to_hash
    self.attributes
  end
    
  # modify standard paths to not include format extension
  # the format is already defined in the header
  def self.new_element_path(prefix_options = {})
    drop_extension(super)
  end

  def self.element_path(id, prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  def self.collection_path(prefix_options = {}, query_options = nil)
    drop_extension(super)
  end

  private

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end
end
