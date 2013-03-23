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

$res=mysql_query("SELECT idRun,dateStart,dateStop,pid,haveError,timediff(dateStop,dateStart) diff FROM `".SQL_PREFIX."run` ORDER BY dateStart DESC LIMIT 1"); 
if($res && ($run=mysql_fetch_assoc($res))){
    if($run['dateStop'] == null){
        
        if(!is_pid_alive($run['pid'])){
            // in this case the pid have been killed externally 
            // from command line or max execution time reached
            mysql_query(
                "UPDATE `".SQL_PREFIX."run` SET haveError=1, dateStop=now(), ".
                "logs=CONCAT(logs,'ERROR ABNORMAL TERMINATION : process may have been killed or reached max execution time\n') ".
                "WHERE idRun = ".$run['idRun']
            );
            echo "<div class='alert alert-error' >Last run done in ".$run['diff']." with error (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' >LOG</a>]</span></div>";
        }else{
            echo "<div class='alert' >Warning: cron is still running (PID: ".$run['pid']." started: ".$run['dateStart'].") <span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' >LOG</a>] [<a style='color:red;' id='stop' data-pid=".$run['pid']." data-runid=".$run['idRun']." href='#' >STOP</a>]</span></div>";
        }
    }else{
        if($run['haveError']){
            echo "<div class='alert alert-error' >Last run done in ".$run['diff']." with error (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' >LOG</a>]</span></div>";
        }else{
            echo "<div class='alert alert-success' >Last run successfully done in ".$run['diff']." (PID: ".$run['pid']." started: ".$run['dateStart'].")<span class='pull-right' >[<a href='logs.php?id=".$run['idRun']."' >LOG</a>]</span></div>";
        }
    }
}

$q = "select max(idCheck) idLastCheck,idGroup from `".SQL_PREFIX."check` where date(`date`) = curdate() group by idGroup";
$res=mysql_query($q);
$groupsCheck= array();
while ($res && ($row=mysql_fetch_assoc($res))){
    
    $q = "select idCheck idPrevCheck ".
            "from `".SQL_PREFIX."check` where idGroup = ".intval($row['idGroup'])." ".
            "and idCheck < ".intval($row['idLastCheck'])." ".
            "order by idCheck DESC limit 1";
    
    $res2=mysql_query($q);
    if($res2 && ($row2=mysql_fetch_row($res2))){
        $row['idPrevCheck'] = $row2[0];
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
    $res=mysql_query($q);
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

if(isset($options['general']['home_top_limit']) && $options['general']['home_top_limit'] != 0){
    echo "<h4>Top ".$options['general']['home_top_limit']." positives changes</h4>\n";
    uasort($topGoodChanges, "cmpGoodChange");
    displayRanks($topGoodChanges);
    
    echo "<h4>Top ".$options['general']['home_top_limit']." negatives changes</h4>\n";
    uasort($topBadChanges, "cmpBadChange");
    displayRanks($topBadChanges);
}

echo "<h4>Positives changes</h4>\n";
uasort($otherGoodChanges, "cmpGoodChange");
displayRanks($otherGoodChanges);

echo "<h4>Negatives changes</h4>\n";
uasort($otherBadChanges, "cmpBadChange");
displayRanks($otherBadChanges);

if($options['general']['home_unchanged'] == "yes"){
    echo "<h4>Unchanged *</h4>\n";
    displayRanks($unchanged);
    
    echo "* Unranked position aren't displayed on home page";
}


function displayRanks($ranks){
    echo "<table class=table >\n";
    echo "<tr><th style='width:50%;' >keyword</th><th style='width:50%;' >domain</th><th style='width:50px;' >Old</th><th style='width:50px;' >Now</th><th style='width:50px;' >+/-</th><th style='width:50px;' >Group</th></tr>\n";
    foreach ($ranks as $key => $rank) {
        
        $split=explode("-",$key);
        echo "<tr><td>".h8($split[1])."</td>";
        echo "<td>".h8($rank['url'])."</td>";
        echo "<td>".(isset($rank['prev']) ? $rank['prev'] : "N/A")."</td>";
        echo "<td>".(isset($rank['now']) ? $rank['now'] : "N/A")."</td>";
        echo "<td>";
        
        if($rank['diff'] == 0){
            echo "<span>=";
        } else if($rank['diff'] > 0 ){
            echo "<span style='color:green' >".($rank['diff'] == 1000 ? "IN" : "+".$rank['diff']);
        }else{
            echo "<span style='color:red' >".($rank['diff'] == -1000 ? "OUT" : $rank['diff']);
        }
        echo "</span></td>";
        echo "<td>[<a href='view.php?idGroup=".h8($rank['group'])."#".h8($rank['url'])."' >view</a>]</td>";
        echo "</tr>";        
    }
    echo "</table>\n";    
}


?>
<script>
    $(function() {
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