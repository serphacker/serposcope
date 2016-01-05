/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.loader = function () {

    var run = function () {
        $('.csp-script').each(function (i, elt) {
            var groups = $(elt).attr('data-src').match(/^([a-zA-Z0-9.]+)\.([a-zA-Z0-9]+)$/);
            if (groups != null) {
                serposcope[groups[1]][groups[2]]();
            }
        });
    };
    
    var oPublic = {
        run: run
    };

    return oPublic;

}();

$(document).ready(function () {
    serposcope.loader.run();
});
