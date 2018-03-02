require 'kitchen/provisioner/shell'
module Kitchen
  module Provisioner
    # Monkey-patching Shell class,
    # when run on Windows a shell file may not inherit execute permission
    class Shell < Base
      # Generates a command string which will perform any commands or
      # configuration required just before the main provisioner run command but
      # after the sandbox has been transferred to the instance. If no work is
      # required, then `nil` will be returned.
      #
      # @return [String] a command string
      def prepare_command
        file_name = File.join(
          config[:root_path],
          File.basename(config[:script])
        )
        code = "chmod +x #{file_name}"
        prefix_command(wrap_shell_code(sudo(code)))
      end
    end
  end
end
