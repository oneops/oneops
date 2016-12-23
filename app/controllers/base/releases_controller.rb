class Base::ReleasesController < ApplicationController
  def restore
    label      = @environment ? 'environment' : 'design'
    ns_path    = @environment ? environment_manifest_ns_path(@environment) : assembly_ns_path(@assembly)
    @release   = Cms::Release.latest(:nsPath => ns_path)
    release_id = params[:id].to_i
    snapshot   = nil
    message    = nil
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
        @release    = Cms::Release.latest(:nsPath => ns_path) if ok
      end
    elsif !message
      message = "Unable to restore release: could not find any prior #{label} snapshots. Release restore feature is only available for releases committed after Jan, 11, 2017."
    end

    respond_to do |format|
      format.js do
        if message
          flash[:error] = message
          render :js => ''
        elsif @release.releaseState != 'open'
            flash[:alert] = "Restore release request resulted in no changes to the curret #{label}."
            render :js => 'hide_modal();'
        else
          render :js => "hide_modal(); window.location = '#{path_to_release(@release)}'"
        end
      end

      format.json do
        if message
          render :json => {:errors => [message]}, :status => :unprocessable_entity
        else
          render :json => @release
        end
      end
    end
  end
end
