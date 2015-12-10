class WatchController < ApplicationController
  def create
    @ci     = []
    ns_path = organization_ns_path
    params[:ciIds].each do |id|
      ci = Cms::Ci.locate(id, ns_path)
      @ci << ci
      proxy = find_or_create_proxy(ci)
      current_user.watches << proxy
    end
    render_response
  end

  def destroy
    @ci     = []
    ns_path = organization_ns_path
    params[:ciIds].each do |id|
      ns_path, id = id.split(',') if id.include?(',')
      ci = Cms::Ci.locate(id, ns_path)
      @ci << ci if ci
      proxy = CiProxy.where(:ci_id => id).first || CiProxy.where(:ci_name => id).first
      current_user.watches.delete(proxy) if proxy
    end
    render_response
  end


  private

  def render_response
    respond_to do |format|
      format.js { render :action => :create }
      format.json { render :json => current.user.watches }
    end
  end
end

