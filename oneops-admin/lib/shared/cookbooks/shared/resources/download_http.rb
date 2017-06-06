actions :create, :create_if_missing

attribute :source, :kind_of => String, :name_attribute => true
attribute :path, :kind_of => String
attribute :headers, :kind_of => Hash, :default => Hash.new
attribute :basic_auth_user, :kind_of => String
attribute :basic_auth_password, :kind_of => String
attribute :content, :kind_of => String
attribute :checksum, :kind_of => String, :default => nil
attribute :owner, :regex => Chef::Config[:user_valid_regex]
attribute :group, :regex => Chef::Config[:group_valid_regex]
attribute :mode, :callbacks => {
                                  "not in valid numeric range" => lambda { |m|
                                    if m.kind_of?(String)
                                      m =~ /^0/ || m="0#{m}"
                                    end
                                    Integer(m)<=07777 && Integer(m)>=0
                                  }
                                }