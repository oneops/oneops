Chef::Log.info('This is a test recipe')
require 'json'

file '/tmp/test' do
  content 'test recipe has run successfully'
end
