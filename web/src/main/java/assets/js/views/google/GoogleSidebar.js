/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.googleSidebar = function () {
    
    var searchHighlighter = function(search){
        return search.name;
    };
    
    var searchSelected = function(search){
        window.location = "/google/" + search.group + "/search/" + search.id;
    };
    
    var targetHighlighter = function(target){
        return target.name;
    };
    
    var targetSelected = function(target){
        window.location = "/google/" + target.group + "/target/" + target.id;
    };
    
    var render = function(){
        $('#sidebar-group-search').typeahead({
            source: JSON.parse($('#sidebar-data').attr('data-groups')),
            minLength: 0,
            showHintOnFocus: true,
            highlighter: serposcope.sidebar.groupHighlighted,
            afterSelect: serposcope.sidebar.groupSelected
        });
        $('#sidebar-google-search-search').typeahead({
            source: JSON.parse($('#sidebar-data').attr('data-google-searches')),
            minLength: 0,
            showHintOnFocus: true,
            highlighter: searchHighlighter,
            afterSelect: searchSelected
        });
        $('#sidebar-target-search').typeahead({
            source: JSON.parse($('#sidebar-data').attr('data-google-targets')),
            minLength: 0,
            showHintOnFocus: true,
            highlighter: targetHighlighter,
            afterSelect: targetSelected
        });        
    };
    
    var oPublic = {
        render: render
    };
    
    return oPublic;

}();
