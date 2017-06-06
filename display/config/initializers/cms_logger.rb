class ActiveResource::Connection
  alias_method :configure_http_base, :configure_http

  def configure_http(http)
    trace = ENV['CMS_TRACE']
    if trace.present? ? ['yes', 'on', 'true'].include?(trace.downcase) : (Rails.env.development? || Rails.env.shared?)
      if http.respond_to?(:set_debug_output)
        http.set_debug_output($stderr)
      elsif http.respond_to?(:debug_output=)
        http.debug_output = $stderr
      end
    end

    configure_http_base(http)
  end
end

module CmsLogger
  class LogSubscriber < ActiveSupport::LogSubscriber
    def request(event)
      self.class.runtime += event.duration
      name = '%s (%.1fms)' % ["HTTP #{event.payload[:method].to_s.capitalize}", event.duration]
      info "  #{color(name, YELLOW, true)}  #{color(event.payload[:request_uri], BOLD, true)}"
    end

    def self.runtime=(value)
      Thread.current["cms_runtime"] = value
    end

    def self.runtime
      Thread.current["cms_runtime"] ||= 0
    end

    def self.reset_runtime
      rt, self.runtime = runtime, 0
      rt
    end
  end

  module ControllerRuntime
    extend ActiveSupport::Concern

    protected

    def append_info_to_payload(payload)
      super
      payload[:cms_runtime] = CmsLogger::LogSubscriber.reset_runtime
    end

    module ClassMethods
      def log_process_action(payload)
        messages, cms_runtime = super, payload[:cms_runtime]
        messages << ("Http: %.1fms" % cms_runtime.to_f) if cms_runtime
        messages
      end
    end
  end

end

CmsLogger::LogSubscriber.attach_to :active_resource

ActiveSupport.on_load(:action_controller) do
  include CmsLogger::ControllerRuntime
end
