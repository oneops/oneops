ENV['KITCHEN_SUITE'] = File.basename(__FILE__, '.*').gsub('_spec', '')
require File.expand_path('../exec_order_spec.rb', File.dirname(__FILE__))
