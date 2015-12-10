directory "#{new_resource.shared_path}/bundle"

link "#{release_path}/bundle" do
  to "#{new_resource.shared_path}/bundle"
end
