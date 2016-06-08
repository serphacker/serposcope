/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.googleSearchController = function () {
    
    var HEADER_SIZE = 100;
    
    var resizeSplit = function() {
        var height = serposcope.theme.availableHeight() - HEADER_SIZE;
        $('#google-search-chart').css("height", (height/2) + "px");
        $('#google-search-table').css("height", (height/2) + "px");        
        $('#google-search-chart-container').show();
        $('#google-search-table').show();
        chartUpdate();
    };
    
    var resizeFullChart = function() {
        var height = serposcope.theme.availableHeight() - HEADER_SIZE;
        $('#google-search-chart').css("height", (height) + "px");
        $('#google-search-table').css("height", (0) + "px");   
        $('#google-search-chart-container').show();
        $('#google-search-table').hide();
        chartUpdate();
    };
    
    var resizeFullTable = function() {
        var height = serposcope.theme.availableHeight() - HEADER_SIZE;
        $('#google-search-chart').css("height", (0) + "px");
        $('#google-search-table').css("height", (height+20) + "px");  
        $('#google-search-chart-container').hide();
        $('#google-search-table').show();
        chartUpdate();
    };
    
    var resize = null;
    
    var changeDisplayMode = function(elt) {
        $('.btn-change-display-mode').removeClass("active");
        var btn = elt.currentTarget;
        switch($(btn).attr('data-mode')){
            case "split":
                resize = resizeSplit;
                break;
            case "chart":
                resize = resizeFullChart;
                break;
            case "table":
                resize = resizeFullTable;
                break;            
        }
        $(btn).addClass("active");
        window.dispatchEvent(new Event('resize'));
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
        labelsDiv: "google-search-legend",
        drawPoints: true,
        pointSize: 2,
        highlightSeriesBackgroundAlpha: 0.8,
        highlightSeriesOpts: { strokeWidth: 2 },
        drawHighlightPointCallback : function(g, seriesName, canvasContext, cx, cy, color, pointSize){},
        axes: {
            x: {
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
        valueRange: [maxRank+maxRankOffset, minRank+minRankOffset]
    };    
    
    var chartUpdate = function() {
        renderChart();
    };

    var renderChart = function () {
        if(chartData.length == 0){
            chart = new Dygraph(document.getElementById("google-search-chart"),"X\n",chartOptions);
            return;
        }
        chart = new Dygraph(document.getElementById("google-search-chart"),chartData,chartOptions);
        chart.ready(function() {
            var events = JSON.parse($("#csp-vars").attr("data-events"));
            var annotations = [];
            for(var i=0;i<chartData.length;i++){
                for(var j=0;j<events.length;j++){
                    var event = events[j];
                    var eventDate = serposcope.utils.eventDate(event);
                    if(eventDate == moment(chartData[i][0]).format("YYYY-MM-DD")){
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
                html:true,
                placement: "bottom"
            });
        });
    };
    
    var drawTop10Click = function() {
        var allActive = true;
        var btns = $('.btn-draw-chart');
        for(var i = 0; i<Math.min(btns.size(), 10); i++){
            var $btn = $(btns[i]);
            allActive = $btn.hasClass('active') && allActive;
        }
        
        for(var i = 0; i<Math.min(btns.size(), 10); i++){
            var $btn = $(btns[i]);
            if(allActive || !$btn.hasClass('active')){
                drawDomain($btn.attr('data-url'), $btn);
            }
        }        
        
        return false;
    };
    
    var drawDomainClick = function(event) {
        var btn = event.currentTarget;
        var url = $(btn).attr('data-url');
        drawDomain(url, btn);
        return false;
    };

    var drawDomain = function (url, btn) {
//        url = url.replace("http://", "").replace("https://", "");
        
        var index = getChartDataIndex(url);

        if (index > -1) {
            for(var day=0; day < chartData.length; day++){
                chartData[day].splice(index, 1);
            }
            chartOptions.labels.splice(index, 1);
            chart.updateOptions({labels : chartOptions.labels});
            $(btn).removeClass("active");
        } else {
            $.getJSON(
                "/google/" + $('#csp-vars').attr('data-group-id') +
                "/search/" + $('#csp-vars').attr('data-search-id') + "/url-ranks",
                {   url: url,                 
                    startDate: $('#csp-vars').attr('data-start-date'),
                    endDate: $('#csp-vars').attr('data-end-date')
                }
            ).done(function (newdata) {
                for (var i = 0; i < chartData.length; i++) {
                    var ts = chartData[i][0];
                    var rank = newdata[ts];
                    if(typeof(rank) == "undefined"){
                        chartData[i].push(null);
                    }else{
                        if(rank > maxRank){
                            maxRank = rank;
                        }
                        chartData[i].push(rank);
                    }
                }
                chartOptions.labels.push(url);
                chart.updateOptions({
                    labels : chartOptions.labels,
                    valueRange: [maxRank+maxRankOffset, minRank+minRankOffset]
                });
                $(btn).addClass("active");
            
            }).fail(function () {
                alert("an error occured");
            });
        }
        return false;
    };

    function getChartDataIndex(site) {
        for (var i=0;i<chartOptions.labels.length;i++) {
            var value = chartOptions.labels[i];
            if (value == site) {
                return i;
            }
        }
        return -1;
    };
    
    var favUrl;
    
    function btnAddFavorite(evt){
        favUrl = document.createElement('a');
        favUrl.href = $(evt.currentTarget).attr("data-url");
        $("#targetName").val(favUrl.host);
        $("#pattern").val(favUrl.host);
        $('#new-target').modal();
    }
    
    var onRadioTargetChange = function(){
        $("#pattern").attr('placeholder', $(this).attr("data-help"));
        $("#pattern").val("");
    };
    
    function escapeRegExp(str) {
        return str.replace(/[\-\[\]\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
    };
    
    function refreshSearch(start, end, label){
        var url = "/google/" + $('#csp-vars').attr('data-group-id');
        url += "/search/" + $('#csp-vars').attr('data-search-id');
        url += "?startDate=" + start.format('YYYY-MM-DD');
        url += "&endDate=" + end.format('YYYY-MM-DD');
        window.location = url;
    };
    
    var renderSearch = function () {
        switch($('#csp-vars').attr("data-display-mode")){
            case "split":
                $('#display-mode-split').addClass("active");
                resize = resizeSplit;       
                break;
            case "chart":
                $('#display-mode-chart').addClass("active");
                resize = resizeFullChart;
                break;
            case "table":
                $('#display-mode-table').addClass("active");
                resize = resizeFullTable;
                break;
            default:
                $('#display-mode-split').addClass("active");
                resize = resizeSplit;
        }        
        $(window).bind("load resize", function (evt) {
            resize();
        }); 
        $('.btn-change-display-mode').click(changeDisplayMode);        
        
        $('.btn-draw-top10').click(drawTop10Click);
        $('.btn-draw-chart').click(drawDomainClick);
        $('#new-target').on('shown.bs.modal', function(){ $('#targetName').focus(); });
        $('.btn-add-favorite').click(btnAddFavorite);
        
        if($('#csp-vars').attr('data-min-date') !== ""){
            $('#daterange-search').removeAttr("disabled");
            var maxDate = $('#csp-vars').attr('data-max-date');
            $('#daterange-search').daterangepicker({
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
            }, refreshSearch);
        }
        
        var jsonData = JSON.parse($('#csp-vars').attr('data-chart'));
        chartData = [];
        if(jsonData != null && typeof(jsonData.ranks) != "undefined" && typeof(jsonData.targets) != "undefined"){
            chartData = jsonData.ranks;
            maxRank = jsonData.maxRank > maxRank ? jsonData.maxRank : maxRank;
            for(var i=0;i<jsonData.targets.length;i++){
                chartOptions.labels.push(jsonData.targets[i].name);
            }
            chartOptions.valueRange = [maxRank+maxRankOffset, minRank+minRankOffset];
        }
        
        $('.target-radio').change(onRadioTargetChange);
        $("#pattern").attr('placeholder', $('#target-domain').attr("data-help"));
    };

    var oPublic = {
        renderSearch: renderSearch
    };

    return oPublic;

}();
