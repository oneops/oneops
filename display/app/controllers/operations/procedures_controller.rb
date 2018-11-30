class Operations::ProceduresController < ApplicationController
  before_filter :find_procedure, :except => [:index, :new, :prepare, :create, :log_data]

  helper_method :allow_access?

  swagger_controller :procedures, 'Operations Procedure/Action Management'

  swagger_api :index do
    summary 'Fetch operations procedures.'
    notes 'Returns a list of most recently created operations procedures (including individual actions) for a given ' \
          'anchor (use <b>ciId</b> param) or action target CI (use <b>actionCiId</b> param) or namespace (use <b>ns_path</b> param).'
    param_org_name
    param :query, 'ciId', :string, :optional, 'Return procedures created by (i.e. anchored on) CI with this id.'
    param :query, 'anchorCiId', :string, :optional, 'Return procedures with any of the actions targeted to CI with this id regardless of procedure anchor CI.  This allows to query for any procedures which included actions for a given CI.'
    param :query, 'ns_path', :string, :optional, 'Return procedures for anchor CIs in this namespace (recursively).'
    param :query, 'state', :string, :optional, 'Commas separated list of procedure states to filter results.'
    param :query, 'name', :string, :optional, 'Procedure name (the same action name except for some cases of dedicated pack-defined procedures) to filter results.'
    param :query, 'size', :number, :optional, 'Max number of procedures to return (default and max value is 100).'
    param :query, 'include_actions', :boolean, :optional, 'true|false. Include actions in response - applicable only when querying by <b>ns_path</b> (default: true).'
  end
  def index
    anchor_ci_id = params[:ciId]
    action_ci_id = params[:actionCiId]
    ns_path      = params[:ns_path]
    search_params = {}
    if anchor_ci_id.present?
      return unauthorized unless allow_access?(anchor_ci_id, true)
      search_params[:ciId] = anchor_ci_id
    elsif action_ci_id.present?
      return unauthorized unless allow_access?(action_ci_id, true)
      search_params[:actionCiId] = action_ci_id
    elsif ns_path.present?
      return unauthorized unless allow_access_to_ns_path?(ns_path, true)
      search_params[:nsPath]    = ns_path
      search_params[:recursive] = true
      search_params[:actions]   = params[:include_actions] != 'false'
    else
      render :json => {:errors => ['ciId or actionCiId or ns_path must be specified!']}, :status => :unprocessable_entity
      return
    end

    limit = params[:size].to_i
    state = params[:state]
    name  = params[:name]
    search_params[:limit] = limit > 100 || limit < 1 ? 100 : limit
    search_params[:state] = state if state.present?
    search_params[:procedureName] = name if name.present?
    @procedures = Cms::Procedure.all(:params => search_params)

    respond_to do |format|
      format.js
      format.json { render :json => @procedures }
    end
  end

  def show
    return unauthorized unless allow_access?(@procedure.ciId, true)
    respond_to do |format|
      format.js { load_action_cis }
      format.json {render_json_ci_response(true, @procedure)}
    end
  end

 def new
    @procedure = Cms::Procedure.build(params)
    @procedure.ciId = params[:ciId]
    return unauthorized unless allow_access?(@procedure.ciId, false)

    procedure_ci_id = params[:procedureCiId]
    if procedure_ci_id.to_i > 0
      procedure_ci = Cms::Ci.find(procedure_ci_id)
      @procedure.procedureName = procedure_ci.ciName
      unless @procedure.arglist.present?
        arguments_json = procedure_ci.ciAttributes.arguments
        build_arglist(arguments_json) if arguments_json.present?
      end
    else
      name = params[:actionName]
      procedure_name = params[:procedureName].presence || name
      @procedure.procedureName = procedure_name

      unless @procedure.arglist.present?
        ci_class_name = params[:actionCiClassName]
        if ci_class_name.present?
          md        = Cms::CiMd.look_up(ci_class_name)
          md_action = md && md.actions.find { |a| a.actionName == name }
          if md_action && md_action.arguments.present?
            build_arglist(md_action.arguments)
          end
        end
      end

      cloud_ids = params[:cloudCiIds]
      if cloud_ids.blank?
        # Specific instance ciIds.
        @target_ids = params[:actionCiIds]
      else
        # All instances in specified clouds.
        @target_ids = []
        cloud_ids.each do |cloud_id|
          @target_ids += Cms::Relation.all(:params => {:nsPath            => @anchor_ci.nsPath.gsub('/manifest/', '/bom/'),
                                                       :relationShortName => 'RealizedAs',
                                                       :toClassName       => @anchor_ci.ciClassName.sub(/^manifest./, 'bom.'),
                                                       :attr              => "toCiName:like:#{@anchor_ci.ciName}-#{cloud_id}-%"}).map(&:toCiId)
        end
      end

      attachment_ci_id = params[:attachmentCiId].to_i
      action = {:actionName => name, :stepNumber => 1, :isCritical => true}
      action[:extraInfo] = attachment_ci_id if attachment_ci_id > 0
      if @target_ids.present?
        full_flow = flow = []
        direction = params[:direction] || 'from'
        relation_names = (params[:relationName] || 'base.RealizedAs').split(',')
        relation_names[0..-2].each do |relation_name|
          flow << {:relationName => relation_name,
                   :direction    => direction,
                   :flow         => []}
          flow = flow.first[:flow]
        end
        flow << {:relationName => relation_names[-1],
                 :direction    => direction,
                 :targetIds    => @target_ids,
                 :actions      => [action]}
        @procedure.definition = {:name => procedure_name, :flow => full_flow}.to_json
      else
        @procedure.definition = {:name => procedure_name, :flow => [], :actions => [action]}.to_json
      end
    end
    render(:action => :new)
 end

  # This is POST version of "new" for situations when 'actionCiIds' list is long and GET url length is restricted.
  def prepare
    new
  end

  swagger_api :create do
    summary 'Create and start a procedure or action.'
    param_org_name
    param :body, :body, :json, :required, 'See examples above.'
    notes <<-NOTE
JSON body payload examples.
'Restart' action for single instance with ciId=2146947:
<pre style="white-space:pre-wrap">
{
  "cms_procedure": {
    "procedureCiId": "0",
    "procedureName": "restart-instance",
    "ciId": "2146947",
    "procedureState": "active",
    "definition": "{\"name\":\"restart\",\"flow\":[],\"actions\":[{\"actionName\":\"restart\"}]}"
  }
}
</pre>
'Reboot' action for all instances of component with ciId=2091578 at the same time:
<pre style="white-space:pre-wrap">
{
  "cms_procedure": {
    "procedureCiId": "0",
    "procedureName": "reboot-all-instances",
    "ciId": "2091578",
    "procedureState": "active",
    "definition": "{\"name\":\"reboot\",\"flow\":[{\"relationName\":\"base.RealizedAs\",\"direction\":\"from\",\"actions\":[{\"actionName\":\"reboot\",\"stepNumber\":1,\"isCritical\":true}]}]}"
  }
}
</pre>
'Repair' action for 4 instances of component with ciId=2091578 two at a time:
<pre style="white-space:pre-wrap">
{
  "cms_procedure": {
    "procedureCiId": "0",
    "procedureName": "repair-by-two",
    "ciId": "2091578",
    "procedureState": "active",
    "definition": "{\"name\":\"repair\",\"flow\":[{\"relationName\":\"base.RealizedAs\",\"direction\":\"from\",\"targetIds\":[2146947,2146948],\"actions\":[{\"actionName\":\"repair\",\"stepNumber\":1,\"isCritical\":true}]},{\"relationName\":\"base.RealizedAs\",\"direction\":\"from\",\"targetIds\":[2146950,2146951],\"actions\":[{\"actionName\":\"repair\",\"stepNumber\":2,\"isCritical\":true}]}]}"
  }
}
</pre>
NOTE
  end
  def create
    @procedure = Cms::Procedure.build(params[:cms_procedure])
    return unauthorized unless allow_access?(@procedure.ciId, false)

    roll_at = params[:roll_at].to_i
    if roll_at >= 1 && roll_at < 100
      definition = JSON.parse(@procedure.definition)
      if definition['flow'].present?
        old_last_node = definition['flow'].first
        old_flow_json = old_last_node.to_json
        old_last_node = old_last_node['flow'].first while old_last_node['flow'].present?
        target_ids = old_last_node['targetIds']
        actions = [{'isCritical' => params[:critical] == 'true'}.reverse_merge(old_last_node['actions'].first)]
        new_flow = []
        roll_at = 10 if roll_at < 1
        step_size = [0, (target_ids.size * roll_at / 100)].max
        (target_ids.size.to_f / step_size).ceil.times do |step|
          new_flow << JSON.parse(old_flow_json)
          new_last_node = new_flow.last
          new_last_node = new_last_node['flow'].first while new_last_node['flow'].present?
          new_last_node['targetIds'] = target_ids[(step_size * step)...(step_size * (step + 1))]
          new_last_node['actions']   = actions
        end
        @procedure.definition = {:name => definition['name'], :flow => new_flow}.to_json
      end
    end

    ok = execute(@procedure, :save)
    respond_to do |format|
      format.js do
        if ok
          flash[:notice] = 'Procedure execution was successfully started.'
          render_edit
        else
          flash[:error] = "Failed to start procedure#{" (#{@procedure.errors.full_messages})" if @procedure.errors.present?}."
          render :nothing => true
        end
      end

      format.json {render_json_ci_response(ok, @procedure)}
    end
  end

  def edit
    return unauthorized unless allow_access?(@procedure.ciId, true)
    render_edit
  end

  def update
    return unauthorized unless allow_access?(@procedure.ciId, false)
    if params[:cms_procedure][:procedureState] == 'active'
      ok = execute(@procedure, :retry)
      # another lookup needed to get the full object again with actionorders
      @procedure = Cms::Procedure.find(params[:id]) if ok
    else
      ok = execute(@procedure, :update_attributes, params[:cms_procedure])
    end

    respond_to do |format|
      format.js { render_edit }

      format.json {render_json_ci_response(ok, @procedure)}
    end
  end

  def status
    return unauthorized unless allow_access?(@procedure.ciId, true)

    @procedure_actions_states = @procedure.actions.inject({}) { |states, action| states[action.actionId] = action.actionState; states }
    @procedure.log_data = pull_log_data(params[:action_ids] || [])

    respond_to do |format|
      format.js
      format.json { render_json_ci_response(@procedure.present?, @procedure) }
    end
  end

  def log_data
    @procedure = Cms::Procedure.find(params[:procedure_id])
    return not_found('procedure not found') unless @procedure
    return unauthorized unless allow_access?(@procedure.ciId, true)

    log_data = pull_log_data(params[:action_ids] || @procedure.actions.map(&:actionId))
    @procedure.log_data = log_data
    respond_to do |format|
      format.html { render :layout => 'log' }
      format.js
      format.json { render_json_ci_response(true, @procedure) }
      format.text do
        if log_data.size == 1
          text = log_data.values.first.map { |m| m['message'] }.join("\n");
        elsif log_data.size > 1
          text = log_data.inject([]) do |a, (action_id, log)|
            a << "ActionId: #{action_id}"
            a << log.map { |m| m['message'] }.join("\n")
            a << "\n\n"
          end
          text = text.join("\n")
        else
          text = ''
        end
        render :text => text
      end
    end
  end


  private

  def find_procedure
    @procedure = Cms::Procedure.find(params[:id])
  end

  def pull_log_data(action_ids)
    Daq.logs(action_ids.map {|id| {:id => id}}).inject({}) {|m, e| m[e['id']] = e['logData']; m}
  end

  def load_action_cis
    cis = {}
    ci_ids = @procedure.actions.map(&:ciId)
    ci_ids.each_slice(100) do |ids|
      cis = Cms::Ci.all(:params => {:ids => ids.join(',')}).inject(cis) do |h, ci|
        h[ci.ciId] = ci
        h
      end
    end

    @procedure.actions.each { |action| action.ci = cis[action.ciId] }
  end

  def render_edit
    load_action_cis
    render :action => :edit
  end

  def allow_access?(anchor_ci_id, read_only)
    org_ci = @current_user.organization.ci
    if org_ci.ciId == anchor_ci_id.to_i
      @anchor_ci = org_ci
      return is_admin?
    end

    unless @anchor_ci
      begin
        @anchor_ci = Cms::Ci.find(anchor_ci_id)
      rescue Exception => e
      end
      return false unless @anchor_ci
    end

    return allow_access_to_ns_path?(@anchor_ci.nsPath, read_only)
  end

  def allow_access_to_ns_path?(ns_path, read_only)
    return true if is_admin?

    _, _, assembly, _ = ns_path.split('/')
    if assembly && !assembly.start_with?('_')
      return read_only ? current_user.has_any_dto?(assembly) : has_operations?(assembly)
    elsif @anchor_ci.ciClassName == 'account.Cloud'
      return has_cloud_services?(@anchor_ci.ciId) || has_cloud_support?(@anchor_ci.ciId)
    end
    return false
  end

  def build_arglist(arguments_json)
    begin
      arguments = JSON.parse(arguments_json)
    rescue Exception => e
      Rails.logger.warn "Failed to parse arguments definition for action #{md_action.to_json}"
    end
    if arguments.present?
      arglist = arguments.values.inject({}) do |m, arg|
        m[arg['name']] = arg['defaultValue']
        m
      end
      @procedure.argMd = arguments
      @procedure.arglist = arglist.to_json
    end
  end
end
