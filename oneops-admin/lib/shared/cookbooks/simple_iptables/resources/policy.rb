actions :set

attribute :chain, :name_attribute => true, :equal_to => ["INPUT", "FORWARD", "OUTPUT", "PREROUTING", "POSTROUTING"], :default => "INPUT"
attribute :table, :equal_to => ["filter", "nat"], :default => "filter"
attribute :policy, :equal_to => ["ACCEPT", "DROP"], :required => true


def initialize(*args)
  super
  @action = :set
end

