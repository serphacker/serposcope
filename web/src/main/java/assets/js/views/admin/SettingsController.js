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
    
    var testCaptcha = function(elt){
        var service = $(elt.currentTarget).attr('data-service');
        var data = {
            service: service
        };
        
        switch(service){
            case "dbc":
                data.user = $('#dbcUser').val();
                data.pass = $('#dbcPass').val();
                break;
                
            case "decaptcher":
                data.user = $('#decaptcherUser').val();
                data.pass = $('#decaptcherPass').val();
                break;
                
            case "anticaptcha":
                data.api = $('#anticaptchaApiKey').val();
                break;
                
            case "twocaptcha":
                data.api = $('#twoCaptchaKey').val();
                break;
                
            case "imagetyperz":
                data.api = $('#imageTyperzKey').val();
                break;                
        }
        
        $.ajax({
            url: $(this).attr("href"),
            data: data,
            success: function(result){
                alert(result);
            }
        });
        return false;
    };
    
    var clickPruneNow = function(){
        if(confirm($(this).attr("data-warning"))){
            $('<form>', {
                'action': $(this).attr("data-action"),
                'method': 'post',
                'target': '_top'
            }).append($('<input>', {
                'name': '_xsrf',
                'value': $('#_xsrf').attr("data-value"),
                'type': 'hidden'
            })).append($('<input>', {
                'name': 'pruneRuns',
                'value': $('#pruneRuns').val(),
                'type': 'hidden'
            })).appendTo(document.body).submit();
        }
    };
    
    var settings = function() {
        $('#btn-reset-settings').click(resetSettings);
        $('.btn-test-captcha').click(testCaptcha);
        $('.btn-prune-now').click(clickPruneNow);
    };
    
    var oPublic = {
        settings: settings
    };
    
    return oPublic;

}();
