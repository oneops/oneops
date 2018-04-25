class User < ActiveRecord::Base
  has_many :authentications, :dependent => :destroy
  belongs_to :organization
  has_many :team_users, :dependent => :destroy
  has_many :teams, through: :team_users
  has_many :teams_via_groups, -> {uniq}, :class_name => 'Team', :through => :groups, :source => :teams
  # has_and_belongs_to_many :current_org_teams, -> {where(['teams.organization_id == users.organization_id'])}, :class_name => 'Team'
  has_and_belongs_to_many :watches,   :class_name => 'CiProxy', :join_table => 'user_watches'
  has_and_belongs_to_many :favorites, :class_name => 'CiProxy', :join_table => 'user_favorites'
  has_and_belongs_to_many :groups, :join_table => 'group_members'
  has_many :group_members, :dependent => :destroy

  before_destroy :check_organizations

  before_save :ensure_authentication_token

  if Settings.authentication == 'ldap'
    before_save :get_ldap_info
    devise :ldap_authenticatable,
           :registerable,
           :trackable,
           :timeoutable
  else
    devise :database_authenticatable,
           :confirmable,
           :lockable,
           :recoverable,
           :registerable,
           :rememberable,
           :trackable,
           :validatable,
           :timeoutable
  end

  attr_accessor :last_sign_in_at_for_current_org

  BLACKLIST_FOR_SERIALIZATION.concat([:authentication_token, :session_token, :eula_accepted_at, :show_wizard, :organization_id])
  BLACKLIST_FOR_SERIALIZATION.delete(:current_sign_in_at)
  BLACKLIST_FOR_SERIALIZATION.delete(:sign_in_count)

  validates_presence_of   :username
  validates_uniqueness_of :username, :case_sensitive => false

  def organizations
    @organizations ||= Organization.where('organizations.id IN (?)', (teams.pluck('teams.organization_id') + teams_via_groups.pluck('teams.organization_id')).uniq)
  end

  def all_teams(org_id = organization_id)
    @all_teams ||= (teams.where(:organization_id => org_id) + teams_via_groups.where(:organization_id => org_id)).uniq(&:id)
  end

  def all_team_ids(org_id = organization_id)
    @all_team_ids ||= (teams.where(:organization_id => org_id).pluck('teams.id') +
                       teams_via_groups.where(:organization_id => org_id).pluck('teams.id')).uniq
  end

  def get_ldap_info
    if self.email.nil? || !self.email.include?('@')
      self.email = self.ldap_get_param(self.username,Settings.ldap_email_attribute) if Settings.ldap_email_attribute
      self.email = self.username + '@' + Settings.ldap_domain unless self.email.present?
    end
    if self.name.nil? || self.name.empty?
      self.name = self.ldap_get_param(self.username,Settings.ldap_name_attribute) if Settings.ldap_name_attribute
    end
  end

  def apply_omniauth(omniauth)
    #self.email = omniauth['user_info']['email'] if email.blank?
    authentications.build(:provider => omniauth['provider'], :uid => omniauth['uid'])
  end

  def password_required?
    authentications.empty? && super
  end

  def do_reset_password_token
    generate_reset_password_token!
  end

  def reset_authentication_token!
    update_attribute(:authentication_token, generate_authentication_token)
  end

  def ensure_authentication_token
    return if authentication_token.present?
    self.authentication_token = generate_authentication_token
  end

  def change_organization(org)
    self.organization = org
    team_users.joins(:team).where("teams.organization_id" => org.id).update_all(last_sign_in_at: DateTime.now)
    save
  end

  def authenticate(password)
    password.present? && (Settings.authentication == 'ldap' ? valid_ldap_authentication?(password) : valid_password?(password))
  end

  def org_favorites(org_id = organization_id)
    favorites.where(:organization_id => org_id)
  end

  def favorite(ci_id)
    @favorite_map ||= favorites.where(:organization_id => organization_id).inject({}) do |m, proxy|
      m[proxy.ci_id] = proxy
      m
    end
    @favorite_map[ci_id]
  end

  def in_group?(names)
    groups.where('groups.name in (?) AND user_id = ?', (names.is_a?(Array) ? names : names.split(',')).map(&:strip), id).first
  end

  def self.global_admin_mode?
    Settings.global_admin_groups.present?
  end

  def is_global_admin?
    if @is_global_admin.nil?
      groups = Settings.global_admin_groups
      @is_global_admin = groups.present? && in_group?(groups)
    end
    return @is_global_admin
  end

  def is_org_admin?(org = nil)
    org_id = org ? (org.is_a?(Organization) ? org.id : org): organization_id
    return false unless org_id

    @is_admin ||= {}
    unless @is_admin.include?(org_id)
      @is_admin[org_id] = teams.where(:organization_id => org_id, 'teams.name' => Team::ADMINS).first.present? ||
                          teams_via_groups.where(:organization_id => org_id, 'teams.name' => Team::ADMINS).first.present?
    end

    return @is_admin[org_id]
  end

  def is_admin?(org = nil)
    is_global_admin? || is_org_admin?(org)
  end

  def manages_admins?
    User.global_admin_mode? ? is_global_admin? : is_admin?
  end

  def manages_team_members?(team)
    team_id = team.id
    @manages_team_members ||= {}
    return @manages_team_members[team_id] if @manages_team_members.include?(team_id)

    if is_admin?
      result = true
    elsif team.org_scope?
      result = false
    else
      # Allow if user belongs to teams with manage_access rights and these teams are assigned to the same assemblies
      # as the team user is trying to manage members for.
      organization_ns_path = "/#{organization.name}"
      team_assembly_ids = team.ci_proxies.where(:ns_path => organization_ns_path).pluck(:ci_id)

      manage_access_team_ids = all_teams.select {|t| t.manages_access}.map(&:id)
      manage_assembly_ids = organization.ci_proxies.joins(:teams).
        where(:ns_path => organization_ns_path).
        where('teams.id IN (?)', manage_access_team_ids).pluck(:ci_id)
      result = team_assembly_ids.present? && (team_assembly_ids - manage_assembly_ids).blank?
    end

    @manages_team_members[team_id] = result
  end

  def creates_assemblies?(org_id = nil)
    org_id ||= organization_id
    return false unless org_id

    @creates_assemblies ||= {}
    return @creates_assemblies[org_id] if @creates_assemblies.include?(org_id)

    if is_global_admin? || is_org_admin?
      result = true
    else
      where = ['teams.organization_id = ? AND teams.manages_access', org_id]
      result = teams.where(where).first || teams_via_groups.where(where).first
    end

    @creates_assemblies[org_id] = result
  end

  def manages_assembly?(assembly_id)
    @manages_assembly ||= {}
    return @manages_assembly[assembly_id] if @manages_assembly.include?(assembly_id)

    result = false
    if is_global_admin? || is_org_admin?
      result = true
    elsif assembly_id.present?
      where = {:organization_id => organization_id, :org_scope => true, :manages_access => true}
      result = teams.where(where).first.present? || teams_via_groups.where(where).first.present?

      unless result
        where = assembly_id =~ /\D/ ? {'ci_proxies.ci_name' => assembly_id, 'ci_proxies.ns_path' => "/#{organization.name}"} :
                                      {'ci_proxies.ci_id' => assembly_id}
        where['teams.organization_id'] = organization_id
        where['teams.manages_access']  = true

        result = teams.joins(:ci_proxies).where(where).first.present? || teams_via_groups.joins(:ci_proxies).where(where).first.present?
      end
    end

    @manages_assembly[assembly_id] = result
  end

  def creates_clouds?(org_id = nil)
    org_id ||= organization_id
    return false unless org_id

    @creates_clouds ||= {}
    return @creates_clouds[org_id] if @creates_clouds.include?(org_id)

    @creates_clouds[org_id] = User.global_admin_mode? ? is_global_admin? : is_org_admin?(org_id)
  end

  def manages_cloud?(cloud_id)
    @manages_cloud ||= {}
    return @manages_cloud[cloud_id] if @manages_cloud.include?(cloud_id)

    result = false
    if User.global_admin_mode?
      result = is_global_admin?
    elsif is_org_admin?
      result = true
    elsif cloud_id.present?
      where = cloud_id =~ /\D/ ? {'ci_proxies.ci_name' => cloud_id, 'ci_proxies.ns_path' => "/#{organization.name}/_clouds"} :
                                 {'ci_proxies.ci_id' => cloud_id}
      where['teams.organization_id'] = organization_id
      where['teams.manages_access']  = true

      result = teams.joins(:ci_proxies).where(where).first.present? || teams_via_groups.joins(:ci_proxies).where(where).first.present?
    end

    @manages_cloud[cloud_id] = result
  end

  def has_org_scope?(org = nil)
    org_id = org ? org.id : organization_id
    return false unless org_id

    @has_org_scope ||= {}
    unless @has_org_scope.include?(org_id)
      @has_org_scope[org_id] = teams.where(:org_scope => true, :organization_id => org_id).first.present? ||
                               teams_via_groups.where(:org_scope => true, :organization_id => org_id).first.present?
    end

    return @has_org_scope[org_id]
  end

  def dto_permissions(assembly_id)
    @dto_permissions ||= {}
    return @dto_permissions[assembly_id] if @dto_permissions.include?(assembly_id)

    dto = 0
    if is_global_admin? || is_org_admin?
      dto = Team::DTO_ALL
    elsif assembly_id.present?
      select = 'bool_or(design) as design, bool_or(transition) as transition, bool_or(operations) as operations'
      where = {'teams.org_scope' => true, 'teams.organization_id' => organization_id}
      permissions = teams.select(select).where(where).group('teams_users.user_id').order(nil).first
      dto |= Team.calculate_dto_permissions(permissions.design, permissions.transition, permissions.operations) if permissions
      unless dto >= Team::DTO_ALL   # No reason to go further if it is already has all permissions.
        permissions = teams_via_groups.select(select).where(where).group('group_members.user_id').order(nil).first
        dto |= Team.calculate_dto_permissions(permissions.design, permissions.transition, permissions.operations) if permissions
      end

      unless dto >= Team::DTO_ALL
        where = (assembly_id =~ /\D/ ? {'ci_proxies.ci_name' => assembly_id, 'ci_proxies.ns_path' => "/#{organization.name}"} : {'ci_proxies.ci_id' => assembly_id})
        where['teams.organization_id'] = organization_id

        permissions = teams.select(select).joins(:ci_proxies).where(where).group('teams_users.user_id').order(nil).first
        dto |= Team.calculate_dto_permissions(permissions.design, permissions.transition, permissions.operations) if permissions

        unless dto >= Team::DTO_ALL
          permissions = teams_via_groups.select(select).joins(:ci_proxies).where(where).group('group_members.user_id').order(nil).first
          dto |= Team.calculate_dto_permissions(permissions.design, permissions.transition, permissions.operations) if permissions
        end
      end
    end

    @dto_permissions[assembly_id] = dto
  end

  def has_any_dto?(assembly_id)
    dto_permissions(assembly_id) > 0
  end

  def has_design?(assembly_id)
    dto_permissions(assembly_id) & Team::DTO_DESIGN > 0
  end

  def has_transition?(assembly_id)
    dto_permissions(assembly_id) & Team::DTO_TRANSITION > 0
  end

  def has_operations?(assembly_id)
    dto_permissions(assembly_id) & Team::DTO_OPERATIONS > 0
  end

  def cloud_permissions(cloud_id)
    @cloud_permissions ||= {}
    return @cloud_permissions[cloud_id] if @cloud_permissions.include?(cloud_id)

    perms = 0
    if User.global_admin_mode?
      perms = is_global_admin? ? Team::CLOUD_ALL : 0
    elsif is_org_admin?
      perms = Team::CLOUD_ALL
    elsif cloud_id.present?
      select = 'bool_or(cloud_services) as services, bool_or(cloud_compliance) as compliance, bool_or(cloud_support) as support'
      where = {'teams.org_scope' => true, 'teams.organization_id' => organization_id}
      permissions = teams.select(select).where(where).group('teams_users.user_id').order(nil).first
      perms |= Team.calculate_cloud_permissions(permissions.services, permissions.compliance, permissions.support) if permissions
      unless perms >= Team::CLOUD_ALL   # No reason to go further if it is already has all permissions.
        permissions = teams_via_groups.select(select).where(where).group('group_members.user_id').order(nil).first
        perms |= Team.calculate_cloud_permissions(permissions.services, permissions.compliance, permissions.support) if permissions
      end

      unless perms >= Team::CLOUD_ALL
        where = (cloud_id =~ /\D/ ? {'ci_proxies.ci_name' => cloud_id, 'ci_proxies.ns_path' => "/#{organization.name}/_clouds"} : {'ci_proxies.ci_id' => cloud_id})
        where['teams.organization_id'] = organization_id

        permissions = teams.select(select).joins(:ci_proxies).where(where).group('teams_users.user_id').order(nil).first
        perms |= Team.calculate_cloud_permissions(permissions.services, permissions.compliance, permissions.support) if permissions

        unless perms >= Team::CLOUD_ALL
          permissions = teams_via_groups.select(select).joins(:ci_proxies).where(where).group('group_members.user_id').order(nil).first
          perms |= Team.calculate_cloud_permissions(permissions.services, permissions.compliance, permissions.support) if permissions
        end
      end
    end

    @cloud_permissions[cloud_id] = perms
  end

  def has_cloud_services?(cloud_id)
    cloud_permissions(cloud_id) & Team::CLOUD_SERVICES > 0
  end

  def has_cloud_compliance?(cloud_id)
    cloud_permissions(cloud_id) & Team::CLOUD_COMPLIANCE > 0
  end

  def has_cloud_support?(cloud_id)
    cloud_permissions(cloud_id) & Team::CLOUD_SUPPORT > 0
  end

  def last_sign_in_at_for_org(org_id)
    team_users.joins(:team).where('teams.organization_id' => org_id)
      .order(:last_sign_in_at).last.try(:last_sign_in_at)
  end


  protected

  def confirmation_required?
    if Settings.confirmation
      super
    else
      false
    end
  end


  private

  def check_organizations
    orgs = []
    teams.joins(:users, :organization).
      where(:name => Team::ADMINS).
      select('count(users.id) as user_count, organizations.name as org_name').
      group('teams.id, org_name').each do |t|
      orgs << t.org_name if t.user_count == 1
    end

    return true if orgs.blank?

    errors.add(:base, "'#{username}' is last admin in #{'organization'.pluralize(orgs.size)}: #{orgs.sort.join(', ')}.")
    return false
  end

  def generate_authentication_token
    loop do
      token = Devise.friendly_token
      break token unless User.where(:authentication_token =>  token).first
    end
  end
end
