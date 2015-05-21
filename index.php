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
include("inc/user.php");
include("inc/header.php");

function procent($czego){
	global $ilef;
	$procent = ($czego * 100) / $ilef;
	return round($procent) .'%';
}

function cmpGoodChange($a,$b){
    return ($b['diff'] - $a['diff']);
}
function cmpBadChange($a,$b){
    return ($a['diff'] - $b['diff']);
}

if(!$adminAcces && !$ID){
// 	echo "<script>if($('.first').attr('href')){window.location.replace('?' + $('.first').attr('href').split('?')[1])};</script>\n";
// 	echo "<script>us='".$_GET['us']."'; if(!us){window.location.replace('?us=".$users."')};</script>\n";
}

// define column title
$up_name = 'Wzloty';
$dw_name = 'Spadki';
$bz_name = 'W stawce';
$out_name = 'Poza stawką';
//$uuu = mysql_fetch_assoc($db->query("select max(idGroup) from `".SQL_PREFIX."check` where date(`date`) = curdate()" ));
//    echo $uuu['max(idGroup)'];

$dzis = $db->query("select max(idCheck) idLastCheck,idGroup from `".SQL_PREFIX."check` where date(`date`) = curdate() group by idGroup");
$ile_dzis = mysql_num_rows($dzis);

$rejj = $db->query("SELECT * FROM `".SQL_PREFIX."keyword`");
$ilef = mysql_num_rows($rejj);
$moduls = $db->query("SELECT `module` FROM `".SQL_PREFIX."group`");
$ile_grup = mysql_num_rows($moduls);
$ile_jeszcze = $ile_grup - $ile_dzis;
if($ile_jeszcze > 0 && !$ID){
	if(!$adminAcces){
			      echo "<div class='CloseScanInfo alert alert-info' >Zaplanowany skan w toku... Aby sprawdzić wyniki, wróć tutaj później...<span style='float:right;'>[<a id='CloseScanInfo' href='#'>Zamknij</a>]</span></div>";
		} else {
            echo "<div class='alert' >Ostatni scan nie został jeszcze wpełni ukończony (pozostało ".$ile_jeszcze." testów)<span class='pull-right' >[<a href='logs.php'>LOG</a>]</span></div>";
          }
}

$res=$db->query("SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff FROM `".SQL_PREFIX."run` ORDER BY dateStart DESC LIMIT 1"); 
if(($res AND ($run=mysql_fetch_assoc($res))) AND $adminAcces){
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
// /************* pentla testu ****************/
for($j=6; $j > -1; $j--){
if($ID){$q= "select max(date(`date`)) from `".SQL_PREFIX."check` where idGroup = $ID limit 1";} else{$q= "select max(date(`date`)) from `".SQL_PREFIX."check` limit 1";}
$res=$db->query($q);
if($res AND ($row =  mysql_fetch_row($res))){
    $dateLastCheck = $row[0];
}else{
    $dateLastCheck = date('d m Y'); // will fail anyway
}
$dateLastCheck = date( "Y-m-d", strtotime( "$dateLastCheck -$j day" ) );
$zzs = 0; 

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
      	if (($check['idGroup'] == $ID) || (strpos($val['url'],$users) && !$ID) || (strpos($val['url'],$users) && $check['idGroup'] == $ID) || ($adminAcces && !$ID)){ 
      		/************* if exclude test access mode rotator ****************/
$zzs++;
/************* prepare precounter data table *****************/
if ($j == 0){
    if($val['now'] == 1) {$top1++;}
    if($val['now'] > 1 && $val['now'] < 4) {$top3++;}
    if($val['now'] > 3 && $val['now'] < 11) {$top10++;}
    if($val['now'] > 10 && $val['now'] < 51) {$top50++;}
    if($val['now'] > 50 && $val['now'] < 101) {$top100++;}
}
if ($j == 1){
    if($val['now'] == 1) {$atop1++;}
    if($val['now'] < 4) {$atop3++;}
    if($val['now'] < 11) {$atop10++;}
    if($val['now'] < 51) {$atop50++;}
}
if ($j == 2){
    if($val['now'] == 1) {$btop1++;}
    if($val['now'] < 4) {$btop3++;}
    if($val['now'] < 11) {$btop10++;}
    if($val['now'] < 51) {$btop50++;}
}
if ($j == 3){
    if($val['now'] == 1) {$ctop1++;}
    if($val['now'] < 4) {$ctop3++;}
    if($val['now'] < 11) {$ctop10++;}
    if($val['now'] < 51) {$ctop50++;}
}
if ($j == 4){
    if($val['now'] == 1) {$dtop1++;}
    if($val['now'] < 4) {$dtop3++;}
    if($val['now'] < 11) {$dtop10++;}
    if($val['now'] < 51) {$dtop50++;}
}
if ($j == 5){
    if($val['now'] == 1) {$etop1++;}
    if($val['now'] < 4) {$etop3++;}
    if($val['now'] < 11) {$etop10++;}
    if($val['now'] < 51) {$etop50++;}
}
if ($j == 6){
    if($val['now'] == 1) {$ftop1++;}
    if($val['now'] < 4) {$ftop3++;}
    if($val['now'] < 11) {$ftop10++;}
    if($val['now'] < 51) {$ftop50++;}
}
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
        } else { // /************* if exclude test access mode calculating offset ****************/
						if ($ID){
							$rexx = $db->query("SELECT * FROM `".SQL_PREFIX."keyword` WHERE `idGroup` != ".$ID);
        			$offset = mysql_num_rows($rexx);
						} else {
							$base = ucfirst($users);
							$rexx = $db->query("SELECT * FROM `".SQL_PREFIX."keyword` JOIN `".SQL_PREFIX."group` USING(idGroup) WHERE `".SQL_PREFIX."group`.`name` != '$base'");
							$offset = mysql_num_rows($rexx);
						}
        	}
        }
    }
}
$ox .= $zzs." ";
$ups_cnt .= count($otherGoodChanges)." ";
$dws_cnt .= count($otherBadChanges)." ";
$bzs_cnt .= count($unchanged)." ";
}
$onday = explode (' ', $ox);
$upcnter = explode (' ', $ups_cnt);
$dwcnter = explode (' ', $dws_cnt);
$bzcnter = explode (' ', $bzs_cnt);
$up_cnt = $upcnter[6];
$dw_cnt = $dwcnter[6];
$bz_cnt = $bzcnter[6];

/************* prepare counter *****************/
$cnt3 = $top1 + $top3;
$cnt10 = $cnt3 + $top10;
$cnt50 = $cnt10 + $top50;
$cnt100 = $cnt50 + $top100;
//
$ilef = $ilef - $offset;
$out = $ilef - $onday[6];

//echo $zzs.' - '.$offset.'<br/>';			//debug mode;

/************* display conter *****************/


$datem = new DateTime($dateLastCheck);
$datem = $datem->format('d M Y');
$datem = to_sPL($datem);
echo '<script>
        $( "#btn_7" ).css("border","solid 2px #D64B46");
        $( "#btn_7" ).css("border-radius","5px");
</script>';

echo "<div style='text-align:center;' ><h3>Zestawienie z ".$datem."</h3></div>\n";
echo "    <div id='graph' style='height: 250px; margin: 0 auto'></div>\n";
if(isset($options['general']['home_top_limit']) && $options['general']['home_top_limit'] != 0){
$tabela = "Top ".$options['general']['home_top_limit']." ".$up_name;
    uasort($topGoodChanges, "cmpGoodChange");
    displayRanks($topGoodChanges);
    
$tabela = "Top ".$options['general']['home_top_limit']." ".$dw_name;
    uasort($topBadChanges, "cmpBadChange");
    displayRanks($topBadChanges);
}

$tabela = $up_name;
uasort($otherGoodChanges, "cmpGoodChange");
displayRanks($otherGoodChanges);

$tabela =  $dw_name;
uasort($otherBadChanges, "cmpBadChange");
displayRanks($otherBadChanges);

if(HOME_UNCHANGED){
$tabela =  $bz_name;
$pozad = TRUE;
displayRanks($unchanged);
}

function displayRanks($ranks){
$dt = date("Y-m-d");
	global $db;
	global $top1;
	global $top3;
	global $top10;
	global $top50;
	global $top100;
	global $cnt3;
	global $cnt10;
	global $cnt50;
	global $cnt100;	
	global $out;
	global $up_name;
	global $dw_name;
	global $bz_name;
	global $out_name;
	global $tabela;
	global $rr;
	global $up_cnt;
	global $dw_cnt;
	global $bz_cnt;

	global $pozad;
	global $w1;
	global $ups_cnt; $upcnter = explode(' ', $ups_cnt);
	global $dws_cnt; $dwcnter = explode(' ', $dws_cnt);
	global $bzs_cnt; $bzcnter = explode(' ', $bzs_cnt);

$rr++;
echo "<div id='_".$rr."'><h4>".$tabela." (".count($ranks).")</h4>
   <table class='rankchange-table table' >
    <thead>
    <tr class='sort'>
        <th data-sort='string' title='segreguj wg. słów kluczowych' rel='tooltip'>słowo kluczowe</th>
        <th data-sort='string' title='segreguj wg. domen' rel='tooltip'>domena</th>
        <th data-sort='change' colspan='2' title='segreguj wg. pozycji' rel='tooltip'>aktualna pozycja</th>
    </tr>
    </thead>
    <tbody>\r\n";
//    var_dump($ranks);
$uu=1;
    foreach ($ranks as $key => $rank) {
        $modulesname = $db->query("SELECT `module` FROM `".SQL_PREFIX."group` WHERE `idGroup` =".$rank['group']);
        while ($row = mysql_fetch_array($modulesname, MYSQL_BOTH)) {$cdd = $row[0];}
        $split=explode("-",$key);
        array_shift($split);
     if ($cdd == 'Ask') 			{	$sUrl='http://www.ask.com/web?q=';}
     if ($cdd == 'Exalead') 	{	$sUrl='http://www.exalead.com/search/web/results/?q=';}
     if ($cdd == 'Yahoo') 		{$sUrl='https://search.yahoo.com/search?p=';}
     if ($cdd == 'Google') 		{$sUrl='http://google.com/search?q=';}
     if ($cdd == 'Bing') 			{$sUrl='http://www.bing.com/search?q=';}
        echo "    <tr class='".$rank['now']."'>
        <td><a class='".strtolower($cdd)."' href='".$sUrl.str_replace(' ','+',h8(implode($split,"-")))."' target='_blanc'>".h8(implode($split,"-"))."</a></td>
                <td><a href='http://".str_replace('*','',h8($rank['url']))."' target='_blanc'>".str_replace('*','',h8($rank['url']))."</a></td>\r\n";
        if($rank['diff'] != 0){
        	echo "        <td rel='tooltip' data-placement='left' class='bz' title='Poprzednio na pozycji: ".(isset($rank['prev']) ? $rank['prev'] : "N/A")."'>".(isset($rank['now']) ? $rank['now'] : "&nbsp;");
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

        echo "        <td style='width:30px;' title='Wykres' rel='tooltip' data-placement='right'><a href='view.php?idGroup=".h8($rank['group'])."#".h8($rank['url'])."' ><img src='img/graph.png' /></a></td>\r\n";
        echo "    </tr>\r\n";

    }
echo "</tbody></table></div>\n";

if($pozad){
	echo "
<script>
var chart;
$(document).ready(function() {
	chart = new Highcharts.Chart({

		exporting: {
		url: 'http://export.highcharts.com/',
//url: 'http://export.seo.hyzne.com/',
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
            categories: ['".$up_name."', '".$dw_name."', '".$bz_name."', '".$out_name."'],
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
												$('#_0').hide(900);
												$('#_1').hide(900);
												$('#_2').hide(900);
												$('#_3').hide(900);
												$('#_4').hide(900);
                    if ($('#'+this.rr).is(':visible') == false){
												$('#'+this.rr).show(900);
                    } else { $('#_0').show(900);}
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
                    if(this.id == 4) {
                    if ($('#_4').is(':visible') == false){
												$('#_0').hide(900);
												$('#_1').hide(900);
												$('#_2').hide(900);
												$('#_3').hide(900);
										$('#_4').show(900);
											} else { $('#_4').hide(900); $('#_0').show(900);}
										} else {
                    if ($('#_0').is(':visible') == false){
												$('#_1').hide(900);
												$('#_2').hide(900);
												$('#_3').hide(900);
												$('#_4').hide(900);
										$('#_0').show(900);
											}
												$('#'+this.id).click();
											}
                    }
                  }
                }
            },
            spline: {
                cursor: 'pointer',
                borderWidth: 0,
                events: {
                    click: function () {
                    if ($('#_0').is(':visible') == false){
												$('#_1').hide(900);
												$('#_2').hide(900);
												$('#_3').hide(900);
												$('#_4').hide(900);
												$('#_0').show(900);
											}
												$('#'+this.name).click();
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
            name: 'Słowa kluczowe',
            data: [
";
if($out){echo "           	 {name:'OUT',y:".$out.",tt:".$out.",color:Highcharts.getOptions().colors[5],sliced:true,selected:true,id:'4'},\r\n";}
if($top100){echo "           	 {name:'Top 100',y:".$top100.",tt:".$cnt100.",color:Highcharts.getOptions().colors[6],id:100},\r\n";}
if($top50){echo "           	 {name:'TOP 50',y:".$top50.",tt:". $cnt50 .",color:Highcharts.getOptions().colors[3],id:50},\r\n";}
if($top10){echo "           	 {name:'TOP 10',y:".$top10.",tt:". $cnt10 .",color:Highcharts.getOptions().colors[0],id:10},\r\n";}
if($top3){echo "           	 {name:'TOP 3',y:".$top3.",tt:".$cnt3 .",color:Highcharts.getOptions().colors[7],id:3},\r\n";}
if($top1){echo "           	 {name:'TOP 1',y:".$top1.",tt:".$top1.",color:Highcharts.getOptions().colors[2],id:1},\r\n";}
echo "            ],
        		tooltip: {
            shared: true,
        	    pointFormat: '<b>Słowa kluczowe: {point.tt}</b>',
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
            data: [\r\n";
if($up_cnt < 1){echo "           	 {name:'',y:null},\r\n";}else{echo "                {name: '".$up_name."',y:".$up_cnt.",color:Highcharts.getOptions().colors[4],rr:'_1'},\r\n";}
if($dw_cnt < 1){echo "           	 {name:'',y:null},\r\n";}else{echo "                {name: '".$dw_name."',y:".$dw_cnt.",color:Highcharts.getOptions().colors[1],rr:'_2'},\r\n";}
if($bz_cnt < 1){echo "           	 {name:'',y:null},\r\n";}else{echo "                {name: '".$bz_name."',y:".$bz_cnt.",color:Highcharts.getOptions().colors[8],rr:'_3'},\r\n";}
if($out < 1){echo "                {name:'',y:null},\r\n";}else{echo "                {name: '".$out_name."',y:".$out.",color:Highcharts.getOptions().colors[5],rr:'_4'},\r\n";}
echo "            ],
        		tooltip: {
        	    pointFormat: '<b>Słowa kluczowe: {point.y}</b>',
        	    shared: true
       			},
            showInLegend: false,
            },{
            type: 'spline',
            name: 'STAWKA',
            color:'#1D741D',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".$bzcnter[0].",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".$bzcnter[1].",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".$bzcnter[2].",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".$bzcnter[3].",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".$bzcnter[4].",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".$bzcnter[5].",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".$bzcnter[6].",x:4.6},
            ],
            marker: {
                symbol: 'square',
                lineWidth: 2,
                lineColor: '#1D741D',
                fillColor: 'white'
            },
        		tooltip: {
        	    pointFormat: '<b>".$bz_name.": {point.y}</b>',
        	    shared: true
       			}
        },{
            type: 'spline',
            name: 'WZLOTY',
            color:'#4572A7',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".$upcnter[0].",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".$upcnter[1].",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".$upcnter[2].",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".$upcnter[3].",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".$upcnter[4].",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".$upcnter[5].",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".$upcnter[6].",x:4.6},
            ],
            marker: {
                symbol: 'circle',
                lineWidth: 2,
                lineColor: '#4572A7',
                fillColor: 'white'
            },
        		tooltip: {
        	    pointFormat: '<b>".$up_name.": {point.y}</b>',
        	    shared: true
       			}
        },{
            type: 'spline',
            name: 'SPADKI',
            color:'#8B0000',
            data: [
                {name:'".to_PL(date( "d M", strtotime( "$dt -6 day" ) ))."',y:".$dwcnter[0].",x:-0.6},
                {name:'".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."',y:".$dwcnter[1].",x:0},
                {name:'".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."',y:".$dwcnter[2].",x:1},
                {name:'".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."',y:".$dwcnter[3].",x:2},
                {name:'".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."',y:".$dwcnter[4].",x:3},
                {name:'".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."',y:".$dwcnter[5].",x:4},
                {name:'".to_PL(date( "d M", strtotime( "$dt -0 day" ) ))."',y:".$dwcnter[6].",x:4.6},
            ],
            marker: {
                symbol: 'diamond',
                lineWidth: 2,
                lineColor: '#8B0000',
                fillColor: 'white'
            	},
        		tooltip: {
        	    pointFormat: '<b>".$dw_name.": {point.y}</b>',
        	    shared: true
       			}
          }
      ]                    
    });
 });

</script>";
}
}
$middle = round(($onday[0] + $onday[1] + $onday[2] + $onday[3] + $onday[4] + $onday[5] + $onday[6])/7);
$jak1 = $onday[5] - $onday[4]; if($jak1 < 0) {$pz1 = '<span class="dw">'.$jak1.'</span>';} if($jak1 > 0) {$pz1 = '<span class="up">+'.$jak1.'</span>';}
$jak2 = $onday[4] - $onday[3]; if($jak2 < 0) {$pz2 = '<span class="dw">'.$jak2.'</span>';} if($jak2 > 0) {$pz2 = '<span class="up">+'.$jak2.'</span>';}
$jak3 = $onday[3] - $onday[2]; if($jak3 < 0) {$pz3 = '<span class="dw">'.$jak3.'</span>';} if($jak3 > 0) {$pz3 = '<span class="up">+'.$jak3.'</span>';}
$jak4 = $onday[2] - $onday[1]; if($jak4 < 0) {$pz4 = '<span class="dw">'.$jak4.'</span>';} if($jak4 > 0) {$pz4 = '<span class="up">+'.$jak4.'</span>';}
$jak5 = $onday[1] - $onday[0]; if($jak5 < 0) {$pz5 = '<span class="dw">'.$jak5.'</span>';} if($jak5 > 0) {$pz5 = '<span class="up">+'.$jak5.'</span>';}
echo "
<br/>
<div id='_0'><h4>Poprzednie wyniki</h4>
<style>.center td, .center th{text-align:center; white-space:nowrap; min-width:79px;}</style>
   <table class='rankchange-table table center' >
    <thead>
    <tr class='sort'>
        <th data-sort='string' style='width:80px;' title='segreguj wg. daty' rel='tooltip'>Data</th>
        <th id='WZLOTY' data-sort='string' style='width:50px;' title='segreguj wg. ".$up_name."' rel='tooltip'>".$up_name."</th>
        <th id='SPADKI' data-sort='string' style='width:50px;' title='segreguj wg. ".$dw_name."' rel='tooltip'>".$dw_name."</th>
        <th id='STAWKA' data-sort='string' style='width:50px;' title='segreguj wg. ".$bz_name."' rel='tooltip'>".$bz_name."</th>
        <th id='1' data-sort='string' style='width:50px;' title='segreguj wg. Top1' rel='tooltip'>Top1</th>
        <th id='3' data-sort='string' style='width:50px;' title='segreguj wg. Top3' rel='tooltip'>Top3</th>
        <th id='10' data-sort='string' style='width:50px;' title='segreguj wg. Top10' rel='tooltip'>Top10</th>
        <th id='50' data-sort='string' style='width:50px;' title='segreguj wg. Top50' rel='tooltip'>Top50</th>
        <th id='100' data-sort='string' style='width:50px;' title='segreguj wg. Top100' rel='tooltip'>Top100</th>
        <th id='7' data-sort='string' style='width:50px;' title='segreguj wg. zminy' rel='tooltip'>Zmiana</th>
       </tr>
    </thead>
    <tbody>
	<tr><td>".to_PL(date( "d M", strtotime( "$dt -1 day" ) ))."</td><td>".$upcnter[5]."</td><td>".$dwcnter[5]."</td><td>".$bzcnter[5]."</td><td>".$atop1."</td><td>".$atop3."</td><td>".$atop10."</td><td>".$atop50."</td><td>".$onday[5]."</td><td>".$pz1."</td></tr>
	<tr><td>".to_PL(date( "d M", strtotime( "$dt -2 day" ) ))."</td><td>".$upcnter[4]."</td><td>".$dwcnter[4]."</td><td>".$bzcnter[4]."</td><td>".$btop1."</td><td>".$btop3."</td><td>".$btop10."</td><td>".$btop50."</td><td>".$onday[4]."</td><td>".$pz2."</td></tr>
	<tr><td>".to_PL(date( "d M", strtotime( "$dt -3 day" ) ))."</td><td>".$upcnter[3]."</td><td>".$dwcnter[3]."</td><td>".$bzcnter[3]."</td><td>".$ctop1."</td><td>".$atop3."</td><td>".$ctop10."</td><td>".$ctop50."</td><td>".$onday[3]."</td><td>".$pz3."</td></tr>
	<tr><td>".to_PL(date( "d M", strtotime( "$dt -4 day" ) ))."</td><td>".$upcnter[2]."</td><td>".$dwcnter[2]."</td><td>".$bzcnter[2]."</td><td>".$dtop1."</td><td>".$dtop3."</td><td>".$dtop10."</td><td>".$dtop50."</td><td>".$onday[2]."</td><td>".$pz4."</td></tr>
	<tr><td>".to_PL(date( "d M", strtotime( "$dt -5 day" ) ))."</td><td>".$upcnter[1]."</td><td>".$dwcnter[1]."</td><td>".$bzcnter[1]."</td><td>".$etop1."</td><td>".$etop3."</td><td>".$etop10."</td><td>".$etop50."</td><td>".$onday[1]."</td><td>".$pz5."</td></tr>
	</tbody>
	</table>
	Średnio w zestawieniu: ".$middle." z ".$ilef." słów kluczowych (".procent($middle).")
</div>
<div id='_4'>
	<h4>".$out_name." (".$out.")</h4><span class='_4'>
	W zestawieniu pominięto <b>".$out." z ".$ilef."</b> (".procent($out).") słów kluczowych które nie znalazły się w pierwszej setce wyszukiwań.</span></div>
";
?>
<script>
function hMsg() {$( ".CloseScanInfo" ).slideUp(900);}
function aMsg() {$( ".alert" ).slideUp(900);}
$( "#CloseScanInfo" ).click(function() { hMsg(); });

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
    setTimeout(function(){ hMsg(); aMsg(); }, 5000); 
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
