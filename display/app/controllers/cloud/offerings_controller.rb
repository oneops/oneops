class Cloud::OfferingsController < ApplicationController
  before_filter :find_parents_and_offering
  before_filter :authorize_write, :only => [:new, :create, :update, :destroy]

  def index
    load_existing_offerings
    respond_to do |format|
      format.js do
        load_available_offerings(@offerings)
        render :action => :index
      end

      format.json { render :json => @offerings }
    end
  end

  def show
    render_json_ci_response(@offering.present?, @offering)
  end

  def new
    load_existing_offerings
    load_available_offerings(@offerings)

    if @available_offerings.blank?
      flash[:error] = 'There are no more offerings to add.'
      render :action => :index
      return
    end

    @mgmt_offering_ci_id = params[:mgmtOfferingCiId].to_i
    if @mgmt_offering_ci_id > 0
      mgmt_ci = Cms::Ci.find(@mgmt_offering_ci_id)
      if mgmt_ci
        ci_hash = (params[:cms_ci].presence || {}).merge(:ciName      => mgmt_ci.ciName,
                                                         :ciClassName => 'cloud.Offering',
                                                         :nsPath      => cloud_service_ns_path(@service))
        ci_hash[:ciAttributes] = mgmt_ci.ciAttributes.attributes if ci_hash[:ciAttributes].blank?
        @offering = Cms::Ci.build(ci_hash)
      else
        @mgmt_offering_ci_id = nil
      end
    end

    respond_to do |format|
      format.js {render :action => :new}
      format.json { render_json_ci_response(true, @offering) }
    end
  end

  def create
    clouds_service_ns_path = cloud_service_ns_path(@service)
    @offering = Cms::Ci.build(params[:cms_ci].merge(:nsPath      => clouds_service_ns_path,
                                                   :ciClassName => 'cloud.Offering'))

    relation = Cms::Relation.build(:relationName       => 'base.Offers',
                                   :fromCiId           => @service.ciId,
                                   :nsPath             => clouds_service_ns_path,
                                   :toCi               => @offering)

    ok = execute_nested(@offering, relation, :save)
    @offering = relation.toCi if ok

    respond_to do |format|
      format.js do
        if ok
          index
        else
          flash[:error] = 'Failed to create offering.'
          new
        end
      end

      format.json { render_json_ci_response(ok, @offering) }
    end
  end

  def edit
    respond_to do |format|
      format.js
      format.json { render_json_ci_response(true, @offering) }
    end
  end

  def update
    ok = execute(@offering, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.js { ok ? index : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @offering) }
    end
  end

  def destroy
    ok = execute(@offering, :destroy)

    respond_to do |format|
      format.js do
        flash[:error] = 'Failed to delete service.' unless ok
        index
      end

      format.json { render_json_ci_response(ok, @offering) }
    end
  end

  def available
    load_available_offerings
    render :json => @mgmt_offerings
  end


  private

  def find_parents_and_offering
    @cloud   = locate_cloud(params[:cloud_id])
    service_id = params[:service_id]
    if service_id.present?
      @service  = Cms::Ci.locate(service_id, cloud_ns_path(@cloud))
      offering  = params[:id]
      @offering = Cms::Ci.locate(offering, cloud_service_ns_path(@service)) if offering.present?
    end
  end

  def authorize_write
    unauthorized unless @cloud && has_cloud_services?(@cloud.ciId)
  end

  def load_existing_offerings
    @offerings = Cms::Relation.all(:params => {:ciId              => @service.ciId,
                                               :direction         => 'from',
                                               :relationShortName => 'Offers'}).map(&:toCi)

  end

  def load_available_offerings(existing_offerings = nil)
    service_template = locate_cloud_service_template(@service)
    @mgmt_offerings = Cms::Relation.all(:params => {:ciId              => service_template.ciId,
                                                    :direction         => 'from',
                                                    :relationShortName => 'Offers'}).map(&:toCi)

    if existing_offerings
      existing_offerings_map = existing_offerings.to_map &:ciName
      @available_offerings   = @mgmt_offerings.reject { |o| existing_offerings_map[o.ciName] }.sort_by(&:ciName)
    end
  end
end
