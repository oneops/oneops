require "rexml/document"
include REXML

module InductorUtil
  # This version number is used as the RubyGem release version.
  version = '1.1.0'
  jar = "target/inductor-#{version}.jar"
  # If we are in the unified build grab the version from the parent POM
  if File.file?('../pom.xml')
    doc = REXML::Document.new(File.new('../pom.xml' ))
    artifactId = XPath.first(doc, '//artifactId').text
    if !artifactId.nil? and artifactId.eql? 'oneops-parent'
      version = XPath.first(doc, '//version').text
      jar = "target/inductor-#{version}.jar"
    end
  end
  JAR = jar
  VERSION = version
end
