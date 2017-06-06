#!/usr/bin/env ruby
require 'ruby_parser'
  require 'ruby_to_ruby_c'
  
  sexp = RubyParser.new.parse(File.read("calc_perf_buckets.rb"))
  c    = RubyToRubyC.new.process sexp
puts c
