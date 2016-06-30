/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global serposcope, Slick */

serposcope.googleGroupControllerGrid = function () {

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
    var data = [];
    var groupId = 1;
    // end    

    var resize = function () {
        $('#group-searches-grid').css("height", serposcope.theme.availableHeight() - 250);
        if (grid != null) {
            grid.resizeCanvas();
        }
    };

    var render = function () {
        setData();
        if(data == null || data.length == 0){
            return;
        }
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
        data = JSON.parse($('#grid-vars').attr('data-data'));
    };

    var renderGrid = function () {
        var options = {
            explicitInitialization: true,
            enableColumnReorder: false,
            enableTextSelectionOnCells: true,
            forceFitColumns: true
        };

        var checkboxSelector = new Slick.CheckboxSelectColumn({cssClass: "slick-cell-checkboxsel"});
        var columns = [checkboxSelector.getColumnDefinition(), {
            id: "keyword", field: "keyword", minWidth: 300, sortable: true, name: 'Keyword', formatter: formatKeyword
        },{
            id: "device", field: "device", minWidth: 100, sortable: true, name: 'Device', formatter: formatDevice
        },{
            id: "tld", field: "tld", minWidth: 60, sortable: true, name: 'TLD'/*, formatter: formatTLD,*/
        },{
            id: "datacenter", field: "datacenter", minWidth: 100, sortable: true, name: 'Datacenter'/*, formatter: formatDatacenter,*/
        },{
            id: "local", field: "local", minWidth: 250, sortable: true, name: 'Local'/*, formatter: formatLocal,*/
        },{
            id: "custom", field: "custom", minWidth: 250, sortable: true, name: 'Custom'/*, formatter: formatCustom*/
        }];

        
        dataView = new Slick.Data.DataView();
        grid = new Slick.Grid("#group-searches-grid", dataView, columns, options);
        grid.registerPlugin(checkboxSelector);
        grid.setSelectionModel(new Slick.RowSelectionModel({selectActiveRow: false}));
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
        dataView.syncGridSelection(grid, false);
        dataView.endUpdate();
    };

    var gridSort = function (e, args) {
        var comparer = function (a, b) {
            return a[args.sortCol.field] > b[args.sortCol.field] ? 1 : -1;
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
//        grid.setSelectedRows([]);
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
        if (filter.keyword !== '' && item.keyword.toLowerCase().indexOf(filter.keyword) === -1) {
            return false;
        }

        if (filter.device !== '' && item.device != filter.device) {
            return false;
        }

        if (filter.tld !== '' && item.tld != filter.tld) {
            return false;
        }

        if (filter.local !== '' && item.local.toLowerCase().indexOf(filter.local) === -1) {
            return false;
        }

        if (filter.datacenter !== '' && item.datacenter != filter.datacenter) {
            return false;
        }

        if (filter.custom !== '' && item.custom.toLowerCase().indexOf(filter.custom) === -1) {
            return false;
        }

        return true;
    };
    
    formatKeyword = function (row, col, unk, colDef, rowData) {
        return "<a href=\"/google/" + groupId + "/search/" + rowData.id + "\" >" + rowData.keyword + "</a>";
    };
    
    formatDevice = function (row, col, unk, colDef, rowData) {
        if(rowData.device === 'M'){
            return "<i data-toggle=\"tooltip\" title=\"mobile\" class=\"fa fa-mobile fa-fw\" ></i>";
        } else {
            return "<i data-toggle=\"tooltip\" title=\"desktop\" class=\"fa fa-desktop fa-fw\" ></i>";
        }
    };    
    
    var getSelection = function() {
        return grid.getSelectedRows().map(dataView.getItem).map((x) => x.id);
    };
    
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

    var setFakeData = function () {
        var searches = 10000;
        for (var i = 0; i < searches; i++) {
            data.push(genFakeSearch(i));
        }
    };

    var oPublic = {
        resize: resize,
        render: render,
        getSelection: getSelection
    };

    return oPublic;

}();
