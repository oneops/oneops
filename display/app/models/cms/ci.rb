class Cms::Ci < Cms::Base
  class NotFoundException < Exception
    attr_accessor :locateId, :nsPath, :ciType

    def initialize(id, ns_path = nil, ci_type = nil)
      super("Could not locate #{ci_type || 'CI'} with id/name '#{id}'#{" in namespace '#{ns_path}'" if ns_path.present?}.")
    end
  end

  self.prefix      = "#{Settings.cms_path_prefix}/cm/simple/"
  self.primary_key = :ciId

  # 2015-07-08 For some weird reason have to use "before_validation" as opposed to usual "validate"
  # in order for validation to run only once.  Using "validate" results in all validation run twice (including
  # expensive calls to 'percolate').
  before_validation :validate_ci

  after_save :save_original_attributes

  def self.valid_ci_name_regexp(ci_class_name = nil)
    "(?=[a-zA-Z])[a-zA-Z0-9\\-#{'_' unless strict_ci_name(ci_class_name)}]#{ci_class_name.end_with?('.Monitor') ? '{1,99}' : '{1,32}'}"
  end

  def self.valid_ci_name_message(ci_class_name = nil)
    "must start with a letter and may be up to 32 characters long consisting of digits, letters #{', underscores' unless strict_ci_name(ci_class_name)} and dashes only"
  end

  def self.strict_ci_name(ci_class_name)
    ci_class_name == 'account.Organization' ||
      ci_class_name == 'account.Assembly' ||
      ci_class_name == 'manifest.Environment' ||
      ci_class_name == 'catalog.Platform' ||
      ci_class_name == 'manifest.Platform'
  end

  def self.build(attributes = {})
    attrs = self.from_ci_md(attributes[:ciClassName]).deep_merge(attributes)
    self.new(attrs)
  end

  def self.locate(qualifier, ns_path, class_name = nil, params = {}, &block)
    ci = nil
    if qualifier =~ /\D/
      # Must be a ciName, look it up by ciName and class name within namespace.
      find_params = {:nsPath => ns_path, :ciName => qualifier}
      find_params[:ciClassName] = class_name if class_name.present?
      find_params.merge!(params) if params.present?
      old_scope = headers['X-Cms-Scope']
      headers['X-Cms-Scope'] = ns_path
      result = all(:params => find_params)
      headers['X-Cms-Scope'] = old_scope
      ci = result.size > 1 ? (block_given? && yield(result)) : result.first
    else
      # All digits, must be a ciId, look it up by ID.
      begin
        ci = find(qualifier, :params => params)
      rescue Exception => e
        if e.is_a?(ActiveResource::BadRequest)
          begin
            error_code = JSON.parse(e.response.body)['errorCode']
          rescue Exception => e2
            raise e
          end
          raise NotFoundException.new(qualifier, ns_path, class_name) if error_code == 1006 || error_code == 2007
        end
        raise e
      end
    end

    if ci && (class_name.blank? || (class_name.include?('.') ? ci.ciClassName == class_name : ci.ciClassName.end_with?(class_name)))
      return ci
    else
      raise NotFoundException.new(qualifier, ns_path, class_name)
    end
  end

  def self.count(ns_path, recursive = false, ci_class_name = nil)
    params = {:nsPath => ns_path, :recursive => recursive}
    params[:ciClassName] = ci_class_name if ci_class_name.present?
    self.get(:count, params)['count']
  end

  def self.count_and_group_by_ns(ns_path)
    self.get(:count, {:nsPath => ns_path, :recursive => true, :groupBy => 'nsPath'})
  end

  def self.list(ids)
    return [] if ids.blank?
    JSON.parse(post(:list, {}, ids).body)
  end

  def self.bulk_save(cis)
    return [] if cis.blank?
    JSON.parse(post(:bulk, {}, cis.to_json).body)
  end

  def self.search(options)
    pluck = options[:_source].present?

    result = Search::Base.search('/cms-all/ci', options)
    return result unless result

    data = result.map do |r|
      r.delete_if {|k, v| v.is_a?(Hash) && k != 'ciAttributes' && k != 'ciAttrProps' && k != 'ciBaseAttributes'} unless pluck
      new(r, true)
    end
    data.info.clear.merge!(result.info)
    data
  end

  def initialize(attributes = {}, persisted = false)
    super
    save_original_attributes
  end

  def save_original_attributes
    @original_attrs = attributes.include?('ciAttributes') ? ciAttributes.attributes.dup : {}
  end

  def original_value(attr_name)
    @original_attrs[attr_name.to_s]
  end

  def policy_locations
    @policy_locations ||= [nsPath.split('/')[0..1].join('/')]
  end

  def policy_locations=(locations)
    @policy_locations = locations
  end

  def add_policy_locations(*locations)
    @policy_locations = policy_locations + locations
  end

  def violates_policies!(active = false)
    percolate(ciId, active)
  end

  def violates_policies(active = false)
    percolate(attributes, active)
  end

  def self.violates_policies!(targets, active = false, count_only = false)
    mpercolate(targets.map(&:ciId), targets.first.policy_locations, active, count_only)
  end

  def self.violates_policies(targets, active = false, count_only = false)
    first_target = targets.first
    return nil if first_target.blank?
    mpercolate(targets.map(&:attributes), first_target.policy_locations, active, count_only)
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
    attributes[:attrProps] && attrProps.attributes['owner']
  end

  def attribute_options_for_select(attr_md)
    form_options = attr_md.options[:form]
    return [] if form_options.blank?
    field_type = form_options[:field]
    return [] unless field_type == 'select' || (field_type == 'checkbox' && form_options[:multiple] == 'true')

    options_for_select = form_options[:options_for_select]
    if options_for_select.is_a?(Hash)
      cms_var = options_for_select[:cms_var]
      options_for_select = options_for_select[:default] if options_for_select.include?(:default)

      if cms_var.present?
        begin
          var = Cms::Var.find(cms_var, :params => {:criteria => nsPath})
          options_for_select = JSON.parse(var.value) if var.present?
        rescue ActiveResource::ResourceNotFound
        rescue Exception => e
          Rails.logger.warn "Failed to parse value of '#{cms_var}' for attribute #{attr_md.attributeName} of ci.ciClassName=#{ciClassName} and ci.nsPath=#{nsPath}: #{e.message}"
        end
      end
    end

    attribute_value = original_value(attr_md.attributeName)
    if attribute_value.present?
      vals = field_type == 'select' ? [attribute_value] : attribute_value.split(',')
      vals.each {|val| options_for_select += [val] if val.present? && options_for_select.none? {|e| val == (e.is_a?(Array) ? e.last : e)}}
    end

    return options_for_select
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
      unless ciName =~ /^#{self.class.valid_ci_name_regexp(ciClassName)}$/
        message = self.class.valid_ci_name_message(ciClassName)
        errors.add(:base, "Invalid name '#{ciName}' (#{message}).")
      end
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
          pattern = "(#{a.options[:pattern]})|(.*\\$OO_(GLOBAL|LOCAL|CLOUD)\\{.*\\}.*)" if pattern.present?
          data_type = a.dataType
          if data_type == 'hash' || data_type == 'array'
            begin
              json = JSON.parse(value)
            rescue Exception => e
              json = nil
              errors.add(:base, "'#{a.description}' must be a valid JSON.")
            end
            if json && pattern.present?
              if data_type == 'hash'
                json.each_pair do |k, v|
                  errors.add(:base, "'#{a.description}' has invalid v for k '#{k}' [expected: #{pattern_desc(pattern)}].") unless check_pattern(pattern, v)
                end
              else
                json.each do |e|
                  errors.add(:base, "'#{a.description}' has invalid entry '#{e}' [expected: #{pattern_desc(pattern)}].") unless check_pattern(pattern, e)
                end
              end
            end
          else
            errors.add(:base, "'#{a.description}' is invalid [expected: #{pattern_desc(pattern)}].") if pattern.present? && !check_pattern(pattern, value)
            options_for_select = attribute_options_for_select(a)
            if options_for_select.present? && !a.options[:form][:allow_input]
              (a.options[:form][:field] == 'select' ? [value] : value.split(',')).each do |val|
                if options_for_select.none? {|e| val == (e.is_a?(Array) ? e.last : e)}
                  errors.add(:base, "'#{a.description}' is invalid [expected: #{options_for_select_desc(options_for_select)}].")
                end
              end
            end
          end
        elsif a.isMandatory
          errors.add(:base, "#{a.description} [#{a.attributeName}] must be present.")
        end
      end
    end
  end

  def check_policy_compliance
    policies = violates_policies(true)
    if policies.present?
      list = policies.map {|p| "#{p.ciName}#{" (#{p.ciAttributes.description})" if p.ciAttributes.description.present?}"}.join('; ')
      errors.add(:base, "Failed compliance for #{'policy'.pluralize(policies.size)}: #{list}.")
    end
  end

  def percolate(target, active = false)
    policy_ids = nil
    begin
      policy_ids = Search::Base.percolate('/cms-all/ci',
                                          target,
                                          'ci.nsPath.keyword'    => policy_locations,
                                          'ci.ciAttributes.mode' => active ? 'active' : %w(active passive))
    rescue Exception => e
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

  def check_pattern(pattern, value)
    pattern = "^#{pattern}" unless pattern.start_with?('^')
    pattern = "#{pattern}$" unless pattern.ends_with?('$')
    pattern.is_a?(Array) ? (pattern.any? {|e| value == (e.is_a?(Array) ? e.last : e)}) : value =~ /#{pattern}/
  end

  def pattern_desc(pattern)
    pattern.is_a?(Array) ? pattern.join('|') : pattern
  end

  def options_for_select_desc(options_for_select)
    options_for_select.map {|e| e.is_a?(Array) ? "'#{e.first}' (#{e.last})" : e}.join(' | ')
  end
end
