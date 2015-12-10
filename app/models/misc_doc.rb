class MiscDoc < ActiveRecord::Base
  ANNOUNCEMENTS = 'announcements'

  serialize :document, Hash

  def self.announcements
    where(:name => ANNOUNCEMENTS).first_or_create
  end
end
