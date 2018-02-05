class Transition::PlatformsController < Base::PlatformsController
  include ::Search
  before_filter :find_assembly_environment_platform
  before_filter :load_scale_relations, :only => [:edit, :update, :show]
  before_filter :find_cloud, :only => [:cloud_configuration, :cloud_priority]

  def index
    @platforms = Cms::DjRelation.all(:params => {:ciId              => @environment.ciId,
                                                 :direction         => 'from',
                                                 :relationShortName => 'ComposedOf',
                                                 :targetClassName   => 'manifest.Platform'}).map(&:toCi)
    respond_to do |format|
      format.js
      format.json {render :json => @platforms}
    end
  end

  def show
    load_clouds
    respond_to do |format|
      format.html { render(:action => :show) }

      format.json do
        if @platform
          @platform.links_to = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                               :direction         => 'from',
                                                               :relationShortName => 'LinksTo',
                                                               :includeToCi       => true}).map { |r| r.toCi.ciName }
          @platform.consumes = @clouds
        end
        render_json_ci_response(true, @platform)
      end
    end
  end

  def edit
    respond_to do |format|
      format.js { render :action => :edit }

      format.json do
        @platform.depends_on = @scale_relations if @scale_relations
        render_json_ci_response(true, @platform)
      end
    end
  end

  def update
    ok = true
    attrs = params[:cms_dj_ci]
    ok = execute(@platform, :update_attributes, attrs) if attrs.present?

    # Save "Scale" relation changes.
    if ok
      scale = params[:depends_on]
      if @scale_relations.present?
        if scale.present?
          @scale_relations.collect! do |relation|
            scale_data = scale[relation.toCiId.to_s]
            if scale_data
              relation.relationAttributes = scale_data[:relationAttributes]
              relation.relationAttrProps  = scale_data[:relationAttrProps]
              to_ci = relation.toCi
              relation.toCi = nil   # No need to pass "ci" while saving relation but restore it after the save because it will referenced down stream.
              ok = execute(relation, :save)
              relation.toCi = to_ci
              unless ok
                @platform.errors.add(:base, "Cannot update scale for #{relation.toCi.ciName}.")
                break
              end
            end
            relation
          end
        end
      end
    end

    respond_to do |format|
      format.js { render :action => :edit }
      format.json do
        @platform.depends_on = @scale_relations if @scale_relations
        render_json_ci_response(ok, @platform)
      end
    end
  end

  def destroy
    if @platform.ciAttributes.attributes.has_key?(:is_active) && @platform.ciAttributes.is_active != 'true'
      ok = execute(@platform, :destroy)
    else
      @platform.errors.add(:base, 'Cannot terminate active platform.')
      ok = false
    end

    respond_to do |format|
      format.js do
        if ok
          render :action => :toggle
        else
          flash[:error] = @platform.errors.full_messages.join(' ')
          render :js => ''
        end
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def activate
    ok = Transistor.activate_platform(@platform.ciId)
    @platform.errors.add(:base, 'Failed to activate platform.') unless ok

    respond_to do |format|
      format.js do
        if ok
          render :action => :toggle
        else
          flash[:error] = @platform.errors.full_messages.join(' ')
          render :js => ''
        end
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def toggle
    relation = Cms::DjRelation.first(:params => {:ciId              => @platform.ciId,
                                                 :direction         => 'to',
                                                 :relationShortName => 'ComposedOf'})
    enabled  = relation.relationAttributes.enabled == 'true'
    ok = Transistor.toggle_platform(@platform, !enabled)

    respond_to do |format|
      format.js do
        if ok
          render :action => :toggle
        else
          flash[:error] = @platform.errors.full_messages.join(' ')
          render :js => ''
        end
      end

      format.json { render_json_ci_response(ok, @platform) }
    end
  end

  def cloud_configuration
    @cloud.relationAttributes.attributes.merge!(params[:attributes])
    ok = check_primary_cloud
    ok &&= execute(@cloud, :save)

    respond_to do |format|
      format.js do
        error = @cloud.errors.present?
        if error
          load_clouds
          flash[:error] = @cloud.errors.full_messages.join(' ')
        end
      end

      format.json { render_json_ci_response(ok, @cloud) }
    end
  end

  def cloud_priority
    @cloud.relationAttributes.priority = params[:priority]
    ok = check_primary_cloud
    if ok
      ok = Transistor.update_platform_cloud(@platform.ciId, @cloud)
      @cloud.errors.add(:base, "Failed to change cloud priority for #{@cloud.toCi.ciName}.") unless ok
    end

    respond_to do |format|
      format.js do
        error = @cloud.errors.present?
        if error
          load_clouds
          flash[:error] = @cloud.errors.full_messages.join(' ')
        end

        render :action => :cloud_configuration
      end

      format.json { render_json_ci_response(ok, @cloud) }
    end
  end


  protected

  def search_ns_path
    @platform.nsPath
  end


  private

  def find_assembly_environment_platform
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    platform_id  = params[:id]
    if platform_id.present?
      @platform = locate_manifest_platform(platform_id, @environment)
      @composed_of = Cms::DjRelation.first(:params => {:ciId              => @platform.ciId,
                                                       :direction         => 'to',
                                                       :relationShortName => 'ComposedOf'}) if @platform
    end
  end

  def load_scale_relations
    if @platform.ciAttributes.availability == 'redundant'
      @scale_relations = Cms::DjRelation.all(:params => {:nsPath       => @platform.nsPath,
                                                         :relationName => 'manifest.DependsOn',
                                                         :attr         => 'flex:eq:true',
                                                         :includeToCi  => true,
                                                         :attrProps    => 'owner'})
    end
  end

  def find_cloud
    cloud_id = params[:cloud_id].to_i
    load_clouds
    @cloud = @clouds.find {|c| c.toCiId == cloud_id}
    not_found("Cloud #{cloud_id} not found for platform #{@platform && @platform.ciId} in #{@platform && @platform.nsPath}") unless @cloud
  end

  def load_clouds
    @clouds = Cms::DjRelation.all(:params => {:relationName    => 'base.Consumes',
                                              :ciId            => @platform.ciId,
                                              :targetClassName => 'account.Cloud',
                                              :direction       => 'from',
                                              :includeToCi     => true}).sort_by {|o| o.toCi.ciName}
  end

  def check_primary_cloud
    ok = @clouds.find {|c| c.relationAttributes.adminstatus != 'offline' && c.relationAttributes.priority == '1'}
    @cloud.errors.add(:base, 'This change will remove all primary clouds for this platform.') unless ok
    return ok
  end
end
