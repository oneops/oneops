/*

 jQuery Textarea Fullscreen Editor v1.0
 Fullscreen text editor plugin for jQuery.

 :For more details visit http://github.com/CreoArt/jquery.textareafullscreen

 - CreoArt <support@creoart.org>
 - http://github.com/CreoArt

 Licensed under Apache - https://raw.githubusercontent.com/CreoArt/jquery.textareafullscreen/master/LICENSE

 */
(function($) {
    "use strict";

    function isFullscreen() {
        return $('.tx-editor-overlay').length > 0;
    }

    function relocate(el) {
        var yPos = ($(window).height() - el.height()) / 2;
        var xPos = ($(window).width() - el.width()) / 2;

        el.css({
            'top': yPos,
            'left': xPos
        });
    }

    function transitions($el, $editor) {
        relocate($editor);

        $el.focus();
        $el[0].setSelectionRange();
        if (!isFullscreen()) {
            $editor.css('opacity', 1);
        }
    }

    function FullscreenTextarea(el, opts) {
        var method,
            i;

        this.$el = $(el);

        this.settings = {
            overlay: true,
            maxWidth: '',
            maxHeight: ''
        };

        for( i = 0; i < this.bindMethods.length; i++) {
            method = this.bindMethods[i];
            this[method] = $.proxy(this, method);
        }

        this.init(opts);
    }

    FullscreenTextarea.prototype.$el = null;

    FullscreenTextarea.prototype.$editor = null;

    FullscreenTextarea.prototype.init = function (opts) {
        var content;

        this.settings = $.extend(true, this.settings, opts);

        if (!this.$el.is('textarea')) {
            $.error(
                'Error initializing Textarea Fullscreen Editor Plugin. It can only work on <textarea> element.'
            );
            return;
        }

        content = '<div class="tx-editor-wrapper"><div class="tx-editor"><div class="tx-toolbar">' +
          '<a class="tx-btn"><i class="fa fa-files-o"></i></a>' +
          '<a class="tx-btn"><i class="fa fa-expand"></i></a>' +
          '</div></div></div>';
        this.$wrapper = $(content).insertAfter(this.$el);
        this.$wrapper.css('width', this.$el.css('width'));
        this.$editor = this.$wrapper.find('.tx-editor');
        this.$editor.append(this.$el);

        this.$el.css({
            'width': '100%',
            'height': '100%',
            'resize': 'none'
        });

        //Fullscreen icon click event
      this.$editor.find('.tx-toolbar i.fa-expand').parent().on('click.txeditor.icon', this.onIconClick);
      var textArea = this.$el[0];
      copyToClipboard(this.$editor.find('.tx-toolbar i.fa-files-o').parent(),
                      function() {return textArea;});

    };


    FullscreenTextarea.prototype.bindMethods = ["onOverlayClick", "onIconClick", "onKeyUp", "onResize"];

    FullscreenTextarea.prototype.showOverlay = function () {
        $('<div class="tx-editor-overlay" />').appendTo('body')
            .fadeTo(0, 1)
            .click(this.onOverlayClick);
        return this;
    };

    FullscreenTextarea.prototype.removeOverlay = function () {
        var $overlay = $('.tx-editor-overlay');
        if ($overlay.length) {
            $overlay.fadeTo(0, 0, function () {
                $(this).remove();
            });
        }
        return this;
    };

    FullscreenTextarea.prototype.expand = function () {
        var settings = this.settings,
            $editor = this.$editor;

        if (settings.maxWidth) {
            $editor.css('max-width', settings.maxWidth);
        }
        if (settings.maxHeight){
            $editor.css('max-height', settings.maxHeight);
        }

        if (settings.overlay) {
            this.showOverlay();
        }

        $editor.addClass('expanded');
        transitions(this.$el, this.$editor);
        $editor.find('.tx-toolbar i.fa-expand').addClass('fa-compress').removeClass('fa-expand');

        //Adjust editor size on resize
        $(window).on('resize.txeditor', this.onResize);

        // ESC = closes the fullscreen mode
        $(window).on("keyup.txeditor", this.onKeyUp);

        return this;
    };

    FullscreenTextarea.prototype.minimize = function () {
        var settings = this.settings,
            $editor = this.$editor;

        $(window).off('resize.txeditor', this.onResize);
        $(window).off('keyup.txeditor', this.onKeyUp);

        $editor.removeClass('expanded')
            .css({
                'max-width': 'none',
                'max-height': 'none'
            });

        transitions(this.$el, $editor);

        $editor.find('.tx-toolbar i.fa-compress').removeClass('fa-compress').addClass('fa-expand');

        if (settings.overlay) {
            this.removeOverlay();
        }

        return this;
    };

    FullscreenTextarea.prototype.destroy = function () {
        var $wrapper = this.$wrapper;

        this.removeOverlay();

        this.$el = this.$el.detach();
        this.$el.insertBefore($wrapper);
        $wrapper.remove();

        this.$wrapper = null;
        this.$editor = null;

        $(window).off('keyup.txeditor', this.onKeyUp)
            .off('resize.txeditor', this.onResize);

        return this;
    };

    FullscreenTextarea.prototype.onOverlayClick = function () {
        this.minimize();
    };

    FullscreenTextarea.prototype.onIconClick = function (e) {
        e.preventDefault();

        if (isFullscreen()) {
            this.minimize();
        } else {
            this.expand();
        }
    };

    FullscreenTextarea.prototype.onKeyUp = function (e) {
        if (e.keyCode === 27 && isFullscreen()) {
            this.minimize();
        }
    };

    FullscreenTextarea.prototype.onResize = function (e) {
        relocate(this.$editor);
    };

    $.fn.textareafullscreen = function(options) {
        return this.each(function () {
            var $this = $(this),
                fullscreenTextarea, args;

            if ("string" === typeof options) {
                fullscreenTextarea = $this.data('textareafullscreendata');

                if ("function" === typeof fullscreenTextarea[options]) {
                    args = Array.prototype.slice.call(arguments, 1);
                    return fullscreenTextarea[options].apply(fullscreenTextarea, args);
                } else {
                    $.error('Method ' + options +
                    ' does not exist on jQuery.textareafullscreen');
                }
            } else {
                $this.data('textareafullscreendata', new FullscreenTextarea(this, options));
            }
        });
    };
})(jQuery);
