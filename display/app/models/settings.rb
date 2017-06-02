class Settings < Settingslogic

  def initialize(hash_or_file = nil, section = nil)
    if hash_or_file
      super
    else
      file_contents = open(File.join(Rails.root, 'config', 'settings.yml')).read

      local_file = File.join(Rails.root, 'config', 'settings.local.yml')
      if File.exists?(local_file)
        file_contents += "\n\r"
        file_contents += open(local_file).read
      end

      hash = YAML.load(ERB.new(file_contents).result).to_hash
      self.replace hash[Rails.env]
      @section = 'settings.yml'
      create_accessors!
    end
  end
end
