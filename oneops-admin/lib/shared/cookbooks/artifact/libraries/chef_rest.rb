require 'net/http'
if Gem::Version.new(RUBY_VERSION.dup) < Gem::Version.new('2.0.0')
  require 'net/https'
end
require 'tempfile'
require 'uri'
require 'openssl'

class Chef
  class REST
    def exit_with_error(msg)
      puts "***FAULT:FATAL=#{msg}"
      Chef::Application.fatal!(msg)
    end

    def streaming_request(url, headers, local_path, &block)
      chunk_minimum = 1048576 * 2 # 1 Mb * 2
      num_chunk_max = 10 # maximum of part download in parallel
      headers = probe_url(url)
      content_length = headers.has_key?("content-length") ? headers["content-length"][0].to_i : 0
      accept_ranges = (headers["accept-ranges"].nil? || headers["accept-ranges"].empty?) ? "" : headers["accept-ranges"][0]
      remote_url = headers["location"].nil? ? url : headers["location"][0]

      if (accept_ranges != "bytes") || (content_length <= chunk_minimum)
        # doesn't support range request
        local_tmp = Tempfile.new(File.basename(local_path, ".*"), File.dirname(local_path))
        local_tmp.binmode
        download_file_single(remote_url, local_tmp.path)
        local_tmp.flush
      else
        # server support range request
        parts_details = calculate_parts(content_length, num_chunk_max, chunk_minimum)
        local_tmp = fetch(remote_url, local_path, parts_details)
      end
      local_tmp
    end

    def probe_url(url)
      url_path = url.to_s
      uri = URI(url_path)

      ssl = uri.scheme == "https" ? true : false
      headers_h, headers = nil
      if Gem::Version.new(RUBY_VERSION.dup) >= Gem::Version.new('2.0.0')
        Net::HTTP.start(uri.host, uri.port, :use_ssl => ssl) { |http|
          url_path = !uri.query.nil? ? "#{uri.path}?#{uri.query}" : uri.path
          headers = http.head(url_path)
          headers_h = headers.to_hash
          if headers.code == "301" || headers.code == "307"
            new_url = headers_h["location"]
            headers_h = probe_url(URI(new_url[0]))
            headers_h["location"] = new_url
          end

        }
      else
        req = Net::HTTP.new(uri.host,uri.port)
        if ssl
          req.use_ssl = true
          req.verify_mode = OpenSSL::SSL::VERIFY_NONE
        end

        req.start { |http|
          url_path = !uri.query.nil? ? "#{uri.path}?#{uri.query}" : uri.path
          headers = http.head(url_path)
          headers_h = headers.to_hash
          if headers.code == "301" || headers.code == "307"
            new_url = headers_h["location"]
            headers_h = probe_url(URI(new_url[0]))
            headers_h["location"] = new_url
          end
        }
      end
      exit_with_error "error message: #{headers.message} .. error code: #{headers.code} .. #{url}" if headers.code.to_i >= 400
      headers_h
    end

    def download_file_single(remote_file, local_file)
      Chef::Log.info("Saving file to #{local_file}")
      Chef::Log.info("Fetching file: #{remote_file}")

      uri = remote_file.class.to_s == "String" ? URI(remote_file) : remote_file

      ssl = uri.scheme == "https" ? true : false

      if Gem::Version.new(RUBY_VERSION.dup) >= Gem::Version.new('2.0.0')
        Net::HTTP.start(uri.host, uri.port, :use_ssl => ssl) do |http|
          request = Net::HTTP::Get.new uri

          http.request request do |response|
            open local_file, 'wb' do |io|
              response.read_body do |chunk|
                io.write chunk
              end
            end
          end
        end
      else
        http = Net::HTTP.new(uri.host,uri.port)
        req = Net::HTTP::Get.new(uri.request_uri)
        if ssl
          http.use_ssl = true
          http.verify_mode = OpenSSL::SSL::VERIFY_NONE
        end

        http.request req do |response|
          open local_file, 'wb' do |io|
            response.read_body do |chunk|
              io.write chunk
            end
          end
        end
      end
    end

    def fetch(_uri, local_path, parts, resume=false)
      uri = _uri.class.to_s == "String" ? URI(_uri) : _uri
      full_path = "#{uri.scheme}://#{uri.host}:#{uri.port}#{uri.path}"
      Chef::Log.info("Fetching resume is set to #{resume}")
      Chef::Log.info("Remote: #{full_path}")
      Chef::Log.info("Local: #{local_path}")
      Chef::Log.info("Fetching in #{parts.length} parts")
      Chef::Log.info("Part details: #{pp parts.inspect}")
      # todo.. resume mode
      #install parallel gem, for windows make sure it installs into chef-dedicated instance of ruby
      if RUBY_PLATFORM =~ /mswin|mingw|cygwin/
        `c:\\opscode\\chef\\embedded\\bin\\gem install parallel -v 1.3.3`
      else
        require 'rubygems'

        begin
          gem 'parallel'
        rescue Gem::LoadError
          system("gem install parallel -v 1.3.3")
          Gem.clear_paths
        end
      end

      require 'parallel'

      download_start = Time.now
      Chef::Log.info("Fetching start at #{download_start}")

      Parallel.map(parts, :in_threads => 5) do |part|
        part_file = "#{local_path}.#{part['slot']}.tmp"
        download_file(part, full_path, part_file)
      end

      download_elapsed = Time.now - download_start

      Chef::Log.info("Download took #{download_elapsed} seconds to complete")

      failure_flag = false

      parts.each do |part|
        part_file = "#{local_path}.#{part['slot']}.tmp"
        size = File.size(part_file)
        if size != part['size']
          Chef::Log.warn("Slot: #{part['slot']} comparing file size: #{size} to expected size: #{part['size']}")
          Chef::Log.warn("Part size does not match downloaded file size")
          Chef::Log.warn("File: #{part_file} seems to not have completed its download, please retry and verify")
          failure_flag = true
        end
      end

      unless !failure_flag
        Chef::Log.fatal("File: #{local_path} size is not what is expected")
        return nil
      end

      assemble_start = Time.now

      Chef::Log.info("Assembling parts start at #{assemble_start}")

      tmp_file = assemble_file(local_path, parts)
      assemble_elapsed = Time.now - assemble_start

      Chef::Log.info("Assembling took #{assemble_elapsed} seconds to complete")

      tmp_file
    end

    def assemble_file(local_path, parts)
      temp_file = Tempfile.new(File.basename(local_path, ".*"), File.dirname(local_path))
      temp_file.binmode

      parts.each do |part|
        file="#{local_path}.#{part['slot']}.tmp"
        temp_file.write(File.open(file, 'rb').read)
      end

      temp_file.flush

      # Remove the temp part file
      parts.each do |part|
        file="#{local_path}.#{part['slot']}.tmp"
        File.delete(file)
      end

      temp_file
    end

    def download_file(part, remote_file, local_file)
      Chef::Log.info("Saving file to #{local_file}")
      Chef::Log.info("Fetching file: #{remote_file} part: #{part['slot']} [Start: #{part['start']} End: #{part['end']}]")

      uri = remote_file.class.to_s == "String" ? URI(remote_file) : remote_file

      ssl = uri.scheme == "https" ? true : false

      if Gem::Version.new(RUBY_VERSION.dup) >= Gem::Version.new('2.0.0')
        Net::HTTP.start(uri.host, uri.port, :use_ssl => ssl) do |http|
          request = Net::HTTP::Get.new uri
          Chef::Log.info("Requesting slot: #{part['slot']} from [#{part['start']} to #{part['end']}]")
          request.add_field('Range', "bytes=#{part['start']}-#{part['end']}")

          http.request request do |response|
            open local_file, 'wb' do |io|
              response.read_body do |chunk|
                io.write chunk
              end
            end
          end
        end
      else
        http = Net::HTTP.new(uri.host,uri.port)
        req = Net::HTTP::Get.new(uri.request_uri)
        Chef::Log.info("Requesting slot: #{part['slot']} from [#{part['start']} to #{part['end']}]")
        req.add_field('Range', "bytes=#{part['start']}-#{part['end']}")
        if ssl
          http.use_ssl = true
          http.verify_mode = OpenSSL::SSL::VERIFY_NONE
        end

        http.request req do |response|

          open local_file, 'wb' do |io|
            response.read_body do |chunk|
              io.write chunk
            end
          end
        end
      end
    end

    def calculate_parts(content_length, parts=10, chunk_size=1048576)
      parts_details = []

      if content_length/chunk_size < parts
        chunk_parts = content_length / chunk_size

        # The remainder will be the amount of bytes left as a percentage of the size of a part.
        content_remainder = content_length % chunk_size # e.g. 31521931 % 10 = 1
      else
        chunk_size = content_length / parts
        chunk_parts = parts

        # The remainder will be the number of bytes left on the last part. If max parts then it would be the 11th part. part[10]
        content_remainder = content_length % parts # e.g. 31521931 % 10 = 1
      end

      # The -1 accounts for this being array positions.
      chunk_size = chunk_size - 1
      byte_start = 0
      byte_end = byte_start + chunk_size

      (0..chunk_parts-1).each do |n|

        # Start at 0 or one after the end position of the last part.
        byte_start = (n==0) ? 0 : byte_end + 1

        # End at a chunk_size distance from start.
        byte_end = byte_start + chunk_size

        # Size is the total number of bytes in this part. The + 1 accounts for the start byte.
        byte_size = (byte_end.to_i - byte_start.to_i) + 1

        parts_details.push({'slot' => n, 'start' => byte_start, 'end' => byte_end, 'size' => byte_size})
      end

      unless (content_remainder == 0)
        # Since content_length == last_position+1; the +1 is not needed.
        byte_start = byte_end + 1
        byte_size = (content_length - byte_start)

        parts_details.push({'slot' => chunk_parts, 'start' => byte_start, 'end' => (content_length-1), 'size' => byte_size})
      end

      parts_details
    end
  end
end