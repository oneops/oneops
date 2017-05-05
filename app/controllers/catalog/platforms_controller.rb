class Catalog::PlatformsController < Base::PlatformsController
  before_filter :find_parent
  before_filter :find_platform, :except => [:index]

  def index
    @platforms = Cms::Relation.all(:params => {:ciId              => @design.ciId,
                                               :direction         => 'from',
                                               :targetClassName   => 'catalog.Platform',
                                               :relatioShortnName => 'ComposedOf'}).map(&:toCi)
    render :json => @platforms
  end

  def show
    respond_to do |format|
      format.html do
        @components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                      :direction         => 'from',
                                                      :relationShortName => 'Requires',
                                                      :includeToCi       => true,
                                                      :attrProps         => 'owner'}).map(&:toCi)
        @policy_compliance = Cms::Ci.violates_policies(@components, false, true) if Settings.check_policy_compliance
      end

      format.json do
        if @design && @platform
          @platform.links_to = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                               :direction         => 'from',
                                                               :relationShortName => 'LinksTo',
                                                               :includeToCi       => true}).map { |r| r.toCi.ciName }
        end
        render_json_ci_response(true, @platform)
      end
    end
  end


  private

  def find_parent
    design_id = params[:design_id]
    if design_id
      # Catalog design scope.
      @design = locate_catalog_design(design_id)
    else
      # Packs scope.
    end
  end

  def find_platform
    if @design
      # Catalog design scope.
      @platform = locate_catalog_design_platform(params[:id], @design)
    else
      # Packs scope.
      @platform = locate_pack_platform(params[:id], params[:source], params[:pack], params[:version], params[:availability])

      # TODO - this is to address the inconsistency with capitalization in the "pack" attribute of "mgmt.*.Platform"
      @platform.ciAttributes.pack = @platform.ciName unless @platform.ciAttributes.pack == @platform.ciName

      @pack = locate_pack_for_platform(@platform)
    end
  end
end
