require 'chef/mixin/shell_out'
include Chef::Mixin::ShellOut

action :append do
  if new_resource.rule.kind_of?(String)
    rules = [new_resource.rule]
  else
    rules = new_resource.rule
  end

  test_rules(new_resource, rules)

  if not node["simple_iptables"]["chains"][new_resource.table].include?(new_resource.chain)
    node.set["simple_iptables"]["chains"][new_resource.table] = node["simple_iptables"]["chains"][new_resource.table].dup << new_resource.chain
    node.set["simple_iptables"]["rules"][new_resource.table] = node["simple_iptables"]["rules"][new_resource.table].dup << "-A #{new_resource.direction} --jump #{new_resource.chain}"
  end

  # Then apply the rules to the node
  rules.each do |rule|
    new_rule = rule_string(new_resource, rule, false)
    if not node["simple_iptables"]["rules"][new_resource.table].include?(new_rule)
      node.set["simple_iptables"]["rules"][new_resource.table] = node["simple_iptables"]["rules"][new_resource.table].dup << new_rule
      new_resource.updated_by_last_action(true)
      Chef::Log.debug("added rule '#{new_rule}'")
    else
      Chef::Log.debug("ignoring duplicate simple_iptables_rule '#{new_rule}'")
    end
  end
end

def test_rules(new_resource, rules)
  shell_out!("iptables --table #{new_resource.table} --new-chain _chef_lwrp_test")
  begin
    rules.each do |rule|
      new_rule = rule_string(new_resource, rule, true)
      new_rule.gsub!("-A #{new_resource.chain}", "-A _chef_lwrp_test")
      shell_out!("iptables #{new_rule}")
    end
  ensure
    shell_out("iptables --table #{new_resource.table} --flush _chef_lwrp_test")
    shell_out("iptables --table #{new_resource.table} --delete-chain _chef_lwrp_test")
  end
end

def rule_string(new_resource, rule, include_table)
  jump = new_resource.jump ? " --jump #{new_resource.jump}" : ""
  table = include_table ? "--table #{new_resource.table} " : ""
  rule = "#{table}-A #{new_resource.chain} #{rule}#{jump}"
  rule
end
