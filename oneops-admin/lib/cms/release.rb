class Cms::Release < ActiveResource::Base
  self.prefix = "/adapter/rest/dj/simple/"
  self.format = :json
  self.include_root_in_json = false
  self.primary_key = :releaseId
  def self.build(attributes = {})
    releaseParams = ActiveSupport::HashWithIndifferentAccess.new( {
      :releaseName => "",
      :description => "",
      :nsPath => "",
      :releaseState => "",
      :createdBy => ""
    })
    self.new(releaseParams.merge(attributes))
  end

  def commit(params = {})
    self.get(:commit, params)
  end

  def rfc_cis
    Cms::RfcCi.all( :params => { :releaseId => self.releaseId })
  end

  def rfc_relations
    Cms::RfcRelations.all( :params => { :releaseId => self.releaseId })
  end

  def to_param
    releaseId.to_s
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

  def custom_method_element_url(method_name, options = {})
    self.class.drop_extension(super)
  end

  private

  def self.drop_extension(path)
    path.gsub(/.#{self.format.extension}/, '')
  end

end
