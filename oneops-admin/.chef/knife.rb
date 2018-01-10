log_level :info
log_location STDOUT
print_after true
admin true
node_name 'base'
register 'base'
version '1.0.0'
nspath '/public'
client_key 'client_key.pem'
validation_client_name 'chef-validator'
validation_key 'validation.pem'
chef_server_url 'http://localhost:4000'
cache_type 'BasicFile'
cache_options(:path => '.chef/checksums')
cookbook_path %w(base shared/cookbooks)
publish_path 'pkgs'
pack_path ['packs']
service_path ['services']
cloud_path ['clouds']
catalog_path ['catalogs']
default_impl 'oo::chef-11.4.0'
