module ApplicationHelper
  SITE_ICONS = {:organization           => 'sitemap',
                :home                   => 'home',
                :dashboard              => 'dashboard',
                :service                => 'cog',
                :catalog                => 'tags',
                :pack                   => 'archive',
                :cloud                  => 'cloud',
                :assembly               => 'cogs',
                :settings               => 'sliders',
                :user                   => 'user',
                :group                  => 'group',
                :announcement           => 'bullhorn',
                :manages_access         => 'user-secret',
                :org_scope              => 'sitemap',
                :design                 => 'puzzle-piece',
                :transition             => 'play-circle-o',
                :operations             => 'signal',
                :environment            => 'cubes',
                :single_availability    => 'cube',
                :redundant_availability => 'cubes',
                :platform               => 'archive',
                :cloud_services         => 'cog',
                :cloud_compliance       => 'briefcase',
                :cloud_support          => 'medkit',
                :cost                   => 'money',
                :export                 => 'download',
                :import                 => 'upload',
                :timeline               => 'clock-o',
                :history                => 'history',
                :release                => 'tag',
                :deployment             => 'cloud-upload',
                :capacity               => 'server',
                :compute                => 'server',
                :support                => 'medkit',
                :search                 => 'search',
                :favorite               => 'bookmark',
                :json                   => 'file-code-o',
                :yaml                   => 'file-text-o',
                :csv                    => 'file-excel-o'}

  GENERAL_SITE_LINKS = [{:label => 'Get help',         :icon => 'comments',  :url => Settings.support_chat_url},
                        {:label => 'Report a problem', :icon => 'bug',       :url => Settings.report_problem_url},
                        {:label => 'Feedback',         :icon => 'comment-o', :url => Settings.feedback_url},
                        {:label => 'Documentation',    :icon => 'book',      :url => Settings.help_url},
                        {:label => 'Release notes',    :icon => 'rss',       :url => Settings.news_url}]

  OPS_HEALTH_LEGEND = [{:name => 'good', :color => '#468847'},
                       {:name => 'notify', :color => '#3a87ad'},
                       {:name => 'unhealthy', :color => '#b94a48'},
                       {:name => 'overutilized', :color => '#f89406'},
                       {:name => 'underutilized', :color => '#800080'},
                       {:name => 'unknown', :color => '#999999'}]

  OPS_HEALTH_LEGEND_MAP = OPS_HEALTH_LEGEND.to_map_with_value {|x| [x[:name], x[:color]]}

  def omniauth_services
    omniauth = Settings.omniauth
    return '' unless omniauth
    services = omniauth.keys.sort.inject([]) {|s, p| s << link_to(p, "/auth/#{p}", :title => p.capitalize)}
    return raw(services.join(' '))
  end

  def title(page_title)
    content_for(:title) { content_tag(:div, page_title.html_safe, :id => "title_text") }
    content_for(:title_clean) {"#{page_title.gsub( /<.+?>/, '')} - OneOps"}
  end

  def organization_home
    current_user.organization ? content_tag(:div, link_to(current_user.organization.name, organization_path).html_safe, :class => "title_text" ) : ''
  end

  def app_nav(items)
    html = '<ul>'
    items.each do |item|
      selected = item[:selected] ? 'selected' : ''
      label = item[:label]
      icon = item[:icon]
      caption = icon.blank? ? label : icon(icon, label)
      html << content_tag(:li, (item[:link] ? link_to(caption, item[:link]) : caption), :class => "#{selected}")
    end
    html << '</ul>'
    content_for(:app_nav) { html.html_safe }
  end

  def organization_title
    content_for(:title) { organization_home }
    content_for(:title_clean) {"#{current_user.organization ? current_user.organization.name : ''} | OneOps" }
  end

  def root_page_header(selected = nil)
    return unless user_signed_in?
    content_for(:title) { content_tag(:div, link_to(current_user.username, root_path).html_safe, :class => 'title_text' ) }
    content_for(:title_clean) {'OneOps' }

    menu_items = [{:label => 'profile',   :link => account_profile_path}]
    if selected
      selected_item = menu_items.detect { |i| i[:label] == selected }
      selected_item[:selected] = true if selected_item
    end
    app_nav(menu_items)
  end

  def support_page_header(page_icon, page_label)
    title('support')
    app_nav([{:label => 'support', :icon => site_icon(:support), :link => support_path}])
    breadcrumb([{:label => icon(site_icon(:support), 'support'), :link => support_path}])
    page_title(:page_icon => page_icon, :page_kind => page_label)
  end

  def organization_page_header(selected = nil)
    return unless user_signed_in? && current_user.organization

    organization_title
    menu_items = []
    menu_items << {:label => 'services', :icon => site_icon(:service), :link => services_path} if current_user.organization.services
    menu_items << {:label => 'catalogs', :icon => site_icon(:catalog), :link => catalog_path} if current_user.organization.catalogs

    if current_user.organization.assemblies
      menu_items << {:label => 'clouds', :icon => site_icon(:cloud), :link => clouds_path}
      menu_items << {:label => 'assemblies', :icon => site_icon(:assembly), :link => assemblies_path}
    end

    menu_items << {:icon => site_icon(:settings), :link => edit_organization_path, :selected => selected == 'settings'}

    if selected
      selected_item = menu_items.detect { |i| i[:label] == selected}
      selected_item[:selected] = true if selected_item
    end

    if @design
      ci = @component || @platform
      begin
        catalog_design_nav(@design, ci)
      rescue Exception => e
        Rails.logger.warn "Failed to generate context nav: #{e}.\nDesign: #{@design.inspect}\nCI: #{ci.inspect if ci}"
      end
    elsif @platform
      begin
        catalog_pack_nav(@platform)
      rescue Exception => e
        Rails.logger.warn "Failed to generate context nav: #{e}.\nPack: #{@platform.inspect}\nCI: #{@component.inspect if @component}"
      end
    end

    app_nav(menu_items)
  end

  def catalog_design_nav(design, ci)
      nav = %(<li class="title">#{link_to(icon(site_icon(:design), "&nbsp;#{context_nav_name_label(design.ciName)}"), catalog_design_path(design))}</li>)
      if ci
        unless ci.ciClassName.end_with?('.Platform')
          nav << '<li class="divider small"></li>'
          nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{context_nav_name_label(@platform.ciName)} platform"), catalog_design_platform_path(@design, @platform))}</li>)
          # more_link = link_to(icon('', 'more...'),
          #                     counterparts_lookup_path(:ci => ci.attributes.slice(:ciId, :nsPath, :ciClassName, :ciName)),
          #                     :remote => true)
          # nav << %(<li class='indent minor more'>#{more_link}</li>)
        end
      end

      content_for(:context_nav, raw(nav))
  end

  def catalog_pack_nav(platform)
    scope = platform.ciClassName.end_with?('catalog.Platform') ?  'design' : platform.nsPath.split('/').last
    nav = %(<li class="title">#{link_to(icon(site_icon(:pack), "&nbsp;#{context_nav_name_label(platform.ciName)} #{content_tag(:sub, icon(site_icon("#{scope}_availability"), scope))}"), catalog_pack_platform_path(:platform_id => platform))}</li>)
    if scope == 'design'
      nav << %(<li class="indent">#{link_to(icon(site_icon(:single_availability), "#{context_nav_name_label('single')} availability"), catalog_pack_platform_path(:id => platform.ciName, :availability => 'single'))}</li>)
      nav << %(<li class="indent">#{link_to(icon(site_icon(:redundant_availability), "#{context_nav_name_label('redundant')} availability"), catalog_pack_platform_path(:id => platform.ciName, :availability => 'redundant'))}</li>)
    elsif scope == 'single'
      nav << %(<li class="indent">#{link_to(icon(site_icon(:design), "#{context_nav_name_label('design')}"), catalog_pack_platform_path(:id => platform.ciName, :availability => nil))}</li>)
      nav << %(<li class="indent">#{link_to(icon(site_icon(:redundant_availability), "#{context_nav_name_label('redundant')} availability"), catalog_pack_platform_path(:id => platform.ciName, :availability => 'redundant'))}</li>)
    elsif scope == 'redundant'
      nav << %(<li class="indent">#{link_to(icon(site_icon(:design), "#{context_nav_name_label('design')}"), catalog_pack_platform_path(:id => platform.ciName, :availability => nil))}</li>)
      nav << %(<li class="indent">#{link_to(icon(site_icon(:single_availability), "#{context_nav_name_label('single')} availability"), catalog_pack_platform_path(:id => platform.ciName, :availability => 'single'))}</li>)
    end

    content_for(:context_nav, raw(nav))
  end

  def assembly_title(assembly)
    content = organization_home
    content << content_tag(:div, ' / ', :class => 'title_text')
    content << content_tag(:div, link_to('assemblies', assemblies_path).html_safe, :class => 'title_text')
    content << content_tag(:div, ' / ', :class => 'title_text')
    content << content_tag(:div, link_to(assembly.ciName, assembly_path(assembly)).html_safe, :class => 'title_text')

    content_for(:title) { content.html_safe }
    content_for(:title_clean) {  "#{assembly.ciName} - #{current_user.organization.name} | OneOps" }
  end

  def assembly_page_header(assembly, selected = nil)
    assembly_title(assembly)

    ci = [@component, @platform, @environment].find {|c| c && c.persisted?}

    dto_links = [{:label => 'design',     :icon => site_icon(:design),     :url => assembly_design_path(assembly)},
                 {:label => 'transition', :icon => site_icon(:transition), :url => assembly_transition_path(assembly)},
                 {:label => 'operations', :icon => site_icon(:operations), :url => assembly_operations_path(assembly)}]

    html = '<ul>'
    if ci
      %w(design transition operations).each do |area|
        html << "<li class='#{area} #{'selected' if area == selected}'>"
        html << link_to(icon(site_icon(area.to_sym), area),
                        counterparts_lookup_path(:ci => ci.attributes.slice(:ciId, :nsPath, :ciClassName, :ciName),
                                                 :dto_area => selected),
                        :remote       => true,
                        :class        => 'dropdown-toggle',
                        'data-toggle' => 'dropdown')
        html << '<ul class="dropdown-menu"><li style="text-align:center"><a href="/"><i class="fa-spinner fa-spin"></i></a></li></ul>'
        html << '</li>'
      end
    else
      dto_links.each do |item|
        label = item[:label]
        html << content_tag(:li, link_to(icon(item[:icon], label), item[:url]), :class => label == selected ? 'selected' : '')
      end
    end
    html << '</ul>'
    content_for(:app_nav, html.html_safe)

    begin
      assembly_nav(assembly, ci, dto_links, selected)
    rescue Exception => e
      Rails.logger.warn "Failed to generate context nav: #{e}.\nAssembly: #{assembly.inspect}\nCI: #{ci.inspect if ci}"
    end
  end

  def assembly_nav(assembly, ci, dto_links, current_dto)
    nav = %(<li class="title">#{link_to(icon(site_icon(:assembly), "&nbsp;#{context_nav_name_label(assembly.ciName)}"), assembly_path(assembly))}</li>)
    nav << %(<li class="divider small"></li>)
    if ci
      ci_class_name = ci.ciClassName
      dto_links.each do |l|
        no_more = false
        dto_area = l[:label]
        nav << %(<li class="major #{'highlight' if dto_area == current_dto}">#{link_to(icon(site_icon(dto_area), dto_area), l[:url])}</li>)
        if ci_class_name == 'manifest.Environment'
          if dto_area == 'design'
            no_more = true
          elsif dto_area != current_dto
            nav << %(<li class="indent">#{link_to(icon("arrow-circle-#{current_dto == 'operations' ? 'left' : 'right'}", "#{context_nav_name_label(ci.ciName)} environment"), path_to_ci(ci, dto_area))}</li>)
          end
        elsif ci_class_name.end_with?('.Platform')
          unless current_dto == 'design'
            if dto_area == 'design'
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-left', "#{context_nav_name_label(ci.ciName)} platform"), path_to_ci(ci, dto_area))}</li>)
            else
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{context_nav_name_label(@environment.ciName)} environment"), path_to_ci(@environment, dto_area))}</li>) if @environment
              nav << %(<li class="indent">#{link_to(icon("arrow-circle-#{current_dto == 'operations' ? 'left' : 'right'}", "#{assembly_nav_platform_label(ci)} platform"), path_to_ci(ci, dto_area))}</li>) unless dto_area == current_dto
            end
          end
        else
          if current_dto == 'design'
            nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{context_nav_name_label(@platform.ciName)} platform"), path_to_ci(@platform, dto_area))}</li>) if @platform && dto_area == 'design'
          else
            if dto_area == 'design'
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{context_nav_name_label(@platform.ciName)} platform"), path_to_ci(@platform, dto_area))}</li>) if @platform
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-left', "#{context_nav_name_label(ci.ciName)} component"), path_to_ci(ci, dto_area))}</li>)
            else
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{context_nav_name_label(@environment.ciName)} environment"), path_to_ci(@environment, dto_area))}</li>) if @environment
              nav << %(<li class="indent">#{link_to(icon('arrow-circle-up', "#{assembly_nav_platform_label(@platform)} platform"), path_to_ci(@platform, dto_area))}</li>) if @platform
              nav << %(<li class="indent">#{link_to(icon("arrow-circle-#{current_dto == 'operations' ? 'left' : 'right'}", "#{context_nav_name_label(ci.ciName)} component"), path_to_ci(ci, dto_area))}</li>) unless dto_area == current_dto
            end
          end
        end
        unless no_more
          more_link = link_to(icon('plus-square-o', 'more...'),
                              counterparts_lookup_path(:ci => ci.attributes.slice(:ciId, :nsPath, :ciClassName, :ciName), :dto_area => current_dto),
                              :remote => true)
          nav << %(<li class='indent minor #{dto_area} more'>#{more_link}</li>)
        end
      end
    else
      dto_links.each do |l|
        dto_area = l[:label]
        nav << %(<li class="#{'highlight' if dto_area == current_dto}">#{link_to(icon(site_icon(dto_area), dto_area), l[:url])}</li>)
      end
    end

    content_for(:context_nav, raw(nav))
  end

  def assembly_nav_platform_label(platform)
    "#{context_nav_name_label(platform.ciName)} <small class=\"muted\">ver. #{platform.ciAttributes.major_version}</small>"
  end

  def context_nav_name_label(name)
    "<span class='name'>#{name}</span>"
  end

  def app_subnav(items)
      html = '<ul>'
      items.each do |item|
        selected = item[:selected] ? 'selected' : ''
        html << '<li>'
        if item[:link]
          html << link_to(item[:label], item[:link], :class => selected)
        else
          html << link_to_function(item[:label], item[:function], :class => selected)
        end
        html << '</li>'
      end
      html << '</ul>'
      content_for(:app_subnav) { html.html_safe }
  end

  def breadcrumb(items)
      html = '<ul class="rounded">'
      items.each do |item|
        html << '<li>'
        block = ''

        icon_name = item[:icon]
        kind      = item[:kind]
        if icon_name.present?
          icon = site_icon!(icon_name)
          if icon.present?
            block << content_tag(:div, icon(icon), :class => 'breadcrumb_image')
          else
            block << content_tag(:div, image_tag(icon_name), :class => 'breadcrumb_image')
          end
        end
        block << content_tag(:div, sanitize(kind), :class => 'item_kind') if kind
        block << content_tag(:div, sanitize(item[:label]), :class => 'item_label')
        html << content_tag(:div, (item[:link] ? link_to(sanitize(block), item[:link]) : block.html_safe), :class => 'breadcrumb_text')
        html << content_tag(:div, icon('angle-right'), :class => 'breadcrumb_separator_text')
        html << '</li>'
      end
      html << '</ul>'
      content_for(:breadcrumb) { html.html_safe }
  end

  def page_title(options)
    html = ''
    page_icon = options[:page_icon]
    page_kind = options[:page_kind]
    icon_lookup = page_icon.presence || page_kind
    icon_name = icon_lookup.present? && (site_icon!(icon_lookup.downcase) || site_icon!(icon_lookup.downcase.singularize))
    if icon_name
      html << icon(icon_name) if icon_name
    else
      html = image_tag(page_icon)
    end

    ci_id = params[:id]
    block = ''
    block << content_tag(:span, sanitize(page_kind), :class => 'page_kind') if page_kind
    doc_link = options[:doc_link]
    if options[:page_label]
      if options[:page_label_url]
        block << link_to(raw("#{sanitize(options[:page_label])}"), options[:page_label_url], :class => 'page_label')
      else
        block << content_tag(:span, raw("#{sanitize(options[:page_label])}"), :class => 'page_label')
      end
      block << favorite_marker(ci_id.to_i) if ci_id.present? && /(\/assemblies\/)|(\/clouds\/)/ =~ request.url
      block << doc_link if doc_link.present?
    end
    block << content_tag(:span, sanitize(options[:page_sublabel]), :class => 'page_sublabel') if options[:page_sublabel]
    html << content_tag(:div, raw(block), :id => 'page_title_text')

    html << content_tag(:ul, raw(options[:page_options].join(' ')), :id => 'page_title_options') if options[:page_options]
    content_for(:page_title, raw(html))
  end

  def favorite_marker(ci_id)
    link_to_function(content_tag(:i, '', :class => "fa fa-bookmark#{'-o' unless current_user.favorite(ci_id)}", :title => 'Mark/remove favorite'),
                     "toggleFavorite(this, '#{ci_id }')",
                     :class => 'favorite')
  end

  def page_info(info = nil, &block)
    content_for(:page_info, sanitize(info) || (block_given? ? capture(&block) : ''))
  end

  def error_messages_for(model)
    errors = model.errors
    html = %(<div class="alert alert-danger error-messages #{'hide' if errors.blank?}">)
    html << 'Please correct the following errors:'
    html << '<ul>'
    errors.full_messages.each {|m| html << "<li>#{html_escape(m)}</li>"}
    html << '</ul>'
    html << '</div>'
    html << '<script>$j(".error-messages")[0].scrollIntoView(false)</script>' if errors.present?
    raw(html)
  end

  def note(options)
     header = content_tag(:div, options[:severity], :class => 'header')
     text = content_tag(:div, options[:text], :class => 'text')
     content_tag :div, header + text, :class => "note #{options[:severity].downcase}"
  end

  def section_panel(title, options = {}, &block)
    options.reverse_merge!(:title => title, :menu => nil)
    defaults = {:width => 'double', :position => 'left'}
    options = defaults.merge(options)
    options.merge!(:body => capture(&block)) if block_given?
    raw(%(#{render(:partial => 'base/shared/section_panel', :locals => options)}))
  end

  def list_simple(collection, options = {}, &block)
    options[:toolbar] = nil if collection.blank?
    options.reverse_merge!({:item_partial => 'base/shared/list_simple_item', :toolbar => {:sort_by => [], :filter_by => %w(id)}})
    render(:partial => 'base/shared/list', :locals => {:list_content => ListItemBuilder.build_list_item_content(collection, self, options, &block), :options => options})
  end

  def list(collection, options = {}, &block)
    options[:toolbar] = nil if collection.blank?
    options.reverse_merge!({:item_partial => 'base/shared/list_item', :toolbar => {:sort_by => [], :filter_by => %w(id)}})
    render(:partial => 'base/shared/list', :locals => {:list_content => ListItemBuilder.build_list_item_content(collection, self, options, &block), :options => options})
  end

  def ci_list(ci_collection, options = {}, &block)
    options[:toolbar] = nil if ci_collection.blank?
    options.reverse_merge!({:item_partial => 'base/shared/ci_list_item', :toolbar => {:sort_by => [%w(Name ciName), %w(Created created), %w(Updated updated)], :filter_by => %w(ciName ciId)}})
    render(:partial => 'base/shared/list', :locals => {:list_content => ListItemBuilder.build_list_item_content(ci_collection, self, options, &block), :options => options})
  end

  def relation_list(relation_collection, options = {}, &block)
    options.reverse_merge!({:item_partial => 'base/shared/relation_list_item', :class => 'list-relation', :toolbar => nil})
    render(:partial => 'base/shared/list', :locals => {:list_content => ListItemBuilder.build_list_item_content(relation_collection, self, options, &block), :options => options})
  end

  def release_list(release_collection, options = {}, &block)
    options[:toolbar] = nil if release_collection.blank? && options[:paginate].blank?
    options.reverse_merge!({:class   => 'list-release',
                            :toolbar => {:list_name => 'release_list',
                                         :sort_by   => [['Release ID', 'releaseId'], %w(Created created), %w(User createdBy)],
                                         :filter_by => %w(releaseId description createdBy)}})
    render(:partial => 'base/shared/list', :locals => {:list_content => render_release_list_content(release_collection, options, &block), :options => options})
  end

  def render_release_list_content(release_collection, options = {}, &block)
    raw ListItemBuilder.build_list_item_content(release_collection, self, options.merge(:item_partial => 'base/shared/release_list_item'), &block)
  end

  def deployment_list(deployment_collection, options = {}, &block)
    options[:toolbar] = nil if deployment_collection.blank? && options[:paginate].blank?
    options.reverse_merge!({:class   => 'list-deployment',
                            :toolbar => {:list_name => 'deployment_list',
                                         :sort_by   => [['Deployment ID', 'deploymentId'], %w(Created created), %w(User createdBy), ['Deployment State', 'deploymentState']],
                                         :filter_by => %w(deploymentId createdBy deploymentState)}})
    render(:partial => 'base/shared/list',
           :locals  => {:list_content => render_deployment_list_content(deployment_collection, options, &block), :options => options})
  end

  def render_deployment_list_content(deployment_collection, options = {}, &block)
    raw ListItemBuilder.build_list_item_content(deployment_collection, self, options.merge(:item_partial => 'base/shared/deployment_list_item'), &block)
  end

  def timeline_list(timeline_collection, options = {}, &block)
    options[:toolbar]   = nil if timeline_collection.blank? && options[:paginate].blank?
    transition = request.url.include?('/transition')
    options.reverse_merge!({:class   => 'list-timeline',
                            :toolbar => (options[:toolbar] || {}).reverse_merge!({:list_name     => 'timeline_list',
                                                                                  :sort_by       => [%w(Created created)],
                                                                                  :filter_by     => %w(),
                                                                                  :quick_filters => [{:label => 'All',         :value => '', :selected => transition},
                                                                                                     {:label => 'Releases',    :value => 'type=release'},
                                                                                                     {:label => 'Deployments', :value => 'type=deployment', :selected => !transition}]})})
    render(:partial => 'base/shared/list',
           :locals  => {:list_content => render_timeline_list_content(timeline_collection, options, &block), :options => options})
  end

  def render_timeline_list_content(timeline_collection, options = {}, &block)
    raw ListItemBuilder.build_list_item_content(timeline_collection, self, options.merge(:item_partial => 'base/shared/timeline_list_item'), &block)
  end

  def notification_list(notification_collection, options = {}, &block)
    if notification_collection
      options[:toolbar] = nil if notification_collection.blank? && options[:paginate].blank?
      options.reverse_merge!({:class   => 'list-notification',
                              :toolbar => {:sort_by   => [%w(Time timestamp), %w(Source source), %w(Severity severity)],
                                           :filter_by => %w(date severity source subject text),
                                           :compact   => true}})
      render(:partial => 'base/shared/list',
             :locals => {:list_content => render_notification_list_content(notification_collection, options, &block), :options => options})
    else
      falied_loading_indicator('Failed to load notification_collection, please try again later.</p>')
    end
  end

  def render_notification_list_content(notification_collection, options = {}, &block)
    raw ListItemBuilder.build_list_item_content(notification_collection, self, options.merge(:item_partial => 'base/shared/notification_list_item'), &block)
  end

  def notification_callback(data)
    ns_path = data['nsPath']
    source  = data['source']
    case source
      when 'procedure', 'opamp', 'ops'
        link_to("#{ns_path}/#{data['cmsId']}", redirect_ci_url(:only_path => false, :id => data['cmsId']))
      else
        link_to(ns_path.sub(/\/((bom)|(manifest))(?=(\/|$))/, ''), redirect_ns_url(:only_path => false, :params => {:path => ns_path}))
    end
  end

  class ListItemBuilder < Hash
    attr_accessor :item

    def initialize(template, options = {})
      @template = template
      @options  = options
    end

    def method_missing(symbol, *args, &block)
      self[symbol] = block_given? ? @template.capture(@item, &block) : args[0]
    end

    def self.build_list_item_content(item_collection, template, options, group = nil, &block)
      partial           = options[:item_partial]
      list_item_builder = ListItemBuilder.new(template, options)
      locals            = {:group        => group,
                           :builder      => list_item_builder,
                           :collapse     => options[:collapse],
                           :multi_select => options[:menu].present?}
      item_collection.inject('') do |content, item|
        list_item_builder.item = item
        template.capture list_item_builder, item, &block if block_given?
        locals[:item] = item
        content << template.render(:partial => partial, :locals => locals)
      end
    end
  end

  def grouped_ci_list(groups, options = {}, &block)
    options[:toolbar] = nil if groups.blank?
    options.reverse_merge!({:item_partial => 'base/shared/ci_list_item', :toolbar => {:filter_by => %w(ciName)}, :collapse => false})

    list_content = groups.inject('') do |content, group|
      list_group_builder = ListGroupBuilder.new(group, self, options)
      capture list_group_builder, group, &block
      content << render(:partial => 'base/shared/list_group', :locals => {:group => group, :builder => list_group_builder})
    end

    render(:partial => 'base/shared/list', :locals => {:list_content => list_content, :options => options})
  end

  def grouped_list(groups, options = {}, &block)
    options[:toolbar] = nil if groups.blank?
    options.reverse_merge!({:item_partial => 'base/shared/list_item', :toolbar => {:sort_by => [], :filter_by => %w(id)}, :collapse => false})

    list_content = groups.inject('') do |content, group|
      list_group_builder = ListGroupBuilder.new(group, self, options)
      capture list_group_builder, group, &block
      content << render(:partial => 'base/shared/list_group', :locals => {:group => group, :builder => list_group_builder})
    end

    render(:partial => 'base/shared/list', :locals => {:list_content => list_content, :options => options})
  end

  class ListGroupBuilder < ListItemBuilder
    def items(item_collection, &block)
      self[:group_content] = self.class.build_list_item_content(item_collection, @template, @options, @item, &block)
    end
  end

  def list_paginate_update(list_id, data, template)
    info        = data ? data.info : {}
    next_offset = info[:next_offset]
    content     = escape_javascript(render(template))
    raw("list_paginate_update($j('##{list_id}'), \"#{content}\", #{info[:total] || -1}, #{data.size}, #{info[:offset] || 0}#{",\"#{next_offset}\"" if next_offset})")
  end

  def link_confirm_busy(link_text, options)
    modal_id = "modal_#{random_dom_id}"
    dialog_options = options.extract!(:confirm, :busy, :url, :method, :remote, :with, :comment, :body).merge(:modal_id => modal_id)
    link_to_function(link_text, %(render_modal("#{modal_id}", "#{escape_javascript(render('base/shared/confirm_busy_block', dialog_options))}")), options)
  end

  def link_busy(link_text, options)
    call_options = options.extract!(:with, :url, :method)
    #call = remote_function(:url => call_options[:url], :method => call_options[:method].presence || :get, :with => call_options[:with])
    call = %($j.ajax("#{call_options[:url]}", {type: "#{(call_options[:method].presence || :get).to_s.upcase}", data: #{call_options[:with] || "''"} + "&authenticity_token=" + encodeURIComponent($j("meta[name=csrf-token]").attr("content"))}))

    message = options[:message].presence || options[:busy]
    validation = options.delete(:validation) || 'true'
    link_to_function(link_text, %(#{"if (!(#{validation})) return;" if validation.present?} show_busy(#{"'#{escape_javascript(message)}'" if message.present?}); #{call}), options)
  end

  def truncate(text, length = 30, truncate_string = '...')
    if text
      l = length - truncate_string.length
      (text.length > length ? text[0...l] + truncate_string : text).to_s
    end
  end

  def action_to_label(action)
    case action
    when 'add'
      'label-success'
    when 'update'
      'label-warning'
    when 'replace'
      'label-success'
    when 'delete'
      'label-important'
    else
      ''
    end
  end

  def action_to_text(action)
    case action
    when 'add'
      'text-success'
    when 'update'
      'text-warning'
    when 'replace'
      'text-success'
    when 'delete'
      'text-error'
    else
      ''
    end
  end

  def action_to_background(action)
    case action
    when 'add'
      'success'
    when 'update'
      'warning'
    when 'replace'
      'success'
    when 'delete'
      'error'
    else
      ''
    end
  end

  def state_to_text(state)
    case state
    when 'enabled'
      'text-success'
    when 'pending'
      'text-info'
    when 'open'
      'text-info'
    when 'active'
      'text-success'
    when 'paused'
      'text-warning'
    when 'complete'
      'text-success'
    when 'closed'
      ''
    when 'inprogress'
      'text-info'
    when 'pending'
      ''
    when 'failed'
      'text-error'
    when 'canceled'
      'text-error'
    when 'inactive'
      'text-error'
    else
      ''
    end
  end

  def state_to_label(state)
    case state
    when 'enabled'
      'label-success'
    when 'stale'
      'label-info'
    when 'open'
      'label-info'
    when 'active'
      'label-info'
    when 'paused'
      'label-warning'
    when 'pausing'
      'label-warning'
    when 'complete'
      'label-success'
    when 'closed'
      'label-success'
    when 'inprogress'
      'label-info'
    when 'pending'
      ''
    when 'failed'
      'label-important'
    when 'canceled'
      'label-important'
    when 'inactive'
      'label-important'
    when 'disabled'
      'label-important'
      when 'replace'
        'label-notice'
    else
      ''
    end
  end

  def health_to_label(state)
    case state
    when 'good'
      'label-success'
    when 'notify'
      'label-info'
    when 'unhealthy'
      'label-important'
    when 'overutilized'
      'label-warning'
    when 'underutilized'
      'label-info'
    else
      ''
    end
  end

  def health_to_text(state)
    case state
    when 'good'
      'text-success'
    when 'notify'
      'text-info'
    when 'unhealthy'
      'text-error'
    when 'overutilized'
      'text-warning'
    when 'underutilized'
      'text-info'
    else
      ''
    end
  end

  def health_icon(state)
    case state
      when 'unhealthy'
        'exclamation-triangle'
      when 'notify'
        'exclamation-circle'
      when 'overutilized'
        'expand'
      when 'underutilized'
        'compress'
      when 'good'
        'check-circle'
      else
        'question-circle'
    end
  end

  def ops_state_legend
    OPS_HEALTH_LEGEND
  end

  def ops_state_legend_map
    OPS_HEALTH_LEGEND_MAP
  end

  def cloud_admin_status_label(status)
    case status
      when 'active'
        'label-success'
      when 'inactive'
        'label-important'
      when 'offline'
        'label-warning'
      when 'inert'
        'label-info'
      else
        ''
    end
  end

  def cloud_admin_status_button(status)
    case status
      when 'active'
        'btn-success'
      when 'inert'
        'btn-info'
      when 'offline'
        'btn-warning'
      else
        ''
    end
  end

  def cloud_admin_status_icon(status)
    case status
      when 'active'
        'cloud-upload'
      when 'inert'
        'cloud'
      when 'offline'
        'cloud-download'
      else
        'cloud'
    end
  end

  def highlight(value, label_class = '', options = {})
    content_tag(:span, value, :class => "highlight #{label_class}")
  end

  def marker(value, label_class = '', options = {})
    toggle = options['data-toggle']
    marker = content_tag(:span, raw("#{value}#{" #{icon('caret-down')}" if toggle}"), :class => "label label-marker #{label_class}")
    id = random_dom_id
    result = content_tag(:div, marker.html_safe, options.merge(:class => 'marker', :id => id))
    result += javascript_tag(%($j("##{id}").#{toggle}())) if toggle
    result
  end

  def count_marker(count, badge_class = '', options = {})
    content_tag(:span, count, options.merge(:class => "badge #{badge_class}"))
  end

  def status_marker(name, value, label_class = '', options = {})
    toggle = options['data-toggle']
    marker = content_tag(:span, raw(name), :class => "label label-marker-name #{options.delete(:name_class)}")
    marker << content_tag(:span, raw("#{value}#{" #{icon('caret-down')}" if toggle}"), :class => "label label-marker-value #{label_class}", :onclick => toggle ? nil : "selectText(this)")
    id = random_dom_id
    result = content_tag(:div, marker.html_safe, options.merge(:class => 'marker', :id => id))
    result += javascript_tag(%($j("##{id}").#{toggle}())) if toggle
    result
  end

  def instance_marker(count)
    return count ? status_marker('instances', count.to_i, 'label-info') : ''
  end

  def cloud_marker(cloud, primary, status)
    marker = status_marker(icon('cloud', primary ? 'primary' : 'secondary'),
                  "#{cloud.ciName} #{icon('external-link')}",
                   cloud_admin_status_label(status),
                  :name_class => primary ? 'info' : '')
    link_to(marker, edit_cloud_path(cloud))
  end

  def health_marker(state)
    status_marker('health', state, health_to_label(state))
  end

  def icon(name, text = '', icon_class = '')
    icon_html = content_tag(:i, '', :class => "fa fa-#{name} #{icon_class}")
    raw("#{icon_html}#{" #{text}" if text.present?}")
  end

  def loading_indicator(message = 'Loading...')
    icon('spinner', message, 'fa-spin')
  end

  def falied_loading_indicator(message = 'Failed to load')
    raw(%(<p class="text-error">#{icon('exclamation-triangle')} <strong>#{message}</strong></p>))
  end

  def notification_icon(source)
    case source
    when 'deployment'
      icon = 'cloud-upload'
    when 'procedure'
      icon = 'cogs'
    when 'ops'
      icon = 'exclamation-triangle'
    when 'opamp'
      icon = 'bar-chart'
    when 'system'
      icon = 'exclamation-triangle'
    else
      icon = 'question-circle'
    end
    content_tag(:i, '', :class => "fa fa-#{icon}")
  end

  def button(text, btn_size = false, btn_class = false)
    size = btn_size ? "btn-#{btn_size}" : ""
    content_tag(:button, text, :class => btn_class ? "btn #{size} btn-#{btn_class}" : "btn #{size}")
  end

  def icon_button(name, text, btn_size = false, btn_class = false)
    size = btn_size ? "btn-#{btn_size}" : ""
    content_tag(:button, icon(name, text, btn_class ? true : false), :class => btn_class ? "btn #{size} btn-#{btn_class}" : "btn #{size}")
  end

  def time_ago_in_words(t)
    time_tag(t, super(t, :include_seconds => true) + ' ago', :title => t)
  end

  def time_duration_in_words(ms)
    if ms < 1000
      "#{ms} ms"
    elsif ms < 60 * 1000
      "#{(ms.to_f / 1000).round(1)} sec"
    elsif ms < 60 * 60 * 1000
      "#{(ms.to_f / (60 * 1000)).round(1)} min"
    else
      "#{(ms.to_f / (60 * 60 * 1000)).round(1)} hr"
    end
  end

  def page_alert
    if @assembly
      controller_name = controller.class.name
      if controller_name.include?('Design')
        release = @release || Cms::Release.latest(:nsPath => assembly_ns_path(@assembly))
        render 'design/page_alert', :assembly => @assembly, :release => release if release && release.releaseState == 'open'
      elsif controller_name.include?('Transition::') && @environment
        release    = @release || Cms::Release.latest(:nsPath => "#{environment_manifest_ns_path(@environment)}")
        deployment = @deployment || Cms::Deployment.latest(:nsPath => "#{environment_ns_path(@environment)}/bom")

        render 'transition/page_alert', :assembly => @assembly, :environment => @environment, :release => release, :deployment => deployment if release && release.releaseState == 'open'
      end
    elsif controller.is_a?(OrganizationController) && action_name == 'edit'
      render 'organization/page_alert'
    end
  end

  def wizard
    return unless user_signed_in? && current_user.organization && current_user.show_wizard?

    assembly = if @assembly && @assembly.persisted?
      @assembly.is_a?(Cms::Ci) ? @assembly : (@assembly.is_a?(Cms::Relation) ? @assembly.toCi : nil)
    else
      assemblies = locate_assemblies
      assemblies.size == 1 ? assemblies.first : nil
    end

    if assembly
      environment = @environment
      unless environment
        environments = Cms::Relation.all(:params => {:ciId              => assembly.ciId,
                                                     :direction         => 'from',
                                                     :relationShortName => 'RealizedIn',
                                                     :targetClassName   => 'manifest.Environment'})
        environment = environments.size == 1 ? environments.first.try(:toCi) : nil
      end
    end

    render 'layouts/wizard', :assembly => assembly, :environment => environment && environment.persisted? ? environment : nil
  end

  def random_dom_id
    "a#{SecureRandom.random_number(36**6).to_s(36)}"
  end

  def diagram
    graph = GraphViz::new( "G" )
    graph_options = {
          :truecolor  => true,
          :rankdir    => 'TB',
          :center     => true,
          :ratio      => 'fill',
          :size       => params[:size] || "6,4",
          :bgcolor    => "transparent"}
    graph[graph_options.merge(params.slice(*graph_options.keys))]
    graph.node[:fontsize  => 8,
               :fontname  => 'ArialMT',
               :fontcolor => 'black',
               :color     => 'black',
               :fillcolor => 'whitesmoke',
               :fixedsize => true,
               :width     => "2.50",
               :height    => "0.66",
               :shape     => 'rect',
               :style     => 'rounded']
    graph.edge[:fontsize  => 10,
               :fontname  => 'ArialMT',
               :fontcolor => 'black',
               :color     => 'gray']


    components = Cms::DjRelation.all(:params => {:ciId              => @platform.ciId,
                                                 :relationShortName => 'Requires',
                                                 :direction         => 'from',
                                                 :includeToCi       => true})
    components.each do |node|
      ci = node.toCi
      url = nil
      if @assembly
        if @environment
          url = edit_assembly_transition_environment_platform_component_path(@assembly, @environment, @platform, ci.id)
        else
          url = edit_assembly_design_platform_component_path(@assembly, @platform, ci.id)
        end
      elsif @design
        url = edit_catalog_design_platform_component_path(@design, @platform, ci.id)
      end
      img = "<img scale='both' src='#{ci_image_url(ci)}>"
      label = "<<table border='0' cellspacing='2' fixedsize='true' width='180' height='48'>"
      label << "<tr><td fixedsize='true' rowspan='2' cellpadding='4' width='40' height='40' align='center'>#{img}</td>"
      label << "<td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='12'>#{ci.ciName}</font></td></tr>"
      label << "<tr><td align='left' cellpadding='0' width='124' fixedsize='true'><font point-size='10'>#{ci.updated_timestamp.to_s(:short_us)}</font></td></tr></table>>"
      graph.add_node(node.toCiId.to_s,
                     :id => node.toCiId.to_s,
                     :target => "_parent",
                     :URL    => url,
                     :label  => label,
                     :color  => rfc_action_to_color(ci.rfcAction))

      Cms::DjRelation.all(:params => {:ciId => node.toCiId, :relationShortName => 'DependsOn', :direction => 'from'}).each do |edge|
        if edge.relationAttributes.flex == 'true'
          edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Scale</font></td></tr>"
          edgelabel << "<tr><td align='left'>Minimum</td><td>#{edge.relationAttributes.min}</td></tr>"
          edgelabel << "<tr><td align='left' bgcolor='#D9EDF7'>Current</td><td bgcolor='#D9EDF7'>#{edge.relationAttributes.current}</td></tr>"
          edgelabel << "<tr><td align='left'>Maximum</td><td>#{edge.relationAttributes.max}</td></tr>"
          edgelabel << "</table>>"
          graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s,
            :labeltarget => "_parent",
            :labelURL => "#{url}",
            :minlen => 1,
            :penwidth => 1,
            :color => 'black',
            :labeldistance => 3.0,
            :arrowtail => 'odiamond',
            :dir => 'back',
            :label => edgelabel
          )
        elsif edge.relationAttributes.converge == 'true'
          edgelabel = "<<table border='0' cellspacing='1'><tr><td border='1' colspan='2'><font point-size='12'>Converge</font></td></tr></table>>"
          graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s,
            :labeltarget => "_parent",
            :labelURL => url,
            :minlen => 1,
            :penwidth => 1,
            :color => 'black',
            :labeldistance => 3.0,
            :arrowhead => 'odiamond',
            :label => edgelabel
          )
        else
          graph.add_edge(edge.fromCiId.to_s, edge.toCiId.to_s)
        end
      end
    end

    send_data(graph.output(:svg => String), :type => 'image/svg+xml', :disposition => 'inline')
  end

  def breadcrumb_marker(text, label_class = '', options = {})
    content_tag(:sub, text, options.merge(:class => "label label-breadcrumb #{label_class}"))
  end

  def breadcrumb_environment_label(env = @environment)
    profile = env.ciAttributes.attributes.has_key?(:profile) && env.ciAttributes.profile
    "#{env.ciName}#{" #{breadcrumb_marker("#{profile}", 'label-info')}" if profile}"
  end

  def breadcrumb_environment_icon(env = @environment)
    "#{env.ciAttributes.availability}_availability"
  end

  def breadcrumb_platform_label(platform = @platform)
    if platform.ciClassName.start_with?('mgmt')
      if platform.ciClassName.end_with?('catalog.Platform')
        bc_label = icon(site_icon('design'), 'design')
      else
        availability = platform.nsPath.split('/').last
        bc_label     = icon(availability == 'redundant' ? 'cubes' : 'cube', "#{availability} availability")
      end
      "#{platform.ciName} #{content_tag(:sub, "ver. #{platform.ciAttributes.version}", :class => 'label label-success')} #{content_tag(:sub, bc_label)}"
    else
      active = platform.ciAttributes.attributes.has_key?(:is_active) && platform.ciAttributes.is_active == 'false' ? false : true
      "#{platform.ciName} #{content_tag(:sub, "ver. #{platform.ciAttributes.major_version}", :class => "label #{'label-success' if active}")}"
    end
  end

  def release_state_icon(state, additional_classes = '')
    case state
      when 'closed'
        icon = 'check-circle'
        text = 'text-success'
      when 'open'
        icon = 'circle-o'
        text = 'text-info'
      when 'canceled'
        icon = 'ban'
        text = 'text-error'
      else
        icon = ''
        text = ''
    end
    content_tag(:i, '', :class => "fa fa-#{icon} #{text} #{additional_classes}", :alt => state)
  end

  def deployment_state_icon(state, additional_classes = '')
    icon = ''
    text = ''
    case state
      when 'pending'
        icon = 'clock-o'
        text = 'muted'
      when 'complete'
        icon = 'check-circle'
        text = 'text-success'
      when 'failed'
        # icon = 'remove'
        icon = 'times-circle'
        text = 'text-error'
      when 'canceled'
        icon = 'ban'
        text = 'text-error'
      when 'active'
        icon = 'spinner fa-spin'
        text = 'text-info'
      when 'paused'
        icon = 'pause'
        text = 'text-warning'
      when 'pausing'
        icon = 'pause'
        text = 'text-warning'
    end
    content_tag(:i, '', :class => "fa fa-#{icon} #{text} #{additional_classes}", :alt => state)
  end

  def deployment_approval_state_icon(state, additional_classes = '')
    icon = ''
    text = ''
    case state
      when 'pending'
        icon = 'clock-o'
        text = 'muted'
      when 'approved'
        icon = 'check-circle'
        text = 'text-success'
      when 'expired'
        icon = 'moon-o'
        text = 'text-warning'
      when 'rejected'
        icon = 'ban'
        text = 'text-error'
    end
    content_tag(:i, '', :class => "fa fa-#{icon} #{text} #{additional_classes}", :alt => state)
  end


  def rfc_action_icon(action, additional_classes = '')
    icon = ''
    text = ''
    case action
      when 'add'
        icon = 'plus'
        text = 'success'
      when 'delete'
        icon = 'minus'
        text = 'error'
      when 'replace'
        icon = 'exchange'
        text = 'success'
      when 'update'
        icon = 'repeat'
        text = 'warning'
    end
    content_tag(:i, ' ', :class => "rfc-action fa fa-#{icon} text-#{text} #{additional_classes}")
  end

  def rfc_state_icon(state, additional_classes = '')
    icon = ''
    text = ''
    case state
      when 'pending'
        icon = 'clock-o'
        text = 'muted'
      when 'inprogress'
        icon = 'spinner fa-spin'
        text = ''
      when 'complete'
        icon = 'check'
        text = 'text-success'
      when 'failed'
        # icon = 'remove'
        icon = 'times-circle'
        text = 'text-error'
      when 'canceled'
        icon = 'ban'
        text = 'text-error'
      when 'active'
        icon = 'spinner fa-spin'
        text = 'text-info'
    end
    content_tag(:i, '', :class => "fa fa-#{icon} #{text} #{additional_classes}", :alt => state)
  end

  def rfc_properties(rfc)
    result = '<dl class="dl-horizontal">'
    result << '<dt>RfcId</dt>'
    result << "<dd>#{rfc.rfcId}</dd>"
    if rfc.is_a?(Cms::RfcCi)
      result << '<dt>Ci</dt>'
      result << "<dd>#{rfc.ciId}</dd>"
    else
      result << '<dt>Relation</dt>'
      result << "<dd>#{rfc.ciRelationId}</dd>"
    end
    result << '<dt>Release</dt>'
    result << "<dd>#{rfc.releaseId}</dd>"
    if rfc.is_a?(Cms::RfcCi)
      result << '<dt>Created</dt>'
      result << "<dd>#{time_ago_in_words(rfc.rfc_created_timestamp) } by #{rfc.rfcCreatedBy }</dd>" if rfc.rfcCreated
      unless rfc.rfcCreated == rfc.rfcUpdated
        result << '<dt>Updated</dt>'
        result << "<dd>#{time_ago_in_words(rfc.rfc_updated_timestamp)}#{" by #{rfc.rfcUpdatedBy}" if rfc.rfcUpdatedBy}</dd>" if rfc.rfcUpdated
      end
    else
      result << '<dt>Created</dt>'
      result << "<dd>#{time_ago_in_words(rfc.created_timestamp) } by #{rfc.createdBy }</dd>" if rfc.created
      unless rfc.created == rfc.updated
        result << '<dt>Updated</dt>'
        result << "<dd>#{time_ago_in_words(rfc.updated_timestamp)}#{" by #{rfc.updatedBy}" if rfc.updatedBy}</dd>" if rfc.updated
      end
    end
    result << '</dl>'
    raw(result)
  end

  def rfc_attributes(rfc)
    base_attrs = rfc.is_a?(Cms::RfcCi) ? rfc.ciBaseAttributes.attributes : rfc.relationBaseAttributes.attributes
    result = '<dl class="dl-horizontal">'
    (rfc.is_a?(Cms::RfcCi) ? rfc.ciAttributes : rfc.relationAttributes).attributes.each do |attr_name, attr_value|
      md_attribute = rfc.meta.md_attribute(attr_name)
      if md_attribute
        description = md_attribute.description.presence || attr_name
        data_type   = md_attribute.dataType
        json        = data_type == 'hash' || data_type == 'array' || data_type == 'struct'
      else
        Rails.logger.warn "======= Could not find metadata for attribute '#{attr_name}' of #{rfc.ciClassName}, rfcId=#{rfc.rfcId}"
        description = attr_name
        json = false
      end

      base_value  = base_attrs[attr_name]
      if json && attr_value.present?
        begin
          attr_value = JSON.parse(attr_value)
        rescue
          json = false
        end
      end
      result << %(<dt title="#{ description }">#{ description }</dt>)
      result << %(<dd class="diff-container">)
      if attr_value.blank?
        result << '&nbsp;'
      else
        result << %(<pre class="changed">#{h(json && attr_value.present? ? JSON.pretty_unparse(attr_value) : attr_value)}</pre>)
      end
      if json && base_value.present?
        begin
          base_value = JSON.parse(attr_value)
        rescue
          json = false
        end
      end
      result << %(<pre class="original hide">#{h(json && base_value.present? ? JSON.pretty_unparse(base_value) : base_value)}</pre>)
      result << '</dd>'
    end
    result << '</dl>'
    raw(result)
  end

  def hash_list(data)
    result = '<dl class="dl-horizontal">'
    data.each_pair do |name, value|
      result << %(<dt title="#{ name }">#{ name }</dt>
                  <dd>#{ value.presence || '&nbsp;' }</dd>)
      end
    result << '</dl>'
    raw(result)
  end

  def site_icon(name)
    SITE_ICONS[name.to_sym] || name
  end

  def site_icon!(name)
    SITE_ICONS[name.to_sym]
  end

  def general_site_links
    return GENERAL_SITE_LINKS
  end

  def team_list_permission_marking(team, perms = global_admin_mode? ? %w(design transition operations) : %w(cloud_services cloud_compliance cloud_support design transition operations))
    admins = team.name == Team::ADMINS
    result = %w(manages_access org_scope).inject('') do |a, perm|
      a << icon(site_icon(perm), '&nbsp;&nbsp;', "fa-lg fa-fw text-error #{'extra-muted' unless admins || team.send("#{perm}?")}")
    end
    result = perms.inject(result) do |a, perm|
      a << icon(site_icon(perm), '&nbsp;&nbsp;', "fa-lg fa-fw #{'extra-muted' unless admins || team.send("#{perm}?")}")
    end
    raw(result)
  end

  def cost_donut(data, category, title, &block)
    total  = data[:total]
    if total && total > 0
      unit       = data[:unit]
      slices     = {}
      max_slices = 10
      data[category].to_a.sort_by(&:last).reverse.each_with_index do |bucket, i|
        key  = bucket.first
        name = block_given? ? yield(key) : key
        cost = bucket.last + (slices[name] ? slices[name][:value] : 0)
        slices[name] = {:name => name,
                        :value => cost,
                        :label => "#{name} - #{number_with_precision(100 * cost / total, :precision => 1)}% (#{number_with_precision(cost, :precision => 2, :delimiter => ',')} #{unit})"}
      end

      slices_count = slices.size
      slices = slices.values.
        sort_by {|s| -s[:value]}[0..(max_slices - 2)].
        select {|s| s[:value] / total > 0.02}.
        to_map {|s| s[:name]}
      if slices.size < slices_count
        name = 'others'
        cost = total - slices.values.sum { |s| s[:value] }
        slices[name] = {:name  => name,
                        :value => cost,
                        :label => "#{name} - #{number_with_precision(100.0 * cost / total, :precision => 1)}% (#{number_with_precision(cost, :precision => 2, :delimiter => ',')} #{unit})"}
      end
      data = {:title  => title,
              :label  => slices_count,
              :slices => slices.values}
      legend = nil
    else
      data   = {:title  => title,
                :label  => 'N/A',
                :slices => [{:name => 'N/A', :value => 1, :label => 'Data not available'}]}
      legend = [{:name => 'N/A', :color => '#aaa'}]
    end

    render 'base/shared/graph_donut', :data => {:data => [data], :legend => legend}, :legend => false
  end

  def format_cost_rate(rate, opts = {})
    raw %(<span class="cost-rate">#{rate.to_human(:precision => (opts[:precision] || 2))} <span class=""><sub>#{CostSummary::UNIT}</sub></span></span>)
  end

  def ci_doc_link(ci, label, opts = {})
    asset_url = Settings.asset_url.presence || 'cms/'
    anchor    = opts[:anchor]
    split     = ci.ciClassName.split('.')
    split     = split[1..-1] if split.first == 'mgmt'
    link_to(raw(label),
            "#{asset_url}#{split[-[split.size - 1, 3].min..-1].join('.')}/index.html#{"##{anchor}" if anchor.present?}",
            :target => '_blank',
            :class  => opts[:class] || '')
  end

  def platform_doc_link(platform, label, opts = {})
    ci_attrs  = platform.ciAttributes
    pack_doc_link(ci_attrs.source, ci_attrs.pack, ci_attrs.version, label, opts)
  end

  def pack_doc_link(source, pack, version, label, opts = {})
    link_to(raw(label),
            pack_doc_url(source, pack, version, opts),
            :target => '_blank',
            :class  => 'doc-link',
            :title  => 'go to documentation')
  end

  def pack_doc_url(source, pack, version, opts = {})
    anchor = opts[:anchor]
    "#{Settings.asset_url.presence || 'cms/'}#{source}/packs/#{pack}/#{version}/#{pack}.html#{"##{anchor}" if anchor.present?}"
  end

  def platform_pack_link(platform, label = icon(site_icon(:pack)))
    ci_attrs     = platform.ciAttributes
    source       = ci_attrs.source
    pack_name    = ci_attrs.pack
    version      = ci_attrs.version
    availability = ci_attrs.attributes[:availability]
    link_to(raw(label),
            catalog_pack_platform_path(source, pack_name, version, availability, pack_name),
            :class => 'doc-link',
            :title => 'go to pack page')
  end

  def expandable_content(options = {}, &block)
    dom_id = random_dom_id
    content = options[:content]
    raw(link_to_function(content_tag(:b, raw(options[:label].presence || '<strong>...</strong>')), %($j(this).hide(); $j("##{dom_id}").toggle(300))) + content_tag(:span, content.present? ? raw(content) : capture(&block), :id => dom_id, :class => 'hide'))
  end

  def expandable_list(items, options = {})
    separator     = options[:separator] || ', '
    visible_count = options[:visible_count] || 3
    html = items[0...visible_count].join(separator)
    if items.size > visible_count
      html << separator
      html << expandable_content(:content => items[visible_count..-1].join(separator), :label => options[:label])
    end
    raw(html)
  end

  def pack_version_text_class(version_ci, org_ns_path = has_support_permission?(Catalog::PacksController::SUPPORT_PERMISSION_PACK_MANAGEMENT) ? nil : organization_ns_path)
    if version_ci.ciAttributes.enabled == 'false'
      visibility = version_ci.altNs.attributes[Catalog::PacksController::ORG_VISIBILITY_ALT_NS_TAG]
      if org_ns_path.blank?
        visibility.present? ? 'text-warning' : 'text-error'
      else
        visibility.try(:include?, organization_ns_path) ? '' : 'text-error'
      end
    else
      'text-success'
    end
  end

  def pack_version_label_class(version_ci)
    pack_version_text_class(version_ci).sub('text-', 'label-').sub('-error', '-important')
  end

  def pack_version_list(versions, org_ns_path)
    versions = semver_sort(versions)
    builder  = lambda {|vv| vv.map {|v| link_to(v.ciName, catalog_pack_platform_path(params[:source], params[:pack], v.ciName, params[:availability], params[:pack]), :class => pack_version_text_class(v, org_ns_path))}.join(', ')}
    result   = builder.call(versions[0..14])
    if versions.size > 15
      result += ', '
      result += expandable_content(:content => capture(versions[15..-1], &builder))
    end
    result
  end

  def sub_url_links(content)
    content.blank? ? content : raw(content.gsub(/http(s)?:\/\/\S*/) {|t| link_to(t, t, :target => '_blank')})
  end
end
