class Team < ActiveRecord::Base
  ADMINS = 'admins'

  DTO_DESIGN     = 0b100
  DTO_TRANSITION = 0b010
  DTO_OPERATIONS = 0b001
  DTO_ALL        = 0b111

  CLOUD_SERVICES   = 0b100
  CLOUD_COMPLIANCE = 0b010
  CLOUD_SUPPORT    = 0b001
  CLOUD_ALL        = 0b111

  belongs_to :organization
  has_and_belongs_to_many :users
  has_and_belongs_to_many :groups
  has_and_belongs_to_many :ci_proxies
  has_many :team_users

  validates_presence_of   :name
  validates_uniqueness_of :name, :scope => :organization_id

  validates_each :name do |r, attr, value|
    r.errors.add(:name, "of '#{ADMINS}' team can not be changed.") if r.changes[:name] && r.changes[:name].first == ADMINS
  end

  before_update {!(changes[:name] && changes[:name].first == ADMINS)}
end
