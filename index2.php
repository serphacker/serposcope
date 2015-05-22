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
 
if(!file_exists('inc/config.php')){
    header("Location: install/",TRUE,302);
    die();
}
require('inc/config.php');
include('inc/define.php');  
include('inc/common.php');
include("inc/header.php");

function cmpGoodChange($a,$b){
    return ($b['diff'] - $a['diff']);
}
function cmpBadChange($a,$b){
    return ($a['diff'] - $b['diff']);
}
// define column title
$ups = 'Wzloty';
$dws = 'Spadki';
$bzs = 'W stawce';
$ous = 'Poza stawk¹';

$rejj = $db->query("SELECT * FROM `".SQL_PREFIX."keyword`");
$ilef = mysql_num_rows($rejj);
$res=$db->query("SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff FROM `".SQL_PREFIX."run` ORDER BY dateStart DESC LIMIT 1"); 
if($res && ($run=mysql_fetch_assoc($res))){
    if($run['dateStop'] == null){
        
        if(!is_pid_alive($run['pid'])){
            // in this case the pid have been killed externally 
            // from command line or max execution time reached
            $db->query(
                "UPDATE `".SQL_PREFIX."run` SET haveError=1, dateStop=now(), ".
                "logs=CONCAT(logs,'ERROR ABNORMAL TERMINATION : process may have been killed or reached max execution time\n') ".
                "WHERE idRun = ".$run['idRun']
            );
            echo "<div class='alert alert-error' >Last run done in ".$run['diff']." with error (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' target='_blanc' >LOG</a>]</span></div>";
        }else{
            echo "<div class='alert' >Warning: cron is still running (PID: ".$run['pid']." started: ".$run['dateStart'].") <span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' target='_blanc' >LOG</a>] [<a style='color:red;' id='stop' data-pid=".$run['pid']." data-runid=".$run['idRun']." href='#' >STOP</a>]</span></div>";
        }
    }else{
        if($run['haveError']){
            echo "<div class='alert alert-error' >Last run done in ".$run['diff']." with error (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' target='_blanc' >LOG</a>]</span></div>";
        }else{
            //echo "<div class='alert alert-success' >Last run successfully done in ".$run['diff']." (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' target='_blanc' >LOG</a>]</span></div>";
        }
    }
}

$q= "select max(date(`date`)) from `".SQL_PREFIX."check` limit 1";
$res=$db->query($q);
if($res && ($row=  mysql_fetch_row($res))){
    $dateLastCheck = $row[0];
}else{
    $dateLastCheck = date('d m Y'); // will fail anyway
}

//$q = "select max(idCheck) idLastCheck,idGroup from `".SQL_PREFIX."check` where date(`date`) = curdate() group by idGroup";
$q = "select max(idCheck) idLastCheck,idGroup from `".SQL_PREFIX."check` where date(`date`) = '".$dateLastCheck."' group by idGroup";
$res=$db->query($q);
$groupsCheck= array();
while ($res && ($row=mysql_fetch_assoc($res))){
    
    $q = "select idCheck idPrevCheck ".
            "from `".SQL_PREFIX."check` where idGroup = ".intval($row['idGroup'])." ".
            "and idCheck < ".intval($row['idLastCheck'])." ".
            "order by idCheck DESC limit 1";
    
    $res2=$db->query($q);
    if($res2){
        if($row2=mysql_fetch_row($res2)){
            $row['idPrevCheck'] = $row2[0];
        }else{
            $row['idPrevCheck'] = null;
        }
        $groupsCheck[]=$row;
    }
}

$topGoodChanges=array();
$topBadChanges=array();
$otherGoodChanges=array();
$otherBadChanges=array();
$unchanged=array();

foreach ($groupsCheck as $check) {
    $q="SELECT idCheck, `".SQL_PREFIX."rank`.idTarget, `".SQL_PREFIX."keyword`.name name, `".SQL_PREFIX."target`.name url, position ".
            " FROM `".SQL_PREFIX."rank` ".
            " JOIN `".SQL_PREFIX."keyword` USING(idKeyword) ".
            " JOIN `".SQL_PREFIX."target` USING(idTarget) ".
            " WHERE idCheck IN (".intval($check['idPrevCheck']).",".intval($check['idLastCheck']).")";
    $res=$db->query($q);
    while ($res && ($row=mysql_fetch_assoc($res))){
        if($row['idCheck'] == $check['idPrevCheck']){
            $check['ranks'][$row['idTarget']."-".$row['name']]['prev'] = $row['position'];
            if(!isset($check['ranks'][$row['idTarget']."-".$row['name']]['url']))
                $check['ranks'][$row['idTarget']."-".$row['name']]['url'] = $row['url'];
        }else{
            $check['ranks'][$row['idTarget']."-".$row['name']]['now'] = $row['position'];
            $check['ranks'][$row['idTarget']."-".$row['name']]['url'] = $row['url'];
        }
        
        $check['ranks'][$row['idTarget']."-".$row['name']]['group'] = $row['url'];
    }
    
    if(isset($check['ranks'])){
        foreach ($check['ranks'] as $key => $val) {
            $val['group']= $check['idGroup'];
            $val['diff'] = 1337;
            if(isset($val['prev']) && isset($val['now']))
                $val['diff'] = $val['prev'] - $val['now'];
            else if(isset($val['prev']) && !isset($val['now']))
                //$val['diff'] = $val['prev'] - 100;
                $val['diff'] = -1000;
            else if(!isset($val['prev']) && isset($val['now']))
                //$val['diff'] = 100 - $val['now'];
                $val['diff'] = 1000;

            if($val['diff'] == 0){
                $unchanged[$key] = $val;
            } else {
                if( isset($options['general']['home_top_limit']) && $options['general']['home_top_limit'] != 0 && (
                        (isset($val['prev']) && $val['prev'] <= $options['general']['home_top_limit']) ||
                        (isset($val['now']) && $val['now'] <= $options['general']['home_top_limit'])
                    )
                ){
                    if($val['diff'] > 0)
                        $topGoodChanges[$key] = $val;
                    else
                        $topBadChanges[$key] = $val;
                    
                }else{
                    if($val['diff'] > 0)
                        $otherGoodChanges[$key] = $val;
                    else
                        $otherBadChanges[$key] = $val;
                }
            }
        }
    }
}

$datem = new DateTime($dateLastCheck);
$datem = $datem->format('d M Y');
$datem = to_sPL($datem);

 if($users != "admin"){
 	echo "<script>if($('.first').attr('href')){window.location.replace($('.first').attr('href'))};</script>\n";
} else { 
echo "<div style='text-align:center;' ><h3>Zestawienie z ".$datem."</h3></div>\n";
echo "    <div id='graph' style='height: 250px; margin: 0 auto'></div>\n";
if(isset($options['general']['home_top_limit']) && $options['general']['home_top_limit'] != 0){
$tabela = "Top ".$options['general']['home_top_limit']." ".$ups;
    uasort($topGoodChanges, "cmpGoodChange");
    displayRanks($topGoodChanges);
    
$tabela = "Top ".$options['general']['home_top_limit']." ".$dws;
    uasort($topBadChanges, "cmpBadChange");
    displayRanks($topBadChanges);
}

$tabela = $ups;
uasort($otherGoodChanges, "cmpGoodChange");
displayRanks($otherGoodChanges);

$tabela =  $dws;
uasort($otherBadChanges, "cmpBadChange");
displayRanks($otherBadChanges);

if(HOME_UNCHANGED){
$tabela =  $bzs;
$pozad = TRUE;
displayRanks($unchanged);
}
}

function displayRanks($ranks){
$dt = date("Y-m-d");
	global $tabela;
	global $ilef;
	global $poza;
	global $pozad;
	global $rr;
	global $w1;
	global $w3;
	global $w10;
	global $w50;
	global $w100;
	global $t3;
	global $t10;
	global $t50;
	global $t100;
	global $up;
	global $dp;
	global $dw;
	global $bz;
	global $dws;
	global $bz;
	global $ups;
	global $dws;
	global $bzs;
	global $ous;
if($tabela == $ups){$dp = count($ranks); if($dp < 1){$dp = 'null';}}
if($tabela == $dws){$dw = count($ranks); if($dw < 1){$dw = 'null';}}
if($tabela == $bzs){$bz = count($ranks); if($bz < 1){$bz = 'null';}}
$rr++;
echo "<div id='_".$rr."'><h4>".$tabela." (".count($ranks).")</h4>
   <table class='rankchange-table table' >
    <thead>
    <tr class='sort'>
        <th data-sort='string' style='width:60%;' title='segreguj wg. s³ów kluczowych'>s³owo kluczowe</th>
        <th data-sort='string' style='width:30%;' title='segreguj wg. domen'>domena</th>
        <th data-sort='change' colspan='2' style='min-width:130px;' title='segreguj wg. pozycji'>aktualna pozycja</th>
    </tr>
    </thead>
    <tbody>\r\n";
    foreach ($ranks as $key => $rank) {
 	      $i++;
        $split=explode("-",$key);
        array_shift($split);
        echo "    <tr class='".$rank['now']."'>
        <td><a href='http://google.com/search?q=".str_replace(' ','+',h8(implode($split,"-")))."' target='_blanc'>".h8(implode($split,"-"))."</a></td>\r\n";
        echo "        <td><a href='http://".str_replace('*','',h8($rank['url']))."' target='_blanc'>".str_replace('*','',h8($rank['url']))."</a></td>\r\n";
        if($rank['diff'] != 0){
        	echo "        <td class='bz' title='Poprzednio na pozycji: ".(isset($rank['prev']) ? $rank['prev'] : "N/A")."'>".(isset($rank['now']) ? $rank['now'] : "&nbsp;");
        } else {
        	echo "        <td class='bz'>".(isset($rank['now']) ? $rank['now'] : "&nbsp;");}
        
        if($rank['diff'] == 0){
            echo "<span>";
        } else if($rank['diff'] > 0 ){
            echo "<span class='up'>".($rank['diff'] == 1000 ? "IN" : "+".$rank['diff']);
        }else{
            echo "<span class='dw'>".($rank['diff'] == -1000 ? "OUT" : $rank['diff']);
        }
        echo "</span></td>\r\n";

        echo "        <td style='width:30px;'><a href='view.php?idGroup=".h8($rank['group'])."#".h8($rank['url'])."' ><img src='img/graph.png' title='Wykres' /></a></td>\r\n";
        echo "    </tr>\r\n";
    if($rank['now'] == 1) {$w1++;}
    if($rank['now'] > 1 && $rank['now'] < 4) {$w3++;}
    if($rank['now'] > 3 && $rank['now'] < 11) {$w10++;}
    if($rank['now'] > 10 && $rank['now'] < 51) {$w50++;}
    if($rank['now'] > 50 && $rank['now'] < 101) {$w100++;}

    if($rank['now'] > 0 && $rank['now'] < 4) {$t3++;}
    if($rank['now'] > 0 && $rank['now'] < 11) {$t10++;}
    if($rank['now'] > 0 && $rank['now'] < 51) {$t50++;}
    if($rank['now'] > 0 && $rank['now'] < 101) {$t100++;}

    }
echo "</tbody></table></div>\n";

if($pozad){$poza = $ilef-$t100; 
	echo "<div id='_4'>
	<h4>".$ous." (".$poza.")</h4><span class='_4'>
	W zestawieniu pominiêto <b>".$poza."</b> z ".$ilef." s³ów kluczowych które nie znalaz³y siê w pierwszej setce wyszukiwañ.</span></div>
<script>
var chart;
$(document).ready(function() {
	chart = new Highcharts.Chart({

		exporting: {
		url: 'http://export.highcharts.com/',
			buttons: {
				printButton:{
					enabled:false
				}
			}
		},
      title: {text:''},
      xAxis: {
            tickmarkPlacement: 'on',
            tickWidth: 0,
            min : 0,
            max : 4,
            showLastLabel: false,
            categories: ['".$ups."', '".$dws."', '".$bzs."', '".$ous."'],
            labels: {
            x: 60,
            },
           },
      yAxis: {
            labels: {
                enabled: false
            },
            title: {
                text: ''
            }
        },
        plotOptions: {
            column: {
                cursor: 'pointer',
                point: {
                events: {
                    click: function () {
                    $('#_1').hide(900);
                    $('#_2').hide(900);
                    $('#_3').hide(900);
                    $('#_4').hide(900);
                    $('#'+this.rr).show(900);
                        $('html,body').animate({scrollTop: $('#'+this.rr).offset().top - 170},'slow');
                  		}
                    }
                }
            },
            pie: {
                cursor: 'pointer',
                borderWidth: 0,
                point: {
                events: {
                    click: function () {
                    //alert(this.name);
                  		}
                    }
                }
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0,
        },
        chart: {
					renderTo: 'graph',
					marginRight: 340,
				},
        series: [{
            type: 'pie',
            name: 'S³owa kluczowe',
            data: [
           	 {name:'OUT',y:".$poza.",tt:".$poza.",color:Highcharts.getOptions().colors[5],sliced:true,selected:true},\r\n";
if(!$w100){echo "           	 {name:'',y:null},\r\n";}else{echo "           	 {name:'Top 100',y:".$w100.",tt:".$t100.",color:Highcharts.getOptions().colors[6]},\r\n";}
if(!$w50){echo "           	 {name:'',y:null},\r\n";}else{echo "           	 {name:'TOP 50',y:".$w50.",tt:".$t50.",color:Highcharts.getOptions().colors[3]},\r\n";}
if(!$w10){echo "           	 {name:'',y:null},\r\n";}else{echo "           	 {name:'TOP 10',y:".$w10.",tt:".$t10.",color:Highcharts.getOptions().colors[0]},\r\n";}
if(!$w3){echo "           	 {name:'',y:null},\r\n";}else{echo "           	 {name:'TOP 3',y:".$w3.",tt:".$t3.",color:Highcharts.getOptions().colors[7]},\r\n";}
if(!$w1){echo "           	 {name:'',y:null},\r\n";}else{echo "           	 {name:'TOP 1',y:".$w1.",tt:".$w1.",color:Highcharts.getOptions().colors[2]},\r\n";}
echo "            ],
        		tooltip: {
            shared: true,
        	    pointFormat: '<b>S³owa kluczowe: {point.tt}</b>',
       			},
            center: [720, 110],
            size: 220,
            showInLegend: true,
            dataLabels: {
                color: 'black',
                distance: -30
            }
          },{
            type: 'column',
            pointRange:1.6,
            pointPlacement: 'between',
            name: 'Zmiany',
            dataLabels: {formatter: function() {return this.y}, enabled:true, inside:true,  style:{verticalAlign:'bottom', 'color': '#000',fontWeight: 'bold', fontSize: '12px',}},
            data: [{
                name: '".$ups."',
                y: ".$dp.",
                color: Highcharts.getOptions().colors[4],
                rr:'_1',
            },{
                name: '".$dws."',
                y: ".$dw.",
                color: Highcharts.getOptions().colors[1],
                rr:'_2',
            },{
                name: '".$bzs."',
                y: ".$bz.",
                color: Highcharts.getOptions().colors[8],
                rr:'_3',
            },{
                name: '".$ous."',
                y: ".$poza.",
                color: Highcharts.getOptions().colors[5],
                rr:'_4',
            }],
        		tooltip: {
        	    pointFormat: '<b>S³owa kluczowe: {point.y}</b>',
        	    shared: true
       			},
            showInLegend: false,
            },{
            type: 'spline',
            name: 'STAWKA',
            color:'#1D741D',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".rand($dp + $dw,$bz + 1).",x:4.6},
            ],
            marker: {
                symbol: 'square',
                lineWidth: 2,
                lineColor: '#1D741D',
                fillColor: 'white'
            },
        		tooltip: {
        	    pointFormat: '<b>".$bzs.": {point.y}</b>',
        	    shared: true
       			}
        },{
            type: 'spline',
            name: 'WZLOTY',
            color:'#4572A7',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".rand($dw,$bz - $dp + 1).",x:4.6},
            ],
            marker: {
                symbol: 'circle',
                lineWidth: 2,
                lineColor: '#4572A7',
                fillColor: 'white'
            },
        		tooltip: {
        	    pointFormat: '<b>".$ups.": {point.y}</b>',
        	    shared: true
       			}
        },{
            type: 'spline',
            name: 'SPADKI',
            color:'#8B0000',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".rand($dp,$dp + $dw + 1).",x:4.6},
            ],
            marker: {
                symbol: 'diamond',
                lineWidth: 2,
                lineColor: '#8B0000',
                fillColor: 'white'
            	},
        		tooltip: {
        	    pointFormat: '<b>".$dws.": {point.y}</b>',
        	    shared: true
       			}
          }
      ]                    
    });
 });

</script>";}
}

?>
<script>
$( "._0" ).click(function() {
	$( "#_0" ).toggle(900);
});
$( "._1" ).click(function() {
	$( "#_1" ).toggle(900);
});
$( "._2" ).click(function() {
	$( "#_2" ).toggle(900);
});
$( "._3" ).click(function() {
	$( "#_3" ).toggle(900);
});
$(document).ready(function(){
    $("#_1").hide();
    $("#_2").hide();
    $("#_3").hide();
    $("#_4").hide();
});
    $(function() {
        
        $('.rankchange-table').stupidtable(
            {"change":function(a,b){
            
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
            }
        });
        
        $("#stop").click(function(){
            var pid = $(this).attr('data-pid');
            var runid = $(this).attr('data-runid');
            $.ajax({
                type: "POST",
                url: "ajax.php",
                data: "action=kill&pid=" + pid +"&runid=" + runid
            }).done(function(rawdata){
                data = JSON.parse(rawdata);
                if(data != null){
                    if(data.kill == true){
                        location.reload();
                    }else{
                        alert("Can't kill PID " + pid);
                    }
                }else{
                    alert("unknow error [1]");
                }
            });
        });
    });
</script>
<?php
include('inc/footer.php');
?>