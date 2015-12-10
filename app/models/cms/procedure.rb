class Cms::Procedure < Cms::Base
  self.prefix      = "#{Settings.cms_path_prefix}/cm/ops/"
  self.primary_key = :procedureId

  def to_param
    procedureId.to_s
  end

  def self.build(attributes = {})
    proc_params = ActiveSupport::HashWithIndifferentAccess.new({:ciId           => '',
                                                                :procedureCiId  => '',
                                                                :procedureState => 'pending',
                                                                :arglist        => '',
                                                                :definition     => ''})
    self.new(proc_params.merge(attributes))
  end

  def retry
    self.get(:retry)
  end
end
