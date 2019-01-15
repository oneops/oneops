Display::Application.routes.draw do

  devise_for :users, :controllers => {:registrations => 'registrations', :sessions => 'sessions', :confirmations => 'confirmations', :passwords => 'passwords'}, :defaults => { :org_name => nil }

  devise_scope(:user) do
    get    'registrations/lookup', :to => 'registrations#lookup',  :as => 'user_lookup', :defaults => { :org_name => nil }
    delete 'registration/:id',     :to => 'registrations#destroy', :as => 'delete_user', :defaults => { :org_name => nil }
  end

  match '*x', :via => 'OPTIONS', :to => 'application#cors_options'

  get '/status/ecv' => 'status#ecv'

  get '/error' => 'welcome#error', :as => :error_redirect
  get '/not_found_error' => 'welcome#not_found_error', :as => :not_found

  get 'users/:id' => 'organization/users#show', :as => 'user'
  match '/auth/:provider/callback' => 'authentications#create', :via => [:get, :post]

  get 'r/ns'                                => 'redirect#ns',          :as => 'redirect_ns'
  get 'r/ns/:path'                          => 'redirect#ns', :constraints => {:path => /[a-zA-Z0-9\-]+(\/[a-zA-Z0-9\-]+)+\/?/}
  get 'r/:id'                               => 'redirect#ci'
  get 'r/ci/:id'                            => 'redirect#ci',          :as => 'redirect_ci'
  get 'r/rfc/:id'                           => 'redirect#rfc',         :as => 'redirect_rfc'
  get 'r/release/:id'                       => 'redirect#release'
  get 'r/r/:id'                             => 'redirect#release',     :as => 'redirect_release'
  get 'r/deployment/:id'                    => 'redirect#deployment'
  get 'r/d/:id'                             => 'redirect#deployment',  :as => 'redirect_deployment'
  get 'r/procedure/:id'                     => 'redirect#procedure'
  get 'r/p/:id'                             => 'redirect#procedure',   :as => 'redirect_procedure'
  get 'r/instances/:id'                     => 'redirect#instance'
  get 'r/i/:id'                             => 'redirect#instance',    :as => 'redirect_instance'
  get 'r/instances/:id/monitors/:monitor/d' => 'redirect#monitor_doc', :as => 'redirect_instance_monitor_doc'
  get 'r/:id'                               => 'redirect#ci', :constraints => {:id => /\d+/}

  get 'l/ci/:id'                  => 'lookup#ci',         :as => 'lookup_ci'
  get 'l/ci/:id/:attribute_name'  => 'lookup#ci',         :as => 'lookup_ci_attribute'
  get 'l/rel/:id'                 => 'lookup#relation',   :as => 'lookup_relation'
  get 'l/rel/:id/:attribute_name' => 'lookup#relation',   :as => 'lookup_relation_attribute'
  get 'l/release/:id'             => 'lookup#release'
  get 'l/r/:id'                   => 'lookup#release',    :as => 'lookup_release'
  get 'l/deployment/:id'          => 'lookup#deployment'
  get 'l/d/:id'                   => 'lookup#deployment', :as => 'lookup_deployment'
  get 'l/procedure/:id'           => 'lookup#procedure'
  get 'l/p/:id'                   => 'lookup#procedure',  :as => 'lookup_procedure'
  get 'l/:id(/:attribute_name)'   => 'lookup#ci', :constraints => {:id => /\d+/}

  get '/api_docs' => 'welcome#api_docs'

  resource :support, :controller => 'support', :only => [:show], :defaults => { :org_name => nil } do
    collection do
      get  'announcements'
      put  'announcements'
      get  'compute_report'
      get  'compute'
      post 'compute'
      get  'search'
      get  'organizations'
      get  'users'
      get  'users/:id' => 'support#user'
      get  'user'
      get  'groups'
      get  'groups/:id' => 'support#group'
      get  'group'
      get  'global_admins'
      get  'cost'
      get  'deployment_to_all_primary_check'
      put  'deployment_to_all_primary_check'
      get  'cloud_supports'
      put  'cloud_supports'

      get 'organization/:name', :action => 'organization', :as => 'organization'
      # delete 'organization/:name', :action => 'organization'
    end
  end

  resource :metadata, :controller => 'metadata', :only => [:show] do
    collection do
      get 'lookup_class_name'
      get 'lookup_attr_name'
    end
  end

  resource :watch, :controller => 'watch', :only => [:show, :create, :destroy]

  post '/:org_name/request_access' => 'organization#request_access', :as => 'organization_request_access'
  get '/:org_name' => 'organization#public_profile', :as => 'organization_public_profile'

  resource :organization, :controller => 'organization', :only => :none do
    get 'announcement', :on => :collection
    put 'announcement', :on => :collection
    get 'lookup',       :on => :collection
  end

  namespace :account, :defaults => { :org_name => nil } do
    resource :profile, :controller => 'profile', :only => [:show, :update] do
      member do
        put  'change_password'
        put  'hide_wizard'
        put  'change_organization'
        put  'reset_authentication_token'
        get  'show_eula'
        put  'accept_eula'
        post 'authentication_token'
        get  'organizations'
        put  'session_preferences'
      end
    end

    resources :favorites, :controller => 'favorites', :only => [:index, :show, :destroy]

    resources :organizations, :only => [:index, :show, :new, :create, :destroy] do
      get    'current_organization', :on => :collection
      delete 'leave', :on => :member
    end

    resources :groups do
      get 'confirm_delete', :on => :member
      get 'lookup', :on => :collection

      resources :members, :controller => 'group_members', :as => 'members', :only => [:index, :show, :create, :update, :destroy]
    end
  end

  # TODO: move under account
  resources :authentications, :only => [:create, :destroy]

  resources :clouds, :controller => 'cloud/clouds', :only => :none do
    collection do
      get :services
      get :offerings
    end
  end

  scope '/:org_name' do
    resource :organization, :controller => 'organization', :only => [:show, :edit, :update] do
      member do
        get 'notifications'
        get 'deployments'
        get 'procedures'
        get 'reports'
        get 'health'
        get 'search'
        get 'cost_rate'
        get 'cost'
      end

      resources :users, :controller => 'organization/users' do
        collection do
          post 'confirm_remove'
          delete 'remove'
        end
      end

      resources :teams, :controller => 'organization/teams' do
        resources :members, :controller => 'organization/team_members', :as => :members, :only => [:index, :create, :destroy]
      end

      resources :environments, :controller => 'organization/environments'
      resources :policies, :controller => 'organization/policies' do
        get 'evaluate', :on => :collection
        get 'evaluate', :on => :member
      end
    end

    resources :notifications

    resource :watch, :controller => 'watch', :only => [:create, :destroy]
    resources :favorites, :controller => 'account/favorites', :only => [:show, :create, :destroy]

    resource :lookup, :controller => 'lookup', :only => :none do
      get 'counterparts', :on => :collection
      get 'variables', :on => :collection
      get 'ci_lookup', :on => :collection
    end

    resource :ci, :controller => 'lookup', :only => :none do
      post 'policy_violations', :on => :collection
    end

    #Provider services
    resources :services, :controller => 'services/services' do
      get :available, :on => :collection
    end

    resources :clouds, :controller => 'cloud/clouds' do
      collection do
        get :locations
        get :services
        get :offerings
      end

      member do
        get :operations
        get :instances
        get :procedures
        get :reports
        get :search
        get :teams
        put :update_teams
      end

      resources :zones, :controller => 'cloud/zones' do
        resources :services, :controller => 'cloud/services'
      end

      resources :services, :controller => 'cloud/services' do
        get :available, :on => :collection
        get :diff, :on => :collection

        resources :offerings, :controller => 'cloud/offerings' do
          get 'available', :on => :collection
        end
      end

      resources :compliances, :controller => 'cloud/compliances' do
        get :available, :on => :collection
      end

      resources :supports, :controller => 'cloud/supports'

      resources :variables, :controller => 'cloud/variables'
    end

    resources :reports, :only => :none do
      get  'compute',      :on => :collection
      post 'compute',      :on => :collection
      get  'health',       :on => :collection
      get  'notification', :on => :collection
      get  'cost',         :on => :collection
    end

    resources :packs, :controller => 'catalog/packs', :only => [:index]

    resource :catalog, :controller => 'catalog', :only => [:show]

    namespace :catalog do
      # Design catalogs.
      resources :designs, :only => [:index, :show, :destroy] do
        get  'export',  :on => :member
        post 'import',  :on => :collection
        get  'diagram', :on => :member

        resources :variables, :only => [:index, :show]

        resources :platforms, :only => [:index, :show] do
          get 'diagram', :on => :member


          resources :variables, :controller => 'local_variables', :only => [:index, :show]

          resources :components, :only => [:index, :show] do
            resources :attachments, :only => [:index, :show]
            resources :monitors, :only => [:index, :show] do
              get 'watched_by', :on => :member
            end
          end

          resources :monitors, :only => [:show] do
            get 'watched_by', :on => :member
          end
        end
      end

      resources :packs, :only => [:index] do
        get 'versions', :on => :collection
      end

      scope '/packs/:source/:pack(/:version)(/:availability)',
            :as => 'pack',
            :constraints => {:version => /\d+(\.\d+(\.\d+)?)?/,
                             :availability => /single|redundant/} do
        get ''                      => 'packs#show',       :as => ''
        put ''                      => 'packs#update',     :as => 'update'
        get 'versions'              => 'packs#versions',   :as => 'versions'
        get 'stats'                 => 'packs#stats',      :as => 'stats'
        put 'visibility'            => 'packs#visibility', :as => 'visibility'
        put 'password'              => 'packs#password',   :as => 'password'
        get 'diff(/:other_version)' => 'packs#diff',       :as => 'diff', :constraints => {:other_version => /\d+(\.\d+(\.\d+)?)?/}

        resources :platforms, :only => [:show] do
          get 'diagram', :on => :member
          get 'diff',    :on => :collection

          resources :variables, :controller => 'local_variables', :only => [:index, :show]
          resources :policies, :only => [:index, :show]

          resources :components, :only => [:index, :show] do
            resources :monitors, :only => [:index, :show]
          end

          resources :monitors, :only => [:show]
        end
      end
    end

    resources :deployments, :controller => 'transition/deployments', :only => :none do
      get 'time_stats', :on => :member
      get 'progress', :on => :member
    end

    resources :assemblies do
      member do
        get  'new_clone'
        post 'clone'
        get  'teams'
        get  'users'
        put  'update_teams'
        get  'notifications'
        get  'reports'
        get  'search'
        get  'cost_rate'
        get  'cost'
        get  'health'
      end

      collection do
        get 'tags'
      end

      resources :instances, :controller => 'operations/instances', :only => [:index]

      # Design.
      resource :design, :controller => 'design', :only => [:show] do
        get  'extract', :on => :member
        get  'load',    :on => :member
        put  'load',    :on => :member
        get  'diagram', :on => :member
        get  'search',  :on => :member
      end

      namespace :design do
        resources :variables do
          put 'lock',    :on => :collection
          put 'unlock',  :on => :collection
          get 'history', :on => :member
        end

        resources :platforms do
          get  'new_clone',       :on => :member
          post 'clone',           :on => :member
          get  'diagram',         :on => :member
          get  'component_types', :on => :member
          get  'diff',            :on => :member
          get  'history',         :on => :member
          put  'pack_refresh',    :on => :member
          put  'pack_update',     :on => :member
          post 'commit',          :on => :member
          post 'discard',         :on => :member

          resources :variables, :controller => 'local_variables' do
            put 'lock',    :on => :collection
            put 'unlock',  :on => :collection
            get 'history', :on => :member
          end

          resources :components do
            get 'history',         :on => :member
            get 'update_services', :on => :member
            put 'update_services', :on => :member

            resources :attachments do
              get 'history', :on => :member
            end

            resources :monitors do
              get 'watched_by',        :on => :member
              put 'update_watched_by', :on => :member
              put 'toggle',            :on => :member
              get 'history',           :on => :member
            end
          end

          resources :attachments, :only => [:show, :edit, :update, :destroy]

          resources :monitors, :only => [:show, :edit, :update, :destroy] do
            get 'watched_by', :on => :member
            put 'update_watched_by', :on => :member
            put 'toggle', :on => :member
            get 'history', :on => :member
          end
        end

        resources :releases, :only => [:edit, :show, :index] do
          get  'latest',  :on => :collection
          post 'commit',  :on => :member
          post 'discard', :on => :member
          post 'restore', :on => :member
        end

        resource :timeline, :controller => '/timeline', :only => [:show] do
          get 'page', :on => :member

          resources :releases, :only => [:show]
          resources :deployments, :only => [:show]
        end
      end

      # Transition.
      resource :transition, :controller => 'transition', :only => [:show, :pull] do
        post 'pull'
      end

      namespace :transition do
        resources :environments do
          post 'pull',         :on => :member
          get  'pull_status',  :on => :member
          post 'commit',       :on => :member
          post 'force_deploy', :on => :member
          post 'discard',      :on => :member
          put  'enable',       :on => :member
          put  'disable',      :on => :member
          get  'diagram',      :on => :member
          get  'search',       :on => :member
          get  'extract',      :on => :member
          get  'load',         :on => :member
          put  'load',         :on => :member

          resources :variables, :only => [:index, :show, :edit, :update] do
            put 'lock',    :on => :collection
            put 'unlock',  :on => :collection
            get 'history', :on => :member
          end

          resources :relays do
            put 'toggle', :on => :member
          end

          resources :platforms, :only => [:index, :show, :edit, :update, :destroy] do
            get 'toggle',              :on => :member
            get 'activate',            :on => :member
            get 'diagram',             :on => :member
            put 'cloud_configuration', :on => :member
            put 'cloud_priority',      :on => :member
            get 'history',             :on => :member
            get 'search',              :on => :member

            resources :components, :only => [:index, :show, :edit, :update] do
              get  'history',           :on => :member
              get  'update_services',   :on => :member
              put  'update_services',   :on => :member
              get  'depends_on',        :on => :member
              put  'update_depends_on', :on => :member
              post 'touch',             :on => :member
              post 'touch',             :on => :collection
              post 'deploy',            :on => :member

              resources :attachments, :only => [:index, :show, :edit, :update] do
                get 'history', :on => :member
              end

              resources :monitors, :only => [:index, :show, :edit, :update] do
                get 'watched_by',        :on => :member
                put 'update_watched_by', :on => :member
                put 'toggle',            :on => :member
                get 'history',           :on => :member
              end

              resources :logs
            end

            resources :variables, :controller => 'local_variables', :only => [:index, :show, :edit, :update] do
              put 'lock',    :on => :collection
              put 'unlock',  :on => :collection
              get 'history', :on => :member
            end

            resources :attachments, :only => [:show, :edit, :update]

            resources :monitors, :only => [:show, :edit, :update] do
              get 'watched_by', :on => :member
              put 'update_watched_by', :on => :member
              put 'toggle', :on => :member
              get 'history', :on => :member
            end
          end

          resources :releases, :only => [:show, :edit, :index] do
            get  'latest',  :on => :collection
            get  'bom',     :on => :collection
            post 'discard', :on => :member
            post 'restore', :on => :member
          end

          resources :deployments, :only => [:new, :create, :edit, :update, :show, :index] do
            get  'latest',         :on => :collection
            get  'compile_status', :on => :collection
            post 'compile_status', :on => :collection
            get  'bom',            :on => :collection
            post 'bom',            :on => :collection
            get  'status',         :on => :member
            post 'status',         :on => :member
            get  'log_data',       :on => :member
            get  'wo_rfcs',       :on => :member

            resources :approvals, :only => [:index] do
              put 'settle', :on => :collection
            end
          end

          resource :timeline, :controller => '/timeline', :only => [:show] do
            get 'page', :on => :member

            resources :releases, :only => [:show]
            resources :deployments, :only => [:show]
          end
        end
      end

      # Operations
      resource :operations, :controller => 'operations', :only => [:show]

      namespace :operations do
        resources :environments, :only => [:index, :show] do
          get 'graph',           :on => :member
          get 'notifications',   :on => :member
          get 'search',          :on => :member
          get 'cost',            :on => :member
          get 'cost_rate',       :on => :member
          get 'cost_estimate',   :on => :member
          get 'health',          :on => :member
          get 'authorized_keys', :on => :member
          get 'user_changes',    :on => :member

          resources :platforms, :only => [:index, :show] do
            resources :components, :only => [:index, :show] do
              get  'actions', :on => :member
              get  'charts',  :on => :member
              post 'charts',  :on => :member

              resources :instances, :only => [:index, :show, :destroy] do
                put 'cancel_deployment', :on => :member
                get 'availability',      :on => :member
                get 'notifications',     :on => :member
                get 'logs',              :on => :member
                put 'state',             :on => :member
                get 'history',           :on => :member
                get 'by_cloud',          :on => :collection

                resources :monitors, :only => [:index, :show] do
                  get 'charts', :on => :member
                  get 'charts', :on => :collection
                end
              end
            end

            # Allow routing to instances without nesting under component.
            resources :instances, :only => [:index, :show, :destroy] do
              put 'cancel_deployment', :on => :member
              get 'availability',      :on => :member
              get 'notifications',     :on => :member
              get 'logs',              :on => :member
              put 'state',             :on => :member
              get 'history',           :on => :member

              resources :monitors, :only => [:index, :show] do
                get 'charts', :on => :collection
              end
            end

            get 'procedures',  :on => :member
            get 'graph',       :on => :member
            put 'autocomply',  :on => :member
            put 'autorepair',  :on => :member
            get 'autoreplace', :on => :member
            put 'autoreplace', :on => :member
            put 'autoscale',   :on => :member
            get 'search',      :on => :member
            get 'health',      :on => :member
          end

          resources :instances, :only => [:index]

          resource :timeline, :controller => '/timeline', :only => [:show] do
            get 'page', :on => :member

            resources :releases, :only => [:show]
            resources :deployments, :only => [:show]
          end
        end

        resources :instances, :only => [:update] do
          put 'state', :on => :member
          put 'state', :on => :collection
        end
      end
    end

    namespace :transition do
      resources :deployments, :only => [:index]
    end

    namespace :operations do
      resources :platforms, :only => :none do
        get 'diagram', :on => :member
      end

      resources :instances, :only => :none do
        put 'state', :on => :collection
      end

      resources :procedures, :except => [:destroy] do
        get  'status',   :on => :member
        post 'status',   :on => :member
        get  'log_data', :on => :collection
        post 'prepare',  :on => :collection
      end
    end

    resource :operations, :controller => 'operations', :only => :none do
      get  'health', :on => :collection
      post 'charts', :on => :collection
    end

    get ':image' => 'welcome#image_not_found', :constraints => {:image => /.*\.png/}
    match '*whatever' => 'welcome#not_found_error', :via => :all
  end

  post 'notify' => 'relays#notify'
  post 'gk' => 'relays#gk'

  match 'error' => 'welcome#error', :via => :all
  get '/404'  => 'welcome#not_found_error'
  get '/500'  => 'welcome#server_error'

  root :to => 'welcome#index', :defaults => {:org_name => nil}, :as => :root

  # The priority is based upon order of creation:
  # first created -> highest priority.

  # Sample of regular route:
  #   match 'products/:id' => 'catalog#view'
  # Keep in mind you can assign values other than :controller and :action

  # Sample of named route:
  #   match 'products/:id/purchase' => 'catalog#purchase', :as => :purchase
  # This route can be invoked with purchase_url(:id => product.id)

  # Sample resource route (maps HTTP verbs to controller actions automatically):
  #   resources :products

  # Sample resource route with options:
  #   resources :products do
  #     member do
  #       get 'short'
  #       post 'toggle'
  #     end
  #
  #     collection do
  #       get 'sold'
  #     end
  #   end

  # Sample resource route with sub-resources:
  #   resources :products do
  #     resources :comments, :sales
  #     resource :seller
  #   end

  # Sample resource route with more complex sub-resources
  #   resources :products do
  #     resources :comments
  #     resources :sales do
  #       get 'recent', :on => :collection
  #     end
  #   end

  # Sample resource route within a namespace:
  #   namespace :admin do
  #     # Directs /admin/products/* to Admin::ProductsController
  #     # (app/controllers/admin/products_controller.rb)
  #     resources :products
  #   end

  # You can have the root of your site routed with "root"
  # just remember to delete public/index.html.
  # root :to => "welcome#index"

  # See how all your routes lay out with "rake routes"

  # This is a legacy wild controller route that's not recommended for RESTful applications.
  # Note: This route will make all actions in every controller accessible via GET requests.
  # match ':controller(/:action(/:id(.:format)))'
end
