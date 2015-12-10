class SupportController < ReportsController
  skip_before_filter :authenticate_user!, :authenticate_user_from_token, :check_reset_password, :check_eula,
                     :check_username, :check_organization, :set_active_resource_headers
  before_filter :clear_active_resource_headers
  http_basic_authenticate_with :name => Settings.support.user, :password =>  Settings.support.password

  def announcements
    @announcements = MiscDoc.announcements
    @announcements.document = {:notice => nil, :critical => nil} unless @announcements.document
  end

  def update_announcements
    @announcements = MiscDoc.announcements
    if @announcements.update_attribute(:document, params[:document])
      flash[:notice] = 'Updated announcements.'
    else
      flash[:error] = "Could not update announcements: #{doc.error.full_messages.join('; ')}"
    end

    render :action => :announcements
  end

  def compute_report
  end

  def user_signed_in?
    false
  end


  protected

  def is_admin?
    return true
  end

  def search_ns_path
    '/'
  end

  def compute_report_graph_data
    assemblies = Cms::Ci.all(:params => {:nsPath      => '/',
                                         :ciClassName => 'account.Assembly',
                                         :recursive   => true}).inject({}) do |h, a|
      h[a.ciName] = a
      h
    end

    quota_map, cloud_compute_tenant_map = get_quota_map('/')

    bom_computes = Cms::Ci.all(:params => {:nsPath      => '/',
                                           :ciClassName => 'Compute',
                                           :recursive   => true}).inject({}) do |m, c|
      m[c.ciId] = c
      m
    end

    data = Cms::Relation.all(:params => {:nsPath            => '/',
                                         :relationShortName => 'DeployedTo',
                                         :fromClassName     => 'Compute',
                                         :recursive         => true}).inject({}) do |m, r|
      bom_compute = bom_computes[r.fromCiId]
      foo, org, assembly, env, area, platform = bom_compute.nsPath.split('/')
      compute_service_info = cloud_compute_tenant_map[r.toCiId]
      if compute_service_info
        compute = compute_service_info[:compute]
        org_tenant = "#{org}/#{compute_service_info ? compute_service_info[:tenant] : ''}"
      else
        compute = "cloud_#{r.toCiId}"
        org_tenant = org
      end
      m[compute] ||= {}
      m[compute][org_tenant] ||= {}
      m[compute][org_tenant][CONSUMED_LABEL] ||= {}
      m[compute][org_tenant][CONSUMED_LABEL][assembly] ||= LeafNode.new(:metrics => empty_compute_metrics,
                                                                        :url     => assembly.present? && env.present? ? assembly_operations_environment_url(:org_name => org, :assembly_id => assembly, :id => env) : assembly_path(:org_name => org, :id => assembly),
                                                                        :info    => {:owner => assemblies[assembly].ciAttributes.owner})
      aggregate_compute_metrics(m[compute][org_tenant][CONSUMED_LABEL][assembly][:metrics], bom_compute)
      m
    end

    graph_data = graph_node('ALL', data)

    compute_nodes = graph_data[:children]
    quota_map.each_pair do |compute_org_tenant, quota|
      split = compute_org_tenant.split('/')
      compute = split.first
      compute_node = compute_nodes.find {|node| node[:name] == compute}
      unless compute_node
        compute_node = {:name => compute, :children => [], :level => 3, :metrics => empty_compute_metrics, :id => random_node_id}
        compute_nodes << compute_node
      end

      org_tenant = split[1..-1].join('/')
      org_tenant_nodes = compute_node[:children]
      org_tenant_node = org_tenant_nodes.find {|node| node[:name] == org_tenant}
      unless org_tenant_node
        org_tenant_node = {:name => org_tenant, :children => [], :level => 2, :metrics => empty_compute_metrics, :id => random_node_id}
        org_tenant_nodes << org_tenant_node
      end
      insert_quota_data(org_tenant_node, quota)
      aggregate_graph_node(org_tenant_node)
      aggregate_graph_node(compute_node)
    end

    aggregate_graph_node(graph_data)

    scope = ['Service', 'Org/Tenant', 'Allocation', {'Assembly' => ['name', 'owner']}]

    return graph_data, scope
  end
end
