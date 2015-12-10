class Cms::DeploymentApproval < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'approval'
  self.primary_key  = :approvalId

  DEFAULT_EXPIRATION = 24 * 60

  def to_param
    approvalId.to_s
  end

  def expiresAt
    approvedAt && (approvedAt + expiresIn * 60 * 1000)
  end

  def approved_timestamp
    Time.at(self.approvedAt / 1000)
  end

  def expires_timestamp
    Time.at(expiresAt / 1000)
  end

  def govern_ci
    return nil if governCiJson.blank?
    @govern_ci ||= Cms::Ci.new(JSON.parse(governCiJson))
  end

  def self.settle(approvals)
    begin
      return JSON.parse(put('', {}, approvals.to_json).body).map {|a| new(a, true)}
    rescue Exception => e
      message = handle_exception(e, "Failed to settle approvals: #{approvals.inspect}")
      return nil, message
    end
  end

  def self.custom_method_collection_url(method_name, options = {})
    result = super
    result[0..-2] if result.end_with?('/')
  end
end
