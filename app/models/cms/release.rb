class Cms::Release < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'release'
  self.primary_key  = :releaseId

  def self.build(attributes = {})
    release_params = ActiveSupport::HashWithIndifferentAccess.new({:releaseName     => '',
                                                                   :parentReleaseId => '',
                                                                   :description     => '',
                                                                   :nsPath          => '',
                                                                   :releaseState    => '',
                                                                   :createdBy       => ''})
    self.new(release_params.merge(attributes))
  end

  def self.locate(qualifier, ns_path, params = {})
    if qualifier =~ /\D/
      # Must be a ciName, look it up by ciName and class name within namespace.
      find_params = {:nsPath => ns_path, :releaseName => qualifier}
      find_params.merge(params) if params.present?
      first(:params => find_params)
    else
      # All digits, must be a ciId, look it up by ID.
      find(qualifier, :params => params)
    end
  end

  def self.latest(options = {})
    Cms::Release.all( :params => {:latest => 'true'}.merge(options) ).first
  end

  def self.search(options)
    data = Search::Base.search('/cms/release', options)
    return nil unless data

    result = data.map { |r| new(r, true) }
    result.info.clear.merge!(data.info)
    return result
  end

  def self.search_latest_by_ns(ns_path, options = {})
    Search::Base.search_latest_by_ns('/cms/release', ns_path, options).map {|r| new(r, true)}
  end

  def to_param
    releaseId.to_s
  end

  def commit(params = {})
    self.get(:commit, params)
  end

  def discard
    self.releaseState = 'canceled'
    self.save
  end

  def rfc_cis
    @rfc_cis ||= Cms::RfcCi.all(:params => {:releaseId => self.releaseId, :attrProps => 'owner'})
  end

  def rfc_relations
    return @rfc_relations if @rfc_relations
    @rfc_relations = Cms::RfcRelation.all(:params => {:releaseId => self.releaseId, :attrProps => 'owner'})
  end
end
