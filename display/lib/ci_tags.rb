module CiTags
  def self.parse_tags(ci_attributes)
    attrs   = ci_attributes || {}
    is_hash = attrs.is_a?(Hash)
    tags    = is_hash ? attrs['tags'] : ci_attributes.tags
    unless tags.is_a?(Hash)
      begin
        tags = tags ? JSON.parse(tags) : {}
      rescue
        tags = {}
      end
    end
    {:tags => tags, :owner => is_hash ? attrs['owner'] : attrs.owner}
  end

  class OrgTags
    def initialize
      @tags = {}
    end

    def get(org_name)
      puts @tags
      tags = @tags[org_name]

      return tags if tags
      begin
        org  = Cms::Ci.locate(org_name, '/', 'account.Organization')
        tags = ::CiTags.parse_tags(org.ciAttributes)
      rescue Exception
        tags = {}
      end
      @tags[org_name] = tags
    end
  end

  class AssemblyTags
    def initialize
      @tags = {}
    end

    def get(org_name, assembly_name)
      key = "#{org_name}/#{assembly_name}"
      tags = @tags[key]
      return tags if tags
      begin
        assembly = Cms::Ci.locate(assembly_name, "/#{org_name}", 'account.Assembly')
        tags = ::CiTags.parse_tags(assembly.ciAttributes)
      rescue Exception
        tags = {}
      end
      @tags[key] = tags
    end
  end
end

