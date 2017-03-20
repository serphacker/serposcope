/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope, canonLoc */

serposcope.adminDebugController = function () {

    var initDateRange = function () {
        var maxDate = moment();
        $('#daterange').daterangepicker({
            "ranges": {
                'Last 90 days': [moment(maxDate).subtract(90, 'days'), moment(maxDate)],
                'Last 30 days': [moment(maxDate).subtract(30, 'days'), moment(maxDate)],
                'Current Month': [moment(maxDate).startOf('month'), moment(maxDate).endOf('month')],
                'Previous Month': [moment(maxDate).subtract(1, 'month').startOf('month'), moment(maxDate).subtract(1, 'month').endOf('month')]
            },
            locale: {
                format: 'YYYY-MM-DD'
            },
            startDate: moment(maxDate).subtract(30, 'days'),
            endDate: moment(maxDate),
            showDropdowns: true
        }, function(start,end,label){
            $('[name="startDate"]').val(start.format("YYYY-MM-DD"));
            $('[name="endDate"]').val(end.format("YYYY-MM-DD"));
        });
        $('[name="startDate"]').val(moment(maxDate).subtract(90, 'days').format("YYYY-MM-DD"));
        $('[name="endDate"]').val(moment(maxDate).format("YYYY-MM-DD"));
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

    var render = function () {
        initDateRange();
    };

    var oPublic = {
        render: render
    };

    return oPublic;

}();
