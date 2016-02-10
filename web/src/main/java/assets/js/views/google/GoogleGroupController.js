/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope, canonLoc, Papa */

serposcope.googleGroupController = function () {
    
    var resizeTabContent = function(){
        $('.tab-content').css("min-height", serposcope.theme.availableHeight() - 150);
    };
    
    var showNewTargetModal = function(){
        $('.modal').modal('hide');
        $('#new-target').modal();
        return false;
    };
    
    var deleteTarget = function(elt){
        var id = $(elt.currentTarget).attr("data-id");
        var name = $("#target-" + id +" .target-name").html();
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Delete website \"" + name + "\" ?\nAll history will be erased.")){
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
        })).append($('<input>', {
            'name': 'id[]',
            'value': id,
            'type': 'hidden'
        })).appendTo(document.body).submit();
        
        return false;
    };
    
    
    var deleteSearch = function(elt){
        var id = $(elt.currentTarget).attr("data-id");
        var name = $("#search-" + id +" .search-keyword").html();
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Delete search \"" + name + "\" ?\nAll history will be erased.")){
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
        })).append($('<input>', {
            'name': 'id[]',
            'value': id,
            'type': 'hidden'
        })).appendTo(document.body).submit();
        
        return false;
    };
    
    var deleteSearches = function(elt){
        if(!confirm("Delete searches ?\nAll history will be erased.")){
            return false;
        }        
        
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).append($('.chk-search'))
        .appendTo(document.body).submit();        
        
        return false;
    };
    
    var showNewSearchModal = function(){
        $('.modal').modal('hide');
        $('#new-search').modal();
        return false;
    };
    
    var showNewBulkSearchModal = function(){
        $('.modal').modal('hide');
        $('#new-search-bulk').modal();
        return false;
    };
    
    var bulkSearchSubmit = function(){
        var keyword = [], tld = [], datacenter = [], device = [], local = [], custom = [];
        if($('#bulk-search').val() == ""){
            alert("no search specified");
            return false;
        }
        
        var lines = $('#bulk-search').val().split(/\r?\n/);
        
        for(var i = 0; i< lines.length; i++){
            lines[i] = lines[i].replace(/(^\s+)|(\s+$)/g,"");
            if(lines[i].length == 0){
                continue;
            }
            
            var params = Papa.parse(lines[i]);
            if(params.data.length != 1){
                alert("error at line " + i + " : " + lines[i]);
                return;
            }
            params = params.data[0];
            keyword[i] = params[0];
            tld[i] = params.length > 1 ? params[1] : $('#csp-vars').attr('data-default-tld');
            datacenter[i] = params.length > 2 ? params[2]  : $('#csp-vars').attr('data-default-datacenter');
            if(params.length > 3){
                switch(params[3].toLowerCase()){
                    case "desktop":
                        device[i] = 0;
                        break;
                    case "mobile":
                        device[i] = 1;
                        break;  
                    default:
                        alert(params[3] + " is an invalid device type, valid values : desktop, mobile");
                        return false;
                }
            } else {
                device[i] = parseInt($('#csp-vars').attr('data-default-device'));
            }
            local[i] = params.length > 4 ? params[4]  : $('#csp-vars').attr('data-default-local');
            custom[i] = params.length > 5 ? params[5]  : $('#csp-vars').attr('data-default-custom');
        }
        
        var form = $('<form>', {
            'action': $("#bulk-import").attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        }));
        
        var inputs = [];
        for(var i=0; i<keyword.length; i++){
            if(typeof(keyword[i]) == "undefined"){
                continue;
            }
            inputs.push($('<input>', {'name': 'keyword[]','value': keyword[i],'type': 'hidden'})[0]);
            inputs.push($('<input>', {'name': 'tld[]','value': tld[i],'type': 'hidden'})[0]);
            inputs.push($('<input>', {'name': 'datacenter[]','value': datacenter[i],'type': 'hidden'})[0]);
            inputs.push($('<input>', {'name': 'device[]','value': device[i],'type': 'hidden'})[0]);
            inputs.push($('<input>', {'name': 'local[]','value': local[i],'type': 'hidden'})[0]);
            inputs.push($('<input>', {'name': 'custom[]','value': custom[i],'type': 'hidden'})[0]);
        }
        form.append(inputs);
        form.appendTo(document.body).submit();
        
        return false;
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
    
    var toggleEvent = function(elt) {
        $('#event-description-' + $(elt.currentTarget).attr('data-id')).toggleClass("hidden");
    };
    
    var addEventModal = function(elt){
        $('#modal-add-event').modal();
    };
    
    var deleteEvent = function(elt){
        var day = $(elt.currentTarget).attr("data-day");
        var href= $(elt.currentTarget).attr("href");
        
        if(!confirm("Delete event \"" + day + "\" ?")){
            return false;
        }
        
        $('<form>', {
            'action': href,
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': 'day',
            'value': day,
            'type': 'hidden'
        })).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
        
        return false;        
    };
    
    var renameGroup = function(elt){
        var href = $(elt.currentTarget).attr("href");
        var name = prompt("new group name");
        
        $('<form>', {
            'action': href,
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': 'name',
            'value': name,
            'type': 'hidden'
        })).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
        
        return false;
    };
    
    var onRadioTargetChange = function(){
        $("#pattern").attr('placeholder', $(this).attr("data-help"));
    };
    
    var searchChecked = false;
    var checksearch = function(){
        $('.chk-search').prop('checked',searchChecked=!searchChecked ? 'checked' : '');
        return false;
    };
    
    var exportSearches = function(elt){
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).append($('.chk-search'))
        .appendTo(document.body).submit();
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
                    if( item.indexOf(array[i]) == -1){
                        return false;
                    }
                }
                return true;
            },
            highlighter: function (item) {return item;}
        });
    };
    
    var renderScoreHistory = function() {
        $('.score-history-inline').sparkline("html", {tagValuesAttribute: "data-values"});        
    };
    
    var view = function() {
        $(window).bind("load resize", function () {
            resizeTabContent();
        });
        $('input[name="day"]').daterangepicker({
            singleDatePicker: true,
            locale: {
                format: 'YYYY-MM-DD'
            }
        });
        
        $('.target-radio').change(onRadioTargetChange);
        $("#pattern").attr('placeholder', $('#target-domain').attr("data-help"));
//        $('#radio-domain').change(onHelperDomain);
//        $('#radio-subdomain').change(onHelperSubdomains);
//        $('#radio-url').change(onHelperURL);
//        $('#input-helper').bind("keyup paste change",onHelperChange);
        
        $('.btn-rename').click(renameGroup);
        $('.toggle-event').click(toggleEvent);
        $('.btn-add-event').click(addEventModal);
        $('.btn-delete-event').click(deleteEvent);
        
        $('.btn-delete-group').click(deleteGroup);
        $('.btn-add-target').click(showNewTargetModal);
        $('.btn-add-search').click(showNewSearchModal);
        $('.btn-add-search-bulk').click(showNewBulkSearchModal);
        $('#bulk-import').click(bulkSearchSubmit);
        
        $('.btn-delete-target').click(deleteTarget);
        $('.btn-delete-search').click(deleteSearch);
        
        $('#btn-chk-search').click(checksearch);
        $('#btn-export-searches').click(exportSearches);
        $('#btn-delete-searches').click(deleteSearches);
        renderScoreHistory();
        loadAsyncCanonical();
    };
    
    var oPublic = {
        view: view
    };
    
    return oPublic;

}();
