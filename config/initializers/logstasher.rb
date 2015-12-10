if defined?(LogStasher) && LogStasher.enabled
  LogStasher.add_custom_fields do |fields|
    # This block is run in application_controller context, so you have access to all controller methods
    fields[:user] = @current_user && @current_user.username  #  We have a hack to use "@current_user" directly (as opposed to 'current_user' method) to work around 'devise ldap' problem in cases for requests with no authentication.
    fields[:client_ip] = request.headers['NSC-Client-IP']
    custom_info = custom_log_info
    fields[:info] = custom_info if custom_info

    # If you are using custom instrumentation, just add it to logstasher custom fields
    #LogStasher.custom_fields << :myapi_runtime
  end
  LogStasher.watch('request.active_resource', :event_group => 'http') do |name, start, finish, id, payload, store|
    duration = ((finish.to_f - start.to_f) * 1000).round(1)
    store[:duration] ||= 0
    store[:duration] += duration
    store[:count] ||= 0
    store[:count] += 1
    store[:requests] ||= []
    url = payload[:request_uri]
    store[:requests] << {:duration => duration,
                         :method   => payload[:method],
                         :url      => url,
    :alias => url.sub(/\?.*$/, '').sub(/^http.*:\d\d\d\d+\//, '').gsub(/\/\d+/, '/<ID>')}
  end
end
