/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope, Slick */

serposcope.googleTargetControllerGrid = function () {

    var UNRANKED = 32767;
    var COL_WIDTH = 21;
    
    var COL_ID = 0;
    var COL_SEARCH = 1;
    var COL_SEARCH_KEYWORD = 0;
    var COL_SEARCH_COUNTRY = 1;
    var COL_SEARCH_DEVICE = 2;
    var COL_SEARCH_LOCAL = 3;
    var COL_SEARCH_DATACENTER = 4;
    var COL_SEARCH_CUSTOM = 5;
    var COL_BEST = 2;
    var COL_BEST_RANK = 0;
    var COL_BEST_DAY = 1;
    var COL_BEST_URL = 2;
    var COL_EVENTS = 3;
    var COL_EVENTS_TITLE=0;
    var COL_EVENTS_DESCRIPTION=1;
    var COL_RANK = 3;
    var COL_RANK_CURRENT = 0;
    var COL_RANK_PREVIOUS = 1;
    var COL_RANK_URL = 2;

    var grid = null;
    var dataView = null;

    var filter = {
        keyword: '',
        country: '',
        device: '',
        local: '',
        datacenter: '',
        custom: ''
    };

    // provided by API
    var days = [];
    var data = [];
    var groupId = 1;
    var startDate;
    var endDate;
    // end    

    var resize = function (height) {
        $('#google-target-table-container').css("min-height", (height) + "px");
        if (grid != null) {
            grid.resizeCanvas();
        }
    };

    var render = function () {
        if (document.getElementById("google-target-table-container") == null) {
            return;
        }
        $('#filter-apply').click(applyFilter);
        $('#filter-reset').click(resetFilter);
        fetchData();
    };
    
    var fetchData = function(){
        groupId = $('#csp-vars').attr('data-group-id');
        startDate = $('#csp-vars').data('start-date');
        endDate = $('#csp-vars').data('end-date');   
        if(startDate == "" || endDate == ""){
            $("#google-target-table-container").html("no data");
            return;
        }        
        var targetId = $('#csp-vars').data('target-id');
        var url = "/google/" + groupId + "/target/" + targetId + "/ranks?startDate=" + startDate + "&endDate=" + endDate;
        $.getJSON(url)
        .done(function (json) {
            $(".ajax-loader").remove();
            data = json[0];
            days = json[1];
            renderGrid();
        }).fail(function (err) {
            $(".ajax-loader").remove();
            console.log("error", err);
            $("#google-target-table-container").html("error");
        });
    };

    var renderGrid = function () {
        var options = {
            explicitInitialization: true,
            enableColumnReorder: false,
            enableTextSelectionOnCells: true
        };

        var columns = [{
                id: "search",
                name: '<div class="header-search" >&nbsp;&nbsp;Searches <i class="glyphicon glyphicon-sort" ></i></div>',
                field: "id", width: 250, formatter: formatSearchCell, sortable: true
            }];
        for (var i = 0; i < days.length; i++) {
            var day = days[i];
            columns.push({
                id: day,
                name: "<span data-toggle='tooltip' title='" + day + "' >" + day.split("-")[2] + "</span>",
                field: i,
                sortable: true,
                width: COL_WIDTH,
                formatter: formatGridCell
            });
        }
        columns.push({
            id: "best",
            name: '<i class="fa fa-trophy" data-toggle="tooltip" title="Best" style="color: gold;" ></i>',
            field: "best", width: COL_WIDTH, formatter: formatBestCell, sortable: true
        });

        dataView = new Slick.Data.DataView();
        grid = new Slick.Grid("#google-target-table-container", dataView, columns, options);
        
        grid.onSort.subscribe(gridSort);

        dataView.onRowCountChanged.subscribe(function (e, args) {
            grid.updateRowCount();
            grid.render();
        });
        dataView.onRowsChanged.subscribe(function (e, args) {
            grid.invalidateRows(args.rows);
            grid.render();
        });

        grid.init();
        dataView.beginUpdate();
        dataView.setItems(data, 0);
        dataView.setFilter(filterGrid);
        dataView.endUpdate();
    };

    var gridSort = function (e, args) {
        var comparer = function (a, b) {
            if (a[COL_ID] == -1) {
                return args.sortAsc ? -1 : 1;
            }
            if (b[COL_ID] == -1) {
                return args.sortAsc ? 1 : -1;
            }
            switch (args.sortCol.field) {
                case "id":
                    return a[COL_SEARCH][COL_SEARCH_KEYWORD] > b[COL_SEARCH][COL_SEARCH_KEYWORD] ? 1 : -1;
                case "best":
                    return a[COL_BEST][COL_BEST_RANK] - b[COL_BEST][COL_BEST_RANK];
                default:
                    var aRank = a[COL_RANK][args.sortCol.field] === 0 ? UNRANKED : a[COL_RANK][args.sortCol.field][COL_RANK_CURRENT];
                    var bRank = b[COL_RANK][args.sortCol.field] === 0 ? UNRANKED : b[COL_RANK][args.sortCol.field][COL_RANK_CURRENT];
                    return aRank - bRank;
            }
        };
        dataView.sort(comparer, args.sortAsc);
    };

    var applyFilter = function () {
        filter.keyword = $('#filter-keyword').val().toLowerCase();
        filter.country = $('#filter-country').val().toLowerCase();
        filter.device = $('#filter-device').val();
        filter.local = $('#filter-local').val().toLowerCase();
        filter.datacenter = $('#filter-datacenter').val().toLowerCase();
        filter.custom = $('#filter-custom').val().toLowerCase();
        dataView.refresh();
    };

    var resetFilter = function () {
        $('#filter-keyword').val('');
        $('#filter-country').val('');
        $('#filter-device').val('');
        $('#filter-local').val('');
        $('#filter-datacenter').val('');
        $('#filter-custom').val('');
        applyFilter();
    };

    var filterGrid = function (item) {
        var search = item[COL_SEARCH];
        if (search === 0) {
            return true;
        }

        if (filter.keyword !== '' && search[COL_SEARCH_KEYWORD].toLowerCase().indexOf(filter.keyword) === -1) {
            return false;
        }

        if (filter.device !== '' && search[COL_SEARCH_DEVICE] != filter.device) {
            return false;
        }

        if (filter.country !== '' && search[COL_SEARCH_COUNTRY].toLowerCase() != filter.country.toLowerCase()) {
            return false;
        }

        if (filter.local !== '' && search[COL_SEARCH_LOCAL].toLowerCase().indexOf(filter.local) === -1) {
            return false;
        }

        if (filter.datacenter !== '' && search[COL_SEARCH_DATACENTER] != filter.datacenter) {
            return false;
        }

        if (filter.custom !== '' && search[COL_SEARCH_CUSTOM].toLowerCase().indexOf(filter.custom) === -1) {
            return false;
        }

        return true;
    };

    var formatSearchCell = function (row, col, unk, colDef, rowData) {
        var search = rowData[COL_SEARCH];
        if (search === 0) {
            return "<div class=\"text-left\">&nbsp;&nbsp;Calendar</div>";
        }

        var ret = "<div class=\"text-left\">";
        ret += "<i data-toggle=\"tooltip\" title=\"Country : " + search[COL_SEARCH_COUNTRY] + "\" class=\"fa fa-globe\" ></i>";
        if (search[COL_SEARCH_DEVICE] === "M") {
            ret += "<i data-toggle=\"tooltip\" title=\"mobile\" class=\"fa fa-mobile fa-fw\" ></i>";
        }
        if (search[COL_SEARCH_LOCAL] != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + search[COL_SEARCH_LOCAL] + "\" class=\"fa fa-map-marker fa-fw\" ></i>";
        }
        if (search[COL_SEARCH_DATACENTER] != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"Datacenter: " + search[COL_SEARCH_DATACENTER] + "\" class=\"fa fa-building fa-fw\" ></i>";
        }
        if (search[COL_SEARCH_CUSTOM] != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + search[COL_SEARCH_CUSTOM] + "\" class=\"fa fa-question-circle fa-fw\" ></i>";
        }
        ret += " <a href=\"/google/" + groupId + "/search/" + rowData[COL_ID] + "\" >" + search[COL_SEARCH_KEYWORD] + "</a>";
        ret += "</div>";
        return ret;
    };

    var formatGridCell = function (row, col, unk, colDef, rowData) {
        if (row == 0) {
            return formatCalendarCell(row, col, unk, colDef, rowData);
        } else {
            return formatRankCell(row, col, unk, colDef, rowData);
        }
    };

    var formatCalendarCell = function (row, col, unk, colDef, rowData) {
        var event = rowData[COL_EVENTS][col-1];
        if (event === 0) {
            return null;
        }
        return  '<div class="text-center pointer" rel="popover" data-toggle="tooltip" ' +
            'title="' + serposcope.utils.escapeHTMLQuotes(event[COL_EVENTS_TITLE]) + '" ' +
            'data-content="' + serposcope.utils.escapeHTMLQuotes(event[COL_EVENTS_DESCRIPTION]) + '" >' +
            '<i class="fa fa-calendar" ></i>' +
            '</div>';
    };

    var formatRankCell = function (row, col, unk, colDef, rowData) {
        var rank = rowData[COL_RANK][col - 1];
        var diffText = "", diffClass = "";
        var bestClass = "", bestText = "";
        var rankText = "";
        if(rank === 0){
            rankText = "-";
            diffText = "out";
            diffClass = "minus";
        } else {
            rankText = rank[COL_RANK_CURRENT];
            var rankDiff = rank[COL_RANK_PREVIOUS] - rank[COL_RANK_CURRENT];
            if (rank[COL_RANK_PREVIOUS] == UNRANKED && rank[COL_RANK_CURRENT] != UNRANKED) {
                diffText = "in";
                diffClass = "plus";
            } else if (rankDiff == 0) {
                diffText = "=";
            } else if (rankDiff > 0) {
                diffText = "+" + rankDiff;
                diffClass = "plus";
            } else {
                diffText = rankDiff;
                diffClass = "minus";
            }
            
            if(rowData[COL_BEST] !== 0 && rowData[COL_BEST][COL_BEST_RANK] == rank[COL_RANK_CURRENT]){
                bestClass = "best-cell";
                bestText = " (best)";
            }
        }
        
        var rankUrl = rank[COL_RANK_URL] == null ? "not provided" : serposcope.utils.escapeHTMLQuotes(rank[COL_RANK_URL]);
        return '<div class="pointer diff-' + diffClass + ' ' + bestClass + '" ' +
            'rel="popover" data-toggle="tooltip" ' +
            'data-tt="' + diffText + bestText + '" ' +
            'data-pt="' + days[col - 1] + '" ' +
            'data-content="' + rankUrl + '" ' +
            '>' + rankText + '</div>';
    };

    var formatBestCell = function (row, col, unk, colDef, rowData) {
        if (row === 0) {
            return "";
        }
        var best = rowData[COL_BEST];
        var rankText = (best[COL_BEST_RANK] == UNRANKED ? "-" : best[COL_BEST_RANK]);
        return '<div class="pointer best-cell" ' +
            'rel="popover" data-toggle="tooltip" ' +
            'title="' + best[COL_BEST_DAY] + '" ' +
            'data-content="' + serposcope.utils.escapeHTMLQuotes(best[COL_BEST_URL]) + '" ' +
            '>' + rankText + '</div>';
    };

    var oPublic = {
        resize: resize,
        render: render
    };

    return oPublic;

}();
