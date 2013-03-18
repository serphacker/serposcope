<?php
/**
 * Serposcope - An open source rank checker for SEO
 * http://serphacker.com/serposcope/
 * 
 * @link http://serphacker.com/serposcope Serposcope
 * @author SERP Hacker <pierre@serphacker.com>
 * @license http://creativecommons.org/licenses/by-nc-sa/3.0/legalcode CC-BY-NC-SA
 * 
 * Redistributions of files must retain the above notice.
 */
if(!defined('INCLUDE_OK'))
    die();

function render($ranks, $target, $keywords){  
    
    $height=450;
    $idDiveu = rand(1000,3000);
    
    echo '<div id="'.$idDiveu.'" style="height: '.$height.'px; margin: 0 auto"></div>';

    echo "
<script type='text/javascript'>

var chart;
$(document).ready(function() {
        chart = new Highcharts.Chart({
                exporting: {
                    url: 'http://export.highcharts.com/index-utf8-encode.php',
                    
                    buttons: {
                        printButton:{
                            enabled:false
                        }
                    }
                },
                chart: {
                        renderTo: '$idDiveu',
                        defaultSeriesType: 'line',
                        zoomType: 'x',
                },


                title: {
                        text: '',
                },
                yAxis: {
                    min : -10,
                    max : 100,
                    endOnTick: false,
                    tickPixelInterval: 40,  
                    reversed : true,
                    title: {
                            text: 'Position'
                    },
                    subtitle: {
                        text: document.ontouchstart === undefined ?
                        'Click and drag in the plot area to zoom in' :
                        'Drag your finger over the plot to zoom in'
                    },
                },

                legend: {
                    align: 'center',
                    verticalAlign: 'bottom',
                    borderWidth: 0
                },

                series: [
";
    
    foreach ($keywords as $keyword) {
        echo "{\n";
        echo "\tname: ".json_encode($keyword,JSON_HEX_TAG).",\n";
        echo "\tdata: [";
        foreach ($ranks as $rank) {
            if(isset($rank[$target]) && isset($rank[$target][$keyword])){
                echo "{y:".$rank[$target][$keyword][0].",name:'".date('d M',strtotime($rank['date']))."<br/>".h8($rank[$target][$keyword][1])."'},";
            }else{
                echo "null,";                
            }
        }
        echo "]\n";
        echo "},\n";
    }
    echo "{\n";
    echo "\tlineWidth: 0,\n";
    echo "\tcolor: 'black',\n";
    echo "\tmarker: {symbol: 'square'},\n";
    echo "\tname: \"Events\",\n";
    echo "\tdata: [";
    foreach ($ranks as $check) {
        $hEvent = false;
        $event = date('d M',strtotime($check['date']))."<br/>";
        if(isset($check['__event']) && isset($check['__event'][$target]) && is_array($check['__event'][$target])){
            foreach ($check['__event'][$target] as $evt) {
                $hEvent = true;
                $event .= "<b>".h8($target)."</b>:<br/>".($evt)."<br/>";
            }
        }
        if($hEvent/*!empty($event)*/){
            echo "{y: -2, name:'".addcslashes($event,"'")."'},";
        }else{
            echo "null,";
        }
    }
    echo "]\n";
    echo "},\n";
    
    echo "
                ],
                    
                xAxis: {
                    categories: [
";
    foreach ($ranks as $rank) {
        echo "'".date('d M',strtotime($rank['date']))."',";
    }
echo "
                    ],
                }
        });
 });
</script>
";
        
    }
    
?>