source 'http://rubygems.org'

gem 'rails', '4.1.10'
gem 'activeresource', '4.0.0', :require => 'active_resource'

# Use SCSS for stylesheets
gem 'sass-rails', '~> 4.0.5'
# Use Uglifier as compressor for JavaScript assets
gem 'uglifier', '>= 1.3.0'
# Use CoffeeScript for .js.coffee assets and views
gem 'coffee-rails', '~> 4.0.1'

gem 'logstasher', '0.6.5'
gem 'ruby-graphviz', :require => 'graphviz'

gem 'json', '1.8.1'
gem 'pg', '0.17.0'
gem 'devise', '3.5.4'
gem 'devise_ldap_authenticatable', '0.6.1'
# gem 'omniauth'
# gem 'omniauth-twitter'
# gem 'omniauth-linkedin'
# gem 'omniauth-google-oauth2'
# gem 'omniauth-github'
gem 'settingslogic', '2.0.9'
gem 'silencer', '0.6.0'

gem 'jquery-rails', '~> 2.2.1'
gem 'jquery-ui-rails', '5.0.5'
gem 'prototype-rails'
gem 'd3-rails', '~> 3.5'
gem 'rickshaw-rails', '1.5.0'

gem 'bootstrap-sass', '~> 2.2.2'
gem 'font-awesome-rails', '4.5.0.1'

gem 'clipboard-rails', '1.5.8'

gem 'swagger-docs', '0.1.9'
gem 'swagger-ui_rails', '0.1.7'

# Turbolinks makes following links in your web application faster. Read more: https://github.com/rails/turbolinks
#gem 'turbolinks'

# This gem is a dependency for 'activeresource-persistent' but have to explicitly reference it to lock down the version.

gem 'net-http-persistent', '2.9.4'
gem 'activeresource-persistent', '0.2.0', :require => 'active_resource/persistent'

# environment specific gems
group :development do
  gem 'quiet_assets'
end

group :shared do
  gem 'quiet_assets'
  gem 'awesome_print', :platforms => :mingw
end

group :production, :enterprise do
  gem 'therubyracer', :platforms => :ruby
end
