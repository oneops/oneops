action :set do
  Chef::Log.debug("setting policy for #{new_resource.chain} to #{new_resource.policy}")
  node.set["simple_iptables"]["policy"][new_resource.table][new_resource.chain] = new_resource.policy
  new_resource.updated_by_last_action(true)
end
