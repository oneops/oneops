class Cms::Namespace < ActiveResource::Base
  self.prefix = "/adapter/rest/ns/"
  self.format = :json
  self.include_root_in_json = false
  self.primary_key = :nsId
  
  def self.build(attributes = {})
    nsParams = ActiveSupport::HashWithIndifferentAccess.new( {
      :nsPath => ""
    })
    self.new(nsParams.merge(attributes))
  end

  
  def created_timestamp
    Time.at(self.created / 1000)
  end

  def to_param
    nsId.to_s
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
