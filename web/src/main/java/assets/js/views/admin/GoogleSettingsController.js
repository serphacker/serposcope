/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global canonLoc, serposcope */

serposcope.adminGoogleSettingsController = function () {
    
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
    
    var loadAsyncCanonical = function() {
        $.ajax({
            url: '/assets/js/canonical-location.js',
            dataType: 'script',
            cache: true, // otherwise will get fresh copy every page load
            success: function () {
                configureSearchLocal();
            } 
        });
    };
    
    var configureSearchLocal = function(){
        $('.search-local').typeahead({
            source: canonLoc,
            minLength: 2,
            items: 100,
            matcher: function(arg){
                var item = arg;
                var array = this.query.split(" ");
                for(var i=0; i<array.length; i++){
                    if( item.indexOf(array[i]) === -1){
                        return false;
                    }
                }
                return true;
            },
            highlighter: function (item) {return item;}
        });
    };
    
    var settings = function() {
        $('#btn-reset-settings').click(resetSettings);
        loadAsyncCanonical();
    };
    
    var oPublic = {
        settings: settings
    };
    
    return oPublic;

}();
