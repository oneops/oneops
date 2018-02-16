def is_propagate_update
  rfcCi = node.workorder.rfcCi
  if node.workorder.rfcCi.ciClassName =~ /bom\.oneops\.1\.Compute/ && (rfcCi.ciBaseAttributes.nil? || rfcCi.ciBaseAttributes.empty?) && rfcCi.has_key?('hint') && !rfcCi.hint.empty?
    hint = JSON.parse(rfcCi.hint)
    puts "rfc hint >> " + rfcCi.hint.inspect
    if hint.has_key?('propagation') && hint['propagation'] == 'true'
      return true;
    end
  end
  return false
end
