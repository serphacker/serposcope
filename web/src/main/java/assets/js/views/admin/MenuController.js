/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.adminMenuController = function () {
    
    var menu = function() {
        $('#import-sql-btn').click(function(){$('#sql-import-modal').modal();});
    };
    
    var oPublic = {
        menu: menu
    };
    
    return oPublic;

}();
