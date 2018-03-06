class Organization < ActiveRecord::Base
  has_many :teams, :dependent => :destroy
  has_one :admin_team, -> {where(:name => Team::ADMINS)}, :class_name => Team
  has_many :users, -> {uniq}, :through => :teams
  has_many :group_users, -> {uniq}, :through => :groups, :source => :users
  has_many :groups, -> {uniq}, :through => :teams
  has_many :admin_users, :through => :admin_team, :source => :users
  has_many :admin_group_users, -> {uniq}, :through => :admin_team, :source => :group_users
  has_many :admin_groups, :through => :admin_team, :source => :groups
  has_many :ci_proxies, :dependent => :destroy

  validates_presence_of :name
  validates_uniqueness_of :name

  after_create :ensure_exists_in_cms

  def ci
    return @ci if @ci.present?
    cms_scope = Cms::Ci.headers['X-Cms-Scope']
    begin
      Cms::Ci.headers.delete('X-Cms-Scope')
      @ci = Cms::Ci.first(:params => {:nsPath => '/', :ciClassName => 'account.Organization', :ciName => name})
    ensure
      Cms::Ci.headers['X-Cms-Scope']  = cms_scope if cms_scope
    end
  end

  def ensure_exists_in_cms
    cms_scope = Cms::Ci.headers['X-Cms-Scope']
    begin
      Cms::Ci.headers.delete('X-Cms-Scope')

      attr = {:nsPath => '/', :ciClassName => 'account.Organization', :ciName => name}
      cms_org = Cms::Ci.first(:params => attr)
      unless cms_org
        user = User.where(:username => Cms::Ci.headers['X-Cms-User']).first
        cms_org = Cms::Ci.create(attr.merge(:ciAttributes => {:owner => user.email || user.username}))
      end
      update_attribute('cms_id', cms_org.ciId) unless cms_org.ciId == cms_id
    ensure
      Cms::Ci.headers['X-Cms-Scope']  = cms_scope if cms_scope
    end
  end
end
