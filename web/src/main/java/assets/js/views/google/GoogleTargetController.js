/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.googleTargetController = function () {

    var HEADER_SIZE = 100;

    var resize = function () {
        var height = serposcope.theme.availableHeight() - HEADER_SIZE;
        $('#google-target-variation-container').css("min-height", (height) + "px");
        $('#google-target-table-container').css("min-height", (height) + "px");
        $('#google-target-chart').css("height", (height) + "px");
        renderChart();
    };

    var maxRank = 50;
    var maxRankOffset = 5;
    var minRank = 0;
    var minRankOffset = -5;
    var chart = null;
    var chartData = [];
    var chartOptions = {
        labels: ["Date", "###CALENDAR###"],
        legend: "always",
        labelsDivWidth: "100%",
        labelsDiv: "google-target-legend",
        drawPoints: true,
        pointSize: 2,
        highlightSeriesBackgroundAlpha: 0.8,
        highlightSeriesOpts: { strokeWidth: 2 },
        drawHighlightPointCallback : function(g, seriesName, canvasContext, cx, cy, color, pointSize){},
        
        axes: {
            x: {
                drawGrid: false,
                axisLabelWidth: 120,
                pixelsPerLabel: 80,
                axisLabelFontSize: 12,
                axisLabelFormatter: function (d, gran) {
                    return new Date(d).format("yyyy-mm-dd");
                },
                valueFormatter: function (ms) {
                    return "<strong>" + new Date(ms).format("yyyy-mm-dd") + "</strong>";
                }
            },
            y: {
                axisLabelWidth: 20,
                axisLabelFontSize: 12,
                valueFormatter: function (y) {
                    return "<strong>" + y + "</strong>";
                }
            }
        },
        valueRange: [maxRank + maxRankOffset, minRank + minRankOffset]
    };
    
    var renderChart = function () {
        if(document.getElementById("google-target-chart") == null){
            $('.calendar-annotation').popover({
                html: true,
                placement: "bottom"
            });            
            return;
        }
        
        if (chartData.length == 0) {
            chart = new Dygraph(document.getElementById("google-target-chart"), "X\n", chartOptions);
            return;
        }
        chart = new Dygraph(document.getElementById("google-target-chart"), chartData, chartOptions);
        chart.ready(function () {
            var events = JSON.parse($("#csp-vars").attr("data-events"));
            var annotations = [];
            for (var i = 0; i < chartData.length; i++) {
                for (var j = 0; j < events.length; j++) {
                    var event = events[j];
                    var eventDate = serposcope.utils.eventDate(event);
                    if (eventDate == moment(chartData[i][0]).format("YYYY-MM-DD")) {
                        annotations.push({
                            series: "###CALENDAR###",
                            x: chartData[i][0],
                            cssClass: "fa fa-calendar calendar-annotation",
                            title: event.title,
                            text: event.description
                        });
                    }
                }
            }
            
            chart.setAnnotations(annotations);
            $('.calendar-annotation').popover({
                html: true,
                placement: "bottom"
            });
        });
    };

    function refresh(start, end, label) {
        var url = "/google/" + $('#csp-vars').attr('data-group-id');
        url += "/target/" + $('#csp-vars').attr('data-target-id');
        url += "?startDate=" + start.format('YYYY-MM-DD');
        url += "&endDate=" + end.format('YYYY-MM-DD');
        url += "&display=" + $('#csp-vars').attr('data-display');
        window.location = url;
    };

    var stripIntCmp = function (a, b) {
        a = a.replace(/[^0-9]/g, "");
        b = b.replace(/[^0-9]/g, "");
        return a - b;
    };
    
    var changeCmp = function(a,b){
        a = a.replace(/\s+/g,"");
        b = b.replace(/\s+/g,"");
        if(a[0] === '-' || a[0] === '+'){
            a = parseInt(a.substring(1),10);
        }
        if(b[0] === '-' || b[0] === '+'){
            b = parseInt(b.substring(1),10);
        }  

        if(a === "IN" || a === "OUT" || a === "N/A"){
            a = 1000;
        }

        if(b === "IN" || b === "OUT" || b === "N/A"){
            b = 1000;
        }

        return a-b;
    };
    
    var render = function () {
        $(window).bind("load resize", function (evt) {
            resize();
        });
        $('#google-target-table').stupidtable({"stripint": stripIntCmp});
        $('.table-position-change').stupidtable({"change": changeCmp});
        
        if ($('#csp-vars').attr('data-min-date') !== "") {
            $('#daterange').removeAttr("disabled");
            var maxDate = $('#csp-vars').attr('data-max-date');
            $('#daterange').daterangepicker({
                "ranges": {
                    'Last 30 days': [moment(maxDate).subtract(30, 'days'), moment(maxDate)],
                    'Current Month': [moment(maxDate).startOf('month'), moment(maxDate).endOf('month')],
                    'Previous Month': [moment(maxDate).subtract(1, 'month').startOf('month'), moment(maxDate).subtract(1, 'month').endOf('month')]
                },
                locale: {
                    format: 'YYYY-MM-DD'
                },
                showDropdowns: true,
                startDate: $('#csp-vars').attr('data-start-date'),
                endDate: $('#csp-vars').attr('data-end-date'),
                minDate: $('#csp-vars').attr('data-min-date'),
                maxDate: $('#csp-vars').attr('data-max-date')
            }, refresh);
        }

        var jsonData = JSON.parse($('#csp-vars').attr('data-chart'));
        chartData = [];
        if (jsonData != null && typeof (jsonData.ranks) != "undefined" && typeof (jsonData.searches) != "undefined") {
            chartData = jsonData.ranks;
            maxRank = jsonData.maxRank > maxRank ? jsonData.maxRank : maxRank;
            for (var i = 0; i < jsonData.searches.length; i++) {
                chartOptions.labels.push(jsonData.searches[i]);
            }
            chartOptions.valueRange = [maxRank + maxRankOffset, minRank + minRankOffset];
        }
        
        $('.poptarget').tooltip({
            title: function(elt){return $(this).attr("data-t-title");},
            placement: "top"
        });
        
        $('.poptarget').popover({
            html: true,
            placement: "bottom",
            title: function(elt){return $(this).attr("data-p-title");}
        });
    };

    var oPublic = {
        render: render
    };

    return oPublic;

}();
