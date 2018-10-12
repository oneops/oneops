namespace :misc do
  ENV['CMS_TRACE'] = 'false'
  desc "Dump azure assemblies with tags to 'azure_assemblies.csv' and 'azure_assemblies.yaml'\nUsage:\n\trake misc:azure_assemblies"

  task :azure_assemblies => :environment do |t, args|

    query = {
      :size  => 0,
      :query => {
        :bool => {
          :must => [
            {
              :wildcard => {
                'ciClassName.keyword' => 'bom*Compute'
              }
            },
            {
              :wildcard => {
                'workorder.cloud.ciName' => 'az'
              }
            }
          ]
        }
      },
      :aggs  => {
        :nsPath => {
          :terms => {
            :field => 'workorder.payLoad.Environment.nsPath.keyword',
            :size  => 9999
          }
        }
      },
      :sort  => {
        'ciId' => 'asc'
      }
    }

    data   = Search::Base.search_raw('cms-all/ci/_search', query)
    result = data['aggregations']['nsPath']['buckets']

    @ots = CiTags::OrgTags.new
    @ats = CiTags::AssemblyTags.new
    # result = JSON.parse(File.read('/Users/lkhusid/temp/es.json'))
    yaml = {}
    csv  = ['org,assembly,compute_count,org_owner,assembly_owner,org_tags,assembly_tags']
    result.each_with_index do |bucket, i|
      _, org, assembly, _ = bucket['key'].split('/')
      ot              = @ots.get(org)
      at              = @ats.get(org, assembly)
      o_owner         = ot[:owner]
      a_owner         = at[:owner]
      o_tags          = ot[:tags].map {|t, v| "#{t}=#{v}"}.join('; ')
      a_tags          = at[:tags].map {|t, v| "#{t}=#{v}"}.join('; ')
      compute_count   = bucket['doc_count']
      csv << %(#{org},#{assembly},#{compute_count},#{o_owner},#{a_owner},"#{o_tags}","#{a_tags}")

      yaml_org = yaml[org]
      unless yaml_org
        yaml_org = {:computes => 0, :owner => o_owner, :tags => ot[:tags], :assemblies => {}}
        yaml[org]  = yaml_org
      end
      yaml_org[:computes] += compute_count
      yaml_org[:assemblies][assembly] = {:computes => compute_count, :owner => a_owner, :tags => at[:tags]}
    end

    File.write('azure_assemblies.csv', csv.join("\n"))
    File.write('azure_assemblies.yml', yaml.to_yaml)
  end
end
