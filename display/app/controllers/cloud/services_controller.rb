class Cloud::ServicesController < ApplicationController
  before_filter :find_parents_and_service
  before_filter :authorize_write, :only => [:new, :create, :update, :destroy]

  def index
    load_available_services
    load_services
    respond_to do |format|
      format.js { render :action => :index }
      format.json { render :json => @services.map(&:toCi) }
    end
  end

  def show
    render_json_ci_response(@service.present?, @service)
  end

  def new
    load_available_services
    if @service_classes.blank?
      redirect_to(path_to_ci(@zone || @cloud))
      flash[:error] = 'There are no more services to add.'
      return
    end

    if @zone
      @zone_service_class = params[:zoneServiceClass]
      if @zone_service_class
        md = Cms::CiMd.look_up(@zone_service_class)
        if md
          @service = Cms::Ci.build((params[:cms_ci].presence || {}).merge(:ciClassName => @zone_service_class,
                                                                          :nsPath      => cloud_zone_ns_path(@zone),
                                                                          :ciName      => @zone_service_class.split('.').last))
        else
          @zone_service_class = nil
        end
      end
    else
      @mgmt_ci_id = params[:mgmtCiId].to_i
      if @mgmt_ci_id > 0
        mgmt_ci = Cms::Ci.find(@mgmt_ci_id)
        if mgmt_ci
          ci_hash               = params[:cms_ci].presence || {}
          ci_hash[:ciName]      = mgmt_ci.ciName
          ci_hash[:ciClassName] = mgmt_ci.ciClassName.gsub('mgmt.', '')
          ci_hash[:nsPath]      = cloud_ns_path(@cloud)
          @service              = Cms::Ci.build(ci_hash)
          ci_attributes = @service.ciAttributes.attributes
          ci_attributes.keys.each do |attribute|
            default = mgmt_ci.ciAttributes.attributes[attribute]
            ci_attributes[attribute] = default if default
            ci_attributes[attribute] = '' if default == '--ENCRYPTED--'
          end
        else
          @mgmt_ci_id = nil
        end
      end
    end

    respond_to do |format|
      format.html { render :action => :new }
      format.js
      format.json { render_json_ci_response(true, @service) }
    end
  end

  def create
    service_type = nil
    if @zone
      ok = true
      @service = Cms::Ci.build(params[:cms_ci].merge(:nsPath => cloud_zone_ns_path(@zone)))
      @zone_service_class = @service.ciClassName
      cloud_service_rel   = Cms::Relation.first(:params => {:ciId              => @cloud.ciId,
                                                            :direction         => 'from',
                                                            :relationShortName => 'Provides',
                                                            :targetClassName   => "cloud.#{@zone_service_class.split('.', 3).last}"})
      if cloud_service_rel
        service_type = cloud_service_rel.relationAttributes.service
        zone_service_rel = Cms::Relation.first(:params => {:ciId              => @zone.ciId,
                                                           :direction         => 'from',
                                                           :relationShortName => 'Provides',
                                                           :attr              => "service:eq:#{service_type}",
                                                           :includeToCi       => true})
        if zone_service_rel
          @service.errors.add(:base, "Service type '#{service_type}' is already added as zone service '#{zone_service_rel.toCi.ciName}'.")
          ok = false
        end
      else
        @service.errors.add(:base, 'No corresponding cloud service exists.')
        ok = false
      end

      if ok
        relation = Cms::Relation.build(:relationName       => 'base.Provides',
                                       :nsPath             => cloud_zone_ns_path(@zone),
                                       :fromCiId           => @zone.ciId,
                                       :relationAttributes => {:service => service_type},
                                       :toCi               => @service)

        ok = execute_nested(@service, relation, :save)
        @service = relation.toCi if ok
      end
    else
      @service = Cms::Ci.build(params[:cms_ci].merge(:nsPath => cloud_ns_path(@cloud)))
      @mgmt_ci_id  = params[:mgmtCiId].to_i
      mgmt_ci      = Cms::Ci.find(@mgmt_ci_id)
      if @mgmt_ci_id > 0
        mgmt_relation = Cms::Relation.first(:params => {:ciId              => @mgmt_ci_id,
                                                        :direction         => 'to',
                                                        :relationShortName => 'Provides',
                                                        :includeToCi       => true})
        if mgmt_relation
          # Public service.
          service_type = mgmt_relation.relationAttributes.service
        else
          # Private service from the same organization.
          service_type = mgmt_ci && mgmt_ci.nsPath == services_ns_path && mgmt_ci.ciClassName.split('.')[1]
        end
      end
      ok = service_type.present?
      if ok
        Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                      :direction         => 'from',
                                      :relationShortName => 'Provides'}).each do |r|
          if r.relationAttributes.service == service_type
            ok = false
            @service.errors.add(:base, "Service type '#{service_type}' is already added as service '#{r.toCi.ciName}'.")
            break
          end
        end

        if ok
          @service.ciName = mgmt_ci.ciName

          relation = Cms::Relation.build(:relationName       => 'base.Provides',
                                         :fromCiId           => @cloud.ciId,
                                         :nsPath             => clouds_ns_path,
                                         :relationAttributes => {:service => service_type},
                                         :toCi               => @service)

          ok = execute_nested(@service, relation, :save)
          @service = relation.toCi if ok
          if ok
            # Pull in offerings.
            cloud_service_ns_path = cloud_service_ns_path(@service)
            Cms::Relation.all(:params => {:ciId => @mgmt_ci_id,
                                          :direction => 'from',
                                          :relationShortName => 'Offers'}).map(&:toCi).each do |o|
              offers_rel = Cms::Relation.build(:relationName => 'base.Offers',
                                             :nsPath       => cloud_service_ns_path,
                                             :fromCiId     => @service.ciId,
                                             :toCi         => Cms::Ci.build(:ciClassName  => 'cloud.Offering',
                                                                            :nsPath       => cloud_service_ns_path,
                                                                            :ciName       => o.ciName,
                                                                            :ciAttributes => o.ciAttributes.attributes))
              unless execute(offers_rel, :save)
                flash[:error] = 'Failed to add one or more offerings.'
              end
            end
          end
        end
      else
        @service.errors.add(:base, 'Unknown cloud service source.')
      end
    end

    respond_to do |format|
      format.html do
        if ok
          redirect_to(@zone ? path_to_ci(@zone) : edit_cloud_path(@cloud))
        else
          flash[:error] = 'Failed to create service.'
          load_available_services
          render :action => :new
        end
      end

      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def edit
    respond_to do |format|
      format.html do
        unless @zone
          @zone_services = Cms::Ci.all(:params => {:nsPath      => cloud_ns_path(@cloud),
                                                   :recursive   => true,
                                                   :ciClassName => "cloud.zone.#{@service.ciClassName.split('.', 2).last}"})
        end
      end

      format.json { render_json_ci_response(true, @service) }
    end
  end

  def update
    ok = execute(@service, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.html { ok ? redirect_to(path_to_ci(@zone || @cloud)) : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def destroy
    ok = execute(@service, :destroy)

    respond_to do |format|
      format.html { ok ? redirect_to(path_to_ci(@zone || @cloud)) : render(:action => :edit) }

      format.js do
        flash[:error] = 'Failed to delete service.' unless ok
        index
      end

      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def available
    @service_classes = Cms::Relation.all(:params => {:nsPath            => '/public',
                                                     :relationShortName => 'Provides',
                                                     :fromClassName     => 'mgmt.Cloud',
                                                     :includeToCi       => true,
                                                     :recursive         => true}).inject({}) do |m, r|
      type = r.relationAttributes.service
      m[type] ||= []
      m[type] << r.toCi
      m
    end
    render :json => @service_classes
  end

  def diff
    changes_only = params[:changes_only] != 'false'
    # We have a problem with Provides relation for cloud services being stored in the wrong namespace -
    # one level higher ("/org/_orgs") instead of in the cloud namespace.  Therefore, we need to do 2 queries
    # below to capture all Provides relations from the cloud and its zones.
    # TODO  Fix the namespace.
    provides_rels = Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                                  :direction         => 'from',
                                                  :relationShortName => 'Provides'}) +
                    Cms::Relation.all(:params => {:nsPath            => cloud_ns_path(@cloud),
                                                  :recursive         => true,
                                                  :relationShortName => 'Provides'})
    @diff = provides_rels.inject([]) do |m, r|
      service  = r.toCi
      template = locate_cloud_service_template(service)
      diff     = calculate_attr_diff(service, template)
      unless changes_only && diff.blank?
        service.diffCi = template
        service.diffAttributes = diff
        m << service
      end
      m
    end

    respond_to do |format|
      format.js
      format.json {render :json => @diff}
    end
  end


  private

  def find_parents_and_service
    @cloud     = locate_cloud(params[:cloud_id])
    zone_id    = params[:zone_id]
    @zone      = Cms::Ci.locate(zone_id, cloud_ns_path(@cloud)) if zone_id.present?
    service_id = params[:id]
    @service   = Cms::Ci.locate(service_id, @zone ? cloud_zone_ns_path(@zone) : cloud_ns_path(@cloud)) if service_id.present?
  end

  def authorize_write
    unauthorized unless @cloud && has_cloud_services?(@cloud.ciId)
  end

  def load_services
    @services = Cms::Relation.all(:params => {:ciId              => (@zone || @cloud).ciId,
                                              :direction         => 'from',
                                              :relationShortName => 'Provides'})
  end

  def load_available_services
    load_services
    if @zone
      available_classes = Cms::CiMd.all(:params => {:package => 'cloud.zone.service'}).to_map(&:className)
      existing_classes = load_services.to_map {|r| r.toCi.ciClassName}
      @service_classes = Cms::Relation.all(:params => {:ciId              => @cloud.ciId,
                                                       :direction         => 'from',
                                                       :relationShortName => 'Provides'}).inject({}) do |m, r|
        short_class_name = r.toCi.ciClassName.split('.', 2).last
        full_class_name = "cloud.zone.#{short_class_name}"
        if available_classes.include?(full_class_name) && !existing_classes.include?(full_class_name)
          type = r.relationAttributes.service
          m[type] ||= []
          m[type] << [short_class_name, full_class_name]
        end
        m
      end

    else
      # Add public service choices.
      existing_types = @services.to_map {|r| r.relationAttributes.service}
      @service_classes = Cms::Relation.all(:params => {:nsPath            => '/public',
                                                       :relationShortName => 'Provides',
                                                       :fromClassName     => 'mgmt.Cloud',
                                                       :includeToCi       => true,
                                                       :recursive         => true}).inject({}) do |m, r|
        type = r.relationAttributes.service
        unless existing_types.include?(type)
          m[type] ||= []
          m[type] << ["#{r.toCi.ciName} (#{r.toCi.nsPath.split('/')[1..2].join('/')})", r.toCi.ciId]
        end
        m
      end

      # Add organization private service choices.
      Cms::Ci.all(:params => {:nsPath => services_ns_path}).inject(@service_classes)  do |m, s|
        type = s.ciClassName.split('.')[1]
        unless existing_types[type]
          m[type] ||= []
          m[type] << ["#{s.ciName} (#{s.nsPath.split('/')[1]})", s.ciId]
        end
        m
      end
    end

    @service_classes = @service_classes.inject([]) { |a, entry| a << [entry.first, entry.last.sort] }.sort_by(&:first)
  end
end
