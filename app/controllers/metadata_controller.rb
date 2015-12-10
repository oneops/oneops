class MetadataController < ApplicationController
  skip_before_filter :authenticate_user!, :authenticate_user_from_token, :check_reset_password, :check_eula, :check_organization, :set_active_resource_headers
  before_filter :clear_active_resource_headers

  def show
    md = nil
    begin
      name = params[:class_name]
      if name.present?
        md = Cms::CiMd.look_up(name)
      else
        name = params[:relation_name]
        md   = Cms::RelationMd.look_up(name) if name.present?
      end
    rescue Exception => e
    end

    render :json => md
  end

  def lookup_class_name
    package = "#{params[:package]}."
    render :json => Cms::CiMd.look_up.map(&:className).select { |n| n.starts_with?(package) }
  end

  def lookup_attr_name
    class_name = params[:class_name]
    if class_name.present?
      begin
        md = Cms::CiMd.look_up(class_name)
      rescue Exception => e
        md = nil
      end
      render :json => md ? md.attributes[:mdAttributes].map(&:attributeName) : []
    else
      begin
        md = %w(account cloud catalog manifest bom).inject([]) { |m, pkg| m + Cms::CiMd.look_up(pkg) }
      rescue Exception => e
        md = nil
      end
      if md.present?
        x = md.to_map_with_value { |c| [c.className, c.attributes[:mdAttributes].map(&:attributeName)] }
        render :json => x
      else
        render :json => {}
      end
    end
  end
end
