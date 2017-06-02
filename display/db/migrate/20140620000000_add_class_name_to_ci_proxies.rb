class AddClassNameToCiProxies < ActiveRecord::Migration
  def up
    add_column :ci_proxies, :ci_class_name, :string, :null => false, :limit => 100, :default => ''
    CiProxy.reset_column_information
    CiProxy.update_all(['ci_class_name = ?', 'account.Assembly'])
  end

  def down
    remove_column :ci_proxies, :ci_class_name
  end
end
