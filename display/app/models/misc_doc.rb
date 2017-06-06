class MiscDoc < ActiveRecord::Base
  ANNOUNCEMENTS                   = 'announcements'
  DEPLOYMENT_TO_ALL_PRIMARY_CHECK = 'deployment_to_all_primary_check'

  serialize :document, Hash

  class << self
    %w(announcements deployment_to_all_primary_check).each do |e|
      const_set(e.upcase, e)
      define_method(e.to_sym) do
        where(:name => e).first_or_create
      end
    end
  end
end
