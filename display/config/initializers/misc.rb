Time::DATE_FORMATS[:short_us] = "%b %d, %Y %H:%M"

class Array
  # Quick convenient way to convert array of objects to hash keyed on value determined by passed-in block.
  #   x = %w(a bb ccc)
  #   x.to_hash(&:size)    #  {1=>"a", 2=>"bb", 3=>"ddd"}
  #   [1, 2, 3].to_hash    #  {1=>1, 2=>2, 3=>3}
  def to_map(&block)
    inject({}) do |h, e|
      block_given? ? key = yield(e) : key = e
      h[key] = e
      h
    end
  end

  # Quick convenient way to convert array of objects to hash of key/value pair determined by passed-in block.
  #   x = %w(a bb ccc)
  #   x.to_hash_with_value {|e| return e, e.size}    #  {"a" +> 1, "bb" => 2, "ddd" => 3}
  def to_map_with_value(&block)
    inject({}) do |h, e|
      key, value = yield(e)
      h[key] = value
      h
    end
  end

  def info
    @info ||= {}
  end

  def delete_blank
    delete_if { |e| (((e.instance_of?(Hash) || e.instance_of?(Array)) && e.delete_blank) || e).blank? }
  end
end

class Hash
  def delete_blank
    delete_if { |k, v| (((v.instance_of?(Hash) || v.instance_of?(Array)) && v.delete_blank) || v).blank? }
  end

  def copy_if(target, *keys)
    (keys.presence || self.keys).each do |key|
      value = self[key]
      target[key] = value if value.present? && (block_given? ? yield(value) : true)
    end
    target
  end

  def transform_values(&block)
    inject({}) {|h, (k, v)| h[k] = v.instance_of?(Hash) ? v.transform_values(&block) : yield(k, v); h}
  end
end

class Numeric
  def to_human(opts = {})
    val = real
    abs_val = real.abs
    precision = opts[:precision] || 0
    if abs_val >= 1000000000
      return "#{(val / 1000000000.0).round(precision)}B"
    elsif abs_val >= 1000000
      return "#{(val / 1000000.0).round(precision)}M"
    elsif abs_val >= 1000
      return "#{(val / 1000.0).round(precision)}K"
    else
      return val.round(precision)
    end
  end
end
