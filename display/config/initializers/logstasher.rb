if defined?(LogStasher) && LogStasher.enabled
  class LogStasher::RequestLogSubscriber
    alias :process_action_override :process_action
    def process_action(event)
      unless event.payload[:path] == '/status/ecv'
        process_action_override(event)
      end
    end
  end

  LogStasher.add_custom_fields do |fields|
    # This block is run in application_controller context, so you have access to all controller methods
    fields[:server_ip] = request.env['SERVER_ADDR']
    fields[:client_ip] = request.env['REMOTE_ADDR']
    fields[:user]      = @current_user && @current_user.username #  We have a hack to use "@current_user" directly (as opposed to 'current_user' method) to work around 'devise ldap' problem in cases for requests with no authentication.
    fields[:auth]      = @auth_token.present? ? 'token' : 'session'

    custom_info   = custom_log_info
    fields[:info] = custom_info if custom_info

    # If you are using custom instrumentation, just add it to logstasher custom fields
    #LogStasher.custom_fields << :myapi_runtime
  end

  LogStasher.watch('request.active_resource', :event_group => 'http') do |name, start, finish, id, payload, store|
    duration         = ((finish.to_f - start.to_f) * 1000).round(1)
    store[:duration] ||= 0
    store[:duration] += duration.round(1)

    store[:count] ||= 0
    store[:count] += 1

    url      = payload[:request_uri]
    category = if url.start_with?(Settings.cms_site)
                 'cms'
               elsif url.start_with?(Settings.transistor_site)
                 'transistor'
               elsif url.start_with?(Settings.search_site)
                 'search'
               elsif url.start_with?(Settings.events_site)
                 'sensor'
               elsif url.start_with?(Settings.metrics_site)
                 'metrics'
               elsif url.start_with?(Settings.log_site)
                 'logs'
               elsif url.start_with?(Settings.notifications_site)
                 'antenna'
               else
                 'other'
               end

    store[category]            ||= {:duration => 0, :requests => []}
    store[category][:duration] += duration.round(1)
    store[category][:requests] << {:duration => duration,
                                   :method   => payload[:method],
                                   :alias    => url.sub(/\?.*$/, '').sub(/^http.*:\d\d\d\d+\//, '').gsub(/\/\d+/, '/<ID>'),
                                   :url      => url}
  end

end
