class Search::Snapshot < Search::Base
  self.prefix       = '/cms-2*/snapshot/_search'
  self.element_name = ''

  def self.find_by_ns_and_release_id(ns_path, release_id)
    search_params = {
      :query => {
        :bool => {
          :must => [{:term => {'namespace.keyword' => ns_path}},
                    {:range => {'release' => {'lte' => release_id}}}]
        }
      },
      :sort => {'release' => 'desc'},
      :size  => 1
    }

    result = nil
    begin
      data   = JSON.parse(post('', {}, search_params.to_json).body)['hits']['hits'].first
      result = data['_source'] if data
    rescue Exception => e
      handle_exception e, "Failed to perform snaphost search for nsPath: #{ns_path} and release: #{release_id}."
      raise e
    end
    result
  end
end
