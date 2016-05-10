class Catalog::DesignsController < ApplicationController
  before_filter :find_catalog, :only => [:show, :destroy, :export, :diagram]

  def index
    @public_designs  = Cms::Ci.all(:params => {:nsPath => catalog_designs_ns_path, :ciClassName => 'account.Design'})
    @private_designs = Cms::Ci.all(:params => {:nsPath => private_catalog_designs_ns_path, :ciClassName => 'account.Design'})

    respond_to do |format|
      format.js { render :action => :index }
      format.json { render(:json => @public_designs + @private_designs) }
    end
  end

  def show
    respond_to do |format|
      format.html do
        @platforms = Cms::Relation.all(:params => {:ciId              => @design.ciId,
                                                   :direction         => 'from',
                                                   :targetClassName   => 'catalog.Platform',
                                                   :relatioShortnName => 'ComposedOf'}).map(&:toCi)
      end
      format.json { render_json_ci_response(@design.present?, @design) }
    end
  end

  def destroy
    ok = @design.nsPath != catalog_designs_ns_path
    if ok
      ok = execute(@design, :destroy)
    else
      message = 'Cannot delete public catalog.'
      flash[:error] = message
      @design.errors.add(:base, message)
    end

    respond_to do |format|
      format.js { index }
      format.json { render_json_ci_response(ok, @design) }
    end
  end

  def export
    data = Transistor.export_catalog(@design.ciId)
    send_data(data.to_yaml, :type => 'text/yaml', :disposition => 'attachment', :filename => "#{data['catalogName']}.yml")
  end

  def import
    data = params[:data]
    if data.blank?
      flash[:error] = 'Please provide a catalog design YAML file.'
    else
      data = YAML.load(data.read)

      data['catalogName'] = params[:name] if params[:name].present?
      data['description'] = params[:description] if params[:description].present?

      ok = Transistor.import_catalog(data)
      flash[:error] = 'Failed to import catalog.' unless ok
    end

    redirect_to catalog_path
  end

  def diagram
    @platforms = Cms::Relation.all(:params => {:ciId              => @design.ciId,
                                               :direction         => 'from',
                                               :targetClassName   => 'catalog.Platform',
                                               :relationShortName => 'ComposedOf',
                                               :includeToCi       => true})
    @links_to  = Cms::Relation.all(:params => {:nsPath            => catalog_design_ns_path(@design),
                                               :relationShortName => 'LinksTo'})
    graph      = platforms_diagram(@platforms, @links_to, catalog_design_path(@design))
    send_data(graphvis_sub_pack_remote_images(graph.output(:svg => String)), :type => 'image/svg+xml', :disposition => 'inline')
  end


  private

  def find_catalog
    @design = locate_catalog_design(params[:id])
  end
end
