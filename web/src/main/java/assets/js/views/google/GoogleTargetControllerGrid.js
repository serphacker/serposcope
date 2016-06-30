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

    var grid = null;
    var dataView = null;

    var filter = {
        keyword: '',
        tld: '',
        device: '',
        local: '',
        datacenter: '',
        custom: ''
    };

    // provided by API
    var days = [];
    var data = [];
    var groupId = 1;
    // end    

    var resize = function () {
        if (grid != null) {
            grid.resizeCanvas();
        }
    };

    var render = function () {
        if (document.getElementById("google-target-table-container") == null) {
            return;
        }
        setData();
        if(days == null || days.length == 0 || data == null || data.length == 0){
            return;
        }
        renderGrid();
        $('body').popover({
            selector: '[rel="popover"]',
            placement: 'bottom',
            container: 'body',
            title: function (elt) {
                return $(this).attr("data-pt");
            }
        });
        $('body').tooltip({
            selector: '[data-toggle="tooltip"]',
            container: 'body',
            title: function (elt) {
                return $(this).attr("data-tt");
            }
        });
        $('#filter-apply').click(applyFilter);
        $('#filter-reset').click(resetFilter);
    };

    var setData = function () {
//        setFakeData();
        groupId = $('#csp-vars').attr('data-group-id');
        days = JSON.parse($('#grid-vars').attr('data-days'));
        data = JSON.parse($('#grid-vars').attr('data-data'));
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
        dataView.setItems(data);
        dataView.setFilter(filterGrid);
        dataView.endUpdate();
    };

    var gridSort = function (e, args) {
        var comparer = function (a, b) {
            if (a.id == -1) {
                return args.sortAsc ? -1 : 1;
            }
            if (b.id == -1) {
                return args.sortAsc ? 1 : -1;
            }

            switch (args.sortCol.field) {
                case "id":
                    return a.search.k > b.search.k ? 1 : -1;
                case "best":
                    return a.best.rank - b.best.rank;
                default:
                    return a.days[args.sortCol.field].r - b.days[args.sortCol.field].r;
            }
        };
        dataView.sort(comparer, args.sortAsc);
    };

    var applyFilter = function () {
        filter.keyword = $('#filter-keyword').val().toLowerCase();
        filter.tld = $('#filter-tld').val().toLowerCase();
        filter.device = $('#filter-device').val();
        filter.local = $('#filter-local').val().toLowerCase();
        filter.datacenter = $('#filter-datacenter').val().toLowerCase();
        filter.custom = $('#filter-custom').val().toLowerCase();
        dataView.refresh();
    };

    var resetFilter = function () {
        $('#filter-keyword').val('');
        $('#filter-tld').val('');
        $('#filter-device').val('');
        $('#filter-local').val('');
        $('#filter-datacenter').val('');
        $('#filter-custom').val('');
        applyFilter();
    };

    var filterGrid = function (item) {
        if (item.search == null) {
            return true;
        }

        if (filter.keyword !== '' && item.search.k.toLowerCase().indexOf(filter.keyword) === -1) {
            return false;
        }

        if (filter.device !== '' && item.search.d != filter.device) {
            return false;
        }

        if (filter.tld !== '' && item.search.t != filter.tld) {
            return false;
        }

        if (filter.local !== '' && item.search.l.toLowerCase().indexOf(filter.local) === -1) {
            return false;
        }

        if (filter.datacenter !== '' && item.search.dc != filter.datacenter) {
            return false;
        }

        if (filter.custom !== '' && item.search.c.toLowerCase().indexOf(filter.custom) === -1) {
            return false;
        }

        return true;
    };

    var formatSearchCell = function (row, col, unk, colDef, rowData) {
        if (rowData.search == null) {
            return "<div class=\"text-left\">&nbsp;&nbsp;Calendar</div>";
        }

        var ret = "<div class=\"text-left\">";
        ret += "<i data-toggle=\"tooltip\" title=\"TLD : " + rowData.search.t + "\" class=\"fa fa-globe\" ></i>";
        if (rowData.search.d === "M") {
            ret += "<i data-toggle=\"tooltip\" title=\"mobile\" class=\"fa fa-mobile fa-fw\" ></i>";
        }
        if (rowData.search.l != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + rowData.search.l + "\" class=\"fa fa-map-marker fa-fw\" ></i>";
        }
        if (rowData.search.dc != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"Datacenter: " + rowData.search.dc + "\" class=\"fa fa-building fa-fw\" ></i>";
        }
        if (rowData.search.c != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + rowData.search.c + "\" class=\"fa fa-question-circle fa-fw\" ></i>";
        }
        ret += " <a href=\"/google/" + groupId + "/search/" + rowData.search.id + "\" >" + rowData.search.k + "</a>";
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
        var event = rowData.days[col - 1];
        if (event == null) {
            return null;
        }
        return  '<div class="text-center pointer" rel="popover" data-toggle="tooltip" ' +
            'title="' + serposcope.utils.escapeHTMLQuotes(event.title) + '" ' +
            'data-content="' + serposcope.utils.escapeHTMLQuotes(event.description) + '" >' +
            '<i class="fa fa-calendar" ></i>' +
            '</div>';
    };

    var formatRankCell = function (row, col, unk, colDef, rowData) {
        var rank = rowData.days[col - 1];
        var rankDiff = rank.p - rank.r, diffText = "", diffClass = "";
        if (rank.p == UNRANKED && rank.r != UNRANKED) {
            diffText = "in";
            diffClass = "plus";
        } else if (rank.r == UNRANKED) {
            diffText = "out";
            diffClass = "minus";
        } else if (rankDiff == 0) {
            diffText = "=";
        } else if (rankDiff > 0) {
            diffText = "+" + rankDiff;
            diffClass = "plus";
        } else {
            diffText = rankDiff;
            diffClass = "minus";
        }
        var bestClass = rowData.best.rank == rank.r ? "best-cell" : "";
        var bestText = rowData.best.rank == rank.r ? " (best)" : "";
        var rankText = (rank.r == UNRANKED ? "-" : rank.r);
        var rankUrl = rank.u == null ? "not provided" : serposcope.utils.escapeHTMLQuotes(rank.u);
        return '<div class="pointer diff-' + diffClass + ' ' + bestClass + '" ' +
            'rel="popover" data-toggle="tooltip" ' +
            'data-tt="' + diffText + bestText + '" ' +
            'data-pt="' + days[col - 1] + '" ' +
            'data-content="' + rankUrl + '" ' +
            '>' + rankText + '</div>';
    };

    var formatBestCell = function (row, col, unk, colDef, rowData) {
        if (row == 0) {
            return "";
        }
        var rankText = (rowData.best.rank == UNRANKED ? "-" : rowData.best.rank);
        return '<div class="pointer best-cell" ' +
            'rel="popover" data-toggle="tooltip" ' +
            'title="' + rowData.best.date + '" ' +
            'data-content="' + serposcope.utils.escapeHTMLQuotes(rowData.best.url) + '" ' +
            '>' + rankText + '</div>';
    };

    var genFakeSearch = function (i) {
        return {
            id: i,
            k: "search#" + parseInt(Math.random() * 100000),
            t: parseInt(Math.random() * 5) == 0 ? 'com' : 'fr',
            d: parseInt(Math.random() * 5) == 0 ? 'M' : 'D',
            l: parseInt(Math.random() * 5) == 0 ? 'Paris' : '',
            dc: parseInt(Math.random() * 5) == 0 ? '1.2.3.4' : '',
            c: parseInt(Math.random() * 5) == 0 ? 'hl=fr' : ''
        };
    };

    var setFakeData = function () {
        var startDay = moment("2015-06-15");
        var dayCount = 30;
        var searches = 10000;

        data.push({id: -1, days: [], best: null});
        // init days && calendar
        for (var j = 0; j < dayCount; j++) {
            days.push(startDay.add(1, 'days').format('YYYY-MM-DD'));
            if (parseInt(Math.random() * 10) == 0) {
                data[0].days.push({
                    title: "event title'\"<h1>lol</h1>",
                    description: "event description'\"<h1>lol</h1>"
                });
            } else {
                data[0].days.push(null);
            }
        }

        // init searches
        for (var i = 0; i < searches; i++) {
            var search = genFakeSearch(i);
            var searchData = {
                search: search,
                id: search.id,
                days: [],
                best: {
                    rank: parseInt(Math.random() * 100) + 1,
                    date: startDay.format('YYYY-MM-DD'),
                    url: "besturl'\""
                }
            };
            for (var j = 0; j < dayCount; j++) {
                searchData.days.push({
                    r: parseInt(Math.random() * 100) + 1,
                    p: parseInt(Math.random() * 100) + 1,
                    u: "urlxxx'\""
                });
            }
            data.push(searchData);
        }
    };

    var oPublic = {
        resize: resize,
        render: render
    };

    return oPublic;

}();
