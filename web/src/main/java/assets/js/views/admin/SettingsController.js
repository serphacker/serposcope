/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.adminSettingsController = function () {
    
    var resetSettings = function(elt){
        $('<form>', {
            'action': $(this).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
    };
    
    var settings = function() {
        $('#btn-reset-settings').click(resetSettings);
    };
    
    var oPublic = {
        settings: settings
    };
    
    return oPublic;

}();
