namespace :oneops do
  namespace :user do
    desc "List inactive users to a csv file 'inactive_users_SINCE_in_ORG.csv'\nUsage:\n\trake user:inactive[inactive_since,org?,verbose?,batch_size?]\n\t e.g.:\n\t\take user:inactive[2017-01-01,some-org,true]"
    task :inactive, [:inactive_since, :org, :verbose, :batch_size] => :environment do |t, args|
      inactive_users = []
      total          = 0
      since          = args[:inactive_since].presence || Time.now
      org            = args[:org]
      verbose        = args[:verbose].to_i
      batch_size     = (args[:batch_size] || 500).to_i

      t = Time.now
      puts "Args: inactive_since=#{since}, org=#{org}, verbose=#{verbose}, batch_size=#{batch_size}"

      user_scope     = org.present? ? Organization.where(:name => org).first.users : User
      inactive_scope = user_scope.where('current_sign_in_at < ?', since)
      puts "\n#{user_scope.count} users, #{inactive_scope.count} inactive users"

      inactive_scope.select('users.id, users.username, users.email, users.name, users.created_at, users.current_sign_in_at').
        find_in_batches(:batch_size => batch_size) do |batch|
        inactive_users += batch
        total          = inactive_users.size
        puts "#{total} users loaded, #{(Time.now - t).round(1)}sec"
      end

      puts "#{(Time.now - t).round(1)}sec"

      open("inactive_users_since_#{since}#{"_in_#{org}" if org.present?}.csv", 'w') do |f|
        f.puts "username,email,name,last_sign_in_at\n"
        inactive_users.sort_by(&:current_sign_in_at).each do |u|
          puts "#{u.username.ljust(20)}#{(u.name || '').ljust(30)}\e[32mactive\e[0m (#{u.current_sign_in_at})" if verbose > 0
          f.puts "#{u.username},#{u.email},#{u.name},#{u.current_sign_in_at}\n"
        end
      end
    end

    desc "List obsolete users to a csv file 'obsolete_users_SINCE_in_ORG.csv'\nUsage:\n\trake user:obsolete[inactive_since?,org?,verbose?,batch_size?]\n\t e.g.:\n\t\take user:obsolete[2017-01-01,some-org,true]"
    task :obsolete, [:inactive_since, :org, :verbose, :batch_size] => :environment do |t, args|
      inactive_users = []
      obsolete_users = {}
      total          = 0
      since          = args[:inactive_since].presence || Time.now
      org            = args[:org]
      verbose        = args[:verbose].to_i
      batch_size     = (args[:batch_size] || 30).to_i
      progress_chunk = 20 * batch_size

      puts "Args: inactive_since=#{since}, org=#{org}, verbose=#{verbose}, batch_size=#{batch_size}"

      t     = Time.now
      users = (org.present? ? Organization.where(:name => org).first.users : User).where('current_sign_in_at < ?', since)
      puts "#{users.count} inactive users in db"

      Devise.ldap_logger = false
      ldap               = Devise::LdapAdapter.ldap_connect(nil)

      users.select('users.id, users.username, users.email, users.name, users.created_at, users.current_sign_in_at').
        find_in_batches(:batch_size => batch_size) do |batch|
        inactive_users += batch
        entries        = ldap.search_for_logins(batch.map(&:username))
        batch.each do |u|
          obsolete_users[u.username] = u unless entries[u.username]
        end
        total = inactive_users.size
        puts "#{total} users checked, #{obsolete_users.size} obsolete, #{(Time.now - t).round(1)}sec" if total % (progress_chunk) == 0
      end

      puts "\n#{total} users, #{obsolete_users.size} obsolete users."
      puts "Done in #{(Time.now - t).round(1)}sec"

      open("obsolete_users_since_#{since}#{"_in_#{org}" if org.present?}.csv", 'w') do |f|
        f.puts "username,email,name,last_sign_in_at\n"
        obsolete_users.values.sort_by(&:current_sign_in_at).each do |u|
          puts "#{u.username.ljust(20)}#{u.name.ljust(30)}\e[31mobsolete\e[0m (#{u.current_sign_in_at})" if verbose > 0
          f.puts "#{u.username},#{u.email},#{u.name},#{u.current_sign_in_at}\n"
        end
      end

      open("inactive_users_since_#{since}#{"_in_#{org}" if org.present?}.csv", 'w') do |f|
        f.puts "username,email,name,last_sign_in_at\n"
        inactive_users.sort_by(&:current_sign_in_at).each do |u|
          unless obsolete_users[u.username]
            puts "#{u.username.ljust(20)}#{(u.name || '').ljust(30)}\e[32mactive\e[0m (#{u.current_sign_in_at})" if verbose > 1
            f.puts "#{u.username},#{u.email},#{u.name},#{u.current_sign_in_at}\n"
          end
        end
      end
    end

    desc "Removes list of users (specified by csv file) from db \nUsage:\n\trake user:remove[file_name,verbose?]"
    task :remove, [:file, :verbose] => :environment do |t, args|
      t       = Time.now
      file    = args[:file]
      verbose = args[:verbose].to_i
      total   = 0
      removed = 0
      unless file.present? && File.exist?(file)
        abort "\e[31mSpecifiy a valid file containing a list of usernames to remove!\e[0m"
      end

      open(file, 'r') do |f|
        f.each_line do |l|
          username = l.split(/[,\n]/, 2).first
          u        = User.find_by_username(username)
          total    += 1
          prefix   = "#{total.to_s.ljust(5)}#{username.ljust(20)}"
          if u
            if u.destroy
              puts "#{prefix}\e[32m removed\e[0m" if verbose > 0
              removed += 1
            else
              puts "#{prefix}\e[31m failed to remove\e[0m"
            end
          else
            puts "#{prefix}\e[31m not found\e[0m" if verbose > 0
          end
          puts "#{total} processed, #{removed} removed, #{(Time.now - t).round(1)}sec" if total % 100 == 0
        end
      end
      puts "\n#{total} users processed, #{removed} users removed."
      puts "Done in #{(Time.now - t).round(1)}sec"

      if verbose > 0
        puts "Counts\n\tUser: #{User.count}\n\tTeamUser: #{TeamUser.count}\n\tGroupMember: #{GroupMember.count}"
      end
    end
  end

  namespace :team do
    desc "Create (unless already exists) team in orgs\nUsage:\n\t rake oneops:team:create[team_name,team_desc,org_scope,design,transition,operations,org_name_regex,verbose?]"
    task :create, [:team_name, :team_desc, :org_scope, :design, :transition, :operations, :org_name_regex, :verbose] => :environment do |t, args|
      t = Time.now

      team_name      = args[:team_name]
      team_desc      = args[:team_desc]
      org_scope      = args[:org_scope]
      design         = args[:design]
      transition     = args[:transition]
      operations     = args[:operations]
      org_name_regex = /#{args[:org_name_regex]}/
      verbose        = args[:verbose]

      teams = Team.where(:name => team_name).inject({}) {|h, t| h[t.organization_id] = t; h}

      added_count    = 0
      failed_count   = 0
      existing_count = 0
      Organization.order(:name).all.each do |o|
        next unless o.name =~ org_name_regex

        print "#{o.name.ljust(30)} - "
        if teams[o.id]
          existing_count += 1
          puts "alredy exists" if verbose
        else
          team = o.teams.create(:name        => team_name,
                                :description => team_desc,
                                :org_scope   => org_scope,
                                :design      => design,
                                :transition  => transition,
                                :operations  => operations)

          if team
            added_count += 1
            puts 'created' if verbose
          else
            failed_count += 1
            puts 'failed to create'
          end
        end
        print "#{' ' * 35}\r"
      end
      puts "#{' ' * 35}"
      puts "Created:        #{added_count}\nFailed:         #{failed_count}\nAlready exists: #{existing_count}"
      puts "Done in #{(Time.now - t).round(1)}sec"
    end
  end

  namespace :group do
    desc "Add specified group to a certain team in orgs\nUsage:\n\t rake group:add_to_team[group_name,team_name,org_name_regex,verbose?]"
    task :add_to_team, [:group_name, :team_name, :org_name_regex, :verbose] => :environment do |t, args|
      t = Time.now

      group_name     = args[:group_name]
      team_name      = args[:team_name]
      org_name_regex = /#{args[:org_name_regex]}/
      verbose        = args[:verbose]

      group = Group.find_by_name(group_name)
      abort "Group '#{group_name}' is not found!" unless group
      teams_with_group = group.teams.where(:name => team_name).inject({}) {|h, t| h[t.id] = t; h}

      teams = Team.where(:name => team_name).inject({}) {|h, t| h[t.organization_id] = t; h}

      added_count         = 0
      already_added_count = 0
      missing_count       = 0
      Organization.order(:name).all.each do |o|
        next unless o.name =~ org_name_regex

        print "#{o.name.ljust(30)} - "
        team = teams[o.id]
        if team
          if teams_with_group[team.id]
            already_added_count += 1
            puts " group already added" if verbose
          else
            team.groups << group
            added_count += 1
            puts 'added' if verbose
          end
        else
          puts " team '#{team_name}' does not exist!"
          missing_count += 1
        end
        print "#{' ' * 35}\r"
      end
      puts "#{' ' * 35}"
      puts "Added:         #{added_count}\nTeam missing:  #{missing_count}\nAlready added: #{already_added_count}"
      puts "Done in #{(Time.now - t).round(1)}sec"
    end
  end
end
