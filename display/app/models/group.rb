class Group < ActiveRecord::Base

  has_many :members, :class_name => 'GroupMember', :dependent => :destroy
  has_many :admins, -> {where({:admin => true})}, :class_name => 'GroupMember'
  has_and_belongs_to_many :users, :join_table => 'group_members'
  has_and_belongs_to_many :teams

  validates_presence_of   :name
  validates_uniqueness_of :name

  def is_admin?(user)
    global_admin_groups = Settings.global_admin_groups
    global_admin_group_names = global_admin_groups.is_a?(Array) ? global_admin_groups : global_admin_groups.split(',')
    (user.is_global_admin? && !global_admin_group_names.include?(name)) || admins.where(:user_id => user.id).first.present?
  end
end
