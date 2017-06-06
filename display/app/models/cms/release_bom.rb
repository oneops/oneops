class Cms::ReleaseBom < Cms::Release
  self.prefix       = "#{Settings.cms_path_prefix}/dj/simple/"
  self.element_name = 'release'
  self.primary_key  = :releaseId

  def bom
    Cms::DjCi.all(:params => {:nsPath => self.nsPath, :recursive => true})
  end

  def manifest
    Cms::DjCi.all(:params => {:nsPath    => self.nsPath.gsub(/\/bom/, '/manifest'),
                              :recursive => true})
  end

  def requires
    Cms::DjRelation.all(:params => {:nsPath            => self.nsPath.gsub(/\/bom/, '/manifest'),
                                    :relationShortName => 'Requires',
                                    :recursive         => true})
  end

  def links_to
    Cms::DjRelation.all(:params => {:nsPath            => self.nsPath.gsub(/\/bom/, ''),
                                    :relationShortName => 'LinksTo'})
  end

  def realized_as
    Cms::DjRelation.all(:params => {:nsPath            => self.nsPath,
                                    :recursive         => true,
                                    :relationShortName => 'RealizedAs'})
  end

  def depends_on
    Cms::DjRelation.all(:params => {:nsPath            => self.nsPath,
                                    :recursive         => true,
                                    :relationShortName => 'DependsOn'})
  end

  def deployed_to
    Cms::DjRelation.all(:params => {:nsPath            => self.nsPath,
                                    :recursive         => true,
                                    :targetClassName   => 'account.Cloud',
                                    :relationShortName => 'DeployedTo'})
  end
end
