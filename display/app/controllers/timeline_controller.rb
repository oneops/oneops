class TimelineController < ApplicationController
  before_filter :find_parents

  def show
    respond_to do |format|
      format.html

      format.js

      format.json do
        @timeline, error = fetch
        render_json_ci_response(@timeline, @timeline, error)
      end
    end
  end

  def page
    @timeline, error = fetch
    respond_to do |format|
      format.js do
        if @timeline
          @release = Cms::Release.latest(:nsPath => @environment ? environment_manifest_ns_path(@environment) : assembly_ns_path(@assembly))
        else
          flash[:error] = 'Faled to load timeline.'
          render :js => ''
        end
      end

      format.json { render_json_ci_response(@timeline, @timeline, [error]) }
    end
  end


  private

  def find_parents
    @assembly    = locate_assembly(params[:assembly_id])
    env_id       = params[:environment_id]
    @environment = locate_environment(env_id, @assembly) if env_id.present?
  end

  def fetch
    size   = (params[:size].presence || 20).to_i
    sort   = params[:sort].presence
    filter = params[:filter]
    search_params = {:sort => sort ? sort.values.first : 'desc'}

    if filter.present?
      query = filter[:query]
      if query.present?
        query_split = query.split(/\s+(AND\s+)?/i)
        query_split.each do |f|
          f = f.gsub(/(^\*)|(\*$)/, '')
          split = f.split(/[=:_]/)
          if split.size == 2 && split[0] == 'release'
            release = Cms::Release.find(split[1])
            return release ? [release] : [], nil
          elsif split.size == 2 && split[0] == 'deployment'
            release = Cms::Deployment.find(split[1])
            return release ? [release] : [], nil
          elsif (split.size == 1 && split[0].downcase == 'release') || (split[0].downcase == 'type' && split[1].downcase == 'release')
            search_params[:type] = 'release'
          elsif (split.size == 1 && split[0].downcase == 'deployment') || (split[0].downcase == 'type' && split[1].downcase == 'deployment')
            search_params[:type] = 'deployment'
          else
            search_params[:filter] = f
          end
        end
      end
    end

    search_params[:offset] = params[:offset]
    timeline, error = Cms::Timeline.fetch(@environment ? environment_ns_path(@environment) : design_ns_path(@assembly), size, search_params)
    if timeline && request.format.json?
      offset      = timeline.info[:offset]
      next_offset = timeline.info[:next_offset]
      response.headers['oneops-list-offset']      = offset.to_s if offset.present?
      response.headers['oneops-list-next-offset'] = next_offset.to_s if next_offset.present?
    end

    return timeline, error
  end
end
