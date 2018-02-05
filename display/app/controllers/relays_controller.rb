class RelaysController < ApplicationController
  skip_before_filter :authenticate_user!, :authenticate_user_from_token, :check_reset_password, :check_eula,
                     :check_username, :check_organization, :set_active_resource_headers,
                     :only => [:notify]
  before_filter :clear_active_resource_headers, :only => [:notify]

  before_filter :weak_ci_relation_data_consistency, :only => [:notify]

  http_basic_authenticate_with :name     => Settings.notification_hook.user,
                               :password => Settings.notification_hook.password,
                               :only     => :notify

  def notify
    notification_ns_path  = params[:nsPath]
    if notification_ns_path.blank?
      render :nothing => true, :status => :not_acceptable
      return
    end

    root, org_name, assembly_name, env_name = notification_ns_path.split('/')
    if assembly_name && assembly_name[0] == '_'
      render :text => 'no assembly', :status => :ok
      return
    end

    assembly = Cms::Ci.locate(assembly_name, "/#{org_name}", 'account.Assembly')

    notification_type = params[:type]

    @notify_stats = {}

    # Process assembly and env watches.
    if env_name.present?
      env = Cms::Ci.locate(env_name, "#{assembly.nsPath}/#{assembly_name}", 'manifest.Environment')
      watch_recipients = User.joins(:watches).where('ci_proxies.ci_id = ? OR ci_proxies.ci_id = ?', assembly.ciId, env.ciId).pluck(:email)
    else
      watch_recipients = User.joins(:watches).where('ci_proxies.ci_id = ?', assembly.ciId).pluck(:email)
    end
    emails = watch_recipients
    @notify_stats[:watches] = watch_recipients.size

    # Process env relays.
    if env_name.present?
      relays = Cms::Relation.all(:params => {:nsPath       => "/#{org_name}/#{assembly_name}/#{env_name}",
                                             :relationName => 'manifest.Delivers',
                                             :includeToCi  => true}).map(&:toCi).select {|r| r.ciAttributes.enabled == 'true'}

      @notify_stats[:relays] = {:total => relays.size}
      if relays.present?
        # TODO For now we support "email" relays only, therefore assuming that every relay is email relay.
        email_relays     = relays
        relay_recipients = []
        filtered         = 0
        relayed          = 0
        email_relays.each do |r|
          if r.ciAttributes.enabled == 'true' &&
            r.ciAttributes.severity.split(',').include?(params[:severity]) &&
            r.ciAttributes.source.split(',').include?(notification_type)
            ns_paths = r.ciAttributes.ns_paths
            ns_paths = ns_paths.present? ? ActiveSupport::JSON.decode(ns_paths) : []
            if ns_paths.blank? || ns_paths.include?(notification_ns_path)
              regex = r.ciAttributes.text_regex
              regex = /#{regex}/ if regex.present?
              if regex.blank? || regex.match(params[:subject]) || regex.match(params[:text])
                filtered += 1
                if r.ciAttributes.attributes['correlation'] != 'true' || check_correlation_filter(params)
                  relay_recipients += r.ciAttributes.emails.split(',')
                  relayed       += 1
                end
              end
            end
          end
        end
        relay_recipients = relay_recipients.uniq
        emails += relay_recipients
        @notify_stats[:relays][:email] = {:total => email_relays.size, :filtered => filtered, :relayed => relayed, :recipients => relay_recipients.size}
      end
    end

    if emails.present?
      emails = emails.uniq
      NotificationMailer.notification(emails, params).deliver
      render :text => 'delivered', :status => :ok
    else
      render :text => 'no subscribers', :status => :ok
    end
    Rails.logger.info "Notify Stats: #{@notify_stats.inspect}"
  end


  protected

  def custom_log_info
    @notify_stats
  end


  private

  def check_correlation_filter(notification)
    payload = notification[:payload]
    return true if payload.blank?

    source = notification[:source]
    if source == 'ops'
      current_state = payload[:newState]
      old_state     = payload[:oldState]
      return false if old_state == current_state

      %w(unhealthy overutilized notify underutilized).each do |state|
        counter = payload[state].to_i
        if counter == 0 && old_state == state
          # Component state reset (severity downgrade).
          return true
        elsif counter == 1 && current_state == state
          # Component state trigger (severity upgrade).
          return true
        elsif counter > 0
          # Component state unchanged (no transition).
          return false
        end
      end
    elsif source == 'procedure'
      repeat_count = payload[:repeatCount]
      return repeat_count.blank?
    else
      return true
    end
  end
end
