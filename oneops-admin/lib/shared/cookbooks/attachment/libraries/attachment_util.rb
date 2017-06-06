require 'json'

def get_attachment_context
  if node.workorder.has_key?('rfcCi')
    action_name = node.workorder.rfcCi.rfcAction
    class_name = node.workorder.rfcCi.ciClassName
    attribute_to_look = 'run_on'
  else
    action_name = node.workorder.actionName
    class_name = node.workorder.ci.ciClassName
    attribute_to_look= 'run_on_action'
  end
  return action_name, class_name, attribute_to_look
end


