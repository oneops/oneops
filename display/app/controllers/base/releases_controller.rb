class Base::ReleasesController < ApplicationController
  def restore
    ns_path    = @environment ? environment_manifest_ns_path(@environment) : assembly_ns_path(@assembly)
    @release   = Cms::Release.latest(:nsPath => ns_path)
    release_id = params[:id].to_i
    snapshot   = nil
    message    = nil
    ok         = false

    begin
      snapshot = Search::Snapshot.find_by_ns_and_release_id(ns_path, release_id)
    rescue Exception => e
      message = "Failed to restore release - could not fetch base snapshot: #{e}"
    end

    if snapshot
      if @release.releaseState == 'open'
        message = 'There is already an open release.  Please discard current open release first before requesting restoration of a prior release.'
      else
        ok, message = Transistor.restore_release(snapshot, release_id)
        if ok
          @release = Cms::Release.latest(:nsPath => ns_path)
          @warnings = message
        end
      end
    elsif !message
      message = "Unable to restore release: could not find any prior #{@environment ? 'environment' : 'design'} snapshots. Release restore feature is only available for releases committed after Jan, 11, 2017."
    end
    respond_to do |format|
      format.js do
        if ok
          render 'base/releases/restore'
        else
          flash[:error] = message
          render :js => ''
        end
      end

      format.json do
        if ok
          render :json => {:release => @release, :warnings => [message]}
        else
          render :json => {:errors => [message]}, :status => :unprocessable_entity
        end
      end
    end
  end
end
