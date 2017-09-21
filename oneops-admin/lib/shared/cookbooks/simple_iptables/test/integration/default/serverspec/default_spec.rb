require 'spec_helper'

describe iptables do
  it { should have_rule('-P INPUT ACCEPT') }
  it { should have_rule('-P OUTPUT ACCEPT') }
  it { should have_rule('-P FORWARD ACCEPT') }
end

describe iptables do
  it { should have_rule('-P INPUT ACCEPT').with_table('mangle').with_chain('INPUT') }
  it { should have_rule('-P OUTPUT ACCEPT').with_table('mangle').with_chain('OUTPUT') }
  it { should have_rule('-P FORWARD ACCEPT').with_table('mangle').with_chain('FORWARD') }
end
