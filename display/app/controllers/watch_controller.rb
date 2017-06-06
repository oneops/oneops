class WatchController < ApplicationController
  def show
    @watches = current_user.watches.includes(:organization).order('organizations.name, ci_name').all.
      group_by {|w| w.organization}.inject([]) do |a, (org, watches)|
      a << {:id => org.id, :organization => org.name, :items => watches}
    end

    respond_to do |format|
      format.html { render 'account/profile/_watching'}
      format.js   { render :action => :show }
      format.json { render :json => @watches }
    end
  end

  def create
    ci_id = params[:ciId]
    (ci_id.present? ? [ci_id] : params[:ciIds]).each do |id|
      ci = Cms::Ci.find(id)
      if ci
        proxy = find_or_create_proxy(ci)
        current_user.watches << proxy
        @ci = ci if ci_id.present?
      end
    end

    render_response
  end

  def destroy
    ci_id = params[:ciId]
    if ci_id.present?
      @ci = Cms::Ci.find(ci_id)
      ci_ids = @ci ? [ci_id] : []
    else
      ci_ids = params[:ciIds]
    end

    ci_ids.each do |id|
      proxy = CiProxy.where(:ci_id => id).first
      current_user.watches.delete(proxy) if proxy
    end

    render_response
  end


  private

  def render_response
    respond_to do |format|
      format.js { params[:index].present? ? show : render(:action => :create) }
      format.json { render :json => current_user.watches }
    end
  end
end

