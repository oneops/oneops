class Swagger::Docs::Config
  def self.transform_path(path, api_version)
    "apidocs/#{path}"
  end
end

class Swagger::Docs::SwaggerDSL
  def param_org_name
    param :path, 'org_name', :string, :required, 'Organization name'
  end

  def param_parent_ci_id(name)
    param :path,
          "#{name}_id",
          :string,
          :required,
          "#{name.capitalize.to_s} CI id (if consists of digits only) or name (otherwise). "
  end

  def param_ci_id(name)
    param :path,
          'id',
          :string,
          :required,
          "#{name.capitalize.to_s} CI id (if consists of digits only) or name (otherwise). "
  end
end

Swagger::Docs::Config.register_apis(
  {
    '1.0' => {
      # the extension used for the API
      :api_extension_type => :json,
      # the output location where your .json files are written to
      :api_file_path      => 'public/apidocs',
      # the URL base path to your API
      # :base_path          => Rails.env.shared? || Rails.env.development? ? 'http://localhost:3000' : '/',
      :base_path          => "#{Settings.protocol}://#{Settings.host}:#{Settings.port}",
      # if you want to delete all .json files at each generation
      :clean_directory    => true,
      # add custom attributes to api-docs
      :attributes         => {
        :info => {
          'title'             => 'OneOps',
          'description'       => 'OneOps API docs',
          'termsOfServiceUrl' => '',
          'contact'           => '',
          'license'           => 'Apache 2.0',
          'licenseUrl'        => 'http://www.apache.org/licenses/LICENSE-2.0.html'
        }
      }
    }
  }
)
