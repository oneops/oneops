class DesignController < ApplicationController
  before_filter :find_assembly

  def show
    @release = Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))

    platforms = Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                :direction       => 'from',
                                                :targetClassName => 'catalog.Platform',
                                                :relationName    => 'base.ComposedOf'})
    @platforms = platforms.map(&:toCi)
    @diagram = prepare_platforms_diagram(platforms)

    render :action => :show
  end

  def diagram
    send_data(prepare_platforms_diagram, :type => 'image/svg+xml', :disposition => 'inline')
  end


  private

  def find_assembly
    @assembly = locate_assembly(params[:assembly_id])
  end

  def prepare_platforms_diagram(platforms = nil)
    platforms ||= Cms::DjRelation.all(:params => {:ciId            => @assembly.ciId,
                                                  :direction       => 'from',
                                                  :targetClassName => 'catalog.Platform',
                                                  :relationName    => 'base.ComposedOf',
                                                  :includeToCi     => true})
    links_to = Cms::DjRelation.all(:params => {:nsPath            => [@assembly.nsPath, @assembly.ciName].join('/'),
                                               :relationShortName => 'LinksTo'})
    begin
      return platforms_diagram(platforms, links_to, assembly_design_path(@assembly)).output(:svg => String)
    rescue
      return nil
    end
  end
end
