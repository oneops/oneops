class Transition::ApprovalsController < ApplicationController
  before_filter :find_parents
  before_filter :load_approvals

  def index
    render :json => @approvals
  end

  def settle
    # Approval can be designated by 'approvalId' or combination of governCi nsPath and ciName concatenated with '!'.
    approvals_to_settle_map = (params[:approvals].presence || {}).values.to_map { |a| a[:approvalId] }
    comments                = params[:comments]
    state                   = params[:state]
    token                   = params[:token]
    expires_in              = params[:expiresIn]
    result_approvals        = []
    error                   = nil

    @approvals.each do |a|
      govern_ci = a.govern_ci
      settling = approvals_to_settle_map[a.approvalId.to_s] || approvals_to_settle_map["#{govern_ci.nsPath}!#{govern_ci.ciName}"]
      if settling
        if allowed_to_settle_approval?(a)
          a.state = settling['state'].presence || state
          a.expiresIn = (settling['expiresIn'].presence || expires_in.presence || 12 * 60).to_i
          a.comments = settling['comments'].presence || comments.presence || ''
          ok = a.settle(settling['token'] || token)
          if ok
            result_approvals << a
          else
            error = "#{govern_ci.nsPath.split('/')[3]} - #{govern_ci.ciName}: #{a.errors.full_messages.join('<br>')}"
            break
          end
        else
          return unauthorized
        end
      end
    end

    respond_to do |format|
      format.js do
        flash[:error] = error if error
      end

      format.json do
        if error
          render :json => {:errors => [error]}, :status => :unprocessable_entity
        else
          render :json => result_approvals
        end
      end
    end
  end


  protected

  def read_only_request?
    action_name == 'settle' || super
  end


  private

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    @environment = locate_environment(params[:environment_id], @assembly)
    @deployment  = Cms::Deployment.find(params[:deployment_id])
  end

  def load_approvals
    @approvals = Cms::DeploymentApproval.all(:params => {:deploymentId => @deployment.deploymentId})
  end
end
