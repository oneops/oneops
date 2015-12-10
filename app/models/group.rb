class Group < ActiveRecord::Base

  has_many :members, :class_name => 'GroupMember', :dependent => :destroy
  has_many :admins, -> {where({:admin => true})}, :class_name => 'GroupMember'
  has_and_belongs_to_many :users, :join_table => 'group_members'
  has_and_belongs_to_many :teams

  validates_presence_of   :name
  validates_uniqueness_of :name

  def is_admin?(user)
    admins.where(:user_id => user.id).first.present?
  end
end
