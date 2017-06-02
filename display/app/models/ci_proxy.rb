class CiProxy < ActiveRecord::Base
  belongs_to :organization
  has_and_belongs_to_many :teams

  has_and_belongs_to_many :watched_by_users, :class_name => 'User', :join_table => 'user_watches'
end
