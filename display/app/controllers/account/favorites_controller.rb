class Account::FavoritesController < ApplicationController
  skip_before_filter :check_organization, :only => [:index]
  def index
    @favorites = current_user.favorites.includes(:organization).order('organizations.name, ci_name').all.
      group_by {|p| p.organization}.inject([]) do |a, (org, favs)|
      a << {:id => org.id, :organization => org.name, :items => favs}
    end
    @favorites = @favorites.sort_by {|g| g[:organization]}

    respond_to do |format|
      format.js   { render :action => :index }
      format.json { render :json => @favorites }
    end
  end

  def show
    ci = nil
    proxy = current_user.favorites.where(:ci_id => params[:id].to_i).first
    if proxy
      begin
        ci = Cms::DjCi.locate(params[:id], proxy.ns_path)
      rescue
      end
      unless ci
        current_user.favorite_ids = current_user.favorite_ids.reject { |p_id| p_id == proxy.id }
        proxy.destroy
      end
    end

    respond_to do |format|
      format.html do
        if ci
          redirect_to path_to_ci(ci)
        else
          redirect_to account_profile_path(:anchor => 'favorites'), :alert => 'Could not find favorite.'
        end
      end

      format.json { render_json_ci_response(ok, ci) }
    end
  end

  def create
    ci = Cms::DjCi.locate(params[:ciId], organization_ns_path)
    if ci
      proxy = find_or_create_proxy(ci)
      current_user.favorites << proxy unless current_user.favorite_ids.include?(proxy.id)
    end
    respond_to do |format|
      format.js { render :action => :create }
      format.json { render :json => current.user.favorites }
    end
  end

  def destroy
    ci_id = params[:id]
    proxy = current_user.favorites.where(:ci_id => params[:id].to_i).first
    if proxy
      current_user.favorite_ids = current_user.favorite_ids.reject {|p_id| p_id == proxy.id}
      # Clean up proxies if ci is gone.
      begin
        Cms::DjCi.locate(ci_id, proxy.ns_path)
      rescue Cms::Ci::NotFoundException => e
        proxy.destroy
      end
    end
    respond_to do |format|
      format.js { params[:index].present? ? index : render(:action => :create) }
      format.json { render :json => current.user.favorites }
    end
  end
end

