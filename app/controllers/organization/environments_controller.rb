class Organization::EnvironmentsController < Base::EnvironmentsController
  before_filter :authorize_admin, :only => [:new, :create, :update, :destroy]
  before_filter :find_environment, :only => [:show, :edit, :update, :destroy]

  def index
    @environments = Cms::Ci.all(:params => {:nsPath      => organization_ns_path,
                                            :ciClassName => 'account.Environment'})

    consumers_rels = Cms::Relation.all(:params => {:nsPath          => organization_ns_path,
                                                   :relationName    => 'base.Consumes',
                                                   :targetClassName => 'account.Cloud',
                                                   :includeToCi     => true})

    @environments.each do |e|
      e.clouds = consumers_rels.select { |r| r.fromCiId == e.ciId }
    end

    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @environments }
    end
  end

  def show
    load_clouds
    @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m}
    render_json_ci_response(true, @environment)
  end

  def new
    @environment ||= Cms::Ci.build(:nsPath => organization_ns_path, :ciClassName => 'account.Environment')
    load_available_clouds
    @clouds ||= []
    render(:action => :edit)
  end

  def create
    cloud_map    = params[:cms_ci].delete(:clouds) || params[:clouds]
    @clouds      = []
    @environment = Cms::Ci.build(params[:cms_ci].merge(:nsPath      => organization_ns_path,
                                                       :ciClassName => 'account.Environment'))
    ok = execute(@environment, :save) && save_consumes_relations(cloud_map)

    respond_to do |format|
      format.js { ok ? index : new }

      format.json do
        @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m} if @environment
        render_json_ci_response(ok, @environment)
      end
    end
  end

  def edit
    load_available_clouds
    load_clouds
    render(:action => :edit)
  end

  def update
    load_clouds
    cloud_map = params[:cms_ci].delete(:clouds) || params[:clouds]
    ok = execute(@environment, :update_attributes, params[:cms_ci]) && save_consumes_relations(cloud_map)

    respond_to do |format|
      format.js { ok ? index : edit }
      format.json do
        @environment.clouds = @clouds.inject({}) {|m, c| m[c.toCiId] = c.relationAttributes.attributes; m}
        render_json_ci_response(ok, @environment)
      end
    end
  end

  def destroy
    if @environment.present?
      has_envs = Cms::Ci.first(:params => {:nsPath    => organization_ns_path,
                                     :recursive => true,
                                     :attr      => "profile:eq:#{@environment.ciName}"})

      if has_envs
        message       = 'Cannot delete environment profile with existing derived environments.'
        flash[:error] = message
        @environment.errors.add(:base, message)
      else
        ok = execute(@environment, :destroy)
        flash[:error] = 'Failed to delete environment profile.' unless ok
      end
    end

    respond_to do |format|
      format.js { index }
      format.json { render_json_ci_response(@environment && @environment.errors.blank?, @environment) }
    end
  end


  private

  def find_environment
    @environment = Cms::Ci.locate(params[:id], organization_ns_path)
  end


  def load_available_clouds
    @available_clouds = Cms::Ci.all(:params => {:nsPath => clouds_ns_path, :ciClassName => 'account.Cloud'}).sort_by(&:ciName)
  end

  def load_clouds
    @clouds ||= Cms::Relation.all(:params => {:relationName    => 'base.Consumes',
                                              :targetClassName => 'account.Cloud',
                                              :direction       => 'from',
                                              :ciId            => @environment.ciId}).sort_by { |o| o.relationAttributes.priority }
  end

  def save_consumes_relations(cloud_map)
    return true unless cloud_map

    ok                       = true
    consumes_rel_map         = @clouds.to_map(&:toCiId)
    @clouds                  = []
    available_cloud_id_map   = load_available_clouds.to_map(&:ciId)
    available_cloud_name_map = load_available_clouds.to_map(&:ciName)
    cloud_map.each_pair do |id, cloud_attr|
      cloud_id = id.to_i
      consumes_rel = consumes_rel_map[cloud_id]
      cloud_attr = {:priority => cloud_attr} unless cloud_attr.is_a?(Hash)   # For backward compatibility (1/22/2015) - TODO: remove at some point.
      priority = cloud_attr[:priority].to_i
      if priority == 1 || priority == 2
        if consumes_rel
          consumes_rel.relationAttributes.attributes.merge!(cloud_attr)
        else
          cloud = available_cloud_id_map[cloud_id] || available_cloud_name_map[id]
          if cloud && cloud.ciAttributes.adminstatus == 'active'
            consumes_rel = Cms::Relation.build({:relationName       => 'base.Consumes',
                                                :nsPath             => @environment.nsPath,
                                                :fromCiId           => @environment.ciId,
                                                :toCiId             => cloud.ciId,
                                                :relationAttributes => cloud_attr})
          else
            consumes_rel = nil
          end
        end
        if consumes_rel
          ok = execute_nested(@environment, consumes_rel, :save)
          @clouds << consumes_rel if ok
        end
      end

      break unless ok
    end

    if ok
      (consumes_rel_map.keys - @clouds.map(&:toCiId)).each do |id|
        ok = execute_nested(@environment, consumes_rel_map[id], :destroy)
        break unless ok
      end
    end

    return ok
  end
end
