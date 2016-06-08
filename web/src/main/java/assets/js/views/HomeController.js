/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope */

serposcope.HomeController = function () {
    
    var resizeTabContent = function(){
        $('.tab-content.tab-groups').css("min-height", serposcope.theme.availableHeight() - 150);
    };
    
    var renderTopCharts = function() {
        $('.hb-piechart').each(function(){
            var canvas = $(this).children("canvas");
            var ctx = canvas[0].getContext("2d");
            var data = [
                {color:"#eDc007",label: "top3",value: $(canvas).attr('data-top3')},
                {color:"#31a354",label: "top10",value: $(canvas).attr('data-top10')},
                {color:"#636363",label: "top100",value: $(canvas).attr('data-top100')},
                {color:"#000000",label: "out",value: $(canvas).attr('data-out')}
            ];
            var myPieChart = new Chart(ctx).Pie(data, {
                animation: true,
                animationSteps: 20,
                animationEasing: "linear",
                legendTemplate : "<% for (var i=0; i<segments.length; i++){%>" + 
                        "<span style=\"color:<%=segments[i].fillColor%>\"><%=segments[i].label%></span> <%=segments[i].value%> <%}%>"
            });            
            $(this).children(".hb-legend")[0].innerHTML = myPieChart.generateLegend();
        });
    };
    
    var renderCharts = function(){
        renderTopCharts();
        $('.hb-score-history').sparkline("html", {
            tagValuesAttribute: "data-values",
            height: "30px"
        });
        $('.hb-score-history-inline').sparkline("html", {
            tagValuesAttribute: "data-values"
        });        
    };
    
    var onFilterChange = function(){
        var regExp = new RegExp($(this).val(), 'i');
        $(".table-summary").children('tbody').children('tr').each(function(index, row){
            var allCells = $(row).find('.filterable');
            var found = false;
            allCells.each(function(index, td){
                if(regExp.test($(td).text())){
                    found = true;
                    return false;
                }
            });

            if(found)
                $(row).show();
            else 
                $(row).hide();
        });
    };
    
    var updateProgressBar = function() {
        var id = $('.progress').attr('data-id');
        if(typeof(id) !== "undefined"){
            $.ajax({
                url: '/task-status/' + id,
                success: function (data) {
                    if(typeof(data.progress) !== "undefined"){
                        $('.progress .progress-bar').css('width', data.progress + '%');
                        $('.progress .progress-bar span').html(data.progress + '%');
                        if(data.progress != 100){
                            setTimeout(updateProgressBar, 3000);
                        }
                    }
                    
                }
            });            
        }
    };
    
    var home = function() {
        $(window).bind("load resize", function () {
            resizeTabContent();
        });
        $('#sidebar-group-search').typeahead({
            source: JSON.parse($('#sidebar-data').attr('data-groups')),
            minLength: 0,
            showHintOnFocus: true,
            highlighter: serposcope.sidebar.groupHighlighted,
            afterSelect: serposcope.sidebar.groupSelected
        });
        
        renderCharts();
        $('.table-summary').stupidtable();
        $('#summary-filter').bind("keyup paste change", onFilterChange);
        
        updateProgressBar();
    };
    
    var oPublic = {
        home: home
    };
    
    return oPublic;

}();
