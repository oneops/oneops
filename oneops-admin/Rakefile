# Rakefile
task :rename_exec_gems do
  puts "renaming exec-gems-az.yaml to exec-gems.yaml"

  f = File.expand_path('lib/shared', File.dirname(__FILE__))
  Dir.chdir f
  File.rename('exec-gems-az.yaml','exec-gems.yaml')
end

task default: :rename_exec_gems