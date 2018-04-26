namespace :user do
  desc "List inactive users to a csv file 'inactive_users_SINCE_in_ORG.csv'\nUsage:\n\trake user:inactive[inactive_since,org?,verbose?,batch_size?]\n\t e.g.:\n\t\take user:inactive[2017-01-01,some-org,true]"
  task :inactive, [:inactive_since, :org, :verbose, :batch_size] => :environment  do |t, args|
    inactive_users = []
    total          = 0
    since          = args[:inactive_since].presence || Time.now
    org            = args[:org]
    verbose        = args[:verbose].to_i
    batch_size     = (args[:batch_size] || 500).to_i

    t = Time.now
    puts "Args: inactive_since=#{since}, org=#{org}, verbose=#{verbose}, batch_size=#{batch_size}"

    user_scope = org.present? ? Organization.where(:name => org).first.users : User
    inactive_scope = user_scope.where('current_sign_in_at < ?', since)
    puts "\n#{user_scope.count} users, #{inactive_scope.count} inactive users"

    inactive_scope.select('users.id, users.username, users.email, users.name, users.created_at, users.current_sign_in_at').
      find_in_batches(:batch_size => batch_size) do |batch|
      inactive_users += batch
      total = inactive_users.size
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
  task :obsolete, [:inactive_since, :org, :verbose, :batch_size] => :environment  do |t, args|
    inactive_users = []
    obsolete_users = {}
    total          = 0
    since          = args[:inactive_since].presence || Time.now
    org            = args[:org]
    verbose        = args[:verbose].to_i
    batch_size     = (args[:batch_size] || 30).to_i
    progress_chunk = 20 * batch_size

    puts "Args: inactive_since=#{since}, org=#{org}, verbose=#{verbose}, batch_size=#{batch_size}"

    t = Time.now
    users = (org.present? ? Organization.where(:name => org).first.users : User).where('current_sign_in_at < ?', since)
    puts "#{users.count} inactive users in db"

    Devise.ldap_logger = false
    ldap = Devise::LdapAdapter.ldap_connect(nil)

    users.select('users.id, users.username, users.email, users.name, users.created_at, users.current_sign_in_at').
      find_in_batches(:batch_size => batch_size) do |batch|
      inactive_users += batch
      entries = ldap.search_for_logins(batch.map(&:username))
      batch.each do |u|
        obsolete_users[u.username] = u unless entries[u.username]
      end
      total = inactive_users.size
      puts "#{total} users checked, #{obsolete_users.size} obsolete, #{(Time.now - t).round(1)}sec" if total % (progress_chunk) == 0
    end

    puts "\n#{total} users, #{obsolete_users.size} obsolete users."
    puts "#{(Time.now - t).round(1)}sec"

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

  desc "Removes a list of users from db \nUsage:\n\trake user:remove[file_name,verbose?]"
  task :remove, [:file, :verbose] => :environment  do |t, args|
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
        u = User.find_by_username(username)
        total += 1
        prefix = "#{total.to_s.ljust(5)}#{username.ljust(20)}"
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
    puts "#{(Time.now - t).round(1)}sec"

    if verbose > 0
      puts "Counts\n\tUser: #{User.count}\n\tTeamUser: #{TeamUser.count}\n\tGroupMember: #{GroupMember.count}"
    end
  end
end
