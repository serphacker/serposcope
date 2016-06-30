/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope, Slick */

serposcope.googleTargetControllerVariation = function () {
    var UNRANKED = 32767;
    var filter = {
        keyword: '',
        tld: '',
        device: '',
        local: '',
        datacenter: '',
        custom: ''
    };

    var hasData = false;
    var groupId = 1;

    var resize = function () {
        $('.variation-grid').css("height", serposcope.theme.availableHeight() - 250);
        for(var i=0; i < grids.length; i++){
            if(grids[i].grid != null){
                grids[i].grid.resizeCanvas();
            }
        }
    };

    var render = function () {
        if($('#variation-vars').size() == 0){
            return;
        }
        setData();
        renderGrid();
        $('#filter-apply').click(applyFilter);
        $('#filter-reset').click(resetFilter);
        $('body').tooltip({
            selector: '[data-toggle="tooltip"]',
            container: 'body',
            title: function (elt) {
                return $(this).attr("data-tt");
            }
        });
    };

    var setData = function () {
//        setFakeData();
        groupId = $('#csp-vars').attr('data-group-id');
        grids[0].data = JSON.parse($('#variation-vars').attr('data-up'));
        grids[1].data = JSON.parse($('#variation-vars').attr('data-down'));
        grids[2].data = JSON.parse($('#variation-vars').attr('data-same'));
        
        for(var i = 0; i < grids.length; i++){
            if(grids[i].data.length > 0){
                hasData = true;
            }
        }
    };
    
    var renderGrid = function () {
        
        var options = {
            explicitInitialization: true,
            enableColumnReorder: false,
            enableTextSelectionOnCells: true,
            forceFitColumns: true
        };
        
        for(var i =0; i< grids.length; i++){
            grids[i].dataView = new Slick.Data.DataView();
            grids[i].grid = new Slick.Grid(grids[i].selector, grids[i].dataView, grids[i].columns, options);
            grids[i].grid.onSort.subscribe(function (e, args) {
                var comparer = null;
                switch (args.sortCol.field) {
                    case "search":
                        comparer = function (a, b) {
                            return a[args.sortCol.field].keyword > b[args.sortCol.field].keyword ? 1 : -1;
                        };
                        break;
                    default:
                        comparer = function (a, b) {
                            return a[args.sortCol.field] > b[args.sortCol.field] ? 1 : -1;
                        };
                }
                this.dataView.sort(comparer, args.sortAsc);
            }.bind(grids[i]));

            grids[i].dataView.onRowCountChanged.subscribe(function (e, args) {
                this.grid.updateRowCount();
                this.grid.render();
            }.bind(grids[i]));
            grids[i].dataView.onRowsChanged.subscribe(function (e, args) {
                this.grid.invalidateRows(args.rows);
                this.grid.render();
            }.bind(grids[i]));
            grids[i].grid.init();

            grids[i].dataView.beginUpdate();
            grids[i].dataView.setItems(grids[i].data);
            grids[i].dataView.setFilter(filterGrid);
            grids[i].dataView.endUpdate();
        }
    };

    var applyFilter = function () {
        filter.keyword = $('#filter-keyword').val().toLowerCase();
        filter.tld = $('#filter-tld').val().toLowerCase();
        filter.device = $('#filter-device').val();
        filter.local = $('#filter-local').val().toLowerCase();
        filter.datacenter = $('#filter-datacenter').val().toLowerCase();
        filter.custom = $('#filter-custom').val().toLowerCase();
        
        for(var i =0; i< grids.length; i++){
            grids[i].dataView.refresh();
        }
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
        if (filter.keyword !== '' && item.search.keyword.toLowerCase().indexOf(filter.keyword) === -1) {
            return false;
        }

        if (filter.device !== '' && item.search.device != filter.device) {
            return false;
        }

        if (filter.tld !== '' && item.search.tld != filter.tld) {
            return false;
        }

        if (filter.local !== '' && item.search.local.toLowerCase().indexOf(filter.local) === -1) {
            return false;
        }

        if (filter.datacenter !== '' && item.search.datacenter != filter.datacenter) {
            return false;
        }

        if (filter.custom !== '' && item.search.custom.toLowerCase().indexOf(filter.custom) === -1) {
            return false;
        }

        return true;
    };
    
    var formatDiff = function(row, col, unk, colDef, rowData){
        if(rowData.prv === UNRANKED){
            return '<span class="text-success">IN</span>';
        } else if(rowData.now === UNRANKED) {
            return '<span class="text-danger">OUT</span>';
        } else if(rowData.diff > 0){
            return '<span class="text-danger">-' + rowData.diff + '</span>';
        } else {
            return '<span class="text-success">+' + Math.abs(rowData.diff) + '</span>';
        }
    };
    
    var formatRank = function(row, col, value, colDef, rowData){
        if(value == UNRANKED){
            return "-";
        } else {
            return value;
        }
    };
    
    var formatKeyword = function (row, col, unk, colDef, rowData) {
        var ret = "<div class=\"text-left\">";
        ret += "<i data-toggle=\"tooltip\" title=\"TLD : " + rowData.search.tld + "\" class=\"fa fa-globe\" ></i>";
        if (rowData.search.device === "M") {
            ret += "<i data-toggle=\"tooltip\" title=\"mobile\" class=\"fa fa-mobile fa-fw\" ></i>";
        }
        if (rowData.search.local != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + rowData.search.local + "\" class=\"fa fa-map-marker fa-fw\" ></i>";
        }
        if (rowData.search.datacenter != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"Datacenter: " + rowData.search.datacenter + "\" class=\"fa fa-building fa-fw\" ></i>";
        }
        if (rowData.search.custom != "") {
            ret += "<i data-toggle=\"tooltip\" title=\"" + rowData.search.custom + "\" class=\"fa fa-question-circle fa-fw\" ></i>";
        }
        ret += " <a href=\"/google/" + groupId + "/search/" + rowData.search.id + "\" >" + rowData.search.keyword + "</a>";
        ret += "</div>";
        return ret;
    };
    
    var getVariableColumns = function() {
        return [ {
            id: "search", field: "search", sortable: true, name: 'Keyword', formatter: formatKeyword
        },{
            id: "prv", field: "prv", maxWidth: 40, sortable: true, name: 'PRV', formatter: formatRank
        },{
            id: "now", field: "now", maxWidth: 40, sortable: true, name: 'NOW', formatter: formatRank
        },{
            id: "diff", field: "diff", maxWidth: 40, sortable: true, name: '+/-', formatter: formatDiff
        }];
    };
    
    var getUnchangedColumns = function() {
        return [ {
            id: "search", field: "search", sortable: true, name: 'Keyword', formatter: formatKeyword
        },{
            id: "now", field: "now", maxWidth: 40, sortable: true, name: 'NOW', formatter: formatRank
        }];
    };    

    var grids = [
        {selector: "#variation-improved-grid", columns: getVariableColumns(), grid: null, dataView: null, data: []},
        {selector: "#variation-lost-grid", columns: getVariableColumns(), grid: null, dataView: null, data: []},
        {selector: "#variation-unchanged-grid", columns: getUnchangedColumns(), grid: null, dataView: null, data: []}
    ];    
    
    var genFakeSearch = function (i) {
        return {
            id: i,
            keyword: "search#" + parseInt(Math.random() * 100000),
            tld: parseInt(Math.random() * 5) == 0 ? 'com' : 'fr',
            device: parseInt(Math.random() * 5) == 0 ? 'M' : 'D',
            local: parseInt(Math.random() * 5) == 0 ? 'Paris' : '',
            datacenter: parseInt(Math.random() * 5) == 0 ? '1.2.3.4' : '',
            custom: parseInt(Math.random() * 5) == 0 ? 'hl=fr' : ''
        };
    };
    
    var genFakeFixed = function(i) {
        return {
            id: i,
            search : genFakeSearch(i),
            now: parseInt(Math.random() * 100)
        };
    };
    
    var genFakeImproved = function(i) {
        return {
            id: i,
            search : genFakeSearch(i),
            now: parseInt(Math.random() * 100),
            prv: parseInt(Math.random() * 100),
            diff: parseInt(Math.random() * 100)
        };
    };
    
    var genFakeLost = function(i) {
        return {
            id: i,
            search : genFakeSearch(i),
            now: parseInt(Math.random() * 100),
            prv: parseInt(Math.random() * 100),
            diff: -parseInt(Math.random() * 100)
        };
    };    

    var setFakeData = function () {
        var searches = 10000;
        for (var i = 0; i < searches; i++) {
            grids[0].data.push(genFakeImproved(i));
            grids[1].data.push(genFakeLost(i));
            grids[2].data.push(genFakeFixed(i));
        }
        hasData = true;
    };

    var oPublic = {
        resize: resize,
        render: render
    };

    return oPublic;

}();
