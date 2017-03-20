/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.adminProxyController = function () {
    
    var checked = false;
    
    var checkproxies = function(){
        $('.chk-proxy').prop('checked',checked=!checked ? 'checked' : '');
        return false;
    };
    
    var deleteProxies = function(elt){
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).append($('.chk-proxy'))
        .appendTo(document.body).submit();
    };
    
    var deleteProxiesInvalid = function(elt){
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).append($('.chk-proxy'))
        .appendTo(document.body).submit();
    };    
    
    var checkProxies = function(elt){
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
    };
    
    var abortCheck = function(elt){
        $('<form>', {
            'action': $(elt.currentTarget).attr("data-action"),
            'method': 'post',
            'target': '_top'
        }).append($('<input>', {
            'name': '_xsrf',
            'value': $('#_xsrf').attr("data-value"),
            'type': 'hidden'
        })).appendTo(document.body).submit();
    };   
    
    var newProxyModal = function(){
        $('#new-proxy').modal();
    };
    
    var render = function() {
        $("#btn-chk-proxy").click(checkproxies);
        $("#btn-delete-proxy").click(deleteProxies);
        $("#btn-delete-proxy-invalid").click(deleteProxiesInvalid);
        $('#new-proxy').on('shown.bs.modal', function(){ $('textarea[name="proxies"]').focus(); });
        $("#btn-add-proxy").click(newProxyModal);
        $("#btn-check-proxy").click(checkProxies);
        $("#btn-abort-proxy").click(abortCheck);
        $('.table-proxy').stupidtable();
    };
    
    var oPublic = {
        render: render
    };
    
    return oPublic;

}();
