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
    return @govern_ci if @govern_ci

    ci_json                = JSON.parse(governCiJson)
    attrs                  = ci_json.delete('attributes')
    ci_json[:ciAttributes] = attrs.keys.to_map_with_value {|name| [name, attrs[name]['dfValue']]} if attrs
    @govern_ci             = Cms::Ci.new(ci_json)
  end

  def settle(token = nil)
    begin
      if token.present?
        self.class.headers['X-Approval-Token'] = token
      else
        self.class.headers.delete('X-Approval-Token')
      end
      return save
    rescue Exception => e
      errors.add(:base, self.class.handle_exception(e, "Failed to settle approvals: #{to_json}"))
      return false
    end
  end

  def self.settle(approvals, token = nil)
    begin
      if token.present?
        headers['X-Approval-Token'] = token
      else
        headers.delete('X-Approval-Token')
      end
      return JSON.parse(put('', {}, approvals.to_json).body).map {|a| new(a, true)}, nil
    rescue Exception => e
      return nil, handle_exception(e, "Failed to settle approvals: #{approvals.inspect}")
    end
  end

  def self.custom_method_collection_url(method_name, options = {})
    result = super
    result[0..-2] if result.end_with?('/')
  end
end
