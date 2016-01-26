class Cms::Ci < Cms::Base
  VALID_CI_NAME_REGEXP = '(?=[a-zA-Z])[a-zA-Z0-9\-]{1,32}'

  self.prefix      = "#{Settings.cms_path_prefix}/cm/simple/"
  self.primary_key = :ciId

  # 2015-07-08 For some weird reason have to use "before_validation" as opposed to usual "validate"
  # in order for validation to run only once.  Using "validate" results in all validation run twice (including
  # expensive calls to 'percolate').
  before_validation :validate_ci

  def self.build(attributes = {})
    attrs = self.from_ci_md(attributes[:ciClassName]).merge(attributes)
    self.new(attrs)
  end

  def self.locate(qualifier, ns_path, class_name = nil, params = {})
    if qualifier =~ /\D/
      # Must be a ciName, look it up by ciName and class name within namespace.
      find_params = {:nsPath => ns_path, :ciName => qualifier}
      find_params[:ciClassName] = class_name if class_name.present?
      find_params.merge!(params) if params.present?
      old_scope = headers['X-Cms-Scope']
      headers['X-Cms-Scope'] = ns_path
      result = all(:params => find_params)
      headers['X-Cms-Scope'] = old_scope
      result.size > 1 ? result : result.first
    else
      # All digits, must be a ciId, look it up by ID.
      find(qualifier, :params => params)
    end
  end

  def self.count(ns_path, recursive = false)
    self.get(:count, {:nsPath => ns_path, :recursive => recursive})
  end

  def self.list(ids)
    JSON.parse(post(:list, {}, ids).body)
  end

  def self.search(options)
    result = Search::Base.search('/cms-all/ci', options)
    return result unless result
    data = result.map do |r|
      r.delete_if do |key, value|
        value.is_a?(Hash) && key != 'ciAttributes' && key != 'ciAttrProps' && key != 'ciBaseAttributes'
      end
      new(r, true)
    end
    data.info.clear.merge!(result.info)
    data
  end

  def violates_policies!(active = false)
    percolate(ciId, active)
  end

  def self.violates_policies!(targets, active = false, count_only = false)
    mpercolate(targets.map(&:ciId), targets.first.nsPath.split('/')[0..1].join('/'), active, count_only)
  end

  def violates_policies(active = false)
    percolate(attributes, active)
  end

  def self.violates_policies(targets, active = false, count_only = false)
    first_target = targets.first
    return nil if first_target.blank?
    mpercolate(targets.map(&:attributes), first_target.nsPath.split('/')[0..1].join('/'), active, count_only)
  end

  def state(state, options = {})
    begin
      put(:states, {:newState => state}.merge(options))
    rescue Exception => e
      message = self.class.handle_exception(e, 'Failed to update state')
      errors.add(:base, message)
    end
  end

  def self.state(ids, state, options = {})
    begin
      put(:states, {:newState => state, :ids => ids.join(',')}.merge(options))
      return true
    rescue Exception => e
      message = handle_exception(e, 'Failed to update state')
      return false, message
    end
  end

  def relations(options = {})
    Cms::Relation.all(:params => {:ciId => self.id}.merge(options))
  end

  def find_or_create_resource_for(name)
    case name
      when :ciAttributes
        self.class.const_get(:Cms).const_get(:AttrMap)
      else
        super
    end
  end

  def meta
    Cms::CiMd.look_up(self.ciClassName)
  end

  def to_param
    ciId.to_s
  end

  def new_record?
    !persisted?
  end

  def persisted?
    id.to_i > 0
  end

  def commited?
    !(new_record? || (is_a?(Cms::DjCi) && rfcAction == 'add'))
  end

  def name_editable?
    new_record? || (is_a?(Cms::DjCi) && rfcAction == 'add' && ciClassName.start_with?('catalog'))
  end

  def as_json(options = nil)
    errors.blank? ? super : {:errors => errors.full_messages}
  end

  def attrOwner
    attrProps.owner
  end


  protected

  def self.from_ci_md(ci_class_name)
    ci_params               = ActiveSupport::HashWithIndifferentAccess.new
    ci_params[:ciName]      = ''
    ci_params[:ciClassName] = ci_class_name
    ci_params[:nsPath]      = '/'
    ci_params[:comments]    = ''

    ci_attrs = ActiveSupport::HashWithIndifferentAccess.new
    Cms::CiMd.look_up(ci_class_name).attributes[:mdAttributes].each { |a| ci_attrs[a.attributeName] = a.defaultValue || '' } if ci_class_name
    ci_params[:ciAttributes] = ci_attrs

    return ci_params
  end


  private

  def validate_ci
    if name_editable?
      errors.add(:base, "Invalid name '#{ciName}' (name should start with a letter and may be up to 32 characters long consisting of digits, letters and dashes only.") unless ciName =~ /^#{VALID_CI_NAME_REGEXP}$/
    end
    check_attribute_pattern
    check_policy_compliance if Settings.check_policy_compliance
  end

  def check_attribute_pattern
    meta.attributes[:mdAttributes].each do |a|
      if ciAttributes.attributes.has_key?(a.attributeName.to_sym)
        value = ciAttributes.send(a.attributeName)
        if value.present?
          pattern = a.options.is_a?(Hash) && a.options.has_key?(:pattern) && a.options[:pattern]
          if pattern
            pattern = /#{pattern}/
            errors.add(:base, "'#{a.description}' is invalid [#{pattern}].") unless value =~ pattern
          end
        elsif a.isMandatory
          errors.add(:base, "'#{a.description}' [#{a.attributeName}] must be present.")
        end
      end
    end
  end

  def check_policy_compliance
    policies = violates_policies(true)
    if policies.present?
      list = policies.map {|p| "#{p.ciName}#{" (#{p.ciAttributes.description})" if p.ciAttributes.description.present?}"}.join('; ')
      errors.add(:base, "Failed policy compliance for: #{list}.")
    end
  end

  def percolate(target, active = false)
    policy_ids = nil
    begin
      policy_ids = Search::Base.percolate('/cms-all/ci',
                                          target,
                                          'ci.nsPath.keyword'    => nsPath.split('/')[0..1].join('/'),
                                          'ci.ciAttributes.mode' => active ? 'active' : %w(active passive))
    rescue  Exception => e
    end

    return policy_ids if policy_ids.blank?

    Cms::Ci.all(:params => {:ids => policy_ids.join(',')})
  end

  def self.mpercolate(targets, ns_path, active, count_only)
    result = nil
    begin
      result = Search::Base.mpercolate('/cms-all/ci',
                                       targets,
                                       'ci.nsPath.keyword'    => ns_path,
                                       'ci.ciAttributes.mode' => active ? 'active' : %w(active passive))
    rescue Exception => e
    end

    return nil unless result && result.size == targets.size

    unless count_only
      policy_ids = result.inject([]) {|a, r| a += r}.uniq
      policies = Cms::Ci.all(:params => {:ids => policy_ids.join(',')}).to_map(&:ciId)
    end

    map = {}
    targets.each_with_index do |t, i|
      ci_id = t.is_a?(Fixnum) || t.is_a?(String) ? t : t['ciId']
      map[ci_id] = count_only ? result[i].size : result[i].map {|policy_id| policies[policy_id.to_i]}
    end
    return map
  end
end
