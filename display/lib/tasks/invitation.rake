namespace :invitation do
  desc "Creates/updates an invitation token for a given email\nUsage:\n\trake invitation:create[abc@xyz.com,\"What a token!\"]"
  task :create, [:email, :comment] => :environment  do |t, args|
    puts
    email = args[:email]
    comment = args[:comment]
    token = "#{SecureRandom.random_number(36**6).to_s(36)}"
    inv = Invitation.where(:email => email).first
    if inv
      comment = comment.presence || inv.comment
      inv.update_attributes(:token =>  token, :comment => comment)
    else
      Invitation.create(:email => email, :token => token, :comment => comment)
    end
    puts "#{inv ? 'Updated' : 'Created'} inviation token for #{email}: ** #{token} ** [#{comment}]"
  end

  desc "Deletes an invitation token for a given email\nUsage:\n\trake invitation:delete[abc@xyz.com]"
  task :delete, [:email] => :environment  do |t, args|
    puts
    email = args[:email]
    inv = Invitation.where(:email => email).first
    if inv
      inv.destroy
      puts "Deleted invitation token for #{email}."
    else
      puts "Invitation token for #{email} does not exist."
    end
  end

  desc "Lists existing invitation tokens\nUsage:\n\trake invitation:list"
  task :list => :environment  do |t, args|
    puts
    Invitation.all.each do |i|
      puts "#{i.token} | #{i.email} - #{i.comment}"
    end
  end
end
