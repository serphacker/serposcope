/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.GroupController = function () {
    
    
    var resizeTabContent = function(){
        $('.tab-content.tab-groups').css("min-height", serposcope.theme.availableHeight() - 100);
    };
    
    var deleteGroup = function(elt) {
        var name = $(elt.currentTarget).attr("data-name");
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Delete group \"" + name + "\" ?\nAll history will be erased.")){
            return false;
        }
        
        $('<form>', {
            'action': href,
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
        
        return false;
    };
    
    
    var render = function() {
        $(window).bind("load resize", function () {
            resizeTabContent();
        });
        $('.btn-delete-group').click(deleteGroup);
        $('#sidebar-group-search').typeahead({
            source: serposcope.sidebar.groupSuggest,
            minLength: 0,
            showHintOnFocus: true,
            highlighter: serposcope.sidebar.groupHighlighted,
            afterSelect: serposcope.sidebar.groupSelected
        });
    };
    
    var oPublic = {
        render: render
    };
    
    return oPublic;

}();
