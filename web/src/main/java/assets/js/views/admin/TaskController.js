/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.adminTaskController = function () {
    
    var deleteRun = function(elt) {
        var date = $(elt.currentTarget).attr("data-date");
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Confirm delete run of the \"" + date + "\" ?\n" + 
            "All  position checked for this day will be erased")
        ){
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
    
    var rescanSerp = function(elt) {
        var date = $(elt.currentTarget).attr("data-date");
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Confirm SERP rescan of the " + date + " run ?\n" +
            "SERP rescan is only needed when : \n" +
            "_ Website added while a task was running\n" +
            "_ Manual import of SERP in the database\n"
        )){
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
        $('.btn-delete-run').click(deleteRun);
        $('.btn-rescan-serp').click(rescanSerp);
    };
    
    var oPublic = {
        render: render
    };
    
    return oPublic;

}();
