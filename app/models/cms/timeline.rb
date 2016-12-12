class Cms::Timeline < Cms::Base
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple"
  self.element_name = ''
  self.include_format_in_path = false

  def self.fetch(ns_path, limit, opts = {})
    begin
      offset = opts.delete(:offset)
      type = opts[:type]
      if offset.present?
        if type == 'release'
          opts[:releaseOffset] = offset
        elsif type == 'deployment'
          opts[:dpmtOffset] = offset
        else
          opts[:releaseOffset], opts[:dpmtOffset] = offset.split('-')
        end
      end

      last_release_id    = opts[:releaseOffset]
      last_deployment_id = opts[:dpmtOffset]

      timeline = get('timeline', opts.merge(:nsPath => ns_path, :limit => limit)).map do |r|
        if r.include?('deploymentId')
          result = Cms::Deployment.new(r, true)
          last_deployment_id = result.deploymentId
        else
          result = Cms::Release.new(r, true)
          last_release_id = result.releaseId
        end
        result
      end

      info = timeline.info
      info[:total]       = -1
      info[:offset]      = opts[:dpmtOffset].presence || opts[:releaseOffset]
      info[:size]        = limit
      info[:next_offset] = if type == 'release'
                             last_release_id
                           elsif type == 'deployment'
                             last_deployment_id
                           else
                             "#{last_release_id}-#{last_deployment_id}"
                           end

      return timeline, nil
    rescue Exception => e
      message = handle_exception(e, "Failed to fetch timeline for nsPath=#{ns_path}")
      return nil, message
    end
  end
end
