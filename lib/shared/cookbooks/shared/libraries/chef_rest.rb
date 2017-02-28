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
        file="#{local_path}.tmp"
        download_file_single(remote_url, file)
        local_tmp = Tempfile.new(File.basename(local_path, ".*"), File.dirname(local_path))
        local_tmp.binmode
        local_tmp.write(File.open(file, 'rb').read)
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
      if Gem::Version.new(RUBY_VERSION.dup) > Gem::Version.new('1.8.7')
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
      exit_with_error "Error Message is: #{headers.message} ... Error Code is: #{headers.code} ... Location is: #{url}" if headers.code.start_with?("4") || headers.code.start_with?("5")
      headers_h
    end

    def download_file_single(remote_file, local_file)
      Chef::Log.info("Saving file to #{local_file}")
      Chef::Log.info("Fetching file: #{remote_file}")

      if Gem::Version.new(RUBY_VERSION.dup) < Gem::Version.new('2.0.0')
        url_uri = URI.parse(remote_file.to_s)
      else
        url_uri = URI(remote_file)
      end
      
      ssl = url_uri.scheme == "https" ? true : false

      if Gem::Version.new(RUBY_VERSION.dup) > Gem::Version.new('1.8.7')
        Net::HTTP.start(url_uri.host, url_uri.port, :use_ssl => ssl) do |http|
          request = Net::HTTP::Get.new url_uri

          http.request request do |response|
            open local_file, 'wb' do |io|
              response.read_body do |chunk|
                io.write chunk
              end
            end
          end
        end
      else
        http = Net::HTTP.new(url_uri.host,url_uri.port)
        req = Net::HTTP::Get.new(url_uri.request_uri)
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

    def fetch(uri, local_path, parts, resume=false)
      full_path = "#{uri.scheme}://#{uri.host}:#{uri.port}#{uri.path}"
      Chef::Log.info("Fetching resume is set to #{resume}")
      Chef::Log.info("Remote: #{full_path}")
      Chef::Log.info("Local: #{local_path}")
      Chef::Log.info("Fetching in #{parts.length} parts")
      Chef::Log.info("Part details: #{pp parts.inspect}")
      # todo.. resume mode
      install_gem_output = `gem install parallel -v 1.3.3` #install parallel gem
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
        if part['end'] != ''
          part_size = (part['slot'] + 1 == parts.length) ? part['size'] : part['size'] + 1
          if size != part_size
            Chef::Log.info("slot: #{part['slot']} comparing #{part_size} == #{part['size']}   fize_size = #{size}")
            Chef::Log.warn("File: #{part_file} does not seem to complete its download, please retry and verify")
            failure_flag = true
          end
        end
      end

      unless !failure_flag
        Chef::Log.fatal("File: #{local_path} failed to complete its download")
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
      uri = URI(remote_file)

      ssl = uri.scheme == "https" ? true : false

      if Gem::Version.new(RUBY_VERSION.dup) > Gem::Version.new('1.8.7')
        Net::HTTP.start(uri.host, uri.port, :use_ssl => ssl) do |http|
          request = Net::HTTP::Get.new uri.request_uri
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
      chunk_parts = content_length / chunk_size

      if chunk_parts >= parts
        chunk_size = content_length / parts
        chunk_parts = parts
      end

      content_remainder = content_length % chunk_parts # e.g. 31521931 % 10 = 1
      byte_end = 0

      (0..chunk_parts-1).each do |n|
        byte_start = (n*chunk_size == 0) ? 0 : (n*chunk_size) + 1
        byte_end = ((n*chunk_size)+chunk_size) <= content_length ? ((n*chunk_size)+chunk_size) : ''
        if byte_end == content_length # http server doesn't like the end of range to be
          byte_end = '' # the same as the content_length
        end
        byte_size = (byte_end == '') ? content_length - byte_start.to_i : byte_end.to_i - byte_start.to_i
        parts_details.push({'slot' => n, 'start' => byte_start, 'end' => byte_end, 'size' => byte_size})
      end

      unless (content_remainder == 0)
        byte_start = byte_end + 1
        byte_size = content_length - byte_start

        if byte_start == content_length
          parts_details[parts_details.length-1]['end'] = ''
          parts_details[parts_details.length-1]['size'] = parts_details[parts_details.length-1]['size'] + 1
        else
          parts_details.push({'slot' => chunk_parts, 'start' => byte_start, 'end' => '', 'size' => byte_size})
        end
      end

      last_slot = parts_details.length - 1
      last_slot_byte_end = parts_details[last_slot]['end']

      if parts_details[last_slot]['end'] != '' && parts_details[last_slot]['end'] < content_length
        byte_start = last_slot_byte_end + 1
        size = content_length - byte_start

        if byte_start == content_length
          parts_details[last_slot-1]['end'] = ''
          parts_details[last_slot-1]['size'] = parts_details[last_slot-1]['size'] + 1
        else
          parts_details.push({'slot' => last_slot + 1, 'start' => byte_start, 'end' => '', size => size})
        end
      end
      parts_details
    end
  end
end