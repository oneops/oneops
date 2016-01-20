class Catalog::CatalogsController < ApplicationController
  before_filter :find_catalog, :only => [:show, :destroy, :export, :diagram]
  def index
    @public_catalogs  = Cms::Ci.all(:params => {:nsPath => catalogs_ns_path,         :ciClassName => 'account.Design'})
    @private_catalogs = Cms::Ci.all(:params => {:nsPath => private_catalogs_ns_path, :ciClassName => 'account.Design'})
    respond_to do |format|
      format.html { render :action => :index }
      format.json { render(:json => @public_catalogs + @private_catalogs) }
    end
  end

  def show
    respond_to do |format|
      format.html do
        @platforms = Cms::Relation.all(:params => {:ciId              => @catalog.ciId,
                                                   :direction         => 'from',
                                                   :targetClassName   => 'catalog.Platform',
                                                   :relatioShortnName => 'ComposedOf'}).map(&:toCi)
      end
      format.json { render_json_ci_response(@catalog.present?, @catalog) }
    end
  end

  def destroy
    ok = @catalog.nsPath != catalogs_ns_path
    if ok
      ok = execute(@catalog, :destroy)
    else
      message = 'Cannot delete public catalog.'
      flash[:error] = message
      @catalog.errors.add(:base, message)
    end

    respond_to do |format|
      format.html { index }
      format.json { render_json_ci_response(ok, @catalog) }
    end
  end

  def export
    data = Transistor.export_catalog(@catalog.ciId)
    send_data(data.to_yaml, :type => 'text/yaml', :disposition => 'attachment', :filename => "#{data['catalogName']}.yml")
  end

  def import
    data = YAML.load(params[:data].read)
    data['catalogName'] = params[:name]        if params[:name].present?
    data['description'] = params[:description] if params[:description].present?
    ok = Transistor.import_catalog(data)
    if ok
      flash[:success] = 'Successful catalog import.'
    else
      flash[:error] = 'Failed to import catalog.'
    end
    index
  end

  def diagram
    @platforms = Cms::Relation.all(:params => {:ciId              => @catalog.ciId,
                                               :direction         => 'from',
                                               :targetClassName   => 'catalog.Platform',
                                               :relationShortName => 'ComposedOf',
                                               :includeToCi       => true})
    @links_to = Cms::Relation.all(:params => {:nsPath            => catalog_ns_path(@catalog),
                                              :relationShortName => 'LinksTo'})
    graph = platforms_diagram(@platforms,@links_to,catalog_path(@catalog))
    send_data(graphvis_sub_pack_remote_images(graph.output(:svg => String)), :type => 'image/svg+xml', :disposition => 'inline')
  end


  private

  def find_catalog
    id = params[:id]
    if params[:public].present?
      @catalog = public_catalog(id)
    elsif params[:private].present?
      @catalog = private_catalog(id)
    else
      @catalog = public_catalog(id) || private_catalog(id)
    end
  end

  def public_catalog(id)
    Cms::Ci.locate(id, catalogs_ns_path, 'account.Design')
  end

  def private_catalog(id)
    Cms::Ci.locate(id, private_catalogs_ns_path, 'account.Design')
  end
end
