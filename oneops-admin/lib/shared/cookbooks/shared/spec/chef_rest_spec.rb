require 'spec_helper'

describe Chef::REST do
	let (:chef) {
		c = Chef::REST.new
	}

	describe ".download_file_single" do
	    let (:remote_file) { "https://raw.githubusercontent.com/oneops/oneops/master/oneops-admin/lib/shared/hiera.yaml"  }
	    let (:local_file) { Dir::Tmpname.make_tmpname "/tmp/hiera", nil}

		after(:each) do
			FileUtils.rm local_file
		end

		it ".download_file_single" do
			stub_const("RUBY_VERSION", "1.8.7")
			expect { chef.download_file_single(remote_file,local_file) }.to_not raise_error
			expect(::File.exists?(local_file)).to be_truthy
		end

		it ".download_file_single" do
			stub_const("RUBY_VERSION", "2.0.0")
			expect { chef.download_file_single(remote_file,local_file) }.to_not raise_error
			expect(::File.exists?(local_file)).to be_truthy
		end
	end

	describe ".probe_url" do 
	    let (:remote_file) { "https://raw.githubusercontent.com/oneops/oneops/master/oneops-admin/lib/shared/hiera.yaml"  }
	    let (:local_file) { Dir::Tmpname.make_tmpname "/tmp/hiera", nil}


		it ".probe_url" do
			stub_const("RUBY_VERSION", "1.8.7")
			expect { chef.probe_url(remote_file) }.to_not raise_error
		end

		it ".probe_url" do
			stub_const("RUBY_VERSION", "2.0.0")
			expect { chef.probe_url(remote_file) }.to_not raise_error
		end
	end

	describe ".calculate_parts" do
		let (:content_length) { 10485760 } 

		it ".calculate_parts" do 
			expect { chef.calculate_parts(content_length) }.to_not raise_error
		end
	end
end