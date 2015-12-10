class Services::ServicesController < ApplicationController
  before_filter :find_service

  def index
    @services = Cms::Ci.all(:params => {:nsPath => services_ns_path})

    respond_to do |format|
      format.html do
        load_service_classes
      end

      format.json { render :json => @services }
    end
  end

  def show
    render_json_ci_response(@service.present?, @service)
  end

  def new
    load_service_classes

    @service_class_name = params[:service_class_name]
    service_class = @service_class_name.present? && Cms::CiMd.find(@service_class_name)
    if service_class
      ci_hash               = params[:cms_ci].presence || {}
      ci_hash[:ciName]      = service_class.className.split('.').last.downcase
      ci_hash[:ciClassName] = service_class.className
      ci_hash[:nsPath]      = services_ns_path
      @service              = Cms::Ci.build(ci_hash)
    else
      @service_class_name = nil
    end

    respond_to do |format|
      format.html
      format.js
      format.json { render_json_ci_response(true, @service) }
    end
  end

  def create
    Cms::Namespace.create(:nsPath => services_ns_path) unless Cms::Namespace.first(:params => {:nsPath => services_ns_path})
    @service = Cms::Ci.build(params[:cms_ci].merge(:nsPath => services_ns_path))
    @service_class_name = @service.ciClassName
    service_class = @service_class_name.present? && Cms::CiMd.find(@service_class_name)
    ok = service_class
    if ok
      ok = execute(@service, :save)
    else
      @service_class_name = nil
      @service.errors.add(:base, 'Unknown service template.')
    end

    respond_to do |format|
      format.html do
        if ok
          redirect_to(services_path)
        else
          flash.now[:error] = 'Failed to create service.'
          load_service_classes
          render :action => :new
        end
      end
      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def edit
    respond_to do |format|
      format.html
      format.json { render_json_ci_response(true, @service) }
    end
  end

  def update
    ok = execute(@service, :update_attributes, params[:cms_ci])

    respond_to do |format|
      format.html { ok ? redirect_to(services_path) : render(:action => :edit) }
      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def destroy
    ok = execute(@service, :destroy)

    respond_to do |format|
      format.html do
        flash.now[:error] = 'Failed to delete service.' unless ok
        redirect_to(services_path)
      end
      format.json { render_json_ci_response(ok, @service) }
    end
  end

  def available
    render :json => Cms::CiMd.all(:params => {:package => 'service'})
  end


  private

  def find_service
    service_id = params[:id]
    @service = Cms::Ci.locate(service_id, services_ns_path) if service_id.present?
  end

  def load_service_classes
    @service_classes = Cms::CiMd.all(:params => {:package => 'service'}).inject({}) do |m, service|
      split = service.className.split('.')
      type = split[1]
      m[type] ||= []
      m[type] << [split.last, service.className]
      m
    end
    @service_classes = @service_classes.inject([]) { |a, entry| a << [entry.first, entry.last.sort] }.sort_by(&:first)
  end
end
