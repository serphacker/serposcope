/* 
 * Serposcope - SEO rank checker https://serposcope.serphacker.com/
 * 
 * Copyright (c) 2016 SERP Hacker
 * @author Pierre Nogues <support@serphacker.com>
 * @license https://opensource.org/licenses/MIT MIT License
 */

/* global Dygraph, serposcope */

Dygraph.Plugins.Serposcope = (function () {

    "use strict";
    
    var popover = function () {
    };
    
    popover.prototype.toString = function () {
        return "Serposcope Plugin";
    };
    
    popover.prototype.activate = function (e) {
        
        return {
            select: this.select,
            deselect: this.deselect,
            didDrawChart: this.didDrawChart,
            clearChart: this.clearChart
        };
    };
    
    
    popover.prototype.select = function(e){
        var series = e.dygraph.getHighlightSeries();
        if(series == null || series == ""){
            return;
        }
        
        var point = null;
        for(var i=0; i < e.selectedPoints.length; i++){
            if( e.selectedPoints[i].name == series){
                point = e.selectedPoints[i];
            }
        }
        
        if(point == null){
            return;
        }
        
        if(isNaN(point.canvasx)  || isNaN(point.canvasy)){
            return;
        }
        
        var serieProp = e.dygraph.getPropertiesForSeries(point.name);
        
        var elements = document.getElementsByClassName("dygraph-cplg");
        if(elements.length == 0){
            return;
        }
        
        var div = elements[0];
        
        div.style.display = "block";
        div.innerHTML = 
            '<span>' +  moment(point.xval).format('YYYY-MM-DD') + '</span><br/>' + 
            '<strong>' + serposcope.utils.escapeHTML(point.name) + " : " + serposcope.utils.escapeHTML(point.yval) + '</strong>';
        div.style.left = (parseInt(point.canvasx - (div.offsetWidth+10))) + 'px';
        div.style.top = (parseInt(point.canvasy) - 10) + 'px';        
        div.style.borderColor = serieProp.color;
        
    };
    
    popover.prototype.deselect = function(e){
        document.getElementsByClassName("dygraph-cplg")[0].style.display = "none";
    };    
    
    popover.prototype.clearChart = function(e){
//        var elements = document.getElementsByClassName("dygraph-cplg");
//        for(var i=0; i<elements.length;i++){
//            elements[i].remove();
//        }
    };
    
    popover.prototype.legendClick = function(e){
        alert(e);
    };
    
    popover.prototype.didDrawChart = function(e){
        if(document.getElementsByClassName("dygraph-cplg").length == 0){
            var div = document.createElement("div");
            div.innerHTML = '';
            div.className = 'dygraph-cplg';
            div.style.position = "absolute";
            div.style.left = "0px";
            div.style.top = "0px";
            div.style.backgroundColor = 'white';
            div.style.border = '2px solid black';
            div.style.padding = '3px';
            div.style.zIndex = 1000;
            div.style.borderRadius = '5px';
            div.style.display = "none";
            e.canvas.parentNode.appendChild(div);
        }
        
        var divLegend = document.getElementById(e.dygraph.getOption('labelsDiv'));
        if(divLegend == null){
            return;
        }
        
        while (divLegend.firstChild) {
            divLegend.removeChild(divLegend.firstChild);
        }
        
        var labels = e.dygraph.getLabels();
        for(var i=1; i<labels.length; i++){
            if(labels[i] === "###CALENDAR###"){
                continue;
            }
            
            var serie = e.dygraph.getPropertiesForSeries(labels[i]);
            
//            console.log(serie);
            var a = document.createElement("a");
            a.href="#";
            a.style.color = serie.color;
            a.style.paddingLeft = "5px";
            a.innerHTML = serposcope.utils.escapeHTML(labels[i]);
            a.dataIndex = i;
            a.dataSerie = serie;
            a.dataChart = e.dygraph;
            a.onclick = function(){
                a.dataChart.setVisibility(this.dataIndex-1, !this.dataSerie.visible);
            };
            a.onmouseover = function() {
                a.dataChart.setSelection(a.dataChart.numRows()-1, this.dataSerie.name);
            };
            a.onmouseout = function() {
                a.dataChart.setSelection(false, null);
            };
            
            divLegend.appendChild(a);
        }
        
    };     

    return popover;
})();