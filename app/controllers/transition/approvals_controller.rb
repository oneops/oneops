class Transition::ApprovalsController < ApplicationController
  before_filter :find_parents
  before_filter :load_approvals

  def index
    render :json => @approvals
  end

  def settle
    # Approval can be designated by 'approvalId' or combination of governCi nsPath and ciName concatenated with '!'.
    approvals_to_settle_map = params[:approvals].to_map { |a| a[:approvalId] }
    comments                = params[:comments]
    state                   = params[:state]
    expires_in              = params[:expiresIn]
    approvals_to_settle = @approvals.inject([]) do |r, a|
      govern_ci = a.govern_ci
      settling = approvals_to_settle_map[a.approvalId.to_s] || approvals_to_settle_map["#{govern_ci.nsPath}!#{govern_ci.ciName}"]
      if settling
        if allowed_to_settle_approval?(a)
          r << {:approvalId   => a.approvalId,
                :deploymentId => @deployment.deploymentId,
                :state        => settling['state'].presence || state,
                :expiresIn    => (settling['expiresIn'].presence || expires_in.presence || 12 * 60).to_i,
                :comments     => settling['comments'].presence || comments.presence || ''}
        else
          return unauthorized
        end
      end
      r
    end

    approvals, message = Cms::DeploymentApproval.settle(approvals_to_settle)
    error = "Failed to settle approvals: #{message}" unless approvals

    respond_to do |format|
      format.js do
        if approvals
          @deployment.reload
        else
          flash[:error] = error
        end
      end

      format.json do
        if approvals
          render :json => approvals
        else
          render :json => {:errors => [error]}, :status => :unprocessable_entity
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
