class Swagger::Docs::Config
  def self.transform_path(path, api_version)
    "apidocs/#{path}"
  end
end

module Swagger::Docs::Methods::ClassMethods
  alias_method :swagger_api_base, :swagger_api

  def swagger_api(action, params = {}, &block)
    responses = params.delete(:responses)
    swagger_api_base(action, params, &block)
    swagger_api_base(action) do
      if responses.blank?
        response :ok
        response :unauthorized
        response :not_found
        response :unprocessable_entity
      else
        responses.each {|r| response r} if responses.present?
      end
    end
  end
end

class Swagger::Docs::SwaggerDSL
  def param_org_name
    param :path, 'org_name', :string, :required, 'Organization name'
  end

  def param_path_ci_id(name)
    param :path, 'id', :string, :required, "#{name.capitalize.to_s} id (ciId) or name (ciName)."
  end

  def param_path_parent_ids(*names)
    param_org_name
    names.each {|n| param :path, "#{n}_id", :string, :required, "#{n.capitalize.to_s} id (ciId) or name (ciName)."}
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
