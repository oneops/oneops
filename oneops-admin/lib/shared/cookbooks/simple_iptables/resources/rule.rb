actions :append

attribute :chain, :name_attribute => true, :kind_of => String
attribute :table, :equal_to => ["filter", "nat"], :default => "filter"
attribute :rule, :kind_of => [String, Array], :required => true
attribute :jump, :kind_of => [String, FalseClass], :default => "ACCEPT"
attribute :direction, :equal_to => ["INPUT", "FORWARD", "OUTPUT", "PREROUTING", "POSTROUTING"], :default => "INPUT"


def initialize(*args)
  super
  @action = :append
end

