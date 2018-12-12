// This is a manifest file that'll be compiled into application.js, which will include all the files
// listed below.
//
// Any JavaScript/Coffee file within this directory, lib/assets/javascripts, vendor/assets/javascripts,
// or vendor/assets/javascripts of plugins, if any, can be referenced here using a relative path.
//
// It's not advisable to add code directly here, but if you do, it'll appear at the bottom of the
// the compiled file.
//
// WARNING: THE FIRST BLANK LINE MARKS THE END OF WHAT'S TO BE PROCESSED, ANY BLANK LINE SHOULD
// GO AFTER THE REQUIRES BELOW.
//
//= require prototype
//= require effects
//= require controls
//= require dragdrop
//= require jquery
//= require jquery_ujs
//= require jquery-ui
//= require bootstrap
//= require tinytable
//= require d3
//= require jquery.textareafullscreen
//= require diff_match_patch
//= require jquery.pretty-text-diff
//= require clipboard

var LOCAL_STORAGE_KEY_PREFIX = "oneops_display_",
    AFTER_SIGNIN_ANCHOR_KEY = LOCAL_STORAGE_KEY_PREFIX + "_after_signin_anchor";

// this allows jquery to be coexist with prototype without any conflicts.
// The only difference is all jquery functions should be called with $j instead of $,
// e.g. $j('#div_id').stuff() instead of $('#div_id').stuff().
var $j = jQuery.noConflict();
$j.ajaxSetup({
  'beforeSend': function (xhr) {xhr.setRequestHeader("Accept", "text/javascript")}
});

// This overrides rails standard confirm behaviour for links (from 'jquery_ujs" of jquery-rails).
// For now it is just to allow :confirm option to work (after it was forced to use 'data-confirm' in Rails 4).
// Later - TODO - we can also add nicer looking confirm dialogs.
$j.rails.allowAction = function (element) {
  var message = element.data('confirm') || element.attr("confirm"),
    answer = false, callback;
  if (!message) {
    return true;
  }
  var rails = $j.rails;
  if (rails.fire(element, 'confirm')) {
    answer = rails.confirm(message);
    callback = rails.fire(element, 'confirm:complete', [answer]);
  }
  return answer && callback;
};

Ajax.Responders.register({
  onCreate: function () {
    if (Ajax.activeRequestCount > 0) {
      $j(".ajax_busy").show();
    }
  },
  onException: function (e, b, c) {
    hide_modal();
    flash(null, "Failed to process, please try again later.");
    console.log(b);
  },
  onComplete: function (request, response) {
    if (Ajax.activeRequestCount == 0) {
      $j(".ajax_busy").hide();
    }
    if (response.status != "200" && response.status != "0") {
      hide_modal();
      flash(null, "Failed to process (code: " + response.status + "), please try again later.");
    }
  }
});

$j(document).ajaxStart(function() {
  $j(".ajax_busy").show();
});

$j(document).ajaxStop(function(a) {
  $j(".ajax_busy").hide();
});

$j(document).ajaxError(function(a, jqxhr, info, error) {
  hide_modal();
  flash(null, "Failed to process, please try again later...");
  console.log(jqxhr);
  console.log(info);
  console.log(error);
});

$j(function() {
  if (localStorage) {
    var anchor = location.hash;
    if (location.pathname == "/users/sign_in") {
      if (anchor) {
        localStorage.setItem(AFTER_SIGNIN_ANCHOR_KEY, anchor);
      }
    }
    else {
      var last_anchor = localStorage.getItem(AFTER_SIGNIN_ANCHOR_KEY);
      if (last_anchor) {
        localStorage.setItem(AFTER_SIGNIN_ANCHOR_KEY, "");
        if (!anchor) {
          location.hash = last_anchor;
        }
      }
    }
  }
});

window.disable_edit = function(container_id) {
  var form = $(container_id).down("form");
  if (form) {
    form.removeClassName("editing");
  }
  $$("#" + container_id + " input").each(function(input) {input.readOnly = true; input.addClassName('readonly')});
  $$("#" + container_id + " textarea").each(function(input) {input.readOnly = true; input.addClassName('readonly')});
  $$("#" + container_id + " input[type=checkbox]").each(function(input) {input.disabled = true});
  $$("#" + container_id + " select").each(function(input) {input.disabled = true});
  $$("#" + container_id + " img").each(function(img) {img.style.cursor = 'default'});
  $$("#" + container_id + " .controls div:not(.alert):not(.radio) a:not(.btn-check-box):not(.lock):not(.tx-btn):not([rel=tooltip])").each(function(a) {a.hide();});
  $$("#" + container_id + " .controls div.radio .btn").each(function(a) {a.addClassName('disabled');});
  $$("#" + container_id + " .controls .check-box").each(function(a) {a.addClassName('disabled');});
  $$("#" + container_id + " .instructions").each(function(a) {a.hide();});
  $j("#" + container_id + " .toolbar").hide();
};

window.enable_edit = function(container_id) {
  var form = $j("#" + container_id).find("form").addClass("editing");
  $$("#" + container_id + " .form-actions input").each(function(input) {input.readOnly = false; input.removeClassName('readonly')});
  $$("#" + container_id + " .controls:not([editable=false]) input").each(function(input) {input.readOnly = false; input.removeClassName('readonly')});
  $$("#" + container_id + " .controls:not([editable=false]) textarea").each(function(input) {input.readOnly = false; input.removeClassName('readonly')});
  $$("#" + container_id + " .controls:not([editable=false]) input[type=checkbox]").each(function(input) {input.disabled = false});

  //$$("#" + container_id + " .controls:not([editable=false]) select").each(function(input) {input.disabled = false});
  $$("#" + container_id + " .controls select").each(function(input) {input.disabled = false});
  //$$("#" + container_id + " .controls select > option").each(function(input) {input.disabled = false});
  $$("#" + container_id + " .controls[editable=false] select > option:not(:selected)").each(function(input) {input.disabled = true});

  $$("#" + container_id + " .controls:not([editable=false]) img").each(function(img) {img.style.cursor = 'pointer';});
  $$("#" + container_id + " .controls:not([editable=false]) a").each(function(a) {a.show();});
  $$("#" + container_id + " .controls .btn-check-box").each(function(a) {a.removeClassName('disabled');});
  $$("#" + container_id + " .controls div.radio .btn").each(function(a) {a.removeClassName('disabled');});
  $$("#" + container_id + " .controls:not([editable=false]) .check-box").each(function(a) {a.removeClassName('disabled');});
  $$("#" + container_id + " .controls:not([editable=false]) .instructions").each(function(a) {a.show();});
  $j("#" + container_id + " .toolbar").show();
};

window.block = function(what_to_block, what_to_toggle) {
  var what_to_block_element = $(what_to_block);
  what_to_block_element.style.position = "relative";
  var blocker_id = what_to_block + "_blocker";
  var blocker = $(blocker_id);
  if (!blocker) {
    blocker = new Element('div');
    blocker.id = blocker_id;
    blocker.style.display = 'none';
    blocker.style.position = 'fixed';
    blocker.style.zIndex = '99';
    blocker.style.top = '0';
    blocker.style.left = '0';
    blocker.style.width = '100%';
    blocker.style.height = '100%';
    blocker.style.backgroundColor = 'black';
    blocker.setOpacity(0.7);
    what_to_block_element.appendChild(blocker);
  }
  blocker.show();

  toggle(what_to_toggle);
};

window.unblock = function(what_to_unblock, what_to_toggle) {
  var blocker_id = what_to_unblock + "_blocker";
  var blocker = $(blocker_id);
  if (blocker) {
    blocker.hide();
  }

  toggle(what_to_toggle);
};

window.toggle = function(what_to_toggle) {
  if (what_to_toggle) {
    if (!Object.isArray(what_to_toggle)) {
      what_to_toggle = [what_to_toggle];
    }

    what_to_toggle.each(function(el) {
      if (typeof(el) == "string") {
        el = $(el);
      }
      el.toggle();
    });
  }
};

window.flash = function(notice, error, alert) {
  if (notice) {
    var fn = $('flash_notice');
    fn.down('.content').update(notice);
    fn.show();
    setTimeout("Effect.Fade('flash_notice', {duration: 2.0, from: " + fn.getOpacity() + "});", 3000)
  }
  if (alert) {
    fn = $('flash_alert');
    fn.down('.content').update(alert);
    fn.show();
  }
  if (error) {
//    var fe = $('flash_error');
//    fe.update(error);
//    fe.show();
//    setTimeout("Effect.Fade('flash_error', {duration: 2.0, from: " + fe.getOpacity() + "});", 7000)
    $j("#flash_error_modal div.modal-body").html(error);
    hide_modal();
    render_modal("flash_error_modal");
    $j("#flash_error_modal .modal-footer .btn").focus();
  }
};

window.center = function(el) {
  center_vertical(el);
  center_horizontal(el);
};

window.center_horizontal = function(el) {
  if (typeof(el) == "string") {
    el = $(el);
  }

  var element_size = el.getDimensions();
  var viewPort = document.viewport.getDimensions();
  var offsets = document.viewport.getScrollOffsets();
  var centerX = viewPort.width / 2 + offsets.left - element_size.width / 2;

  el.setStyle( { position: 'absolute', left: Math.floor(centerX) + 'px' } );
};

window.center_vertical = function(el) {
  if (typeof(el) == "string") {
    el = $(el);
  }

  var element_size = el.getDimensions();
  var viewPort = document.viewport.getDimensions();
  var offsets = document.viewport.getScrollOffsets();
  var centerY = viewPort.height / 2 + offsets.top - element_size.height / 2;

  el.setStyle( { position: 'absolute', top: Math.floor(centerY) + 'px'} );
};

window.sort_list = function(container, sortAttrs, desc) {
  container = $(container);
  var result;
  var items = container.childElements().sort(function(a, b) {
    result = 0;
    for (var i = 0; i < sortAttrs.length; i++) {
      var a_value = a.getAttribute(sortAttrs[i]);
      var b_value = b.getAttribute(sortAttrs[i]);
      if (a_value != null && b_value != null) {
        a_value = a_value.toLowerCase();
        b_value = b_value.toLowerCase();
        result = a_value < b_value ? -1 : (a_value > b_value ? 1 : 0);
        if (result != 0) {
          break;
        }
      }
      else if (a_value != null) {
        result = -1;
        break;
      }
      else if (b_value != null) {
        result = 1;
        break
      }
    }
    // console.log(result);
    return result * (desc ? -1 : 1);
  });
  items.each(function(li) {
    li.remove();
    container.appendChild(li);
  });
};

// Multi-term list filtering assumes that:
//  - terms are white-space delimited;
//  - each term is either general 'value' (match against any attribute) or in 'attr=value' form (match against
//    specific attribute only);
//  - terms are implicitly 'AND' concatenated (each term has to match);
window.filter_list = function (items, attribute_names, filter) {
  var terms = filter && filter.replace(/\s+AND(\s+|$)/gi, " ")
    .split(/\s+/)
    .map(function (t) {
      var split = t.split(/[:=]/);
      return {
        regex: new RegExp(split[split.length - 1], 'i'),
        attr:  split.length > 1 && split[0]
      }
    });
  for (var i = 0; i < items.length; i++) {
    var item = items[i];
    var match = !terms || terms.every(function (t) {
      var filter_regexp = t.regex,
        filter_attr = t.attr;
      return attribute_names.some(function (attr) {
        var value = item.getAttribute(attr);
        // return ((!filter_attr || attr == filter_attr) && (value == null || filter_regexp.test(value)));
        return ((!filter_attr || attr == filter_attr) && filter_regexp.test(value));
      });
    });
    match ? item.show() : item.hide();
  }
};

window.show_busy = function(message) {
  hide_modal();
  var modal = $j("#busy_modal");
  if (message) {
    modal.find(".modal-body .message").html(message);
  }
  modal.modal({backdrop: "static", keyboard: false});
};

window.hide_busy = function() {
  $j("#busy_modal").modal('hide');
};

window.render_modal = function(modal_id, html) {
  hide_modal();
  if (!$j("#" + modal_id)[0]) {
    $j("body").append(html);
  }
  $j("#" + modal_id).modal({backdrop: "static"});
};

window.hide_modal = function() {
  $j(".modal").modal('hide');
};

window.push_state = function(url, state) {
  if (history && history.pushState) {
    history.pushState(state || {path: url}, url, url);
  }
};

window.replace_state = function(url, state) {
  if (history && history.replaceState) {
    history.replaceState(state || {path: url}, url, url);
  }
};

//if (history && history.pushState) {
//  window.onpopstate = function() {
//    new Ajax.Request(location.href, {method: 'get'});
//  };
//}

function shadeColor(color, percent) {
  var num = parseInt(color.slice(1), 16),
    amt = Math.round(2.55 * percent),
    R = (num >> 16) + amt,
    B = (num >> 8 & 0x00FF) + amt,
    G = (num & 0x0000FF) + amt;
  return "#" + (0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 + (B < 255 ? B < 1 ? 0 : B : 255) * 0x100 + (G < 255 ? G < 1 ? 0 : G : 255)).toString(16).slice(1);
}

// http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
function colorBrightness(rgb) {
  return rgb.r * .299 + rgb.g * .587 + rgb.b * .114;
}

function getHashParam(name) {
  if (!location.hash) {
    return null;
  }

  var prefix = name == "#" ? name : ("/" + name + "/");
  var param = location.hash.split(prefix)[1];
  return param && param.split('/')[0];
}

function setHashParam(name, newValue) {
  var oldValue = getHashParam(name);
  var prefix = name == "#" ? name : ("/" + name + "/");
  if (oldValue && newValue && oldValue != newValue) {
    location.hash = location.hash.replace(prefix + oldValue, prefix + newValue);
  }
  else if (oldValue && !newValue) {
    location.hash = location.hash.replace(prefix + oldValue, "");
  }
  else if (!oldValue && newValue) {
    var loc = location.href,
        hash = location.hash,
        new_hash = prefix + newValue;
    replace_state(hash ? loc.replace(hash, hash + new_hash) : (loc + (prefix == "#" ? "" : "#") + new_hash));
  }
}

function copyToClipboard(trigger, target) {
  $j(trigger).one("click",
                  function (e) {
                    new Clipboard(this, {target: target})
                      .on('success', function (e) {
                            e.clearSelection();
                            $j(e.trigger).tooltip({title: "Copied to<br>clipboard!", html: true, placement: "bottom"})
                              .tooltip("show")
                              .on("mouseout", function (e) {
                                    $j(this).tooltip("destroy")
                                  });
                          })
                      .on('error', function (e) {
                            $j(e.trigger).tooltip({
                              title:     "Press<br>⌘-C / Ctrl-C<br>to copy",
                              html:      true,
                              placement: "bottom"
                            })
                              .tooltip("show")
                              .on("mouseout", function (e) {
                                    $j(this).tooltip("destroy")
                                  });
                          })
                      .onClick(e);
                  });
}

function showCopyToClipboardModal(title, content) {
  $j("#copy_to_clipboard_modal .modal-header h3").html(title);
  $j("#copy_to_clipboard_modal .modal-body").html(content);
  render_modal('copy_to_clipboard_modal');
}

function toggleAttrPropOwner(source) {
  source = $j(source);
  if (source.parents("form").hasClass('editing') && !source.parents(".control-group[editable=false]").length) {
    var input      = source.find("input[type=hidden]"),
        ownerValue = input.attr('data-owner-value');
    input.val(input.val() == ownerValue ? "" : ownerValue);
    source.find("i.fa").toggleClass("fa-lock fa-unlock");
  }
}

function selectText(el) {
  var range = document.createRange();
  range.selectNodeContents(el);
  var sel = window.getSelection();
  sel.removeAllRanges();
  sel.addRange(range);
}

function randomDomId() {
  return "x" + Math.round(Math.random() * 1000000).toString(36);
}

function formatKMBT(y) {
	var abs_y = Math.abs(y);
	if (abs_y >= 1000000000000)   { return y / 1000000000000 + "T" }
	else if (abs_y >= 1000000000) { return y / 1000000000 + "B" }
	else if (abs_y >= 1000000)    { return y / 1000000 + "M" }
	else if (abs_y >= 1000)       { return y / 1000 + "K" }
	else if (abs_y < 1 && y > 0)  { return y.toFixed(2) }
	else if (abs_y === 0)         { return '0' }
	else                          { return y }
}
function formatBase1024KMGTP(y) {
    var abs_y = Math.abs(y);
    if (abs_y >= 1125899906842624)  { return y / 1125899906842624 + "P" }
    else if (abs_y >= 1099511627776){ return y / 1099511627776 + "T" }
    else if (abs_y >= 1073741824)   { return y / 1073741824 + "G" }
    else if (abs_y >= 1048576)      { return y / 1048576 + "M" }
    else if (abs_y >= 1024)         { return y / 1024 + "K" }
    else if (abs_y < 1 && y > 0)    { return y.toFixed(2) }
    else if (abs_y === 0)           { return '' }
    else                            { return y }
}
